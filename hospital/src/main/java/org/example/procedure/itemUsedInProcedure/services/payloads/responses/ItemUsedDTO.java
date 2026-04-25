package org.example.procedure.itemUsedInProcedure.services.payloads.responses;

import org.example.procedure.itemUsedInProcedure.domains.ItemUsed;

import java.math.BigDecimal;

public class ItemUsedDTO {
    public Long id;
    public Long procedureId;
    public Long itemId;
    public BigDecimal quantityUsed;
    public String procedureName;
    public String itemName;

    public ItemUsedDTO(ItemUsed itemUsed) {
        if (itemUsed != null) {
            this.id = itemUsed.id;
            this.procedureId = itemUsed.procedureId;
            this.itemId = itemUsed.itemId;
            this.quantityUsed = itemUsed.quantityUsed;
            this.procedureName = itemUsed.procedureName;
            this.itemName = itemUsed.itemName;
        }
    }
}










