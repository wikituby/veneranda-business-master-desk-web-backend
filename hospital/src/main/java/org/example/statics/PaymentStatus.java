package org.example.statics;

public enum PaymentStatus {

    PENDING("Pending".toUpperCase()),
    APPROVED("Approved".toUpperCase()),
    REJECTED("Rejected".toUpperCase());

    public final String label;

    PaymentStatus(String label) {
        this.label = label;
    }
}






