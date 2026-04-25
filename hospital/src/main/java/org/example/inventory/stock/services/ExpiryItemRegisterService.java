package org.example.inventory.stock.services;



import io.quarkus.panache.common.Sort;

import jakarta.enterprise.context.ApplicationScoped;

import jakarta.inject.Inject;

import jakarta.transaction.Transactional;

import jakarta.ws.rs.core.Response;

import org.example.configuration.handler.ResponseMessage;
import org.example.inventory.item.services.ShopItemService;

import org.example.inventory.stock.domains.ExpiryItemRegister;

import org.example.inventory.stock.domains.StockBatch;

import org.example.inventory.stock.domains.repositories.ExpiryItemRegisterRepository;

import org.example.inventory.stock.domains.repositories.StockBatchRepository;

import org.example.inventory.stock.services.StockTrackingService;
import org.example.inventory.stock.services.payloads.requests.ExpiryItemRegisterRequest;

import org.example.inventory.stock.services.payloads.responses.dtos.ExpiryItemRegisterDTO;



import java.math.BigDecimal;

import java.time.LocalDateTime;

import java.util.List;

import java.util.stream.Collectors;



@ApplicationScoped

public class ExpiryItemRegisterService {



    @Inject

    ExpiryItemRegisterRepository expiryItemRegisterRepository;



    @Inject

    StockBatchRepository stockBatchRepository;



    @Inject

    ShopItemService itemService;



    private static BigDecimal nz(BigDecimal v) {

        return v != null ? v : BigDecimal.ZERO;

    }



    /**

     * Resolve the batch to write off: prefer {@code stockBatchId}, else unique match on item + batch number + expiry.

     */

    private StockBatch resolveStockBatch(ExpiryItemRegisterRequest request) {

        if (request.stockBatchId != null) {

            return stockBatchRepository.findById(request.stockBatchId);

        }

        if (request.batchNumber == null || request.expiryDate == null) {

            return null;

        }

        List<StockBatch> list = stockBatchRepository.list(

                "stockItemId = ?1 and batchNumber = ?2 and expiryDate = ?3",

                request.stockItemId, request.batchNumber, request.expiryDate);

        if (list.isEmpty()) {

            return null;

        }

        if (list.size() > 1) {

            return null;

        }

        return list.get(0);

    }



    @Transactional

    public Response addNew(ExpiryItemRegisterRequest request) {

        if (request.stockItemId == null) {

            return Response.status(Response.Status.BAD_REQUEST)

                    .entity(new ResponseMessage("stockItemId is required", null))

                    .build();

        }



        StockBatch batch = resolveStockBatch(request);

        if (batch == null) {

            return Response.status(Response.Status.BAD_REQUEST)

                    .entity(new ResponseMessage(

                            "Stock batch not found or ambiguous. Send stockBatchId (preferred), or batchNumber and expiryDate that match exactly one batch.",

                            null))

                    .build();

        }



        if (batch.stockItemId == null || !batch.stockItemId.equals(request.stockItemId)) {

            return Response.status(Response.Status.BAD_REQUEST)

                    .entity(new ResponseMessage("stockItemId does not match the resolved stock batch.", null))

                    .build();

        }



        batch = stockBatchRepository.findById(batch.id);

        BigDecimal before = nz(batch.stockAtHand);

        if (before.compareTo(BigDecimal.ZERO) <= 0) {

            return Response.status(Response.Status.BAD_REQUEST)

                    .entity(new ResponseMessage(

                            "This batch has no stock at hand to remove. Stock is already zero.", null))

                    .build();

        }



        if (request.stockAtHand != null && request.stockAtHand.compareTo(before) != 0) {

            return Response.status(Response.Status.BAD_REQUEST)

                    .entity(new ResponseMessage(

                            "Registered quantity must match current batch stock at hand (" + before + ").",

                            null))

                    .build();

        }



        ExpiryItemRegister row = new ExpiryItemRegister();

        row.stockItemId = request.stockItemId;

        row.stockBatchId = batch.id;

        row.batchNumber = batch.batchNumber != null ? batch.batchNumber : request.batchNumber;

        row.stockAtHand = before;

        row.expiryDate = request.expiryDate != null ? request.expiryDate : batch.expiryDate;

        row.dateOfStockRemoval = request.dateOfStockRemoval != null ? request.dateOfStockRemoval : LocalDateTime.now();

        row.removedBy = request.removedBy;



        expiryItemRegisterRepository.persist(row);



        try {

            itemService.deductStockBatchWithTracking(

                    batch,

                    before,

                    StockTrackingService.SRC_EXPIRY_ITEM_REGISTER,

                    row.id,

                    StockTrackingService.REF_EXPIRY_ITEM_REGISTER);

        } catch (IllegalStateException e) {

            return Response.status(Response.Status.BAD_REQUEST)

                    .entity(new ResponseMessage(e.getMessage(), "INSUFFICIENT_STOCK"))

                    .build();

        }



        return Response.ok(new ResponseMessage("Expiry item registered and batch stock set to zero", new ExpiryItemRegisterDTO(row)))

                .build();

    }



