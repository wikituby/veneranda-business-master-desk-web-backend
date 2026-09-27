package org.example.inventory.stock.services.payloads.requests;

public class StockItemIndicationRequest {
    /** Optional existing id (ignored on full replace sync). */
    public Long id;
    public String text;
}
