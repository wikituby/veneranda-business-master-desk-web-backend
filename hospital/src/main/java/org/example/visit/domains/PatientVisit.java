package org.example.visit.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.persistence.*;
import org.example.client.domains.Patient;
import org.example.consultations.domains.Consultation;
import org.example.finance.invoice.domains.Invoice;
import org.example.procedure.procedureRequested.domains.ProcedureRequested;
import org.example.referrals.domains.ReferralForm;
import org.example.treatment.domains.TreatmentRequested;
import org.example.admissions.domains.InPatientTreatment;
import org.example.vitals.domains.InitialTriageVitals;
import org.example.vitals.domains.VitalsMonitoringChart;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class PatientVisit extends PanacheEntity {

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    public Patient patient;

    @Column
    @JsonbDateFormat(value = "dd/MM/yyyy")
    public LocalDate visitDate;

    @Column
    public LocalTime visitTime;  // Stores the time of the visit

    @Column
    public String visitType; // e.g., "outpatient or inpatient"

    @Column(nullable = false)
    public int visitNumber;  // A unique identifier for each visit within a patient’s records

    @Column
    public String visitReason;

    @Column
    public String patientName;

    @Column
    public BigDecimal patientAge;

    @Column
    public String patientAddress;

    @Column
    public String patientContact;

    @Column
    public String occupation;

    @Column
    public String nextOfKinName;

    @Column
    public String nextOfKinContact;

    @Column
    public String relationship;

    @Column
    public String nextOfKinAddress;

    @Column
    public String visitGroup;

    @Column
    public String visitStatus;

    @Column
    public String visitName;

    @Column
    public BigDecimal balanceDue;

    @Column
    public BigDecimal amountPaid;

    @Column
    public BigDecimal totalAmount;

    @Column
    public BigDecimal subTotal;


    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate visitLastUpdatedDate;


    //////////////////////////// Medical Details ///////////////////////////////


    @OneToMany(mappedBy = "visit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TreatmentRequested> treatmentRequested = new ArrayList<>(); // Private list

    @OneToMany(mappedBy = "visit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProcedureRequested> ProceduresRequested = new ArrayList<>(); // Private list

    @OneToMany(mappedBy = "visit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InitialTriageVitals> initialTriageVitals = new ArrayList<>();

    @OneToMany(mappedBy = "visit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VitalsMonitoringChart> vitalsMonitoringChart = new ArrayList<>();  // Private list

    @OneToMany(mappedBy = "visit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Consultation> consultation = new ArrayList<>(); ;  // A single Consultation for the visit

    //@OneToMany(mappedBy = "visit", cascade = CascadeType.ALL, orphanRemoval = true)
    //private List<Recommendation> recommendation = new ArrayList<>(); ;  // A single recommendation for the visit

    @OneToMany(mappedBy = "visit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InPatientTreatment> inPatientTreatments = new ArrayList<>();  // Private list

    @OneToMany(mappedBy = "visit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReferralForm> referralForm = new ArrayList<>(); ;  // A single referral form for the visit

    @OneToMany(mappedBy = "visit", fetch = FetchType.EAGER,cascade = CascadeType.ALL, orphanRemoval = true)
    public List<Invoice> invoice = new ArrayList<>(); ;  // A single invoice for the visit

    // Getters and Setters for private fields


    public List<TreatmentRequested> getTreatmentRequested() {
       return treatmentRequested;
    }

    public void setTreatmentRequested(List<TreatmentRequested> treatmentRequested) {
        this.treatmentRequested = treatmentRequested;
    }

    public List<ProcedureRequested> getProceduresRequested() {
        return ProceduresRequested;
    }

    public void setProceduresRequested(List<ProcedureRequested> proceduresRequested) {
        this.ProceduresRequested = proceduresRequested;
    }

    public List<InitialTriageVitals> getInitialTriageVitals() {
        return initialTriageVitals;
    }

    public void setInitialTriageVitals(List<InitialTriageVitals> initialTriageVitals) {
        this.initialTriageVitals = initialTriageVitals;
    }

    public List<VitalsMonitoringChart> getVitalsMonitoringChart() {
        return vitalsMonitoringChart;
    }

    public void setVitalsMonitoringChart(List<VitalsMonitoringChart> vitalsMonitoringChart) {
        this.vitalsMonitoringChart = vitalsMonitoringChart;
    }

    public List<Consultation> getConsultation() {
        return consultation;
    }

    public void setConsultation(List<Consultation> consultation) {
        this.consultation = consultation;
    }

    public List<Invoice> getInvoice() {
        return invoice;
    }

    public void setInvoice(List<Invoice> invoice) {
        this.invoice = invoice;
    }


    //public List<Recommendation> getRecommendation() {
    //    return recommendation;
    //}

    //public void setRecommendation(List<Recommendation> recommendation) {
     //   this.recommendation = recommendation;
    //}

    public List<InPatientTreatment> getInPatientTreatments() {
        return inPatientTreatments;
    }

    public void setInPatientTreatments(List<InPatientTreatment> inPatientTreatments) {
        this.inPatientTreatments = inPatientTreatments;
    }

    public List<ReferralForm> getReferralForm() {
        return referralForm;
    }

    public void setReferralForm(List<ReferralForm> referralForm) {
        this.referralForm = referralForm;
    }

    public Patient getPatient() {
        return patient;
    }

    // Setter for patient (optional, depending on your needs)
    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Object getTotalBalanceDue() {
        return balanceDue;
    }

    public BigDecimal getBalanceDue() {
        return balanceDue;
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
}




