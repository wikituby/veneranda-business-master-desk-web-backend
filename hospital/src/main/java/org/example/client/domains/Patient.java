package org.example.client.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.persistence.*;
import org.example.visit.domains.PatientVisit;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Patient extends PanacheEntity {

    ///////////////////// patient data//////////////////////////
    @Column(nullable = false)
    public String patientFirstName;

    @Column(nullable = false)
    public String patientSecondName;

    @ManyToOne
    @JoinColumn(name = "group_id")
    public PatientGroup patientGroup;

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
    @JsonbDateFormat(value = "yyyy-MM-dd")
    public LocalDate patientDateOfBirth;

    @Column
    public String patientProfilePic;

    @Column
    public String occupation;

    @Column
    public LocalDate creationDate;

    @Column
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
    private final List<PatientVisit> patientVisits = new ArrayList<>();


    public List<PatientVisit> getPatientVisits() {
        return patientVisits;
    }

    public Long getId(){
        return id;
    }


    public void setPatientGroup(PatientGroup patientGroup) {
        this.patientGroup = patientGroup;
    }



    public PatientGroup getPatientGroup() {
        return patientGroup;
    }

    public BigDecimal getTotalBalanceDue() {
        return totalAmountDue;
    }

    public void setTotalBalanceDue(BigDecimal totalAmountDue) {
        this.totalAmountDue = totalAmountDue;
    }


    public String getFirstName() {
        return patientFirstName;
    }

    public String getLastName() {
        return patientSecondName;
    }
}







