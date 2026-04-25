package org.example.inventory.item.services.payloads.responses.dtos;

import org.example.inventory.item.domain.Strength;
import java.math.BigDecimal;

public class StrengthDTO {
    public Long id;
    public Long activeIngredientId;
    public BigDecimal strengthValue;
    public Long strengthUnitId;

    public StrengthDTO(Strength strength) {
        this.id = strength.id;
        this.activeIngredientId = strength.activeIngredientId;

        this.strengthValue = strength.strengthValue;
        this.strengthUnitId = strength.strengthUnitId;
    }
}
