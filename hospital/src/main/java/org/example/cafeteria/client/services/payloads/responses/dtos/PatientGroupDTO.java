package org.example.cafeteria.client.services.payloads.responses.dtos;

import org.example.cafeteria.client.domains.BuyerGroup;

public class PatientGroupDTO {

    public Long id;
    public String groupName;
    public String groupNameShortForm;
    public String description;
    public String groupAddress;
    public String groupEmail;
    public String groupContact;


    public PatientGroupDTO(BuyerGroup patientGroup){
        this.id = patientGroup.id;
        this.groupName = patientGroup.groupName;
        this.description = patientGroup.description;
        this.groupNameShortForm = patientGroup.groupNameShortForm;
        this.groupAddress = patientGroup.groupAddress;
        this.groupEmail = patientGroup.groupEmail;
        this.groupContact = patientGroup.groupContact;





    }

}






