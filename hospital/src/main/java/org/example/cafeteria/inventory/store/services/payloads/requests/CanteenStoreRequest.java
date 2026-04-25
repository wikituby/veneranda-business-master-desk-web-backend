package org.example.cafeteria.inventory.store.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class CanteenStoreRequest {

    @Schema(example = "Main")
    public String name;

    @Schema(example = "Hospital")
    public String location;

    @Schema(example = "Stores all the drugs for sale")
    public String description;

}






