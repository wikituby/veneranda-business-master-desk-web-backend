package org.example.inventory.item.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class FormulationRequest {

    @Schema(example = "Tablet")
    public String name;

    @Schema(example = "for oral swallowing")
    public String description;
}
