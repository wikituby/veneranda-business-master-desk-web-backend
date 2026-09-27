package org.example.hmis.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "hmis_tracer_item")
public class HmisTracerItem extends PanacheEntity {

    @Column(nullable = false, length = 40)
    public String hmisTracerCode;

    @Column(nullable = false, length = 200)
    public String tracerName;

    @Column
    public Long stockItemId;

    @Column
    public Long shopItemId;

    @Column(nullable = false)
    public Boolean active = true;

    @Column
    public Integer sortOrder;
}