package org.example.lab.cbc.services.Payloads.responses;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.annotation.JsonbDateFormat;
import org.example.lab.cbc.domains.Cbc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

public class CbcDTO {
    public Long id;
    public Long visitId;
    public Long procedureRequestedId;
    public String test;
    public String patientName;
    public String gender;
    public BigDecimal patientAge;
    public String labReportTitle;
    public String doneBy;
    public String wbc;
    public String lymph;
    public String mid;
    public String gran;
    public String lymphPercent;
    public String midPercent;
    public String granPercent;
    public String hgb;
    public String rbc;
    public String hct;
    public String mcv;
    public String mch;
    public String mchc;
    public String rdwCv;
    public String rdwSd;
    public String plt;
    public String mpv;
    public String pdw;
    public String pct;
    /** Parsed JSON object when stored value is JSON; otherwise plain text string or null. */
    public Object interpretations;

    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDateTime sampleCollectionDateAndTime;

    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDateTime procedureDoneDateAndTime;

    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDateTime reportUpDatedDateAndTime;

    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDateTime reportCreationDateAndTime;

    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate labRequestDate;

    public CbcDTO(Cbc cbc) {
        if (cbc != null) {
            this.id = cbc.id;
            this.visitId = cbc.visit != null ? cbc.visit.id : null;
            this.procedureRequestedId = cbc.procedureRequested != null ? cbc.procedureRequested.id : null;
            this.test = cbc.test;
            this.patientName = cbc.patientName;
            this.gender = cbc.gender;
            this.patientAge = cbc.patientAge;
            this.labReportTitle = cbc.labReportTitle;
            this.doneBy = cbc.doneBy;
            this.wbc = cbc.wbc;
            this.lymph = cbc.lymph;
            this.mid = cbc.mid;
            this.gran = cbc.gran;
            this.lymphPercent = cbc.lymphPercent;
            this.midPercent = cbc.midPercent;
            this.granPercent = cbc.granPercent;
            this.hgb = cbc.hgb;
            this.rbc = cbc.rbc;
            this.hct = cbc.hct;
            this.mcv = cbc.mcv;
            this.mch = cbc.mch;
            this.mchc = cbc.mchc;
            this.rdwCv = cbc.rdwCv;
            this.rdwSd = cbc.rdwSd;
            this.plt = cbc.plt;
            this.mpv = cbc.mpv;
            this.pdw = cbc.pdw;
            this.pct = cbc.pct;
            this.interpretations = parseInterpretationsForResponse(cbc.interpretations);
            this.sampleCollectionDateAndTime = cbc.sampleCollectionDateAndTime;
            this.procedureDoneDateAndTime = cbc.procedureDoneDateAndTime;
            this.reportUpDatedDateAndTime = cbc.reportUpDatedDateAndTime;
            this.reportCreationDateAndTime = cbc.reportCreationDateAndTime;
            this.labRequestDate = cbc.labRequestDate;
        }
    }

    private static Object parseInterpretationsForResponse(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String trimmed = raw.trim();
        if (trimmed.charAt(0) != '{') {
            return trimmed;
        }
        try (Jsonb jsonb = JsonbBuilder.create()) {
            return jsonb.fromJson(trimmed, Map.class);
        } catch (Exception e) {
            return trimmed;
        }
    }
}
























