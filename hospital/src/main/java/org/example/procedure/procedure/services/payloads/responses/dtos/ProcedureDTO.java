package org.example.procedure.procedure.services.payloads.responses.dtos;

import org.example.procedure.procedure.domains.Procedure;

import java.math.BigDecimal;

public class ProcedureDTO {
    public Long id;
    //public String procedureType;
    public String category;
    public Long categoryId;
    public String categoryParent;
    public Long categoryParentId;
    public String description;
    public BigDecimal unitCostPrice;
    public BigDecimal unitSellingPrice;
    public Long unitSellingModelId;
    public String procedureName;

    public ProcedureDTO(Procedure procedure) {
        if (procedure != null) {
            this.id = procedure.id;
            //this.procedureType = procedure.procedureType;

            this.category = procedure.category != null ? procedure.category.name : null;
            this.categoryId = procedure.category != null ? procedure.category.id : null;

            this.categoryParent = procedure.parentCategory != null ? procedure.parentCategory.name : null;
            this.categoryParentId = procedure.parentCategory != null ? procedure.parentCategory.id : null;

            //this.categoryParent = procedure.parentCategory != null && procedure.category.parent != null ? procedure.category.parent.name : null;
            //this.categoryParentId = procedure.category != null && procedure.category.parent != null ? procedure.category.parent.id : null;

            this.description = procedure.description;
            this.unitCostPrice = procedure.unitCostPrice;
            this.unitSellingPrice = procedure.unitSellingPrice;
            this.unitSellingModelId = procedure.unitSellingModelId;
            this.procedureName = procedure.procedureName;
        }
    }
}










