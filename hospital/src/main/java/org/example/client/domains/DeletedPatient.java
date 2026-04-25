package org.example.client.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
public class DeletedPatient extends PanacheEntity {

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


}








