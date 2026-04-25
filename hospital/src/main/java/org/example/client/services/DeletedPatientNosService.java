package org.example.client.services;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.example.client.domains.Patient;
import org.example.client.domains.repositories.DeletePatientNosRepository;
import org.example.client.domains.DeletedPatient;

import static io.quarkus.hibernate.orm.panache.PanacheEntityBase.delete;

@ApplicationScoped
public class DeletedPatientNosService {

    @Inject
    DeletePatientNosRepository deletePatientNosRepository;

    @Transactional
    public void saveDeletedPatientNo(Patient deletedPatient){

        DeletedPatient dpn = new DeletedPatient();
        dpn.patientGroup = deletedPatient.patientGroup;
        dpn.patientFirstName = deletedPatient.patientFirstName;
        dpn.patientSecondName = deletedPatient.patientSecondName;
        dpn.patientAddress = deletedPatient.patientAddress;
        dpn.patientAge = deletedPatient.patientAge;
        dpn.patientContact = deletedPatient.patientContact;
        dpn.patientGender = deletedPatient.patientGender;

        dpn.patientDateOfBirth = deletedPatient.patientDateOfBirth;
        dpn.creationDate = deletedPatient.creationDate;

        dpn.nextOfKinName = deletedPatient.nextOfKinName;
        dpn.nextOfKinAddress = deletedPatient.nextOfKinAddress;
        dpn.nextOfKinContact = deletedPatient.nextOfKinContact;
        dpn.relationship = deletedPatient.relationship;

        dpn.patientNo = deletedPatient.patientNo;
        dpn.patientFileNo = deletedPatient.patientFileNo;

        deletePatientNosRepository.persist(dpn);

    }

    @Transactional
    public DeletedPatient findFirstDeletedPatient() {
        return deletePatientNosRepository.listAll(Sort.ascending("patientNo"))
                .stream()
                .findFirst()
                .orElse(null);
    }


    @Transactional
    public void deleteByDeletedPatient(DeletedPatient deletedPatient) {
        deletePatientNosRepository.delete(deletedPatient);
    }
}






