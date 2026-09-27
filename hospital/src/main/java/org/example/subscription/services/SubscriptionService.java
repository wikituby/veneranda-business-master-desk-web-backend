package org.example.subscription.services;



import jakarta.enterprise.context.ApplicationScoped;

import jakarta.inject.Inject;

import jakarta.transaction.Transactional;

import jakarta.ws.rs.WebApplicationException;

import org.example.subscription.domains.ActivationToken;

import org.example.subscription.domains.FacilitySubscription;

import org.example.subscription.domains.HealthFacility;

import org.example.subscription.domains.repositories.ActivationTokenRepository;

import org.example.subscription.domains.repositories.FacilitySubscriptionRepository;

import org.example.subscription.domains.repositories.HealthFacilityRepository;

import org.example.subscription.services.payloads.SignedActivationPayload;

import org.example.subscription.services.payloads.SubscriptionStatusDTO;

import org.example.user.domains.User;

import org.example.user.domains.repositories.UserRepository;



import java.time.LocalDateTime;

import java.util.List;

import java.util.Locale;

import java.util.Optional;



@ApplicationScoped

public class SubscriptionService {



    @Inject

    HealthFacilityRepository healthFacilityRepository;



    @Inject

    ActivationTokenRepository activationTokenRepository;



    @Inject

    FacilitySubscriptionRepository facilitySubscriptionRepository;



    @Inject

    UserRepository userRepository;



    @Inject

    SignedActivationTokenVerifier signedActivationTokenVerifier;



    public SubscriptionStatusDTO getStatusForUser(Long userId) {

        User user = userRepository.findById(userId);

        if (user == null) {

            throw new WebApplicationException("User not found", 404);

        }

        Long facilityId = user.facilityId;

        if (facilityId == null) {

            facilityId = resolveDefaultFacilityId();

        }

        if (facilityId == null) {

            SubscriptionStatusDTO dto = new SubscriptionStatusDTO();

            dto.active = false;

            dto.status = "none";

            dto.message = "No active subscription. Enter an activation code to continue.";

            return dto;

        }

        return buildStatusDto(facilityId);

    }

    /** Blocks queue / hospital directory APIs when the facility has no active subscription. */
    public void requireActiveFacilitySubscription() {
        Long facilityId = resolveDefaultFacilityId();
        if (facilityId == null) {
            throw new WebApplicationException("Active subscription required", 403);
        }
        SubscriptionStatusDTO dto = buildStatusDto(facilityId);
        if (!dto.active) {
            throw new WebApplicationException("Active subscription required", 403);
        }
    }

    @Transactional

    public SubscriptionStatusDTO activate(Long userId, String rawToken) {

        if (rawToken == null || rawToken.isBlank()) {

            throw new WebApplicationException("Activation code is required", 400);

        }

        User user = userRepository.findById(userId);

        if (user == null) {

            throw new WebApplicationException("User not found", 404);

        }

        if (!canManageSubscription(user.role)) {

            throw new WebApplicationException("Only MD or admin users can activate a subscription", 403);

        }



        if (SignedActivationTokenVerifier.isSignedToken(rawToken)) {

            return activateSignedToken(userId, rawToken.trim());

        }

        return activateLegacyToken(userId, rawToken.trim());

    }

    @Transactional
    public SubscriptionStatusDTO cancel(Long userId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new WebApplicationException("User not found", 404);
        }
        if (!canManageSubscription(user.role)) {
            throw new WebApplicationException("Only MD or admin users can cancel a subscription", 403);
        }

        Long facilityId = user.facilityId;
        if (facilityId == null) {
            facilityId = resolveDefaultFacilityId();
        }
        if (facilityId == null) {
            throw new WebApplicationException("No active subscription to cancel", 404);
        }

        FacilitySubscription subscription = facilitySubscriptionRepository.findLatestByFacilityId(facilityId)
                .orElseThrow(() -> new WebApplicationException("No active subscription to cancel", 404));

