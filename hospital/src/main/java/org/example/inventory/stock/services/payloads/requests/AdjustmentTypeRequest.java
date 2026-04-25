package org.example.inventory.stock.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class AdjustmentTypeRequest {

    @Schema(example = "Damaged")
    public String name;

    @Schema(description = "Uppercase code for logic", example = "DAMAGED")
    public String code;

    @Schema(description = "Inactive types cannot be used on new adjustments")
    public Boolean active;
}
