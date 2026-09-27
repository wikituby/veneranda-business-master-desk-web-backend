package org.example.subscription.dgateway.payloads;

public class DGatewaySettingsRequest {
    public String apiUrl;
    public String apiKey;
    public String webhookSecret;
    public String webhookCallbackUrl;
    public String stripePublishableKey;
    public String defaultCurrency;
    public Boolean enabled;
}