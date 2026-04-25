package org.example.cafeteria.sales.saleDay.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class SaleDayUpdateRequest {

    @Schema(example = "admission")
    public String visitType; // e.g., "outpatient or inpatient"

    @Schema(example = "Review")
    public String visitReason;
}






