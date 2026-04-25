package org.example.store.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.store.domains.Store;
import org.example.store.domains.repositories.StoreRepository;
import org.example.store.services.payloads.requests.StoreRequest;
import org.example.store.services.payloads.requests.StoreUpdateRequest;
import org.example.store.services.payloads.responses.StoreDTO;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class StoreService {

    @Inject
    StoreRepository storeRepository;

    public static final String NOT_FOUND = "Store not found!";

    @Transactional
    public Response createNewStore(StoreRequest request) {
        if (request == null || request.name == null || request.name.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Store name is required", null))
                    .build();
        }

        Store store = new Store();
        store.name = request.name.trim();
        store.location = request.location != null ? request.location.trim() : null;
        store.description = request.description != null ? request.description.trim() : null;
        store.creationDate = LocalDate.now();

        storeRepository.persist(store);

        return Response.ok(new ResponseMessage("Store created successfully", new StoreDTO(store))).build();
    }

    @Transactional
    public Response updateStore(StoreUpdateRequest request) {
        Store store = storeRepository.findById(request.storeId);
        if (store == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Store not found for ID: " + request.storeId))
                    .build();
        }

        if (request.name != null && !request.name.isBlank()) {
            store.name = request.name.trim();
        }
        store.location = request.location != null ? request.location.trim() : null;
        store.description = request.description != null ? request.description.trim() : null;
        store.lastUpdatedDate = LocalDate.now();

        storeRepository.persist(store);

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

        return Response.ok(new ResponseMessage("Store deleted successfully", null)).build();
    }

    @Transactional
    public List<StoreDTO> getAllStores() {
        return storeRepository.listAll(io.quarkus.panache.common.Sort.descending("creationDate"))
                .stream()
                .map(StoreDTO::new)
                .toList();
    }

    @Transactional
    public Response getStoreById(Long id) {
        Store store = storeRepository.findById(id);
        if (store == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Store not found for ID: " + id))
                    .build();
        }

        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, new StoreDTO(store))).build();
    }
}
