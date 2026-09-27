package org.example.client.services.payloads.responses.dtos;

import org.example.client.domains.PatientGroup;
import org.example.client.services.PatientGroupCreditUtil;

public class PatientGroupDTO {

    public Long id;
    public String groupName;
    public String groupNameShortForm;
    public String description;
    public String groupAddress;
    public String groupEmail;
    public String groupContact;
    public Boolean allowsCreditDespiteUnpaidBalance;

    public PatientGroupDTO(PatientGroup patientGroup) {
        this.id = patientGroup.id;
        this.groupName = patientGroup.groupName;
        this.description = patientGroup.description;
        this.groupNameShortForm = patientGroup.groupNameShortForm;
        this.groupAddress = patientGroup.groupAddress;
        this.groupEmail = patientGroup.groupEmail;
        this.groupContact = patientGroup.groupContact;
        this.allowsCreditDespiteUnpaidBalance =
                PatientGroupCreditUtil.allowsCreditDespiteUnpaidBalance(patientGroup);
    }
}










