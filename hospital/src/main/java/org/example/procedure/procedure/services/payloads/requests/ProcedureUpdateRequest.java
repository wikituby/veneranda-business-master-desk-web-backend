package org.example.procedure.procedure.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;

public class ProcedureUpdateRequest {
    @Schema(example = "labtest")
    public String procedureType;

    @Schema(example = "1")
    public Long categoryId;

    @Schema(example = "1")
    public Long parentCategoryId;

    @Schema(example = "Complete Blood Count")
    public String procedureName;

    @Schema(example = "A test that measures different components of blood")
    public String description;

    @Schema(example = "5000")
    public BigDecimal unitSellingPrice;

    @Schema(example = "1")
    public Long unitSellingModelId;

    @Schema(example = "3000")
    public BigDecimal unitCostPrice;
}










