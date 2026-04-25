package org.example.inventory.stock.services.payloads.responses.dtos;

import org.example.inventory.stock.domains.AdjustmentType;

public class AdjustmentTypeDTO {
    public Long id;
    public String name;
    public String code;
    public Boolean active;

    public AdjustmentTypeDTO(AdjustmentType entity) {
        this.id = entity.id;
        this.name = entity.name;
        this.code = entity.code;
        this.active = entity.active;
    }
}
