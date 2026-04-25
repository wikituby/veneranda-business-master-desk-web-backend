package org.example.procedure.procedure.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "ProcedureTypeTable")
public class ProcedureType extends PanacheEntity {

    @Column(nullable = false)
    public String procedureType;

    // Description of the lab test, providing additional information on how its done
    @Column(columnDefinition = "TEXT") // Assuming this may be a longer text
    public String typeDescription;


}
