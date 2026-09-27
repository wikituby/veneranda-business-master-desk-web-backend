package org.example.visit.services.paloads.responses;

import org.example.consultations.services.payloads.responses.ConsultationDTO;
import org.example.pharmacy.sundry.domains.VisitSundry;
import org.example.procedure.procedureRequested.domains.ProcedureRequested;
import org.example.procedure.procedureRequested.services.payloads.responses.ProcedureRequestedDTO;
import org.example.referrals.services.ReferralFormDTO;
import org.example.treatment.domains.TreatmentRequested;
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
    /** Seller confirmed goods delivered for this order (independent of payment). */
    public Boolean goodsDelivered;
    /** Note entered when delivery was confirmed. */
    public String deliveryNote;
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
    /** Total amount the patient pays (after discount and tax). */
    public BigDecimal totalSell;
    public BigDecimal tax;
    public BigDecimal discount;
    /** Internal cost of drugs/procedures/sundries for the visit (TT cost / expenses). */
    public BigDecimal totalCost;
    public BigDecimal treatmentTotalSell;
    public BigDecimal totalSellForProcedure;
    public BigDecimal totalCostOfProcedures;
    public BigDecimal totalCostOfTreatment;

    public String visitGroup;

    /** Primary consultation diagnosis for list/filter views (not a persisted visit column). */
    public String diagnosis;



    public List<ProcedureRequestedDTO> proceduresRequested;
    public List<InitialTriageVitalsDTO> initialTriageVitals;
    public List<VitalsMonitoringChartDTO> vitalsMonitoringChart;
    public List<ConsultationDTO> consultation;
    public List<InvoiceDTO> invoice;
    public List<InPatientTreatmentDTO> inPatientTreatments;
    public List<ReferralFormDTO> referralForm;

    // Constructor
    public PatientVisitDTO(PatientVisit patientVisit) {
        this(patientVisit, true);
    }

    /**
     * Lightweight row for visit list / filter endpoints.
     * Skips nested collections and per-visit financial recomputation (those caused
     * connection timeouts on multi-month ranges).
     */
    public static PatientVisitDTO forListRow(PatientVisit patientVisit, String diagnosis) {
        PatientVisitDTO dto = new PatientVisitDTO(patientVisit, false);
        dto.diagnosis = diagnosis;
        return dto;
    }

    private PatientVisitDTO(PatientVisit patientVisit, boolean includeDetails) {
        this.id = patientVisit.id;
        this.patientId = patientVisit.patient != null ? patientVisit.patient.id : null;
        this.visitDate = patientVisit.visitDate;
        this.visitTime = patientVisit.visitTime;
        this.visitType = patientVisit.visitType;
        this.visitNumber = patientVisit.visitNumber;
        this.visitReason = patientVisit.visitReason;
        this.visitName = patientVisit.visitName;
        this.visitStatus = patientVisit.visitStatus;
        this.goodsDelivered = Boolean.TRUE.equals(patientVisit.goodsDelivered);
        this.deliveryNote = patientVisit.deliveryNote;
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
        this.totalSell = patientVisit.totalSell != null ? patientVisit.totalSell : patientVisit.totalAmount;
        this.tax = patientVisit.tax;
        this.discount = patientVisit.discount;
        this.visitGroup = patientVisit.visitGroup;

        if (!includeDetails) {
            this.totalCost = patientVisit.totalCost != null ? patientVisit.totalCost : BigDecimal.ZERO;
            this.totalCostOfProcedures = patientVisit.totalCostOfProcedures != null
                    ? patientVisit.totalCostOfProcedures
                    : BigDecimal.ZERO;
            this.totalCostOfTreatment = patientVisit.totalCostOfTreatment != null
                    ? patientVisit.totalCostOfTreatment
                    : BigDecimal.ZERO;
            this.treatmentTotalSell = patientVisit.treatmentTotalSell != null
                    ? patientVisit.treatmentTotalSell
                    : BigDecimal.ZERO;
            this.totalSellForProcedure = patientVisit.totalSellForProcedure != null
                    ? patientVisit.totalSellForProcedure
                    : BigDecimal.ZERO;
            return;
        }

        ComputedVisitFinancials computed = needsComputedFinancials(patientVisit)
                ? computeVisitFinancials(patientVisit)
                : null;
        this.totalCost = patientVisit.totalCost != null ? patientVisit.totalCost : computed.totalCost();
        this.totalCostOfProcedures = patientVisit.totalCostOfProcedures != null
                ? patientVisit.totalCostOfProcedures
                : computed.procedureCost();
        this.totalCostOfTreatment = patientVisit.totalCostOfTreatment != null
                ? patientVisit.totalCostOfTreatment
                : computed.treatmentCost();
        this.treatmentTotalSell = patientVisit.treatmentTotalSell != null
                ? patientVisit.treatmentTotalSell
                : computed.treatmentSell();
        this.totalSellForProcedure = patientVisit.totalSellForProcedure != null
                ? patientVisit.totalSellForProcedure
                : computed.procedureSell();

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

    private static boolean needsComputedFinancials(PatientVisit visit) {
        return visit.totalCost == null
                || visit.totalCostOfProcedures == null
                || visit.totalCostOfTreatment == null
                || visit.treatmentTotalSell == null
                || visit.totalSellForProcedure == null;
    }

    /**
     * Cost and selling breakdown for visits that predate persisted financial columns.
     */
    private static ComputedVisitFinancials computeVisitFinancials(PatientVisit visit) {
        if (visit == null || visit.id == null) {
            return new ComputedVisitFinancials(
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        BigDecimal treatmentCost = BigDecimal.ZERO;
        BigDecimal treatmentSell = BigDecimal.ZERO;
        List<TreatmentRequested> treatments = TreatmentRequested.find("visit.id", visit.id).list();
        if (treatments != null) {
            for (TreatmentRequested t : treatments) {
                if (t == null || !t.countsTowardInvoice()) {
                    continue;
                }
                if (t.unitBuy != null && t.quantity != null) {
                    treatmentCost = treatmentCost.add(t.unitBuy.multiply(t.quantity));
                }
                if (t.totalAmount != null) {
                    treatmentSell = treatmentSell.add(t.totalAmount);
                }
            }
        }

        BigDecimal procedureCost = BigDecimal.ZERO;
        BigDecimal procedureSell = BigDecimal.ZERO;
        List<ProcedureRequested> procedures = ProcedureRequested.find("visit.id", visit.id).list();
        if (procedures != null) {
            for (ProcedureRequested p : procedures) {
                if (p == null) {
                    continue;
                }
                if (p.procedure != null && p.procedure.unitCostPrice != null) {
                    procedureCost = procedureCost.add(
                            p.procedure.unitCostPrice.multiply(BigDecimal.valueOf(p.quantity)));
                }
                if (p.totalAmount != null) {
                    procedureSell = procedureSell.add(p.totalAmount);
                }
            }
        }

        BigDecimal sundryCost = BigDecimal.ZERO;
        BigDecimal sundrySell = BigDecimal.ZERO;
        List<VisitSundry> sundries = VisitSundry.find("patientVisitId", visit.id).list();
        if (sundries != null) {
            for (VisitSundry s : sundries) {
                if (s == null || s.quantityUsed == null) {
                    continue;
                }
                if (s.unitCostPrice != null) {
                    sundryCost = sundryCost.add(s.quantityUsed.multiply(s.unitCostPrice));
                }
                if (s.unitSellingPrice != null) {
                    sundrySell = sundrySell.add(s.quantityUsed.multiply(s.unitSellingPrice));
                }
            }
        }

        treatmentCost = treatmentCost.add(sundryCost);
        treatmentSell = treatmentSell.add(sundrySell);
        return new ComputedVisitFinancials(
                treatmentCost.add(procedureCost),
                procedureCost,
                treatmentCost,
                treatmentSell,
                procedureSell);
    }

    private record ComputedVisitFinancials(
            BigDecimal totalCost,
            BigDecimal procedureCost,
            BigDecimal treatmentCost,
            BigDecimal treatmentSell,
            BigDecimal procedureSell
    ) {}

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





