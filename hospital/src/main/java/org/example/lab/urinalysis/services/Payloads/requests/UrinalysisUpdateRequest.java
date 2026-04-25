package org.example.lab.urinalysis.services.Payloads.requests;

import jakarta.json.JsonObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class UrinalysisUpdateRequest {
    @Schema(example = "Dr. John Doe")
    public String doneBy;

    @Schema(example = "Urinalysis Report")
    public String labReportTitle;

    @Schema(example = "6.0")
    public String ph;

    @Schema(example = "1.015")
    public String sg;

    @Schema(example = "Negative")
    public String protein;

    @Schema(example = "Normal")
    public String glucose;

    @Schema(example = "Negative")
    public String ketones;

    @Schema(example = "Negative")
    public String blood;

    @Schema(example = "Negative")
    public String bilirubin;

    @Schema(example = "Normal")
    public String urobilinogen;

    @Schema(example = "Negative")
    public String nitrite;

    @Schema(example = "Negative")
    public String leukocyteE;

    @Schema(example = "0-2")
    public String epithelialCells;

    @Schema(example = "0-2")
    public String pusCellsWbcs;

    @Schema(example = "None")
    public String casts;

    @Schema(example = "0-2")
    public String redCells;

    @Schema(example = "None")
    public String crystals;

    @Schema(example = "Yellow")
    public String color;

    @Schema(example = "Clear")
    public String appearance;

    @Schema(example = "10 ml")
    public String volume;

    @Schema(description = "Per-parameter interpretations as a JSON object. Omit to leave unchanged.")
    public JsonObject interpretations;

    @Schema(example = "Additional findings or comments not covered above")
    public String others;
}
























