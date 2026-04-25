package org.example.procedure.itemUsedInProcedure.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ResponseMessage;
import org.example.inventory.item.domain.Item;
import org.example.procedure.procedure.domains.Procedure;
import org.example.procedure.itemUsedInProcedure.domains.ItemUsed;
import org.example.procedure.itemUsedInProcedure.domains.repositories.ItemUsedRepository;
import org.example.procedure.itemUsedInProcedure.services.payloads.requests.ItemUsedRequest;
import org.example.procedure.itemUsedInProcedure.services.payloads.responses.ItemUsedDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class ItemUsedService {

    @Inject
    ItemUsedRepository itemUsedRepository;

    private static final String NOT_FOUND = "Not found!";

    @Transactional
    public ItemUsedDTO addItemUsed(ItemUsedRequest request) {
        Procedure procedure = Procedure.findById(request.procedureId);
        Item item = Item.findById(request.itemId);

        if (procedure == null || item == null) {
            throw new WebApplicationException("Item or Procedure not found", Response.Status.NOT_FOUND);
        }

        // Fixed line: use raw field names, not nested entity paths
        ItemUsed existing = ItemUsed.find("procedureId = ?1 and itemId = ?2", request.procedureId, request.itemId).firstResult();

        if (existing != null) {
            throw new WebApplicationException("Item already used in this procedure", Response.Status.CONFLICT);
        }

        // Create new item-usage entry
        ItemUsed itemUsed = new ItemUsed();
        itemUsed.procedureName = procedure.procedureName;
        itemUsed.itemName = item.title;
        itemUsed.procedureId = procedure.id;
        itemUsed.itemId = item.id;
        itemUsed.quantityUsed = BigDecimal.valueOf(request.quantityUsed);
        itemUsed.persist();

        return new ItemUsedDTO(itemUsed);

    }


    public List<ItemUsedDTO> getItemsUsedByProcedure(Long procedureId) {
        List<ItemUsed> itemsUsed = ItemUsed
                .find("procedureId = ?1 ORDER BY id DESC", procedureId)
                .list();

        return itemsUsed.stream()
                .map(ItemUsedDTO::new)
                .collect(Collectors.toList());
    }



    @Transactional
    public void performProcedure(Long procedureId) {
        Procedure procedure = Procedure.findById(procedureId);
        if (procedure == null) {
            //throw new IllegalArgumentException("Procedure not found.");
            throw new WebApplicationException("Procedure not found",409);


        }

        List<ItemUsed> usedItems = ItemUsed.list("procedureId", procedure.id);

        for (ItemUsed usage : usedItems) {
            Item item = Item.findById(usage.itemId);

            if (item == null) {
               // throw new IllegalStateException("Item not found for usage record.");
                throw new WebApplicationException("Item not found for usage record",409);

            }

            if (item.stockAtHand.compareTo(usage.quantityUsed) < 0) {
                //throw new IllegalStateException("Not enough stock for item: " + item.title);
                throw new WebApplicationException("Not enough stock for item: " + item.title);

            }

            item.stockAtHand = item.stockAtHand.subtract(usage.quantityUsed);
            item.persist();
        }

    }


    @Transactional
    public void restoreStockOnProcedureDelete(Long procedureId) {
        Procedure procedure = Procedure.findById(procedureId);
        if (procedure == null) {
            throw new WebApplicationException("procedure not found for usage record",409);

        }

        List<ItemUsed> usedItems = ItemUsed.list("procedureId", procedure.id);

        if (usedItems.isEmpty()) {
            return;
        }


        for (ItemUsed usage : usedItems) {
            Item item = Item.findById(usage.itemId);

            if (item == null) {
                continue;

            }

            // Add back the used quantity
            item.stockAtHand = item.stockAtHand.add(usage.quantityUsed);
            item.persist();
        }

    }



    public List<ItemUsedDTO> getCart(Long labTestId) {
        // Fetch the list of ItemUsed entities by labTestId
        List<ItemUsed> itemUsedList = ItemUsed.find("labTest.id", labTestId).list();

        // Map the ItemUsed entities to ItemUsedDTOs using a method reference
        return itemUsedList.stream()
                .map(this::mapToDTO) // Replace lambda with method reference
                .collect(Collectors.toList()); // Collect the DTOs into a list
    }

    // Method to map ItemUsed entity to ItemUsedDTO
    private ItemUsedDTO mapToDTO(ItemUsed itemUsed) {
        return new ItemUsedDTO(itemUsed);
    }

    @Transactional
    public Response deleteItemUsed(Long id){
        ItemUsed itemUsed = ItemUsed.findById(id); // correct: call on your entity

        if (itemUsed == null) {
            //throw new WebApplicationException("ItemUsed with id " + id + " not found", 404);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("ItemUsed with id" + " " + id + " " +"not found"))
                    .build();
        }

        itemUsed.delete(); // directly call delete on the entity

        return Response.ok(new ResponseMessage("Item Used Deleted Successfully")).build();
    }






}