    @Transactional

    public Response update(Long id, ExpiryItemRegisterRequest request) {

        ExpiryItemRegister row = expiryItemRegisterRepository.findById(id);

        if (row == null) {

            return Response.status(Response.Status.NOT_FOUND)

                    .entity(new ResponseMessage("Expiry item register entry not found for ID: " + id, null))

                    .build();

        }



        if (request.stockItemId != null) {

            row.stockItemId = request.stockItemId;

        }

        if (request.stockBatchId != null) {

            row.stockBatchId = request.stockBatchId;

        }

        if (request.batchNumber != null) {

            row.batchNumber = request.batchNumber;

        }

        if (request.stockAtHand != null) {

            row.stockAtHand = request.stockAtHand;

        }

        if (request.expiryDate != null) {

            row.expiryDate = request.expiryDate;

        }

        if (request.dateOfStockRemoval != null) {

            row.dateOfStockRemoval = request.dateOfStockRemoval;

        }

        if (request.removedBy != null) {

            row.removedBy = request.removedBy;

        }



        return Response.ok(new ResponseMessage("Expiry item register entry updated successfully", new ExpiryItemRegisterDTO(row)))

                .build();

    }



    @Transactional

    public Response delete(Long id) {

        ExpiryItemRegister row = expiryItemRegisterRepository.findById(id);

        if (row == null) {

            return Response.status(Response.Status.NOT_FOUND)

                    .entity(new ResponseMessage("Expiry item register entry not found for ID: " + id, null))

                    .build();

        }

        expiryItemRegisterRepository.delete(row);

        return Response.ok(new ResponseMessage("Expiry item register entry deleted successfully", null)).build();

    }



    public List<ExpiryItemRegisterDTO> getAll() {

        return expiryItemRegisterRepository.listAll(Sort.descending("id")).stream()

                .map(ExpiryItemRegisterDTO::new)

                .collect(Collectors.toList());

    }



    public Response getById(Long id) {

        ExpiryItemRegister row = expiryItemRegisterRepository.findById(id);

        if (row == null) {

            return Response.status(Response.Status.NOT_FOUND)

                    .entity(new ResponseMessage("Expiry item register entry not found for ID: " + id, null))

                    .build();

        }

        return Response.ok(new ResponseMessage("Expiry item register entry retrieved successfully", new ExpiryItemRegisterDTO(row)))

                .build();

    }



    public List<ExpiryItemRegisterDTO> getByStockItemId(Long stockItemId) {

        return expiryItemRegisterRepository.list("stockItemId = ?1", Sort.descending("id"), stockItemId).stream()

                .map(ExpiryItemRegisterDTO::new)

                .collect(Collectors.toList());

    }

}


