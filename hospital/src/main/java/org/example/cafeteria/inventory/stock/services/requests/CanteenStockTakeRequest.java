package org.example.cafeteria.inventory.stock.services.requests;

import jakarta.json.bind.annotation.JsonbDateFormat;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CanteenStockTakeRequest {

    @Schema(example = "251")
    public Long itemId;

    @Schema(example = "5000")
    public BigDecimal quantityReceived;

    @Schema(example = "200000")
    public BigDecimal totalCostPrice;

    @Schema(example = "400")
    public BigDecimal unitSellingPrice;

    @Schema(example = "2026/08/22")
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate expiryDate;

    @Schema(example = "2025/08/22")
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate receiveDate;

    @Schema(example = "Notes About the Batch")
    public String notes;

    @Schema(example = "REVIDOL")
    public String brand;

    @Schema(example = "main")
    public String store;

    @Schema(example = "1x10")
    public String packaging;




}
