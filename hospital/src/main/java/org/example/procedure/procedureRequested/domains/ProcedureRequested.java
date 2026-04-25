package org.example.procedure.procedureRequested.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.persistence.*;
import org.example.procedure.procedure.domains.Procedure;
import org.example.visit.domains.PatientVisit;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "ProcedureRequested")
public class ProcedureRequested extends PanacheEntity {

    // Reference to the associated visit
    @ManyToOne
    @JoinColumn(nullable = false)
    public PatientVisit visit;

    @ManyToOne
    @JoinColumn(name = "procedure_id")
    public Procedure procedure;

    @Column
    public String procedureRequestedType;

    @Column(nullable = false)
    public String procedureRequestedName;

    @Column
    public Long procedureId;

    @Column
    public String category;

    // Quantity of lab tests requested
    @Column(nullable = false)
    public int quantity;

    // Unit price of the lab test
    @Column
    public BigDecimal unitSellingPrice;

    // Total amount for the requested lab tests (unitPrice * quantity)
    @Column
    public BigDecimal totalAmount;

    @Column
    public String report;

    @Column
    public String status;

    @Column
    public String bgColor;

    @Column
    public String patientName;

    // Name or ID of the person who ordered the lab test
    @Column
    public String orderedBy;

    // Name or ID of the person who performed or is responsible for the lab test
    @Column
    public String doneBy;

    @Column
    public String exam;

    @Column(columnDefinition = "TEXT")
    public String indication;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate dateOfProcedure;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate updateDate;

    @Column
    public LocalTime timeOfProcedure;



}







