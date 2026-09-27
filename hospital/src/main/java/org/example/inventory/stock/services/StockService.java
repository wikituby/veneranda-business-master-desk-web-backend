package org.example.inventory.stock.services;

import io.quarkus.panache.common.Sort;
import io.vertx.mutiny.sqlclient.Pool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ResponseMessage;
import org.example.inventory.item.domain.Item;
import org.example.inventory.item.services.ShopItemService;
import org.example.inventory.stock.domains.Stock;
import org.example.inventory.item.domain.repositories.ItemRepository;
import org.example.inventory.stock.domains.repositories.StockRepository;
import org.example.store.domains.repositories.StoreRepository;
import org.example.inventory.stock.services.payloads.requests.StockTakeRequest;
import org.example.inventory.stock.services.payloads.responses.dtos.StockDTO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class StockService {

    @Inject
    StockRepository stockRepository;

    @Inject
    ItemRepository itemRepository;

    @Inject
    StoreRepository storeRepository;

    @Inject
    ShopItemService itemService;

    @Inject
    Pool client;

    public static final String INVALID_REQUEST = "Invalid request data!";

    public Response receiveStock(StockTakeRequest request) {

        Item item = itemRepository.findById(request.itemId);
        if (item == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Item not found for ID:" + request.itemId, null))
                    .build();
        }

        // Calculate unit cost
        BigDecimal unitCost = request.totalCostPrice.divide(request.quantityReceived, RoundingMode.HALF_UP);

        Stock stock = new Stock();
        stock.item = item;
        stock.totalCostPrice = request.totalCostPrice;
        stock.quantityReceived = request.quantityReceived;
        stock.unitCostPrice = unitCost;
        stock.quantityAvailable = item.stockAtHand;
        stock.newQuantity = request.quantityReceived.add(item.stockAtHand);
        stock.expiryDate = request.expiryDate;
        stock.brand = request.brand;
        stock.store = request.store;
        stock.packaging = request.packaging;

        // Update unitSellingPrice only if it's not zero
        if (request.unitSellingPrice != null && request.unitSellingPrice.compareTo(BigDecimal.ZERO) > 0) {
            stock.unitSellingPrice = request.unitSellingPrice;
        } else {
            stock.unitSellingPrice = item.sellingPrice;
        }

        stock.receiveDate = (request.receiveDate != null) ? request.receiveDate : LocalDate.now();

        // Persist the stock
        stockRepository.persist(stock);

        // Update the item's stock at hand
        itemService.updateItemStockAtHand(stock, item);

        // Return the response
        return Response.ok(new ResponseMessage("New stock Received successfully", new StockDTO(stock))).build();
    }




    @Transactional
    public List<StockDTO> getAllStockReceives() {
        return stockRepository.listAll(Sort.descending("id"))
                .stream()
                .map(StockDTO::new)
                .toList();
    }


    @Transactional
    public Response deleteStockReceivedById(Long id) {
        Optional<Stock> stockOpt = stockRepository.findByIdOptional(id);
        if (stockOpt.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Stock not found", null))
                    .build();
        }
        Stock stock = stockOpt.get();
        Item item = stock.item;
        BigDecimal quantity = stock.quantityReceived;

        if (item.stockAtHand.compareTo(quantity) < 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Part of this Stock has already been sold, so you have to sell the rest of the stock ", null))
                    .build();
        }

        stockRepository.delete(stock);

        item.stockAtHand = item.stockAtHand.subtract(quantity);
        itemRepository.persist(item);


        return Response.ok(new ResponseMessage("Stock deleted successfully")).build();
    }



}




