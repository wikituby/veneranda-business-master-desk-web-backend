package org.example.inventory.item.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class DosageFormRequest {

    @Schema(example = "oral tablet")
    public String name;

    @Schema(example = "1")
    public Long formulationId;

    @Schema(example = "Route of admin and formulation")
    public String description;
}
