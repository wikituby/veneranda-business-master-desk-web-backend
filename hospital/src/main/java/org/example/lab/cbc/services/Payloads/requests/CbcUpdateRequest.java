package org.example.lab.cbc.services.Payloads.requests;

import jakarta.json.JsonObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class CbcUpdateRequest {
    @Schema(example = "Dr. John Doe")
    public String doneBy;

    @Schema(example = "CBC Report")
    public String labReportTitle;

    @Schema(example = "6.2")
    public String wbc;

    @Schema(example = "2.1")
    public String lymph;

    @Schema(example = "0.4")
    public String mid;

    @Schema(example = "3.7")
    public String gran;

    @Schema(example = "34")
    public String lymphPercent;

    @Schema(example = "6")
    public String midPercent;

    @Schema(example = "60")
    public String granPercent;

    @Schema(example = "13.5")
    public String hgb;

    @Schema(example = "4.5")
    public String rbc;

    @Schema(example = "40")
    public String hct;

    @Schema(example = "88")
    public String mcv;

    @Schema(example = "29")
    public String mch;

    @Schema(example = "33")
    public String mchc;

    @Schema(example = "12.5")
    public String rdwCv;

    @Schema(example = "41")
    public String rdwSd;

    @Schema(example = "250")
    public String plt;

    @Schema(example = "9.5")
    public String mpv;

    @Schema(example = "13")
    public String pdw;

    @Schema(example = "0.25")
    public String pct;

    @Schema(description = "Per-parameter interpretations (JSON object). Omit field entirely to leave DB value unchanged. "
            + "If sent, blank string values are stripped; if nothing remains, interpretations are stored as empty (avoids huge {\"k\":\"\"...} payloads).")
    public JsonObject interpretations;
}
























