package org.example.subscription.flutterwave.payloads;

public class FlutterwaveSettingsRequest {
    public String apiUrl;
    public String tokenUrl;
    public String clientId;
    public String secretKey;
    public String publicKey;
    public String encryptionKey;
    public String webhookSecretHash;
    public String webhookCallbackUrl;
    public String defaultEmail;
    public String defaultCurrency;
    public Boolean enabled;
}