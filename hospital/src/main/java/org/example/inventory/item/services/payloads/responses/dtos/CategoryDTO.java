package org.example.inventory.item.services.payloads.responses.dtos;

import org.example.inventory.item.domain.Category;

public class CategoryDTO {
    public Long id;
    public String name;
    public Long parentId;




    public CategoryDTO(Category category) {
        this.id = category.id;
        this.name = category.name;
        this.parentId = category.parent != null ? category.parent.id : null;





}
}
