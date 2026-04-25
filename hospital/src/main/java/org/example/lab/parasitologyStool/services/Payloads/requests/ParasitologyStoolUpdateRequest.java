package org.example.lab.parasitologyStool.services.Payloads.requests;

import jakarta.json.JsonObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class ParasitologyStoolUpdateRequest {
    @Schema(example = "Dr. John Doe")
    public String doneBy;

    @Schema(example = "Parasitology Stool Report")
    public String labReportTitle;

    @Schema(example = "Not seen")
    public String ova;

    @Schema(example = "Not seen")
    public String cysts;

    @Schema(example = "Not seen")
    public String larvae;

    @Schema(example = "Brown")
    public String color;

    @Schema(example = "Formed")
    public String consistency;

    @Schema(example = "Absent")
    public String blood;

    @Schema(example = "Absent")
    public String mucus;

    @Schema(example = "None")
    public String visibleParasites;

    @Schema(example = "Nil")
    public String others;

    @Schema(description = "Per-parameter interpretations as a JSON object. Omit to leave unchanged.")
    public JsonObject interpretations;
}
























