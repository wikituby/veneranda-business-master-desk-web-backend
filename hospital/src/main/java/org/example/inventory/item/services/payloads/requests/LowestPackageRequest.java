package org.example.inventory.item.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class LowestPackageRequest {

    @Schema(example = "bottle")
    public String title;

    @Schema(example = "bt")
    public String standardAbbreviation;

    @Schema(example = "lowest packet for solutions or powders")
    public String description;
}
