package org.example.lab.parasitologyStool.services.Payloads.responses;

import jakarta.json.bind.annotation.JsonbDateFormat;
import org.example.lab.parasitologyStool.domains.ParasitologyStool;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ParasitologyStoolDTO {
    public Long id;
    public Long visitId;
    public Long procedureRequestedId;
    public String test;
    public String patientName;
    public String gender;
    public BigDecimal patientAge;
    public String labReportTitle;
    public String doneBy;
    public String ova;
    public String cysts;
    public String larvae;
    public String color;
    public String consistency;
    public String blood;
    public String mucus;
    public String visibleParasites;
    public String others;
    public String interpretations;

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

    public ParasitologyStoolDTO(ParasitologyStool report) {
        if (report != null) {
            this.id = report.id;
            this.visitId = report.visit != null ? report.visit.id : null;
            this.procedureRequestedId = report.procedureRequested != null ? report.procedureRequested.id : null;
            this.test = report.test;
            this.patientName = report.patientName;
            this.gender = report.gender;
            this.patientAge = report.patientAge;
            this.labReportTitle = report.labReportTitle;
            this.doneBy = report.doneBy;
            this.ova = report.ova;
            this.cysts = report.cysts;
            this.larvae = report.larvae;
            this.color = report.color;
            this.consistency = report.consistency;
            this.blood = report.blood;
            this.mucus = report.mucus;
            this.visibleParasites = report.visibleParasites;
            this.others = report.others;
            this.interpretations = report.interpretations;
            this.sampleCollectionDateAndTime = report.sampleCollectionDateAndTime;
            this.procedureDoneDateAndTime = report.procedureDoneDateAndTime;
            this.reportUpDatedDateAndTime = report.reportUpDatedDateAndTime;
            this.reportCreationDateAndTime = report.reportCreationDateAndTime;
            this.labRequestDate = report.labRequestDate;
        }
    }
}
























