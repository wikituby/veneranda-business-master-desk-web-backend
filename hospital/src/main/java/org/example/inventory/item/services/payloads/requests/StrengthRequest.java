package org.example.inventory.item.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;

public class StrengthRequest {

    @Schema(example = "1")
    public Long activeIngredientId;

    @Schema(description = "Legacy field; use activeIngredientId when possible.")
    public Long itemId;

    @Schema(example = "40")
    public BigDecimal strengthValue;

    @Schema(example = "1")
    public Long strengthUnitId;
}
