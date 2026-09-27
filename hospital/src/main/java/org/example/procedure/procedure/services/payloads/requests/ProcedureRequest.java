package org.example.procedure.procedure.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;

public class ProcedureRequest {


    @Schema(example = "Hematology")
    public Long categoryId;

    @Schema(example = "Hematology")
    public Long parentCategoryId;

    @Schema(example = "Complete Blood Count")
    public String procedureName;

    @Schema(example = "A test that measures different components of blood")
    public String description;

    @Schema(example = "5000")
    public BigDecimal unitSellingPrice;

    @Schema(example = "1")
    public Long unitSellingModelId;

    @Schema(description = "Initial unit selling model when creating a new service")
    public org.example.inventory.stock.services.payloads.requests.UnitSellingModelRequest initialUnitSellingModel;

    @Schema(example = "3000")
    public BigDecimal unitCostPrice;
}










