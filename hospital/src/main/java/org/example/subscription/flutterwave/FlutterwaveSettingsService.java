package org.example.subscription.flutterwave;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import java.time.LocalDateTime;
import org.example.subscription.flutterwave.domains.FlutterwaveSettings;
import org.example.subscription.flutterwave.domains.repositories.FlutterwaveSettingsRepository;
import org.example.subscription.flutterwave.payloads.FlutterwaveSettingsDTO;
import org.example.subscription.flutterwave.payloads.FlutterwaveSettingsRequest;
import org.example.subscription.services.SubscriptionService;
import org.example.user.domains.User;

@ApplicationScoped
public class FlutterwaveSettingsService {

    @Inject
    FlutterwaveSettingsRepository settingsRepository;

    public void assertCanManage(User user) {
        if (user == null || !SubscriptionService.canManageSubscription(user.role)) {
            throw new WebApplicationException("Only MD or admin users can manage Flutterwave settings", 403);
        }
    }

    @Transactional
    public FlutterwaveSettings ensureSettings() {
        FlutterwaveSettings settings = settingsRepository.getSingleton();
        if (settings == null) {
            settings = new FlutterwaveSettings();
            settings.apiUrl = "https://developersandbox-api.flutterwave.com";
            settings.tokenUrl = "https://idp.flutterwave.com/realms/flutterwave/protocol/openid-connect/token";
            settings.defaultCurrency = "UGX";
            settings.defaultEmail = "billing@facility.local";
            settings.enabled = Boolean.TRUE;
            settings.updatedAt = LocalDateTime.now();
            settingsRepository.persist(settings);
        }
        return settings;
    }

    @Transactional
    public FlutterwaveSettingsDTO getSettings(User user) {
        assertCanManage(user);
        return FlutterwaveSettingsDTO.from(ensureSettings());
    }

    @Transactional
    public FlutterwaveSettingsDTO saveSettings(User user, FlutterwaveSettingsRequest request) {
        assertCanManage(user);
        if (request == null) {
            throw new WebApplicationException("Request body required", 400);
        }
        FlutterwaveSettings settings = ensureSettings();
        if (request.apiUrl != null && !request.apiUrl.isBlank()) {
            settings.apiUrl = trimSlash(request.apiUrl.trim());
        }
        if (request.tokenUrl != null && !request.tokenUrl.isBlank()) {
            settings.tokenUrl = request.tokenUrl.trim();
        }
        if (request.clientId != null) {
            settings.clientId = blankToNull(request.clientId);
        }
        if (request.secretKey != null) {
            settings.secretKey = blankToNull(request.secretKey);
        }
        if (request.publicKey != null) {
            settings.publicKey = blankToNull(request.publicKey);
        }
        if (request.encryptionKey != null) {
            settings.encryptionKey = blankToNull(request.encryptionKey);
        }
        if (request.webhookSecretHash != null) {
            settings.webhookSecretHash = blankToNull(request.webhookSecretHash);
        }
        if (request.webhookCallbackUrl != null) {
            settings.webhookCallbackUrl = blankToNull(request.webhookCallbackUrl);
        }
        if (request.defaultEmail != null && !request.defaultEmail.isBlank()) {
            settings.defaultEmail = request.defaultEmail.trim();
        }
        if (request.defaultCurrency != null && !request.defaultCurrency.isBlank()) {
            settings.defaultCurrency = request.defaultCurrency.trim().toUpperCase();
        }
        if (request.enabled != null) {
            settings.enabled = request.enabled;
        }
        settings.updatedAt = LocalDateTime.now();
        return FlutterwaveSettingsDTO.from(settings);
    }

    @Transactional
    public FlutterwaveSettings findSettings() {
        return settingsRepository.getSingleton();
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