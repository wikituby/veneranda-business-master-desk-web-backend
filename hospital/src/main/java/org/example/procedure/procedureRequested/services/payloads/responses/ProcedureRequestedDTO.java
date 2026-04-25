package org.example.procedure.procedureRequested.services.payloads.responses;

import jakarta.json.bind.annotation.JsonbDateFormat;
import org.example.procedure.procedureRequested.domains.ProcedureRequested;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class ProcedureRequestedDTO {
    public Long id;
    public Long visitId;
    //public String procedureRequestedType;
    public String procedureRequestedName;
    public String category;
    public int quantity;
    public BigDecimal unitSellingPrice;
    public BigDecimal unitCostPrice;
    public BigDecimal totalAmount;
    public String report;
    public String status;
    public String bgColor;
    public String patientProfilePic;

    public String patientName;
    public BigDecimal patientAge;
    public String patientGender;
    public String patientFileNumber;
    public String patientAddress;
    public String patientOccupation;
    public String patientContact;
    public Long patientId;

    public String orderedBy;
    public String doneBy;
    public String exam;
    public String indication;
    
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate dateOfProcedure;
    
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate updateDate;
    
    public LocalTime timeOfProcedure;
    public Long procedureId;

    public ProcedureRequestedDTO(ProcedureRequested procedureRequested) {
        if (procedureRequested != null) {
            this.id = procedureRequested.id;
            this.visitId = procedureRequested.visit != null ? procedureRequested.visit.id : null;
            //this.procedureRequestedType = procedureRequested.procedureRequestedType;
            this.procedureRequestedName = procedureRequested.procedure != null ? procedureRequested.procedure.procedureName : procedureRequested.procedureRequestedName;
            this.category = procedureRequested.procedure != null && procedureRequested.procedure.category != null ? procedureRequested.procedure.category.name : procedureRequested.category;
            this.quantity = procedureRequested.quantity;
            this.unitSellingPrice = procedureRequested.unitSellingPrice;
            this.unitCostPrice = procedureRequested.procedure != null ? procedureRequested.procedure.unitCostPrice : BigDecimal.ZERO;
            this.totalAmount = procedureRequested.totalAmount;
            this.report = procedureRequested.report;
            this.status = procedureRequested.status;
            this.bgColor = procedureRequested.bgColor;

            this.patientName = procedureRequested.visit != null ? procedureRequested.visit.patientName : procedureRequested.patientName;
            
            if(procedureRequested.visit != null && procedureRequested.visit.patient != null){
                this.patientAddress = procedureRequested.visit.patient.nextOfKinAddress;
                this.patientAge = procedureRequested.visit.patient.patientAge;
                this.patientGender = procedureRequested.visit.patient.patientGender;
                this.patientOccupation = procedureRequested.visit.patient.occupation;
                this.patientId = procedureRequested.visit.patient.id;
                this.patientContact = procedureRequested.visit.patient.patientContact;
                
                if(procedureRequested.visit.patient.patientFileNo != null){
                    this.patientFileNumber = procedureRequested.visit.patient.patientFileNo;
                }
                
                if(procedureRequested.visit.patient.patientProfilePic != null){
                    this.patientProfilePic = procedureRequested.visit.patient.patientProfilePic;
                }
            }
        

            this.orderedBy = procedureRequested.orderedBy;
            this.doneBy = procedureRequested.doneBy;
            this.exam = procedureRequested.exam;
            this.indication = procedureRequested.indication;
            this.dateOfProcedure = procedureRequested.dateOfProcedure;
            this.updateDate = procedureRequested.updateDate;
            this.timeOfProcedure = procedureRequested.timeOfProcedure;
            this.procedureId = procedureRequested.procedureId;
        }
    }
}










