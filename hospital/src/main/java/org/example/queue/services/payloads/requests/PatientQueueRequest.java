package org.example.queue.services.payloads.requests;

public class PatientQueueRequest {

    public Long patientId;

    public Long patientVisitId;

    public Long fromModuleId;

    public Long toModuleId;

    public Long clinicId;

    public String note;

    public String visitDefaultScheme;

    public Long patientGroupId;

    public Boolean emergency;

    public Boolean revisit;

    public String queuedBy;
}
