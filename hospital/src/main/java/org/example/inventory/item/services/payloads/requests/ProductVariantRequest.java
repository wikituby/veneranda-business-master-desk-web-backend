package org.example.inventory.item.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

public class ProductVariantRequest {

    @Schema(description = "ID of the Brand this variant belongs to", example = "1")
    public Long brandId;

    @Schema(example = "1")
    public Long dosageFormId;

    @Schema(example = "1")
    public Long formulationId;

    @Schema(description = "Strength description for the whole unit, e.g., '40/320 mg per tablet'", example = "40/320 mg per tablet")
    public String strengthDescription;

}
