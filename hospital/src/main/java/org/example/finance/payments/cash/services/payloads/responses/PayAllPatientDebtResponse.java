package org.example.finance.payments.cash.services.payloads.responses;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PayAllPatientDebtResponse {
    public BigDecimal totalApplied;
    public BigDecimal remainingUnapplied;
    public List<PayAllPatientDebtAllocation> allocations = new ArrayList<>();

    public static class PayAllPatientDebtAllocation {
        public Long visitId;
        public Integer visitNumber;
        public BigDecimal amountPaid;
        public PaymentDTO payment;

        public PayAllPatientDebtAllocation(Long visitId, Integer visitNumber, BigDecimal amountPaid, PaymentDTO payment) {
            this.visitId = visitId;
            this.visitNumber = visitNumber;
            this.amountPaid = amountPaid;
            this.payment = payment;
        }
    }
}
