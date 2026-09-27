package org.example.subscription.mobilemoney;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import java.time.LocalDateTime;
import java.util.Locale;
import org.example.subscription.mobilemoney.domains.MobileMoneySettings;
import org.example.subscription.mobilemoney.domains.repositories.MobileMoneySettingsRepository;
import org.example.subscription.mobilemoney.payloads.MobileMoneySettingsDTO;
import org.example.subscription.mobilemoney.payloads.MobileMoneySettingsRequest;
import org.example.subscription.services.SubscriptionService;
import org.example.user.domains.User;

@ApplicationScoped
public class MobileMoneySettingsService {

    private static final String SANDBOX_BASE = "https://sandbox.momodeveloper.mtn.com";
    private static final String PROD_BASE = "https://proxy.momoapi.mtn.com";

    @Inject
    MobileMoneySettingsRepository settingsRepository;

    public void assertCanManage(User user) {
        if (user == null || !SubscriptionService.canManageSubscription(user.role)) {
            throw new WebApplicationException("Only MD or admin users can manage mobile money settings", 403);
        }
    }

    @Transactional
    public MobileMoneySettings ensureSettings() {
        MobileMoneySettings settings = settingsRepository.getSingleton();
        if (settings == null) {
            settings = new MobileMoneySettings();
            settings.mtnTargetEnvironment = "sandbox";
            settings.mtnBaseUrl = SANDBOX_BASE;
            settings.airtelBaseUrl = "https://openapiuat.airtel.africa";
            settings.airtelCountry = "UG";
            settings.airtelCurrency = "UGX";
            settings.enabled = Boolean.TRUE;
            settings.updatedAt = LocalDateTime.now();
            settingsRepository.persist(settings);
        }
        return settings;
    }

    @Transactional
    public MobileMoneySettingsDTO getSettings(User user) {
        assertCanManage(user);
        MobileMoneySettings settings = ensureSettings();
        return toDto(settings);
    }

    @Transactional
    public MobileMoneySettingsDTO saveSettings(User user, MobileMoneySettingsRequest request) {
        assertCanManage(user);
        if (request == null) {
            throw new WebApplicationException("Request body required", 400);
        }
        MobileMoneySettings settings = ensureSettings();

        if (request.mtnTargetEnvironment != null && !request.mtnTargetEnvironment.isBlank()) {
            String env = request.mtnTargetEnvironment.trim().toLowerCase(Locale.ROOT);
            if (!"sandbox".equals(env) && !"production".equals(env)) {
                throw new WebApplicationException("MTN environment must be sandbox or production", 400);
            }
            settings.mtnTargetEnvironment = env;
            if (request.mtnBaseUrl == null || request.mtnBaseUrl.isBlank()) {
                settings.mtnBaseUrl = "sandbox".equals(env) ? SANDBOX_BASE : PROD_BASE;
            }
        }
        if (request.mtnBaseUrl != null && !request.mtnBaseUrl.isBlank()) {
            settings.mtnBaseUrl = trimSlash(request.mtnBaseUrl.trim());
        }
        if (request.mtnSubscriptionKey != null) {
            settings.mtnSubscriptionKey = blankToNull(request.mtnSubscriptionKey);
        }
        if (request.mtnApiUser != null) {
            settings.mtnApiUser = blankToNull(request.mtnApiUser);
        }
        if (request.mtnApiKey != null) {
            settings.mtnApiKey = blankToNull(request.mtnApiKey);
        }

        if (request.airtelBaseUrl != null && !request.airtelBaseUrl.isBlank()) {
            settings.airtelBaseUrl = trimSlash(request.airtelBaseUrl.trim());
        }
        if (request.airtelClientId != null) {
            settings.airtelClientId = blankToNull(request.airtelClientId);
        }
        if (request.airtelClientSecret != null) {
            settings.airtelClientSecret = blankToNull(request.airtelClientSecret);
        }
        if (request.airtelCountry != null && !request.airtelCountry.isBlank()) {
            settings.airtelCountry = request.airtelCountry.trim().toUpperCase(Locale.ROOT);
        }
        if (request.airtelCurrency != null && !request.airtelCurrency.isBlank()) {
            settings.airtelCurrency = request.airtelCurrency.trim().toUpperCase(Locale.ROOT);
        }
        if (request.enabled != null) {
            settings.enabled = request.enabled;
        }

        settings.updatedAt = LocalDateTime.now();
        return toDto(settings);
    }

    /** Effective row for clients (may be null before first ensure). */
    @Transactional
    public MobileMoneySettings findSettings() {
        return settingsRepository.getSingleton();
    }

    private static MobileMoneySettingsDTO toDto(MobileMoneySettings settings) {
        boolean mtn = notBlank(settings.mtnSubscriptionKey)
                && notBlank(settings.mtnApiUser)
                && notBlank(settings.mtnApiKey);
        boolean airtel = notBlank(settings.airtelClientId) && notBlank(settings.airtelClientSecret);
        return MobileMoneySettingsDTO.from(settings, mtn, airtel);
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    private static String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String t = value.trim();
        return t.isEmpty() ? null : t;
    }

    private static String trimSlash(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
