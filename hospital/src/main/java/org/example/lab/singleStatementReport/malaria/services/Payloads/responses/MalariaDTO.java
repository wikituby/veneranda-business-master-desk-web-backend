package org.example.lab.singleStatementReport.malaria.services.Payloads.responses;

import jakarta.json.bind.annotation.JsonbDateFormat;
import org.example.lab.singleStatementReport.malaria.domains.Malaria;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class MalariaDTO {
    public Long id;
    public Long visitId;
    public Long procedureRequestedId;
    public String test;
    public String bs;
    public String mrdt;
    public String notes;
    public String patientName;
    public String gender;
    public BigDecimal patientAge;
    public String recommendation;
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

    public MalariaDTO(Malaria malaria) {
        if (malaria != null) {
            this.id = malaria.id;
            this.visitId = malaria.visit != null ? malaria.visit.id : null;
            this.procedureRequestedId = malaria.procedureRequested != null ? malaria.procedureRequested.id : null;
            this.test = malaria.test;
            this.bs = malaria.bs;
            this.mrdt = malaria.mrdt;
            this.notes = malaria.notes;
            this.patientName = malaria.patientName;
            this.gender = malaria.gender;
            this.patientAge = malaria.patientAge;
            this.recommendation = malaria.recommendation;
            this.labReportTitle = malaria.labReportTitle;
            this.doneBy = malaria.doneBy;
            this.sampleCollectionDateAndTime = malaria.sampleCollectionDateAndTime;
            this.procedureDoneDateAndTime = malaria.procedureDoneDateAndTime;
            this.reportUpDatedDateAndTime = malaria.reportUpDatedDateAndTime;
            this.reportCreationDateAndTime = malaria.reportCreationDateAndTime;
            this.labRequestDate = malaria.labRequestDate;
        }
    }
}










