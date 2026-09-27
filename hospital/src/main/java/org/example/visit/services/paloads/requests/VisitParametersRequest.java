package org.example.visit.services.paloads.requests;

import jakarta.ws.rs.QueryParam;

import java.math.BigDecimal;
import java.time.LocalDate;

public class VisitParametersRequest {

    @QueryParam("visitGroup")
    public String visitGroup;

    /** When set, matches patients in this group (and visits tagged with the same group). */
    @QueryParam("patientGroupId")
    public Long patientGroupId;

    @QueryParam("datefrom")
    public LocalDate datefrom;

    @QueryParam("dateto")
    public LocalDate dateto;

    /** Partial match on patient first + second name or visit snapshot `patientName` (case-insensitive). */
    @QueryParam("patientName")
    public String patientName;

    /** Partial match on consultation `diagnosis` (case-insensitive). */
    @QueryParam("diagnosis")
    public String diagnosis;

    /**
     * When set, only visits that have a requested procedure with this catalog id
     * ({@code ProcedureRequested.procedureId} or linked {@code Procedure} id).
     */
    @QueryParam("procedureId")
    public Long procedureId;

    /** Minimum patient age (inclusive), matched against visit snapshot or linked patient age. */
    @QueryParam("ageFrom")
    public BigDecimal ageFrom;

    /** Maximum patient age (inclusive), matched against visit snapshot or linked patient age. */
    @QueryParam("ageTo")
    public BigDecimal ageTo;

    /** Exact match on linked patient gender (case-insensitive), e.g. Male, Female, Other. */
    @QueryParam("patientGender")
    public String patientGender;

    /** Minimum patient weight in kg (inclusive), from visit triage vitals. */
    @QueryParam("weightFrom")
    public Double weightFrom;

    /** Maximum patient weight in kg (inclusive), from visit triage vitals. */
    @QueryParam("weightTo")
    public Double weightTo;

    /**
     * When set, only visits with a treatment plan line ({@code TreatmentRequested}) for this stock item id.
     */
    @QueryParam("treatmentItemId")
    public Long treatmentItemId;

    /** Partial match on treatment plan drug name ({@code TreatmentRequested.itemName}, case-insensitive). */
    @QueryParam("treatmentDrugName")
    public String treatmentDrugName;

    /**
     * When {@code true}, only visits with outstanding balance ({@code balanceDue} &gt; 0).
     * When {@code false}, only visits with no outstanding balance.
     * Omit for all visits.
     */
    @QueryParam("hasDebt")
    public Boolean hasDebt;
}






