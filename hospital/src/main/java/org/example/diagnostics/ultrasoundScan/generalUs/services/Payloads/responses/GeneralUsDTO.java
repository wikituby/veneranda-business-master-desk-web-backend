package org.example.diagnostics.ultrasoundScan.generalUs.services.Payloads.responses;

import jakarta.json.bind.annotation.JsonbDateFormat;
import org.example.diagnostics.ultrasoundScan.generalUs.domains.GeneralUs;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class GeneralUsDTO {
    public Long id;
    public Long visitId;
    public Long procedureRequestedId;
    public String indication;
    public String patientName;
    public String scanReportTitle;
    public BigDecimal patientAge;
    public String gender;
    public String doneBy;
    public String exam;
    public String findings;
    public String impression;
    public String recommendation;
    
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate upDatedDate;
    
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate scanRequestDate;
    
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate scanPerformingDate;
    
    public LocalTime timeOfProcedure;

    public GeneralUsDTO(GeneralUs generalUs) {
        if (generalUs != null) {
            this.id = generalUs.id;
            this.visitId = generalUs.visit != null ? generalUs.visit.id : null;
            this.procedureRequestedId = generalUs.procedureRequested != null ? generalUs.procedureRequested.id : null;
            this.indication = generalUs.indication;
            this.patientName = generalUs.patientName;
            this.scanReportTitle = generalUs.scanReportTitle;
            this.patientAge = generalUs.patientAge;
            this.gender = generalUs.gender;
            this.doneBy = generalUs.doneBy;
            this.exam = generalUs.procedureRequested.procedureRequestedName;
        
            this.findings = generalUs.findings;
            this.impression = generalUs.impression;
            this.recommendation = generalUs.recommendation;
            this.upDatedDate = generalUs.upDatedDate;
            this.scanRequestDate = generalUs.scanRequestDate;
            this.scanPerformingDate = generalUs.scanPerformingDate;
            this.timeOfProcedure = generalUs.timeOfProcedure;
        }
    }
}










