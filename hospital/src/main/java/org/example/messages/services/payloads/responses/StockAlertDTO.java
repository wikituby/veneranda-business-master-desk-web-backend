package org.example.messages.services.payloads.responses;

import org.example.inventory.item.domain.Item;

import java.math.BigDecimal;

public class StockAlertDTO {
    public Long itemId;
    public String itemName;
    public String stockAtHand;
    public Integer reOrderLevel;

    public StockAlertDTO() {
    }

    public StockAlertDTO(Item item) {
        if (item == null) {
            return;
        }
        this.itemId = item.id;
        this.itemName = item.title;
        this.stockAtHand = formatQty(item.stockAtHand);
        this.reOrderLevel = item.reOrderLevel;
    }

    private static String formatQty(BigDecimal qty) {
        if (qty == null) {
            return "0";
        }
        return qty.stripTrailingZeros().toPlainString();
    }
}
