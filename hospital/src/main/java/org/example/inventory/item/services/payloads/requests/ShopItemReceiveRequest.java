package org.example.inventory.item.services.payloads.requests;

import jakarta.json.bind.annotation.JsonbDateFormat;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ShopItemReceiveRequest {

    @Schema(example = "12", required = true)
    public Long itemId;

    @Schema(example = "50", required = true)
    public BigDecimal quantityReceived;

    @Schema(example = "800")
    public BigDecimal unitCostPrice;

    @Schema(example = "800")
    public BigDecimal costPrice;

    @Schema(example = "1200")
    public BigDecimal unitSellingPrice;

    @Schema(example = "1200")
    public BigDecimal sellingPrice;

    @Schema(example = "Box of 10")
    public String packaging;

    @Schema(example = "BATCH-001")
    public String batchNumber;

    @Schema(example = "2026/12/31")
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate expiryDate;

    @Schema(example = "2026/08/15")
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate receiveDate;
}
