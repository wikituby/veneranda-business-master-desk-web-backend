package org.example.inventory.store.services;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ResponseMessage;
import org.example.inventory.store.domains.Store;
import org.example.inventory.store.domains.repositories.StoreRepository;
import org.example.inventory.store.services.payloads.requests.StoreRequest;
import org.example.inventory.store.services.payloads.responses.dtos.StoreDTO;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class StoreService {

    @Inject
    StoreRepository storeRepository;

    @Transactional
    public StoreDTO createNewStore(StoreRequest request){

        Store store = new Store();
        store.name = request.name;
        store.location = request.location;
        store.description = request.description;
        store.creationDate = LocalDate.now();
        
        // Set defaultStatus: if true, set all other stores to false (only one default at a time)
        if (request.defaultStatus != null && request.defaultStatus) {
            // Set all other stores to false
            List<Store> allStores = storeRepository.listAll();
            for (Store s : allStores) {
                s.defaultStatus = "false";
                storeRepository.persist(s);
            }
            store.defaultStatus = "true";
        } else {
            store.defaultStatus = request.defaultStatus != null ? String.valueOf(request.defaultStatus) : "false";
        }

        storeRepository.persist(store);

        return new StoreDTO(store);

    }

    @Transactional
    public List<Store> getAllStores() {
        return storeRepository.listAll(Sort.descending("creationDate"));
    }

    @Transactional
    public Response updateStore(Long id, StoreRequest request) {
        Store store = storeRepository.findById(id);
        if (store == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Store not found for ID: " + id))
                    .build();
        }

        store.name = request.name;
        store.location = request.location;
        store.description = request.description;
        
        // Set defaultStatus: if true, set all other stores to false (only one default at a time)
        if (request.defaultStatus != null && request.defaultStatus) {
            // Set all other stores to false
            List<Store> allStores = storeRepository.listAll();
            for (Store s : allStores) {
                if (!s.id.equals(id)) {
                    s.defaultStatus = "false";
                    storeRepository.persist(s);
                }
            }
            store.defaultStatus = "true";
        } else if (request.defaultStatus != null) {
            store.defaultStatus = String.valueOf(request.defaultStatus);
        }

        return Response.ok(new ResponseMessage("Store updated successfully", new StoreDTO(store))).build();
    }

    @Transactional
    public Response deleteStore(Long id) {
        Store store = storeRepository.findById(id);
        if (store == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Store not found for ID: " + id))
                    .build();
        }

        storeRepository.delete(store);
        return Response.ok(new ResponseMessage("Store deleted successfully")).build();
    }

}
