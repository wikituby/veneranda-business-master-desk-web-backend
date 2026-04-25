package org.example.procedure.procedure.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class ProcedureTypeUpdateRequest {

    @Schema(example = "1")
    public Long procedureId;

    @Schema(example = "for malaria")
    public String typeDescription;
}
