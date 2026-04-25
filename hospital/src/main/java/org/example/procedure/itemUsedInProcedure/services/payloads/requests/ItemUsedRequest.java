package org.example.procedure.itemUsedInProcedure.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class ItemUsedRequest {
    @Schema(example = "1")
    public Long procedureId;

    @Schema(example = "1")
    public Long itemId;

    @Schema(example = "5")
    public int quantityUsed;
}










