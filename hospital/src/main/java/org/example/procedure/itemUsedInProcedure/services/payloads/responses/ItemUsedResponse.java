package org.example.procedure.itemUsedInProcedure.services.payloads.responses;

import org.example.inventory.item.domain.Item;

import java.util.List;

public class ItemUsedResponse {
    public List<Item> items;
    public int totalQuantity;

    public ItemUsedResponse(List<Item> items, int totalQuantity) {
        this.items = items;
        this.totalQuantity = totalQuantity;
    }
}
