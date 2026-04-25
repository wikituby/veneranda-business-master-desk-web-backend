package org.example.cafeteria.client.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class GroupByBuyerIdRequest {

    @Schema(example = "Katoma Child and Youth Development Center")
    public String groupName;
}






