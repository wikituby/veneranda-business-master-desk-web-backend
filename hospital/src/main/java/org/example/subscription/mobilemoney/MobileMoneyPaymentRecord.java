package org.example.subscription.mobilemoney;

import java.time.Instant;

final class MobileMoneyPaymentRecord {
    final String referenceId;
    final String provider;
    final String phoneNumber;
    final double amount;
    final String currency;
    final boolean mock;
    final Instant createdAt;
    String status;
    String message;
    String externalTransactionId;

    MobileMoneyPaymentRecord(
            String referenceId,
            String provider,
            String phoneNumber,
            double amount,
            String currency,
            boolean mock,
            String status,
            String message) {
        this.referenceId = referenceId;
        this.provider = provider;
        this.phoneNumber = phoneNumber;
        this.amount = amount;
        this.currency = currency;
        this.mock = mock;
        this.createdAt = Instant.now();
        this.status = status;
        this.message = message;
    }
}
