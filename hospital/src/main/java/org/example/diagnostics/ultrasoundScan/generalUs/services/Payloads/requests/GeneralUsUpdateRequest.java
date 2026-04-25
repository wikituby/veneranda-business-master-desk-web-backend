package org.example.diagnostics.ultrasoundScan.generalUs.services.Payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class GeneralUsUpdateRequest {
    @Schema(example = "Abdominal pain")
    public String indication;

    @Schema(example = "Dr. John Doe")
    public String doneBy;

    @Schema(example = "Follow up in 2 weeks")
    public String recommendation;

    @Schema(example = "General Ultrasound Scan Report")
    public String scanReportTitle;

    @Schema(example = "Abdominal Ultrasound")
    public String exam;

    @Schema(example = "Normal findings")
    public String findings;

    @Schema(example = "No abnormalities detected")
    public String impression;
}










