package org.example.queue.services.payloads.requests;

public class DischargePatientQueueRequest {

    public Long patientId;

    public Long patientVisitId;

    /** Legacy field — discharge now clears all active queues for the patient. */
    public Long fromModuleId;

    public String note;

    public String dischargedBy;
}
