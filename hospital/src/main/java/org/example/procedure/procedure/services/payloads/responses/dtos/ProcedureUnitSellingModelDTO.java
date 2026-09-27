package org.example.procedure.procedure.services.payloads.responses.dtos;

import org.example.procedure.procedure.domains.ProcedureUnitSellingModel;

import java.math.BigDecimal;

public class ProcedureUnitSellingModelDTO {
    public Long id;
    public Long procedureId;
    public String name;
    public BigDecimal unitSellingPrice;
    public Integer unitsInBundle;
    public BigDecimal bundlePrice;
    public BigDecimal profitMargin;
    public Integer sortOrder;
    public Boolean isDefault;

    public ProcedureUnitSellingModelDTO() {
    }

    public ProcedureUnitSellingModelDTO(ProcedureUnitSellingModel entity) {
        this.id = entity.id;
        this.procedureId = entity.procedure != null ? entity.procedure.id : null;
        this.name = entity.name;
        this.unitSellingPrice = entity.unitSellingPrice;
        this.unitsInBundle = entity.unitsInBundle;
        this.bundlePrice = entity.bundlePrice;
        this.profitMargin = entity.profitMargin;
        this.sortOrder = entity.sortOrder;
        this.isDefault = entity.isDefault;
    }
}
