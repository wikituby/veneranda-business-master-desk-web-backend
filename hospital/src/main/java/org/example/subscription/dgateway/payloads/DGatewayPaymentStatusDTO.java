package org.example.subscription.dgateway.payloads;

public class DGatewayPaymentStatusDTO {
    public String referenceId;
    public String provider;
    public String phoneNumber;
    public Double amount;
    public String currency;
    public String status;
    public String message;
    public boolean mock;
    public String providerRef;
    public String clientSecret;
    public String stripePublishableKey;
    public String failureReason;
}