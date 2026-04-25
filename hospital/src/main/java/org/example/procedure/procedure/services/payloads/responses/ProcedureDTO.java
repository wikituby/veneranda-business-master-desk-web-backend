package org.example.procedure.procedure.services.payloads.responses;

import org.example.procedure.procedure.domains.Procedure;

import java.math.BigDecimal;

public class ProcedureDTO {
    public Long id;
    public String procedureName;
    public String categoryName;
    public Long categoryId;
    public String description;
    public BigDecimal unitCostPrice;
    public BigDecimal unitSellingPrice;
    //public List<ItemDTO> itemsUsed;

    public ProcedureDTO(Procedure procedure){
        this.id = procedure.id;
        //this.procedureType = procedure.procedureType;
        this.procedureName = procedure.procedureName;
        this.categoryId = procedure.category != null ? procedure.category.id : null;
        this.categoryName = procedure.category != null ? procedure.category.name : null;
        this.description = procedure.description;
        this.unitCostPrice = procedure.unitCostPrice;
        this.unitSellingPrice = procedure.unitSellingPrice;
        // Mapping lists with proper null check and stream processing
       /*this.itemsUsed = labTest.getItemsUsed() != null ?
                labTest.getItemsUsed().stream()
                        .map(ItemDTO::new)
                        .collect(Collectors.toList()) : null;*/

    }

}
