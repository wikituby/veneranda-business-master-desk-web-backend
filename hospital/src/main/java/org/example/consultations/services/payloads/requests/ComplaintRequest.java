package org.example.consultations.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class ComplaintRequest {
    @Schema(example = "1", description = "ID of the ComplaintSite")
    public Long siteId;

    @Schema(example = "1", description = "ID of the ComplaintType")
    public Long typeId;

    @Schema(example = "3 days")
    public String duration;

    @Schema(example = "Sharp")
    public String natureCharacter;

    @Schema(example = "Moderate")
    public String severity;

    @Schema(example = "Sudden")
    public String onset;

    @Schema(example = "Worsening")
    public String courseProgression;

    @Schema(example = "Movement, Deep breathing")
    public String aggravatingFactors;

    @Schema(example = "Rest, Pain medication")
    public String relievingFactors;

    @Schema(example = "Nausea, Shortness of breath")
    public String associatedSymptoms;
}






