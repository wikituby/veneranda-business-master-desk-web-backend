package org.example.subscription.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.example.subscription.domains.ActivationToken;
import org.example.subscription.domains.repositories.ActivationTokenRepository;
import org.example.subscription.services.payloads.ActivationTokenDTO;
import org.example.subscription.services.payloads.CreateActivationTokenRequest;

import java.time.Year;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class ActivationTokenService {

    @Inject
    ActivationTokenRepository activationTokenRepository;

    @ConfigProperty(name = "subscription.vendor-api-key", defaultValue = "KMC-VENDOR-2026")
    String vendorApiKey;

    public void assertVendorKey(String providedKey) {
        if (providedKey == null || providedKey.isBlank()) {
            throw new WebApplicationException("Vendor API key is required", 401);
        }
        if (!vendorApiKey.equals(providedKey.trim())) {
            throw new WebApplicationException("Invalid vendor API key", 403);
        }
    }

    public List<ActivationTokenDTO> listAll() {
        return activationTokenRepository.listAll().stream()
                .sorted(Comparator.comparing((ActivationToken t) -> t.id).reversed())
                .map(ActivationTokenDTO::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public ActivationTokenDTO create(CreateActivationTokenRequest request) {
        if (request == null) {
            throw new WebApplicationException("Request body is required", 400);
        }
        if (request.facilityName == null || request.facilityName.isBlank()) {
            throw new WebApplicationException("Facility name is required", 400);
        }
        if (request.daysPaid == null || request.daysPaid <= 0) {
            throw new WebApplicationException("Days paid must be greater than zero", 400);
        }

        String tokenValue = normalizeToken(request.token);
        if (tokenValue == null || tokenValue.isBlank()) {
            tokenValue = generateToken(request.facilityName.trim());
        } else {
            tokenValue = tokenValue.toUpperCase(Locale.ROOT);
        }

        Optional<ActivationToken> existing = activationTokenRepository.findByToken(tokenValue);
        if (existing.isPresent()) {
            throw new WebApplicationException("Token already exists: " + tokenValue, 409);
        }

        ActivationToken token = new ActivationToken();
        token.token = tokenValue;
        token.tokenHash = TokenFingerprint.hash(tokenValue);
        token.facilityName = request.facilityName.trim();
        token.facilityAddress = request.facilityAddress != null ? request.facilityAddress.trim() : null;
        token.subscribedModuleKeys = normalizeModules(request.subscribedModuleKeys);
        token.durationDays = request.daysPaid;
        token.durationMonths = null;
        token.status = "unused";
        activationTokenRepository.persist(token);

        return ActivationTokenDTO.from(token);
    }

    private String normalizeToken(String raw) {
        if (raw == null) {
            return null;
        }
        return raw.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeModules(String modules) {
        if (modules == null || modules.isBlank() || "all".equalsIgnoreCase(modules.trim())) {
            return "all";
        }
        return modules.trim();
    }

    private String generateToken(String facilityName) {
        String prefix = facilityAbbreviation(facilityName);
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase(Locale.ROOT);
        return prefix + "-" + suffix + "-" + Year.now().getValue();
    }

    private String facilityAbbreviation(String facilityName) {
        String[] words = facilityName.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!word.isBlank()) {
                sb.append(Character.toUpperCase(word.charAt(0)));
            }
            if (sb.length() >= 4) {
                break;
            }
        }
        if (sb.length() < 2) {
            sb.append("SUB");
        }
        return sb.substring(0, Math.min(sb.length(), 4));
    }
}
