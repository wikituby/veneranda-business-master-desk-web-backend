package org.example.inventory.stock.services.payloads.requests;

import java.math.BigDecimal;

/** One row of active-ingredient composition for a stock item {@code ingredients} list. */
public class StockItemIngredientRequest {

    /** Preferred: FK to ActiveIngredient. */
    public Long activeIngredientId;
    /** Legacy: same as activeIngredientId when sent under the old name. */
    public Long itemId;
    public BigDecimal strength;
    public Long unitId;
}
