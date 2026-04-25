package org.example.visit.services.paloads.responses;

import org.example.consultations.services.payloads.responses.ConsultationDTO;
import org.example.procedure.procedureRequested.services.payloads.responses.ProcedureRequestedDTO;
import org.example.referrals.services.ReferralFormDTO;
import org.example.visit.domains.PatientVisit;
import org.example.vitals.services.payloads.responses.VitalsMonitoringChartDTO;
import org.example.admissions.services.payloads.responses.InPatientTreatmentDTO;
import org.example.finance.invoice.services.payloads.responses.InvoiceDTO;
import org.example.vitals.services.payloads.responses.InitialTriageVitalsDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

public class PatientVisitDTO {
    public Long id;
    public Long patientId;
    public LocalDate visitDate;
    public LocalTime visitTime;
    public String visitType;
    public Integer visitNumber;
    public String visitReason;
    public String visitName;
    public String visitStatus;
    public BigDecimal balanceDue;
    public BigDecimal amountPaid;
    public String patientName;
    public BigDecimal patientAge;
    public String patientAddress;
    public String patientContact;
    public String occupation;
    public String nextOfKinName;
    public String nextOfKinContact;
    public String relationship;
    public String nextOfKinAddress;
    public BigDecimal totalAmount;
    public BigDecimal subTotal;

    public String visitGroup;



    public List<ProcedureRequestedDTO> proceduresRequested;
    public List<InitialTriageVitalsDTO> initialTriageVitals;
    public List<VitalsMonitoringChartDTO> vitalsMonitoringChart;
    public List<ConsultationDTO> consultation;
    public List<InvoiceDTO> invoice;
    public List<InPatientTreatmentDTO> inPatientTreatments;
    public List<ReferralFormDTO> referralForm;

    // Constructor
    public PatientVisitDTO(PatientVisit patientVisit) {
        this.id = patientVisit.id;
        this.patientId = patientVisit.patient != null ? patientVisit.patient.id : null;
        this.visitDate = patientVisit.visitDate;
        this.visitTime = patientVisit.visitTime;
        this.visitType = patientVisit.visitType;
        this.visitNumber = patientVisit.visitNumber;
        this.visitReason = patientVisit.visitReason;
        this.visitName = patientVisit.visitName;
        this.visitStatus = patientVisit.visitStatus;
        this.patientName = patientVisit.patientName;
        this.patientAge = patientVisit.patientAge;
        this.patientAddress = patientVisit.patientAddress;
        this.patientContact = patientVisit.patientContact;
        this.occupation = patientVisit.occupation;
        this.nextOfKinName = patientVisit.nextOfKinName;
        this.nextOfKinContact = patientVisit.nextOfKinContact;
        this.relationship = patientVisit.relationship;
        this.nextOfKinAddress = patientVisit.nextOfKinAddress;

        this.balanceDue = patientVisit.balanceDue;
        this.amountPaid = patientVisit.amountPaid;
        this.totalAmount = patientVisit.totalAmount;
        this.subTotal = patientVisit.subTotal;
        this.visitGroup = patientVisit.visitGroup;



        this.proceduresRequested = patientVisit.getProceduresRequested() != null ?
                patientVisit.getProceduresRequested().stream()
                        .map(ProcedureRequestedDTO::new)
                        .collect(Collectors.toList()) : null;

        this.initialTriageVitals = patientVisit.getInitialTriageVitals() != null ?
                patientVisit.getInitialTriageVitals().stream()
                        .map(InitialTriageVitalsDTO::new)
                        .collect(Collectors.toList()) : null;

        this.vitalsMonitoringChart = patientVisit.getVitalsMonitoringChart() != null ?
                patientVisit.getVitalsMonitoringChart().stream()
                        .map(VitalsMonitoringChartDTO::new)
                        .collect(Collectors.toList()) : null;

        this.consultation = patientVisit.getConsultation() != null ?
                patientVisit.getConsultation().stream()
                        .map(ConsultationDTO::new)
                        .collect(Collectors.toList()) : null;

        this.invoice = patientVisit.getInvoice() != null ?
                patientVisit.getInvoice().stream()
                        .map(InvoiceDTO::new)
                        .collect(Collectors.toList()) : null;

        this.inPatientTreatments = patientVisit.getInPatientTreatments() != null ?
                patientVisit.getInPatientTreatments().stream()
                        .map(InPatientTreatmentDTO::new)
                        .collect(Collectors.toList()) : null;

        this.referralForm = patientVisit.getReferralForm() != null ?
                patientVisit.getReferralForm().stream()
                        .map(ReferralFormDTO::new)
                        .collect(Collectors.toList()) : null;
    }

    // Getters
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

    public String getVisitStatus() {
        return visitStatus;
    }

    public List<ProcedureRequestedDTO> getProceduresRequested() {
        return proceduresRequested;
    }

    public List<InitialTriageVitalsDTO> getInitialTriageVitals() {
        return initialTriageVitals;
    }

    public List<VitalsMonitoringChartDTO> getVitalsMonitoringChart() {
        return vitalsMonitoringChart;
    }

    public List<ConsultationDTO> getConsultation() {
        return consultation;
    }

    public List<InvoiceDTO> getInvoice() {
        return invoice;
    }

    public List<InPatientTreatmentDTO> getInPatientTreatments() {
        return inPatientTreatments;
    }

    public List<ReferralFormDTO> getReferralForm() {
        return referralForm;
    }
}





