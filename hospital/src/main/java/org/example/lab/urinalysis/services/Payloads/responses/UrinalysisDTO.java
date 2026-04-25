package org.example.lab.urinalysis.services.Payloads.responses;

import jakarta.json.bind.annotation.JsonbDateFormat;
import org.example.lab.urinalysis.domains.Urinalysis;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class UrinalysisDTO {
    public Long id;
    public Long visitId;
    public Long procedureRequestedId;
    public String test;
    public String patientName;
    public String gender;
    public BigDecimal patientAge;
    public String labReportTitle;
    public String doneBy;
    public String ph;
    public String sg;
    public String protein;
    public String glucose;
    public String ketones;
    public String blood;
    public String bilirubin;
    public String urobilinogen;
    public String nitrite;
    public String leukocyteE;
    public String epithelialCells;
    public String pusCellsWbcs;
    public String casts;
    public String redCells;
    public String crystals;
    public String color;
    public String appearance;
    public String volume;
    public String interpretations;
    public String others;

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

    public UrinalysisDTO(Urinalysis urinalysis) {
        if (urinalysis != null) {
            this.id = urinalysis.id;
            this.visitId = urinalysis.visit != null ? urinalysis.visit.id : null;
            this.procedureRequestedId = urinalysis.procedureRequested != null ? urinalysis.procedureRequested.id : null;
            this.test = urinalysis.test;
            this.patientName = urinalysis.patientName;
            this.gender = urinalysis.gender;
            this.patientAge = urinalysis.patientAge;
            this.labReportTitle = urinalysis.labReportTitle;
            this.doneBy = urinalysis.doneBy;
            this.ph = urinalysis.ph;
            this.sg = urinalysis.sg;
            this.protein = urinalysis.protein;
            this.glucose = urinalysis.glucose;
            this.ketones = urinalysis.ketones;
            this.blood = urinalysis.blood;
            this.bilirubin = urinalysis.bilirubin;
            this.urobilinogen = urinalysis.urobilinogen;
            this.nitrite = urinalysis.nitrite;
            this.leukocyteE = urinalysis.leukocyteE;
            this.epithelialCells = urinalysis.epithelialCells;
            this.pusCellsWbcs = urinalysis.pusCellsWbcs;
            this.casts = urinalysis.casts;
            this.redCells = urinalysis.redCells;
            this.crystals = urinalysis.crystals;
            this.color = urinalysis.color;
            this.appearance = urinalysis.appearance;
            this.volume = urinalysis.volume;
            this.interpretations = urinalysis.interpretations;
            this.others = urinalysis.others;
            this.sampleCollectionDateAndTime = urinalysis.sampleCollectionDateAndTime;
            this.procedureDoneDateAndTime = urinalysis.procedureDoneDateAndTime;
            this.reportUpDatedDateAndTime = urinalysis.reportUpDatedDateAndTime;
            this.reportCreationDateAndTime = urinalysis.reportCreationDateAndTime;
            this.labRequestDate = urinalysis.labRequestDate;
        }
    }
}
























