package org.example.lab.generalReport.services.Payloads.responses;

import jakarta.json.bind.annotation.JsonbDateFormat;
import org.example.lab.generalReport.domains.GeneralLabReport;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class GeneralLabReportDTO {
    public Long id;
    public Long visitId;
    public Long procedureRequestedId;
    public String test;
    public String result;
    public String notes;
    public String recommendation;
    public String patientName;
    public String gender;
    public BigDecimal patientAge;
    public String labReportTitle;
    public String doneBy;

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

    public GeneralLabReportDTO(GeneralLabReport report) {
        if (report != null) {
            this.id = report.id;
            this.visitId = report.visit != null ? report.visit.id : null;
            this.procedureRequestedId = report.procedureRequested != null ? report.procedureRequested.id : null;
            this.test = report.test;
            this.result = report.result;
            this.notes = report.notes;
            this.recommendation = report.recommendation;
            this.patientName = report.patientName;
            this.gender = report.gender;
            this.patientAge = report.patientAge;
            this.labReportTitle = report.labReportTitle;
            this.doneBy = report.doneBy;
            this.sampleCollectionDateAndTime = report.sampleCollectionDateAndTime;
            this.procedureDoneDateAndTime = report.procedureDoneDateAndTime;
            this.reportUpDatedDateAndTime = report.reportUpDatedDateAndTime;
            this.reportCreationDateAndTime = report.reportCreationDateAndTime;
            this.labRequestDate = report.labRequestDate;
        }
    }
}

























