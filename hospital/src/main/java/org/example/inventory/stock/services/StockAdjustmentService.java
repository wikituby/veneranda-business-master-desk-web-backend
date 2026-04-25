package org.example.inventory.stock.services;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ResponseMessage;
import org.example.inventory.stock.domains.AdjustmentType;
import org.example.inventory.stock.domains.StockAdjustment;
import org.example.inventory.stock.domains.StockBatch;
import org.example.inventory.stock.domains.repositories.AdjustmentTypeRepository;
import org.example.inventory.stock.domains.repositories.StockAdjustmentRepository;
import org.example.inventory.stock.domains.repositories.StockBatchRepository;
import org.example.inventory.stock.services.payloads.requests.StockAdjustmentRequest;
import org.example.inventory.stock.services.payloads.responses.dtos.StockAdjustmentDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class StockAdjustmentService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    @Inject
    StockAdjustmentRepository stockAdjustmentRepository;

    @Inject
    StockBatchRepository stockBatchRepository;

    @Inject
    AdjustmentTypeRepository adjustmentTypeRepository;

    @Inject
    StockTrackingService stockTrackingService;

    private static BigDecimal nz(BigDecimal v) {
        return v != null ? v : ZERO;
    }

    @Transactional
    public Response addNew(StockAdjustmentRequest request) {
        if (request == null || request.stockBatchId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("stockBatchId is required", null))
                    .build();
        }
        if (request.adjustmentTypeId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("adjustmentTypeId is required", null))
                    .build();
        }
        if (request.quantityChanged == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("quantityChanged is required (use signed value: + or -)", null))
                    .build();
        }

        StockBatch batch = stockBatchRepository.findById(request.stockBatchId);
        if (batch == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Stock batch not found for ID: " + request.stockBatchId, null))
                    .build();
        }

        AdjustmentType type = adjustmentTypeRepository.findById(request.adjustmentTypeId);
        if (type == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Adjustment type not found for ID: " + request.adjustmentTypeId, null))
                    .build();
        }
        if (Boolean.FALSE.equals(type.active)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("This adjustment type is inactive and cannot be used.", null))
                    .build();
        }

        BigDecimal before = nz(batch.stockAtHand);
        BigDecimal delta = request.quantityChanged;
        BigDecimal after = before.add(delta);

        if (after.compareTo(ZERO) < 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage(
                            "Adjustment would make stock negative. Before: " + before + ", change: " + delta + ".",
                            null))
                    .build();
        }

        batch.stockAtHand = after;
        stockBatchRepository.persist(batch);

        StockAdjustment adj = new StockAdjustment();
        adj.stockBatch = batch;
        adj.adjustmentType = type;
        adj.quantityBefore = before;
        adj.quantityChanged = delta;
        adj.quantityAfter = after;
        adj.reason = request.reason;
        adj.date = request.date != null ? request.date : LocalDateTime.now();
        adj.doneBy = request.doneBy;
        stockAdjustmentRepository.persist(adj);

        if (delta.compareTo(ZERO) != 0) {
            String txType = delta.compareTo(ZERO) > 0 ? StockTrackingService.TX_IN : StockTrackingService.TX_OUT;
            BigDecimal magnitude = delta.abs();
            stockTrackingService.recordBatchMovement(
                    batch,
                    before,
                    after,
                    txType,
                    magnitude,
                    StockTrackingService.SRC_STOCK_ADJUSTMENT,
                    adj.id,
                    StockTrackingService.REF_STOCK_ADJUSTMENT);
        }

        return Response.ok(new ResponseMessage("Stock adjustment recorded successfully", new StockAdjustmentDTO(adj)))
                .build();
    }

    @Transactional
    public List<StockAdjustmentDTO> getAll() {
        return stockAdjustmentRepository.listAll(Sort.descending("id")).stream()
                .map(StockAdjustmentDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public Response getById(Long id) {
        StockAdjustment sa = stockAdjustmentRepository.findById(id);
        if (sa == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Stock adjustment not found for ID: " + id, null))
                    .build();
        }
        return Response.ok(new ResponseMessage("Stock adjustment retrieved successfully", new StockAdjustmentDTO(sa)))
                .build();
    }

    @Transactional
    public List<StockAdjustmentDTO> getByStockBatchId(Long stockBatchId) {
        return stockAdjustmentRepository.list("stockBatch.id = ?1", Sort.descending("id"), stockBatchId).stream()
                .map(StockAdjustmentDTO::new)
                .collect(Collectors.toList());
    }
}
