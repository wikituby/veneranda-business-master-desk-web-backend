package org.example.inventory.item.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class StrengthUnitRequest {

    @Schema(example = "milligrams")
    public String title;

    @Schema(example = "mg")
    public String standardAbbreviation;

    @Schema(example = "1000mg = 1g")
    public String description;
}
