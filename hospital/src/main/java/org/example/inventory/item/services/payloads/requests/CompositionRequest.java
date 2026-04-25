package org.example.inventory.item.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;

public class CompositionRequest {

    @Schema(description = "ID of the active ingredient (Item)", example = "1")
    public Long stockItemId;

    @Schema(description = "ID of the strength", example = "1")
    public Long strengthId;

}
