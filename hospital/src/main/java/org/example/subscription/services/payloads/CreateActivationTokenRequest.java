package org.example.subscription.services.payloads;

public class CreateActivationTokenRequest {
    public String facilityName;
    public String facilityAddress;
    public String subscribedModuleKeys;
    /** Number of days the customer paid for. */
    public Integer daysPaid;
    /** Optional custom token; auto-generated if blank. */
    public String token;
}
