package org.example.consultations.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class PhysicalExaminationRequest {

    @Schema(example = "Respiratory", required = true)
    public String system;

    @Schema(example = "Reduced air entry left base with crepitations", required = true)
    public String findings;

    @Schema(example = "Left lower lobe")
    public String site;

    @Schema(example = "Abnormal")
    public String status;

    @Schema(example = "Chest moves symmetrically")
    public String inspection;

    @Schema(example = "No tenderness")
    public String palpation;

    @Schema(example = "Dull at left base")
    public String percussion;

    @Schema(example = "Crepitations left base")
    public String auscultation;

    @Schema(example = "Optional clinician notes")
    public String notes;
}
