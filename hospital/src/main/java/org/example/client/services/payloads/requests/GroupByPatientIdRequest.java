package org.example.client.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class GroupByPatientIdRequest {

    @Schema(example = "Katoma Child and Youth Development Center")
    public String groupName;
}






