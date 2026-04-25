package org.example.procedure.itemUsedInProcedure.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import java.math.BigDecimal;

@Entity
public class ItemUsed extends PanacheEntity {

    @Column(name = "procedure_id")
    public Long procedureId;

    @Column(name = "item_id")
    public Long itemId;

    @Column
    public BigDecimal quantityUsed;

    @Column
    public String procedureName;

    @Column
    public  String itemName;


}






