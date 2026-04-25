package org.example.procedure.procedure.services.payloads.responses.dtos;

import org.example.procedure.procedure.domains.ProcedureCategory;

public class ProcedureCategoryDTO {
    public Long id;
    public String name;
    public Long parentId;
    public String parentCategoryName;



    public ProcedureCategoryDTO(ProcedureCategory category) {
        this.id = category.id;
        this.name = category.name;
    
        if (category.parent != null) {
            this.parentId = category.parent.id;
            this.parentCategoryName = category.parent.name;
        }
    }
    


}










