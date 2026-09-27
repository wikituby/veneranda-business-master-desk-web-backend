package org.example.subscription.mobilemoney.payloads;

public class MobileMoneyPaymentStatusDTO {
    public String referenceId;
    public String provider;
    public String phoneNumber;
    public Double amount;
    public String currency;
    /** PENDING | SUCCESSFUL | FAILED | TIMEOUT | UNKNOWN */
    public String status;
    public String message;
    public boolean mock;
    public String externalTransactionId;
}
