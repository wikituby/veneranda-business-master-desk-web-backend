package org.example.cafeteria.sales.saleDay.services.payloads.responses;

import org.example.cafeteria.finance.invoice.services.payloads.responses.CanteenInvoiceDTO;
import org.example.cafeteria.sales.saleDay.domains.SaleDay;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

public class SaleDayDTO {
    public Long id;
    public Long patientId; // New field for patient ID
    public LocalDate visitDate;
    public LocalTime visitTime;
    public String visitType;
    public Integer visitNumber;
    public String visitReason;
    public String visitName;
    public String visitStatus;


    public List<CanteenInvoiceDTO> invoice;


    // Constructor
    public SaleDayDTO(SaleDay patientVisit) {
        this.id = patientVisit.id;
        this.patientId = patientVisit.patient != null ? patientVisit.patient.id : null;
        this.visitDate = patientVisit.visitDate;
        this.visitTime = patientVisit.visitTime;
        this.visitType = patientVisit.visitType;
        this.visitNumber = patientVisit.visitNumber;
        this.visitReason = patientVisit.visitReason;
        this.visitName = patientVisit.visitName;
        this.visitStatus = patientVisit.visitStatus;


        // Mapping lists with proper null check and stream processing

        /*this.treatmentRequested = patientVisit.getTreatmentRequested() != null ?
                patientVisit.getTreatmentRequested().stream()
                        .map(TreatmentRequestedDTO::new)
                        .collect(Collectors.toList()) : null;*/

        //this.patientId = patientVisit.patient != null ? patientVisit.patient.id : null;


        // this.invoiceId = patientVisit.getInvoice() != null ? patientVisit.getInvoice().id : null;


        this.invoice = patientVisit.getInvoice() != null ?
                patientVisit.getInvoice().stream()
                        .map(CanteenInvoiceDTO::new)
                        .collect(Collectors.toList()) : null;


    }

    // Getters (for accessing the public fields)
    public Long getId() {
        return id;
    }

    public Long getPatientId() {
        return patientId;
    }


    public LocalDate getVisitDate() {
        return visitDate;
    }

    public LocalTime getVisitTime() {
        return visitTime;
    }

    public String getVisitType() {
        return visitType;
    }

    public Integer getVisitNumber() {
        return visitNumber;
    }

    public String getVisitReason() {
        return visitReason;
    }

    public String getVisitName() {
        return visitName;
    }


    public List<CanteenInvoiceDTO> getInvoice() {
        return invoice;
    }

}






