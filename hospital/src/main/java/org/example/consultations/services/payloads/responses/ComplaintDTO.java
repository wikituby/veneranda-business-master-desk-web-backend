package org.example.consultations.services.payloads.responses;

import org.example.consultations.domains.Complaint;

public class ComplaintDTO {
    public Long id;
    public ComplaintSiteDTO site;
    public ComplaintTypeDTO type;
    public String duration;
    public String natureCharacter;
    public String severity;
    public String onset;
    public String courseProgression;
    public String aggravatingFactors;
    public String relievingFactors;
    public String associatedSymptoms;
    public Long consultationId;

    public ComplaintDTO(Complaint complaint) {
        this.id = complaint.id;
        this.site = complaint.site != null ? new ComplaintSiteDTO(complaint.site) : null;
        this.type = complaint.type != null ? new ComplaintTypeDTO(complaint.type) : null;
        this.duration = complaint.duration;
        this.natureCharacter = complaint.natureCharacter;
        this.severity = complaint.severity;
        this.onset = complaint.onset;
        this.courseProgression = complaint.courseProgression;
        this.aggravatingFactors = complaint.aggravatingFactors;
        this.relievingFactors = complaint.relievingFactors;
        this.associatedSymptoms = complaint.associatedSymptoms;
        this.consultationId = complaint.consultation != null ? complaint.consultation.id : null;
    }

    public ComplaintDTO() {
    }
}






