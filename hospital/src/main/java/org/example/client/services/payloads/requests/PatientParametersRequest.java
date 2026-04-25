package org.example.client.services.payloads.requests;

import jakarta.ws.rs.QueryParam;

public class PatientParametersRequest {

    @QueryParam("group_id")
    public Long group_id;

    @QueryParam("patientAddress")
    public String patientAddress;


    @QueryParam("patientGender")
    public String patientGender;


}






