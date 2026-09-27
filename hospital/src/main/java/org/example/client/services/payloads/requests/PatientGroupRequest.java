package org.example.client.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class PatientGroupRequest {

    @Schema(example = "Katoma Child and Youth Development Center ")
    public String groupName;

    @Schema(example = "Katoma Child and Youth Development Center, Takes care of the under privileged")
    public String description;

    @Schema(example = "compassion")
    public String groupNameShortForm;

    @Schema(example = "compassion@gmail.com")
    public String groupEmail;

    @Schema(example = "256773974885")
    public String groupContact;

    @Schema(example = "katoma")
    public String groupAddress;

    @Schema(description = "When true, group members may incur more debt despite unpaid balances")
    public Boolean allowsCreditDespiteUnpaidBalance;
}
