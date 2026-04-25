package org.example.cafeteria.client.services;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.example.cafeteria.client.domains.DeletedBuyerNos;
import org.example.cafeteria.client.domains.repositories.DeleteBuyerNosRepository;

@ApplicationScoped
public class DeletedBuyerNosService {

    @Inject
    DeleteBuyerNosRepository deleteBuyerNosRepository;

    @Transactional
    public void saveDeletedPatientNo(int deletedPatientNoFromDeleteMethod){
        DeletedBuyerNos dpn = new DeletedBuyerNos();
        dpn.deletedPatientNo = deletedPatientNoFromDeleteMethod;

        deleteBuyerNosRepository.persist(dpn);

    }

    @Transactional
    public int findFirstDeletedPatientNo() {
        return deleteBuyerNosRepository.listAll(Sort.ascending("deletedPatientNo"))
                .stream()
                .map(DeletedPatientNos -> DeletedPatientNos.deletedPatientNo)
                .findFirst()
                .orElse(0);
    }

    @Transactional
    public void deleteByDeletedPatientNumber(int deletedPatientNumber) {
        deleteBuyerNosRepository.delete("deletedPatientNo = ?1", deletedPatientNumber);
    }
}






