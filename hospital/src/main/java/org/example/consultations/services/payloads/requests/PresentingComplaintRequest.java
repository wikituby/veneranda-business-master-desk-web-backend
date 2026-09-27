package org.example.consultations.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class PresentingComplaintRequest {

    @Schema(example = "Chest pain", required = true)
    public String complaint;

    @Schema(example = "Left lower chest")
    public String site;

    @Schema(example = "Moderate")
    public String severity;

    @Schema(example = "Sudden")
    public String onset;

    @Schema(example = "3")
    public Integer durationValue;

    @Schema(example = "day", enumeration = {"day", "week", "month", "year"})
    public String durationUnit;

    @Schema(example = "Sharp, stabbing")
    public String nature;

    @Schema(example = "Worsening")
    public String course;

    @Schema(example = "Deep breathing, movement")
    public String aggravatingFactors;

    @Schema(example = "Rest, sitting forward")
    public String alleviatingFactors;

    @Schema(example = "Shortness of breath, nausea")
    public String associatedSymptoms;
}
