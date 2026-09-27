package org.example.client.services.payloads.responses.dtos;

import org.example.client.domains.Patient;
import org.example.client.domains.PatientGroup;

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
    public java.util.List<Long> patientGroupIds;
    public java.util.List<String> patientGroupNames;
    public BigDecimal totalAmountDue;
    public String nextOfKinAddress;
    public String occupation;
    public String bloodGroup;


    public PatientDTO(Patient patient) {
        this.id = patient.id;
        this.patientGroupId = patient.patientGroup != null ? patient.patientGroup.id : null;
        this.patientGroupName = patient.patientGroup != null ? patient.patientGroup.groupName : null;
        this.patientGroupIds = new java.util.ArrayList<>();
        this.patientGroupNames = new java.util.ArrayList<>();
        for (PatientGroup g : patient.resolveAllGroups()) {
            if (g == null || g.id == null) {
                continue;
            }
            this.patientGroupIds.add(g.id);
            this.patientGroupNames.add(g.groupName != null ? g.groupName : ("Group #" + g.id));
        }
        // If membership empty but primary set, still expose primary.
        if (this.patientGroupIds.isEmpty() && this.patientGroupId != null) {
            this.patientGroupIds.add(this.patientGroupId);
            if (this.patientGroupName != null) {
                this.patientGroupNames.add(this.patientGroupName);
            }
        }
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
        this.bloodGroup = patient.bloodGroup;

        // Business UI aliases (Angular uses client* field names).
        this.clientFirstName = this.patientFirstName;
        this.clientSecondName = this.patientSecondName;
        this.clientAddress = this.patientAddress;
        this.clientContact = this.patientContact;
        this.clientGender = this.patientGender;
        this.clientAge = this.patientAge;
        this.clientDateOfBirth = this.patientDateOfBirth;
        this.clientProfilePic = this.patientProfilePic;
        this.clientFileNo = this.patientFileNo;
        this.clientNo = this.patientNo;
        this.clientGroupId = this.patientGroupId;
        this.clientGroupName = this.patientGroupName;
        this.clientGroupIds = this.patientGroupIds;
        this.clientGroupNames = this.patientGroupNames;
        this.clientLastUpdatedDate = this.patientLastUpdatedDate;
    }

    // --- business UI aliases (serialized alongside patient* fields) ---
    public String clientFirstName;
    public String clientSecondName;
    public String clientAddress;
    public String clientContact;
    public String clientGender;
    public BigDecimal clientAge;
    public LocalDate clientDateOfBirth;
    public String clientProfilePic;
    public String clientFileNo;
    public Integer clientNo;
    public Long clientGroupId;
    public String clientGroupName;
    public java.util.List<Long> clientGroupIds;
    public java.util.List<String> clientGroupNames;
    public LocalDate clientLastUpdatedDate;
}










