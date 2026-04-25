package org.example.client.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.client.domains.Patient;

import java.math.BigDecimal;
import java.util.Objects;

@ApplicationScoped
public class PatientRepository implements PanacheRepository<Patient> {

    /**
     * Finds a patient by their first name and second name, regardless of the order.
     *
     * @param firstName  The first name of the patient.
     * @param secondName The second name of the patient.
     * @return The patient entity if found, otherwise null.
     */
    public Patient findByFirstNameAndSecondName(String firstName, String secondName) {
        return find("(patientFirstName = ?1 and patientSecondName = ?2) or (patientFirstName = ?2 and patientSecondName = ?1)",
                firstName, secondName).firstResult();
    }

    public int generateNextPatientNo() {
        Patient maxPatient = Patient.find("ORDER BY patientNo DESC").firstResult();
        return (maxPatient != null ? maxPatient.patientNo : 0) + 1;
    }


    public BigDecimal sumTotalDebtForCompassionPatients() {
        // Using Panache query
        return find("totalBalanceDue > 0 and patientGroup.groupNameShortForm = ?1", "compassion")
                .stream()
                .map(Patient::getTotalBalanceDue)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}





