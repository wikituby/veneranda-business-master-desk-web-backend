package org.example.inventory.item.services.payloads.responses.dtos;

import org.example.inventory.item.domain.ActiveIngredient;

public class ActiveIngredientDTO {
    public Long id;
    public String number;
    public Long categoryId;
    public String categoryName;
    public String genericName;
    public String description;

    public ActiveIngredientDTO(ActiveIngredient activeIngredient) {
        this.id = activeIngredient.id;
        this.number = activeIngredient.number;
        this.categoryId = activeIngredient.categoryId;
        this.genericName = activeIngredient.genericName;
        this.categoryName = activeIngredient.categoryName;
        this.description = activeIngredient.description;
    }
}
