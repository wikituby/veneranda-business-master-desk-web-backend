package org.example.cafeteria.sales.saleDone.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;

public class SaleRequest {

    @Schema(example = "1")
    public BigDecimal quantity;

    @Schema(example = "1")
    public Long itemId;

    @Schema(example = "1")
    public BigDecimal unitSellingPrice;

}






