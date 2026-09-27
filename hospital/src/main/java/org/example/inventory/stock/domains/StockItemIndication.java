package org.example.inventory.stock.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

/**
 * One medical indication line for a stock/consumable item.
 * Multiple rows replace the legacy single TEXT {@link StockItem#indication} field
 * (which is kept as a joined summary for older clients).
 */
@Entity
public class StockItemIndication extends PanacheEntity {

    @Column(nullable = false)
    public Long stockItemId;

    @Column(columnDefinition = "TEXT", nullable = false)
    public String text;

    @Column
    public Integer sortOrder;
}
