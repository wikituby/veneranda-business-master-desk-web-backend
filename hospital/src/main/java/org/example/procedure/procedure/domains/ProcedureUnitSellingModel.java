package org.example.procedure.procedure.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.math.BigDecimal;

@Entity
public class ProcedureUnitSellingModel extends PanacheEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "procedure_id", nullable = false)
    public Procedure procedure;

    @Column(nullable = false, length = 120)
    public String name;

    @Column(nullable = false)
    public BigDecimal unitSellingPrice;

    @Column
    public Integer unitsInBundle;

    @Column
    public BigDecimal bundlePrice;

    @Column
    public BigDecimal profitMargin;

    @Column
    public Integer sortOrder;

    @Column
    public Boolean isDefault = false;
}
