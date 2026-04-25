package org.example.lab.singleStatementReport.malaria.services.Payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class MalariaUpdateRequest {
    @Schema(example = "Dr. John Doe")
    public String doneBy;

    @Schema(example = "Follow up in 2 weeks")
    public String recommendation;

    @Schema(example = "Negative")
    public String bs;

    @Schema(example = "Negative")
    public String mrdt;

    @Schema(example = "No abnormalities detected")
    public String notes;
}










