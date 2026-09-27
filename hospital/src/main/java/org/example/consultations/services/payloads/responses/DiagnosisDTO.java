package org.example.consultations.services.payloads.responses;

import org.example.consultations.domains.Diagnosis;
import org.example.treatment.services.payloads.responses.TreatmentRequestedDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class DiagnosisDTO {
    public Long id;
    public String name;
    public String severity;
    public String kind;
    public String notes;
    public String hmisCode;
    public String icd10Code;
    public Long diagnosisTypeId;
    public Long consultationId;
    public Long visitId;
    public LocalDate creationDate;
    public LocalDate updateDate;
    public List<TreatmentRequestedDTO> treatments;

    public DiagnosisDTO() {
    }

    public DiagnosisDTO(Diagnosis diagnosis) {
        this.id = diagnosis.id;
        this.name = diagnosis.name;
        this.severity = diagnosis.severity;
        this.kind = diagnosis.kind;
        this.notes = diagnosis.notes;
        this.hmisCode = diagnosis.hmisCode;
        this.icd10Code = diagnosis.icd10Code;
        this.diagnosisTypeId = diagnosis.diagnosisType != null ? diagnosis.diagnosisType.id : null;
        this.consultationId = diagnosis.consultation != null ? diagnosis.consultation.id : null;
        this.visitId = diagnosis.consultation != null && diagnosis.consultation.visit != null
                ? diagnosis.consultation.visit.id
                : null;
        this.creationDate = diagnosis.creationDate;
        this.updateDate = diagnosis.updateDate;
        this.treatments = diagnosis.treatments != null
                ? diagnosis.treatments.stream().map(TreatmentRequestedDTO::new).collect(Collectors.toList())
                : List.of();
    }
}
