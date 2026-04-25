package org.example.inventory.item.services.payloads.responses.dtos;

import org.example.inventory.item.domain.Composition;

import java.math.BigDecimal;

public class CompositionDTO {
    public Long id;
    public Long stockItemId;
    public Long strengthId;

    public CompositionDTO(Composition composition) {
        this.id = composition.id;
        this.stockItemId = composition.stockItemId;
        this.strengthId = composition.strengthId;

    }
}
