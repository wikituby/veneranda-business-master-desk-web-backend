package org.example.subscription.flutterwave.payloads;

public class FlutterwavePaymentStatusDTO {
    public String referenceId;
    public String provider;
    public String phoneNumber;
    public Double amount;
    public String currency;
    public String status;
    public String message;
    public boolean mock;
    public String providerRef;
    public String network;
    public String failureReason;
}