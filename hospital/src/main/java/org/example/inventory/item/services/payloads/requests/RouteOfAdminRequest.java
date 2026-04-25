package org.example.inventory.item.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class RouteOfAdminRequest {

    @Schema(example = "intravenous")
    public String title;

    @Schema(example = "iv")
    public String standardAbbreviation;

    @Schema(example = "through the vein")
    public String description;
}
