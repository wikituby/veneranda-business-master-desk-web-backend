package org.example.inventory.item.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class CategoryRequest {

    @Schema(example = "artesunate")
    public String name;

    @Schema(example = "1")
    public Long parentId;

}
