package org.example.cafeteria.client.services.payloads.responses.dtos;

import org.example.cafeteria.client.domains.Buyer;

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
    public BigDecimal totalAmountDue;

    public String nextOfKinAddress;

    public PatientDTO(Buyer buyer) {
        this.id = buyer.id;
        this.patientGroupId = buyer.patientGroup!= null ? buyer.patientGroup.id : null;
        this.patientFirstName = buyer.patientFirstName;
        this.patientSecondName = buyer.patientSecondName;
        this.patientAddress = buyer.patientAddress;
        this.patientContact = buyer.patientContact;
        this.patientGender = buyer.patientGender;
        this.patientAge = buyer.patientAge;
        this.patientDateOfBirth = buyer.patientDateOfBirth;
        this.patientProfilePic = buyer.patientProfilePic;
        this.creationDate = buyer.creationDate;
        this.patientLastUpdatedDate = buyer.patientLastUpdatedDate;
        this.patientFileNo = buyer.patientFileNo;
        this.patientNo = buyer.patientNo;
        this.nextOfKinName = buyer.nextOfKinName;
        this.nextOfKinContact = buyer.nextOfKinContact;
        this.relationship = buyer.relationship;
        this.totalAmountDue = buyer.totalAmountDue;
        this.nextOfKinAddress = buyer.nextOfKinAddress;
    }
}







