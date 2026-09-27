package org.example.queue.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import org.example.client.domains.Patient;
import org.example.client.domains.PatientGroup;
import org.example.visit.domains.PatientVisit;

import java.time.LocalDateTime;

@Entity
public class PatientQueueEntry extends PanacheEntity {

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    public Patient patient;

    @ManyToOne
    @JoinColumn(name = "patient_visit_id")
    public PatientVisit patientVisit;

    @ManyToOne
    @JoinColumn(name = "from_module_id", nullable = false)
    public HospitalModule fromModule;

    @ManyToOne
    @JoinColumn(name = "to_module_id", nullable = false)
    public HospitalModule toModule;

    @ManyToOne
    @JoinColumn(name = "clinic_id")
    public HospitalClinic clinic;

    @Column(columnDefinition = "TEXT")
    public String note;

    @Column
    public String visitDefaultScheme;

    @ManyToOne
    @JoinColumn(name = "patient_group_id")
    public PatientGroup patientGroup;

    @Column(nullable = false)
    public boolean emergency = false;

    @Column(nullable = false)
    public boolean revisit = false;

    @Column(nullable = false)
    public String queueNumber;

    /**
     * Optional manual ordering within a destination department.
     * Lower numbers appear first. Null means "no manual position".
     */
    @Column
    public Integer queuePosition;

    /** WAITING, CALLED, SERVING, COMPLETED, CANCELLED, DISCHARGED */
    @Column(nullable = false)
    public String status = "WAITING";

    @Column(nullable = false)
    public LocalDateTime queuedAt;

    @Column
    public LocalDateTime calledAt;

    @Column
    public LocalDateTime completedAt;

    @Column
    public String queuedBy;

    /** Set when the patient is forwarded to another department/room. */
    @ManyToOne
    @JoinColumn(name = "referred_from_module_id")
    public HospitalModule referredFromModule;

    @ManyToOne
    @JoinColumn(name = "referred_from_clinic_id")
    public HospitalClinic referredFromClinic;

    @Column
    public LocalDateTime referredAt;

    @Column
    public String referredBy;
}