        if (!"active".equalsIgnoreCase(subscription.status)) {
            throw new WebApplicationException("Subscription is not active", 409);
        }

        subscription.status = "cancelled";
        subscription.periodEnd = LocalDateTime.now();
        facilitySubscriptionRepository.persist(subscription);

        SubscriptionStatusDTO dto = buildStatusDto(facilityId);
        dto.message = "Subscription has been cancelled";
        return dto;
    }

    private SubscriptionStatusDTO activateSignedToken(Long userId, String rawToken) {
        SignedActivationPayload payload = signedActivationTokenVerifier.verifyAndParse(rawToken);
        String jti = payload.jti.trim();
        String tokenHash = TokenFingerprint.hash(rawToken);

        assertActivationCodeUnused(jti, tokenHash, true);

        ActivationToken record = activationTokenRepository.findByJti(jti).orElseGet(ActivationToken::new);
        record.token = jti;
        record.tokenHash = tokenHash;
        record.facilityName = payload.fn.trim();
        record.facilityAddress = payload.fa != null ? payload.fa.trim() : null;
        record.subscribedModuleKeys = normalizeModules(payload.modules);
        record.durationDays = payload.days;
        record.durationMonths = null;
        record.status = "used";
        record.usedAt = LocalDateTime.now();
        record.usedByUserId = userId;
        activationTokenRepository.persist(record);

        return completeActivation(userId, record, normalizeModules(payload.modules), now -> now.plusDays(payload.days), jti);
    }

    private SubscriptionStatusDTO activateLegacyToken(Long userId, String rawToken) {
        String normalized = rawToken.toUpperCase(Locale.ROOT);
        String tokenHash = TokenFingerprint.hash(rawToken);

        assertActivationCodeUnused(normalized, tokenHash, false);

        ActivationToken token = activationTokenRepository.findByToken(normalized)
                .orElseThrow(() -> new WebApplicationException("Invalid activation code", 404));

        if (!"unused".equalsIgnoreCase(token.status)) {
            throw new WebApplicationException("This activation code has already been used", 409);
        }

        token.tokenHash = tokenHash;
        token.status = "used";
        token.usedAt = LocalDateTime.now();
        token.usedByUserId = userId;
        activationTokenRepository.persist(token);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime periodEnd = resolvePeriodEnd(token, now);
        return completeActivation(
                userId,
                token,
                normalizeModules(token.subscribedModuleKeys),
                start -> periodEnd,
                token.token
        );
    }

    private void assertActivationCodeUnused(String lookupKey, String tokenHash, boolean signedJti) {
        if (activationTokenRepository.isUsedTokenHash(tokenHash)) {
            throw new WebApplicationException("This activation code has already been used", 409);
        }
        Optional<ActivationToken> byKey = signedJti
                ? activationTokenRepository.findByJti(lookupKey)
                : activationTokenRepository.findByToken(lookupKey);
        if (byKey.isPresent() && "used".equalsIgnoreCase(byKey.get().status)) {
            throw new WebApplicationException("This activation code has already been used", 409);
        }
        if (facilitySubscriptionRepository.wasActivationCodeUsed(lookupKey)) {
            throw new WebApplicationException("This activation code has already been used", 409);
        }
    }

    private SubscriptionStatusDTO completeActivation(
            Long userId,
            ActivationToken token,
            String modules,
            java.util.function.Function<LocalDateTime, LocalDateTime> periodEndFn,
            String activationKeyRecorded) {
        HealthFacility facility = findOrCreateFacility(token);
        LocalDateTime now = LocalDateTime.now();

        FacilitySubscription subscription = new FacilitySubscription();
        subscription.facilityId = facility.id;
        subscription.status = "active";
        subscription.subscribedModuleKeys = modules;
        subscription.periodStart = now;
        subscription.periodEnd = periodEndFn.apply(now);
        subscription.activationTokenUsed = activationKeyRecorded;
        facilitySubscriptionRepository.persist(subscription);

        linkAllUsersToFacility(facility.id);

        SubscriptionStatusDTO dto = buildStatusDto(facility.id);
        dto.message = "Subscription activated successfully";
        return dto;
    }



    private LocalDateTime resolvePeriodEnd(ActivationToken token, LocalDateTime start) {

        if (token.durationDays != null && token.durationDays > 0) {

            return start.plusDays(token.durationDays);

        }

        int months = token.durationMonths != null && token.durationMonths > 0 ? token.durationMonths : 12;

        return start.plusMonths(months);

    }



    private HealthFacility findOrCreateFacility(ActivationToken token) {

        Optional<FacilitySubscription> existingSub = facilitySubscriptionRepository

                .find("order by id desc").firstResultOptional();

        if (existingSub.isPresent()) {

            HealthFacility existing = healthFacilityRepository.findById(existingSub.get().facilityId);

            if (existing != null) {

                if (token.facilityName != null && !token.facilityName.isBlank()) {

                    existing.name = token.facilityName.trim();

                }

                if (token.facilityAddress != null && !token.facilityAddress.isBlank()) {

                    existing.address = token.facilityAddress.trim();

                }

                healthFacilityRepository.persist(existing);

                return existing;

            }

        }



        HealthFacility facility = new HealthFacility();

        facility.name = token.facilityName != null && !token.facilityName.isBlank()

                ? token.facilityName.trim()

                : "Health Facility";

        facility.address = token.facilityAddress;

        facility.status = "active";

        healthFacilityRepository.persist(facility);

        return facility;

    }



    private void linkAllUsersToFacility(Long facilityId) {

        List<User> users = userRepository.listAll();

        for (User u : users) {

            u.facilityId = facilityId;

            userRepository.persist(u);

        }

    }



    private Long resolveDefaultFacilityId() {

        return facilitySubscriptionRepository.find("order by id desc").firstResultOptional()

                .map(s -> s.facilityId)

                .orElse(null);

    }



    private SubscriptionStatusDTO buildStatusDto(Long facilityId) {

        HealthFacility facility = healthFacilityRepository.findById(facilityId);

        FacilitySubscription subscription = facilitySubscriptionRepository.findLatestByFacilityId(facilityId)

                .orElse(null);



        SubscriptionStatusDTO dto = new SubscriptionStatusDTO();

        dto.facilityId = facilityId;

        if (facility != null) {

            dto.facilityName = facility.name;

            dto.facilityAddress = facility.address;

        }

        if (subscription == null) {

            dto.active = false;

            dto.status = "none";

            dto.message = "No subscription found for this facility.";

            return dto;

        }



        dto.status = subscription.status;

        dto.subscribedModuleKeys = subscription.subscribedModuleKeys;

        dto.periodStart = subscription.periodStart;

        dto.periodEnd = subscription.periodEnd;



        boolean dateValid = subscription.periodEnd == null || !LocalDateTime.now().isAfter(subscription.periodEnd);

        dto.active = "active".equalsIgnoreCase(subscription.status) && dateValid;

        if (!dto.active && subscription.periodEnd != null && LocalDateTime.now().isAfter(subscription.periodEnd)) {

            dto.status = "expired";

            dto.message = "Subscription expired on " + subscription.periodEnd.toLocalDate();

        } else if (dto.active) {

            dto.message = "Subscription is active";

        }

        return dto;

    }



    private String normalizeModules(String modules) {

        if (modules == null || modules.isBlank() || "all".equalsIgnoreCase(modules.trim())) {

            return "all";

        }

        return modules.trim();

    }



    public static boolean canManageSubscription(String role) {

        if (role == null) {

            return false;

        }

        String r = role.toLowerCase(Locale.ROOT).trim();

        return "md".equals(r) || "admin".equals(r);

    }

}


