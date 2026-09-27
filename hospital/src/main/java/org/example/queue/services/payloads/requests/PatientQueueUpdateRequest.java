package org.example.queue.services.payloads.requests;

public class PatientQueueUpdateRequest {

    public Long fromModuleId;

    public Long toModuleId;

    public Long clinicId;

    public String note;

    public String visitDefaultScheme;

    public Long patientGroupId;

    public Boolean emergency;

    public Boolean revisit;

    /** Manual ordering number (lower first). */
    public Integer queuePosition;

    /** WAITING, CALLED, SERVING, COMPLETED, CANCELLED, DISCHARGED */
    public String status;

    /** Updated when re-queuing to next room */
    public String queuedBy;
}

