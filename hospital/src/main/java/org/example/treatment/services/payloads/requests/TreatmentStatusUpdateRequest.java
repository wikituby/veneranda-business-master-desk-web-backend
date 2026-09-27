package org.example.treatment.services.payloads.requests;

/** Pharmacist / billing lifecycle: status plus PD / DSP / ADM flags. */
public class TreatmentStatusUpdateRequest {
    public String status;
    public Boolean paid;
    public Boolean dispensed;
    public Boolean administered;
}
