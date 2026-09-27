package org.example.pharmacy.otc.services.payloads.requests;

import java.math.BigDecimal;
import java.util.List;

public class OtcSaleCompleteRequest {
    public BigDecimal amountReceived;
    /** Optional discount subtracted from line total before payment check. */
    public BigDecimal discount;
    /** Tax amount added after discount (e.g. VAT). */
    public BigDecimal tax;
    /**
     * Final payable total from the POS (subtotal − discount + tax).
     * When set, this is stored as {@code sale.totalAmount} instead of recomputing.
     */
    public BigDecimal payableTotal;
    public String paymentForm;
    public String receivedBy;
    public String notes;
    /** Visit id when billing doctor prescriptions at the counter. */
    public Long visitId;
    /** Patient/client to tag the OTC sale / unpaid balance. */
    public Long patientId;
    public String patientName;
    /**
     * When false, stock is reduced and DSP is set but no visit payment is recorded
     * (dispensed, money not received). Null/true keeps the default paid flow.
     */
    public Boolean recordPayment;
    /**
     * Commercial document stage: quotation | sales_order | proforma | sales_invoice | receipt.
     * Quotation / sales_order / proforma do not deduct stock.
     */
    public String documentType;
    /**
     * When false, records the sale/document without reducing stock.
     * Null defaults from documentType (quote/proforma/sales_order → false).
     */
    public Boolean deductStock;
    /**
     * When set, revise this existing sale instead of creating a duplicate
     * (one commercial document per type per order).
     */
    public Long saleId;
    public List<OtcSaleLineRequest> lines;
}
