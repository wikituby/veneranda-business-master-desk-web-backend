package org.example.subscription.mobilemoney.payloads;

public class MobileMoneyPayRequest {
    /** mtn | airtel */
    public String provider;
    /** Phone with mobile money (e.g. 0770123456 or 256770123456) */
    public String phoneNumber;
    public Double amount;
    public String currency;
    public String note;
}
