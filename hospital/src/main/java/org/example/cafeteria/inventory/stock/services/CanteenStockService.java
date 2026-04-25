package org.example.cafeteria.inventory.stock.services;

import io.quarkus.panache.common.Sort;
import io.vertx.mutiny.mysqlclient.MySQLPool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ResponseMessage;
import org.example.cafeteria.inventory.item.domains.CanteenItem;
import org.example.cafeteria.inventory.item.domains.repositories.CanteenItemRepository;
import org.example.cafeteria.inventory.item.services.CanteenItemService;
import org.example.cafeteria.inventory.stock.domains.CanteenStock;
import org.example.cafeteria.inventory.stock.domains.repositories.CanteenStockRepository;
import org.example.cafeteria.inventory.stock.services.requests.CanteenStockTakeRequest;
import org.example.cafeteria.inventory.stock.services.responses.dtos.CanteenStockDTO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class CanteenStockService {

    @Inject
    CanteenStockRepository canteenStockRepository;

    @Inject
    CanteenItemRepository canteenItemRepository;

    @Inject
    CanteenItemService canteenItemService;

    @Inject
    MySQLPool client;

    public static final String INVALID_REQUEST = "Invalid request data!";

    public Response receiveCanteenStock(CanteenStockTakeRequest request) {

        CanteenItem canteenItem = canteenItemRepository.findById(request.itemId);
        if (canteenItem == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Item not found for ID:" + request.itemId, null))
                    .build();
        }

        // Calculate unit cost
        BigDecimal unitCost = request.totalCostPrice.divide(request.quantityReceived, RoundingMode.HALF_UP);

        CanteenStock canteenStock = new CanteenStock();
        canteenStock.item = canteenItem;
        canteenStock.totalCostPrice = request.totalCostPrice;
        canteenStock.quantityReceived = request.quantityReceived;
        canteenStock.unitCostPrice = unitCost;
        canteenStock.quantityAvailable = canteenItem.stockAtHand;
        canteenStock.newQuantity = request.quantityReceived.add(canteenItem.stockAtHand);
        canteenStock.expiryDate = request.expiryDate;
        canteenStock.brand = request.brand;
        canteenStock.store = request.store;
        canteenStock.packaging = request.packaging;

        // Update unitSellingPrice only if it's not zero
        if (request.unitSellingPrice != null && request.unitSellingPrice.compareTo(BigDecimal.ZERO) > 0) {
            canteenStock.unitSellingPrice = request.unitSellingPrice;
        } else {
            canteenStock.unitSellingPrice = canteenItem.sellingPrice;
        }

        // ✅ If receiveDate is null, set it to today
        canteenStock.receiveDate = (request.receiveDate != null) ? request.receiveDate : LocalDate.now();

        // Persist the stock
        canteenStockRepository.persist(canteenStock);

        // Update the item's stock at hand
        canteenItemService.updateCanteenItemStockAtHand(canteenStock, canteenItem);

        // Return the response
        return Response.ok(new ResponseMessage("New stock Received successfully", new CanteenStockDTO(canteenStock))).build();
    }




    @Transactional
    public List<CanteenStock> getAllCanteenStockReceives() {
        return canteenStockRepository.listAll(Sort.descending("id"));
    }


    @Transactional
    public Response deleteCanteenStockReceivedById(Long id) {
        Optional<CanteenStock> stockOpt = canteenStockRepository.findByIdOptional(id);
        if (stockOpt.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Stock not found", null))
                    .build();
        }
        CanteenStock canteenStock = stockOpt.get();
        CanteenItem canteenItem = canteenStock.item;
        BigDecimal quantity = canteenStock.quantityReceived;

        if (canteenItem.stockAtHand.compareTo(quantity) < 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Part of this Stock has already been sold, so you have to sell the rest of the stock ", null))
                    .build();
        }

        canteenStockRepository.delete(canteenStock);

        canteenItem.stockAtHand = canteenItem.stockAtHand.subtract(quantity);
        canteenItemRepository.persist(canteenItem);


        return Response.ok(new ResponseMessage("Stock deleted successfully")).build();
    }



}
