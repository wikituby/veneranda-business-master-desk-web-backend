package org.example.consultations.services.payloads.responses;

import org.example.consultations.domains.PresentingComplaint;

public class PresentingComplaintDTO {
    public Long id;
    public String complaint;
    public String site;
    public String severity;
    public String onset;
    public Integer durationValue;
    public String durationUnit;
    public String duration;
    public String nature;
    public String course;
    public String aggravatingFactors;
    public String alleviatingFactors;
    public String associatedSymptoms;
    public Long consultationId;

    public PresentingComplaintDTO() {
    }

    public PresentingComplaintDTO(PresentingComplaint entity) {
        this.id = entity.id;
        this.complaint = entity.complaint;
        this.site = entity.site;
        this.severity = entity.severity;
        this.onset = entity.onset;
        this.durationValue = entity.durationValue;
        this.durationUnit = entity.durationUnit;
        this.duration = entity.duration;
        this.nature = entity.nature;
        this.course = entity.course;
        this.aggravatingFactors = entity.aggravatingFactors;
        this.alleviatingFactors = entity.alleviatingFactors;
        this.associatedSymptoms = entity.associatedSymptoms;
        this.consultationId = entity.consultation != null ? entity.consultation.id : null;
    }
}
