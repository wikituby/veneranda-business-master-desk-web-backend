package org.example.cafeteria.client.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.persistence.*;
import org.example.cafeteria.sales.saleDay.domains.SaleDay;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Buyer extends PanacheEntity {

    ///////////////////// patient data//////////////////////////
    @Column(nullable = false)
    public String patientFirstName;

    @Column(nullable = false)
    public String patientSecondName;

    @ManyToOne
    @JoinColumn(name = "group_id")
    public BuyerGroup patientGroup;

    @Column
    public String patientAddress;

    @Column
    public String patientContact;

    @Column
    public String patientGender;

    @Column
    public BigDecimal patientAge;

    @Column
    public BigDecimal totalAmountDue;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate patientDateOfBirth;

    @Column
    public String patientProfilePic;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate creationDate;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate patientLastUpdatedDate;

    @Column(unique = true)
    public String patientFileNo;

    @Column(unique = true)
    public int patientNo;

    ///////////////////// Next of keen data//////////////////////////

    @Column
    public String nextOfKinName;

    @Column
    public String nextOfKinContact;

    @Column
    public String relationship;

    @Column
    public String nextOfKinAddress;

    ///////////////////// Patient Visits //////////////////////////

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    //public List<PatientVisit> patientVisits = new ArrayList<>(); // Initialize to an empty list
    private final List<SaleDay> patientVisits = new ArrayList<>();


    public List<SaleDay> getPatientVisits() {
        return patientVisits;
    }

    public Long getId(){
        return id;
    }


    public void setPatientGroup(BuyerGroup patientGroup) {
        this.patientGroup = patientGroup;
    }



    public BuyerGroup getPatientGroup() {
        return patientGroup;
    }

    public BigDecimal getTotalBalanceDue() {
        return totalAmountDue;
    }

    public void setTotalBalanceDue(BigDecimal totalAmountDue) {
        this.totalAmountDue = totalAmountDue;
    }


}







