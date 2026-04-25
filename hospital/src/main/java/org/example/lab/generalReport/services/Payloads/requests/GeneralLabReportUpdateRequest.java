package org.example.lab.generalReport.services.Payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class GeneralLabReportUpdateRequest {
    @Schema(example = "Dr. John Doe")
    public String doneBy;

    @Schema(example = "Follow up in 2 weeks")
    public String recommendation;

    @Schema(example = "Negative")
    public String result;

    @Schema(example = "No abnormalities detected")
    public String notes;
}

























