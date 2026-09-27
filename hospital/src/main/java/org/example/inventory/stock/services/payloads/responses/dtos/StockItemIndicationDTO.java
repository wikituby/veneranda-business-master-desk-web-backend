package org.example.inventory.stock.services.payloads.responses.dtos;

import org.example.inventory.stock.domains.StockItemIndication;

public class StockItemIndicationDTO {
    public Long id;
    public String text;
    public Integer sortOrder;

    public StockItemIndicationDTO() {
    }

    public StockItemIndicationDTO(StockItemIndication entity) {
        this.id = entity.id;
        this.text = entity.text;
        this.sortOrder = entity.sortOrder;
    }

    public StockItemIndicationDTO(Long id, String text, Integer sortOrder) {
        this.id = id;
        this.text = text;
        this.sortOrder = sortOrder;
    }
}
