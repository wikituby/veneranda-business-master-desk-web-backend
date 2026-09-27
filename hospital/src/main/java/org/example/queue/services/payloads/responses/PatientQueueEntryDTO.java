package org.example.queue.services.payloads.responses;

import org.example.queue.domains.PatientQueueEntry;

import java.time.LocalDateTime;

public class PatientQueueEntryDTO {

    public Long id;
    public Long patientId;
    public String patientSurname;
    public String patientOtherNames;
    public String patientFileNo;
    public String patientAge;
    public Long patientVisitId;
    public Long fromModuleId;
    public String fromModuleName;
    public Long toModuleId;
    public String toModuleName;
    public Long clinicId;
    public String clinicName;
    public String note;
    public String visitDefaultScheme;
    public Long patientGroupId;
    public boolean emergency;
    public boolean revisit;
    public String queueNumber;
    public Integer queuePosition;
    public String status;
    public LocalDateTime queuedAt;
    public LocalDateTime calledAt;
    public LocalDateTime completedAt;
    public String queuedBy;
    public Long referredFromModuleId;
    public String referredFromModuleName;
    public Long referredFromClinicId;
    public String referredFromClinicName;
    public LocalDateTime referredAt;
    public String referredBy;
    public long waitMinutes;

    public PatientQueueEntryDTO() {
    }

    public PatientQueueEntryDTO(PatientQueueEntry entry) {
        this.id = entry.id;
        if (entry.patient != null) {
            this.patientId = entry.patient.id;
            this.patientSurname = entry.patient.patientSecondName;
            this.patientOtherNames = entry.patient.patientFirstName;
            this.patientFileNo = entry.patient.patientFileNo;
            if (entry.patient.patientAge != null) {
                this.patientAge = entry.patient.patientAge.stripTrailingZeros().toPlainString() + " yrs";
            }
        }
        if (entry.patientVisit != null) {
            this.patientVisitId = entry.patientVisit.id;
        }
        if (entry.fromModule != null) {
            this.fromModuleId = entry.fromModule.id;
            this.fromModuleName = entry.fromModule.name;
        }
        if (entry.toModule != null) {
            this.toModuleId = entry.toModule.id;
            this.toModuleName = entry.toModule.name;
        }
        if (entry.clinic != null) {
            this.clinicId = entry.clinic.id;
            this.clinicName = entry.clinic.name;
        }
        this.note = entry.note;
        this.visitDefaultScheme = entry.visitDefaultScheme;
        if (entry.patientGroup != null) {
            this.patientGroupId = entry.patientGroup.id;
            if (this.visitDefaultScheme == null || this.visitDefaultScheme.isBlank()) {
                this.visitDefaultScheme = entry.patientGroup.groupName;
            }
        }
        this.emergency = entry.emergency;
        this.revisit = entry.revisit;
        this.queueNumber = entry.queueNumber;
        this.queuePosition = entry.queuePosition;
        this.status = entry.status;
        this.queuedAt = entry.queuedAt;
        this.calledAt = entry.calledAt;
        this.completedAt = entry.completedAt;
        this.queuedBy = entry.queuedBy;
        if (entry.referredFromModule != null) {
            this.referredFromModuleId = entry.referredFromModule.id;
            this.referredFromModuleName = entry.referredFromModule.name;
        }
        if (entry.referredFromClinic != null) {
            this.referredFromClinicId = entry.referredFromClinic.id;
            this.referredFromClinicName = entry.referredFromClinic.name;
        }
        this.referredAt = entry.referredAt;
        this.referredBy = entry.referredBy;
        if (entry.queuedAt != null) {
            LocalDateTime end = entry.completedAt != null ? entry.completedAt
                    : (entry.calledAt != null ? entry.calledAt : LocalDateTime.now());
            this.waitMinutes = java.time.Duration.between(entry.queuedAt, end).toMinutes();
        }
    }
}
