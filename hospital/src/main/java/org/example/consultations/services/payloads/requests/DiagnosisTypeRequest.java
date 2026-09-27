package org.example.consultations.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class DiagnosisTypeRequest {
    @Schema(example = "Pneumonia", description = "Title of the diagnosis type")
    public String title;

    @Schema(example = "Lower respiratory tract infection", description = "Optional description")
    public String description;

    @Schema(example = "MA.", description = "Uganda HMIS code")
    public String hmisCode;

    @Schema(description = "Optional ICD-10 code")
    public String icd10Code;

    @Schema(description = "Comma-separated keywords for auto-mapping, e.g. malaria,fever")
    public String matchKeywords;
}
