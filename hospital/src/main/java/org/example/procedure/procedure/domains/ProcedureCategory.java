package org.example.procedure.procedure.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.util.List;

import jakarta.json.bind.annotation.JsonbDateFormat;
import java.time.LocalDate;

@Entity
@Table(name = "ProcedureCategoryTable")
public class ProcedureCategory extends PanacheEntity {

    @Column
    public String name;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    public ProcedureCategory parent;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate creationDate;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate LastUpdatedDate;

}






