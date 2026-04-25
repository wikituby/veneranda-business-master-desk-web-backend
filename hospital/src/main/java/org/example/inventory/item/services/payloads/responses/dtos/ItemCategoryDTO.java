package org.example.inventory.item.services.payloads.responses.dtos;

import org.example.inventory.item.domain.ItemCategory;

public class ItemCategoryDTO {
    public Long id;
    public String name;
    public Long parentId;
    public String parentCategoryName;

    public ItemCategoryDTO(ItemCategory category) {
        this.id = category.id;
        this.name = category.name;

        if (category.parent != null) {
            this.parentId = category.parent.id;
            this.parentCategoryName = category.parent.name;
        }
    }
}

