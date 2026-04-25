package org.example.inventory.store.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class StoreRequest {

    @Schema(example = "Main")
    public String name;

    @Schema(example = "Hospital")
    public String location;

    @Schema(example = "Stores all the drugs for sale")
    public String description;

    @Schema(example = "true", description = "Whether this store is the default store")
    public Boolean defaultStatus;

}
