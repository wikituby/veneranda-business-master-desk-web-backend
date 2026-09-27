package org.example.subscription.dgateway;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import java.time.LocalDateTime;
import org.example.subscription.dgateway.domains.DGatewaySettings;
import org.example.subscription.dgateway.domains.repositories.DGatewaySettingsRepository;
import org.example.subscription.dgateway.payloads.DGatewaySettingsDTO;
import org.example.subscription.dgateway.payloads.DGatewaySettingsRequest;
import org.example.subscription.services.SubscriptionService;
import org.example.user.domains.User;

@ApplicationScoped
public class DGatewaySettingsService {

    @Inject
    DGatewaySettingsRepository settingsRepository;

    public void assertCanManage(User user) {
        if (user == null || !SubscriptionService.canManageSubscription(user.role)) {
            throw new WebApplicationException("Only MD or admin users can manage DGateway settings", 403);
        }
    }

    @Transactional
    public DGatewaySettings ensureSettings() {
        DGatewaySettings settings = settingsRepository.getSingleton();
        if (settings == null) {
            settings = new DGatewaySettings();
            settings.apiUrl = "https://dgatewayapi.desispay.com";
            settings.defaultCurrency = "UGX";
            settings.enabled = Boolean.TRUE;
            settings.updatedAt = LocalDateTime.now();
            settingsRepository.persist(settings);
        }
        return settings;
    }

    @Transactional
    public DGatewaySettingsDTO getSettings(User user) {
        assertCanManage(user);
        return DGatewaySettingsDTO.from(ensureSettings());
    }

    @Transactional
    public DGatewaySettingsDTO saveSettings(User user, DGatewaySettingsRequest request) {
        assertCanManage(user);
        if (request == null) {
            throw new WebApplicationException("Request body required", 400);
        }
        DGatewaySettings settings = ensureSettings();
        if (request.apiUrl != null && !request.apiUrl.isBlank()) {
            String url = request.apiUrl.trim();
            if (url.endsWith("/")) {
                url = url.substring(0, url.length() - 1);
            }
            settings.apiUrl = url;
        }
        if (request.apiKey != null) {
            settings.apiKey = blankToNull(request.apiKey);
        }
        if (request.webhookSecret != null) {
            settings.webhookSecret = blankToNull(request.webhookSecret);
        }
        if (request.webhookCallbackUrl != null) {
            settings.webhookCallbackUrl = blankToNull(request.webhookCallbackUrl);
        }
        if (request.stripePublishableKey != null) {
            settings.stripePublishableKey = blankToNull(request.stripePublishableKey);
        }
        if (request.defaultCurrency != null && !request.defaultCurrency.isBlank()) {
            settings.defaultCurrency = request.defaultCurrency.trim().toUpperCase();
        }
        if (request.enabled != null) {
            settings.enabled = request.enabled;
        }
        settings.updatedAt = LocalDateTime.now();
        return DGatewaySettingsDTO.from(settings);
    }

    @Transactional
    public DGatewaySettings findSettings() {
        return settingsRepository.getSingleton();
    }

    private static String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String t = value.trim();
        return t.isEmpty() ? null : t;
    }
}