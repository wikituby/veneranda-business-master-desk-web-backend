package org.example.subscription.dgateway.payloads;

import org.example.subscription.dgateway.domains.DGatewaySettings;

public class DGatewaySettingsDTO {
    public Long id;
    public String apiUrl;
    public String apiKey;
    public String webhookSecret;
    public String webhookCallbackUrl;
    public String stripePublishableKey;
    public String defaultCurrency;
    public Boolean enabled;
    public boolean configured;
    public String updatedAt;
    public String hint;

    public static DGatewaySettingsDTO from(DGatewaySettings s) {
        DGatewaySettingsDTO dto = new DGatewaySettingsDTO();
        dto.id = s.id;
        dto.apiUrl = s.apiUrl;
        dto.apiKey = s.apiKey;
        dto.webhookSecret = s.webhookSecret;
        dto.webhookCallbackUrl = s.webhookCallbackUrl;
        dto.stripePublishableKey = s.stripePublishableKey;
        dto.defaultCurrency = s.defaultCurrency;
        dto.enabled = s.enabled;
        dto.configured = s.apiKey != null && !s.apiKey.isBlank();
        dto.updatedAt = s.updatedAt != null ? s.updatedAt.toString() : null;
        dto.hint = "Keys are stored in the database. Get dgw_test_* / dgw_live_* and webhook secret from dgatewayadmin.desispay.com.";
        return dto;
    }
}