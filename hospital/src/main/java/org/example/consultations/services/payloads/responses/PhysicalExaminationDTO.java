package org.example.consultations.services.payloads.responses;

import org.example.consultations.domains.PhysicalExamination;

public class PhysicalExaminationDTO {
    public Long id;
    public String system;
    public String findings;
    public String site;
    public String status;
    public String inspection;
    public String palpation;
    public String percussion;
    public String auscultation;
    public String notes;
    public Long consultationId;

    public PhysicalExaminationDTO() {
    }

    public PhysicalExaminationDTO(PhysicalExamination entity) {
        this.id = entity.id;
        this.system = entity.examSystem;
        this.findings = entity.findings;
        this.site = entity.site;
        this.status = entity.status;
        this.inspection = entity.inspection;
        this.palpation = entity.palpation;
        this.percussion = entity.percussion;
        this.auscultation = entity.auscultation;
        this.notes = entity.notes;
        this.consultationId = entity.consultation != null ? entity.consultation.id : null;
    }
}
