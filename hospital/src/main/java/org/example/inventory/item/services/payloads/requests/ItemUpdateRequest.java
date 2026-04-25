package org.example.inventory.item.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;

public class ItemUpdateRequest {

    @Schema(example = "drug")
    public Long categoryId;

    @Schema(example = "Artesunate 60mg")
    public String genericName;

    @Schema(example = "Great outerwear jackets for Spring Autumn Winter, suitable for many occasions, such as working, hiking, camping, mountain rock climbing,")
    public String description;



}
