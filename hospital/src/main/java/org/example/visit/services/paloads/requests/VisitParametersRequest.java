package org.example.visit.services.paloads.requests;

import jakarta.ws.rs.QueryParam;

import java.time.LocalDate;

public class VisitParametersRequest {

    @QueryParam("visitGroup")
    public String visitGroup;

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
}






