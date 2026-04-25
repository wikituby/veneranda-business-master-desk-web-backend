package org.example.store.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class StoreUpdateRequest {

    @Schema(example = "1")
    public Long storeId;

    @Schema(example = "Main Pharmacy")
    public String name;

    @Schema(example = "Block A, Ground Floor")
    public String location;

    @Schema(example = "Stores all medications and supplies")
    public String description;
}










