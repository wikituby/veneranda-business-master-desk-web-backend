package org.example.procedure.procedureRequested.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;

public class ProcedureRequestedRequest {
    @Schema(example = "1")
    public Long procedureId;

    @Schema(example = "1")
    public int quantity;

    @Schema(example = "5000")
    public BigDecimal unitSellingPrice;

    @Schema(example = "Dr. John Doe")
    public String orderedBy;

    @Schema(example = "Rule out appendicitis; persistent RLQ pain")
    public String indication;
}










