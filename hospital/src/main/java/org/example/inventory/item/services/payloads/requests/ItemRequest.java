package org.example.inventory.item.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class ItemRequest {

    @Schema(example = "Artesunate 30mg IV")
    public String genericName;

    @Schema(example = "1")
    public Long categoryId;

    @Schema(example = "complicated malaria")
    public String contraIndication;

    @Schema(example = "pregnancy")
    public String indication;

    @Schema(example = "This is a sample description")
    public String description;

}
