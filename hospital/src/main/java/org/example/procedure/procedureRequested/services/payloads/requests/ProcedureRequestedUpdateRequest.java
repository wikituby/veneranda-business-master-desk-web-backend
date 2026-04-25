package org.example.procedure.procedureRequested.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class ProcedureRequestedUpdateRequest {
    @Schema(example = "1")
    public Long procedureId;

    @Schema(example = "Dr. Jane Smith")
    public String doneBy;

    @Schema(example = "Dr. John Doe")
    public String orderedBy;

    @Schema(example = "Test results are normal")
    public String report;

    @Schema(example = "2")
    public int quantity;

    @Schema(example = "Updated clinical indication")
    public String indication;
}







