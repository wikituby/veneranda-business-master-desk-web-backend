package org.example.pharmacy.otc.services.payloads.requests;

import jakarta.json.bind.annotation.JsonbProperty;

import java.math.BigDecimal;

/** One cart line: stock batch OR shop item OR stock item (services / batchless), quantity only for OTC counter. */
public class OtcSaleLineRequest {
    @JsonbProperty("stockBatchId")
    public Long stockBatchId;

    /** Shop item id when selling without a stock batch (ITM / OTC). */
    @JsonbProperty("itemId")
    public Long itemId;

    /**
     * Stock item id when selling a catalog StockItem without a batch
     * (e.g. saleCategory=Service, or out-of-stock product deferred to delivery).
     */
    @JsonbProperty("stockItemId")
    public Long stockItemId;

    /** When set, line is a visit prescription tied to a treatment request. */
    public Long treatmentRequestedId;

    /**
     * When set, line is a visit sundry already stock-deducted at recording time.
     * completeSale bills it and marks dispensed — does not deduct stock again.
     */
    public Long visitSundryId;

    public BigDecimal quantity;
    public BigDecimal amountPerFrequencyValue;
    public String amountPerFrequencyUnit;
    public BigDecimal frequencyValue;
    public Integer frequencyUnit;
    public BigDecimal durationValue;
    public Integer durationUnit;
    public BigDecimal totalUnits;
    public String instructions;
    public String route;
    /** When false, line is listed but excluded from billing and stock deduction. Defaults to true. */
    public Boolean given;
    /** Selected unit selling model for OTC counter lines. */
    public Long unitSellingModelId;
    /**
     * Open / cart selling price (e.g. transport, services with no fixed catalog price).
     * When &gt; 0, complete-sale uses this instead of the catalog / model price.
     */
    @JsonbProperty("unitSellingPrice")
    public BigDecimal unitSellingPrice;
    /** Optional line total from cart; used with quantity when unit price is missing. */
    @JsonbProperty("totalAmount")
    public BigDecimal totalAmount;
}
