package org.example.cafeteria.inventory.store.services;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.example.cafeteria.inventory.store.domains.CanteenStore;
import org.example.cafeteria.inventory.store.domains.repository.CanteenStoreRepository;
import org.example.cafeteria.inventory.store.services.payloads.requests.CanteenStoreRequest;
import org.example.cafeteria.inventory.store.services.payloads.responses.CanteenStoreDTO;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class CanteenStoreService {

    @Inject
    CanteenStoreRepository canteenStoreRepository;

    @Transactional
    public CanteenStoreDTO createNewCanteenStore(CanteenStoreRequest request){

        CanteenStore store = new CanteenStore();
        store.name = request.name;
        store.location = request.location;
        store.description = request.description;
        store.creationDate = LocalDate.now();

        canteenStoreRepository.persist(store);

        return new CanteenStoreDTO(store);

    }

    @Transactional
    public List<CanteenStore> getAllCanteenStores() {
        return canteenStoreRepository.listAll(Sort.descending("creationDate"));
    }



}






