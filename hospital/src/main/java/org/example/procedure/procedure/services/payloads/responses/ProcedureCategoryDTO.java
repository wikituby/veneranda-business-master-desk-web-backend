package org.example.procedure.procedure.services.payloads.responses;

import org.example.procedure.procedure.domains.ProcedureCategory;

public class ProcedureCategoryDTO {

    public Long id;
    public String name;
    public String parentName;
    public Long parentId;

    public ProcedureCategoryDTO(ProcedureCategory category) {
        this.id = category.id;
        this.name = category.name;
        this.parentName = category.parent != null ? category.parent.name : null;
        this.parentId = category.parent != null ? category.parent.id : null;
    }
}
