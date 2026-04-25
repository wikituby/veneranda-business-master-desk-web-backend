package org.example.client.services.payloads.responses.dtos;

import org.example.client.domains.Patient;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PatientDTO {
    public Long id;
    public String patientFirstName;
    public String patientSecondName;
    public String patientAddress;
    public String patientContact;
    public String patientGender;
    public BigDecimal patientAge;
    public LocalDate patientDateOfBirth;
    public String patientProfilePic;
    public LocalDate creationDate;
    public LocalDate patientLastUpdatedDate;
    public String patientFileNo;
    public Integer patientNo;
    public String nextOfKinName;
    public String nextOfKinContact;
    public String relationship;
    public Long patientGroupId;
    public String patientGroupName;
    public BigDecimal totalAmountDue;
    public String nextOfKinAddress;
    public String occupation;


    public PatientDTO(Patient patient) {
        this.id = patient.id;
        this.patientGroupId = patient.patientGroup != null ? patient.patientGroup.id : null;
        this.patientGroupName = patient.patientGroup != null ? patient.patientGroup.groupName : null;
        this.patientFirstName = patient.patientFirstName;
        this.patientSecondName = patient.patientSecondName;
        this.patientAddress = patient.patientAddress;
        this.patientContact = patient.patientContact;
        this.patientGender = patient.patientGender;
        this.patientAge = patient.patientAge;
        this.patientDateOfBirth = patient.patientDateOfBirth;
        this.patientProfilePic = patient.patientProfilePic;
        this.creationDate = patient.creationDate;
        this.patientLastUpdatedDate = patient.patientLastUpdatedDate;
        this.patientFileNo = patient.patientFileNo;
        this.patientNo = patient.patientNo;
        this.nextOfKinName = patient.nextOfKinName;
        this.nextOfKinContact = patient.nextOfKinContact;
        this.relationship = patient.relationship;
        this.totalAmountDue = patient.totalAmountDue;
        this.nextOfKinAddress = patient.nextOfKinAddress;
        this.occupation = patient.occupation;
    }
}










