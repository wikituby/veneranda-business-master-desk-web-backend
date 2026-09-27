package org.example.pharmacy.otc.services.payloads.responses;

import java.math.BigDecimal;

/** OTC / Rx sale result including optional visit invoice payment snapshot. */
public class OtcPharmacySaleCompleteResult {
    public OtcPharmacySaleDTO sale;
    public Long visitId;
    public boolean visitPaymentRecorded;
    public BigDecimal amountPaid;
    public BigDecimal balanceDue;
    public BigDecimal totalAmount;

    public OtcPharmacySaleCompleteResult() {
    }

    public OtcPharmacySaleCompleteResult(OtcPharmacySaleDTO sale) {
        this.sale = sale;
        if (sale != null) {
            this.visitId = sale.visitId;
        }
    }
}
