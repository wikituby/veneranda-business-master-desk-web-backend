package org.example.cafeteria.sales.saleDay.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.persistence.*;
import org.example.cafeteria.client.domains.Buyer;
import org.example.cafeteria.finance.invoice.domains.CanteenInvoice;
import org.example.cafeteria.sales.saleDone.domains.Sale;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class SaleDay extends PanacheEntity {

    @ManyToOne
    @JoinColumn(name = "buyer_id", nullable = false)
    public Buyer patient;

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
    public String visitStatus;

    @Column
    public String visitName;


    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate visitLastUpdatedDate;


    //////////////////////////// Medical Details ///////////////////////////////


    @OneToMany(mappedBy = "saleDay", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Sale> treatmentRequested = new ArrayList<>(); // Private list

    @OneToMany(mappedBy = "saleDay", fetch = FetchType.EAGER,cascade = CascadeType.ALL, orphanRemoval = true)
    public List<CanteenInvoice> invoice = new ArrayList<>(); ;  // A single invoice for the visit

    // Getters and Setters for private fields


    public List<Sale> getTreatmentRequested() {
       return treatmentRequested;
    }

    public void setTreatmentRequested(List<Sale> treatmentRequested) {
        this.treatmentRequested = treatmentRequested;
    }



    public List<CanteenInvoice> getInvoice() {
        return invoice;
    }

    public void setInvoice(List<CanteenInvoice> invoice) {
        this.invoice = invoice;
    }


    //public List<Recommendation> getRecommendation() {
    //    return recommendation;
    //}

    //public void setRecommendation(List<Recommendation> recommendation) {
     //   this.recommendation = recommendation;
    //}


    public Buyer getPatient() {
        return patient;
    }

    // Setter for patient (optional, depending on your needs)
    public void setPatient(Buyer patient) {
        this.patient = patient;
    }

}






