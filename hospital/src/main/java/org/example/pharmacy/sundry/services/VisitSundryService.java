package org.example.pharmacy.sundry.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ResponseMessage;
import org.example.finance.invoice.services.InvoiceService;
import org.example.inventory.item.domain.Item;
import org.example.inventory.item.domain.repositories.ItemRepository;
import org.example.inventory.item.services.ShopItemService;
import org.example.inventory.stock.domains.StockBatch;
import org.example.inventory.stock.domains.repositories.StockBatchRepository;
import org.example.inventory.stock.services.StockTrackingService;
import org.example.pharmacy.sundry.domains.VisitSundry;
import org.example.pharmacy.sundry.services.payloads.requests.VisitSundryRequest;
import org.example.pharmacy.sundry.services.payloads.responses.VisitSundryDTO;
import org.example.visit.domains.PatientVisit;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class VisitSundryService {

    @Inject
    StockBatchRepository stockBatchRepository;

    @Inject
    ItemRepository itemRepository;

    @Inject
    ShopItemService shopItemService;

    @Inject
    InvoiceService invoiceService;

    private static BigDecimal nz(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    @Transactional
    public Response addVisitSundry(Long visitId, VisitSundryRequest request) {
        if (visitId == null || visitId <= 0) {
            return badRequest("A valid patient visit id is required.");
        }
        if (request == null) {
            return badRequest("Request body is required.");
        }

        boolean hasBatch = request.stockBatchId != null && request.stockBatchId > 0;
        boolean hasItem = request.itemId != null && request.itemId > 0;
        if (!hasBatch && !hasItem) {
            return badRequest("Either stockBatchId or itemId is required.");
        }
        if (hasBatch && hasItem) {
            return badRequest("Provide either stockBatchId or itemId, not both.");
        }

        BigDecimal qty = nz(request.quantity);
        if (qty.compareTo(BigDecimal.ZERO) <= 0) {
            return badRequest("quantity must be greater than zero.");
        }

        PatientVisit visit = PatientVisit.findById(visitId);
        if (visit == null) {
            return notFound("Patient visit not found for ID: " + visitId);
        }

        if (hasBatch) {
            return addFromStockBatch(visitId, request.stockBatchId, qty, request.usedBy);
        }
        return addFromItem(visitId, request.itemId, qty, request.usedBy);
    }

    private Response addFromStockBatch(Long visitId, Long stockBatchId, BigDecimal qty, String usedBy) {
        StockBatch batch = stockBatchRepository.findById(stockBatchId);
        if (batch == null) {
            return notFound("Stock batch not found for ID: " + stockBatchId);
        }

        BigDecimal stock = nz(batch.stockAtHand);
        if (qty.compareTo(stock) > 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage(
                            "Insufficient stock. Available: " + stock + ", Required: " + qty,
                            "INSUFFICIENT_STOCK"))
                    .build();
        }

        VisitSundry row = new VisitSundry();
        row.patientVisitId = visitId;
        row.stockBatchId = batch.id;
        row.itemId = null;
        row.itemName = batch.stockItemName;
        row.unitOfMeasure = batch.unitOfMeasure;
        row.quantityUsed = qty;
        row.unitSellingPrice = nz(batch.unitSellingPrice);
        row.unitCostPrice = nz(batch.unitCostPrice);
        row.lineTotal = qty.multiply(row.unitSellingPrice);
        row.usedBy = usedBy;
        row.recordedAt = LocalDateTime.now();
        row.persist();

        try {
            shopItemService.deductStockBatchWithTracking(
                    batch,
                    qty,
                    StockTrackingService.SRC_VISIT_SUNDRY_DISPENSE,
                    row.id,
                    StockTrackingService.REF_VISIT_SUNDRY);
        } catch (IllegalStateException ex) {
            row.delete();
            return badRequest(ex.getMessage());
        }

        invoiceService.syncInvoiceTotalsForVisit(visitId);
        return Response.ok(new VisitSundryDTO(row)).build();
    }

    private Response addFromItem(Long visitId, Long itemId, BigDecimal qty, String usedBy) {
        Item item = itemRepository.findById(itemId);
        if (item == null) {
            return notFound("Item not found for ID: " + itemId);
        }

        BigDecimal stock = nz(item.stockAtHand);
        if (qty.compareTo(stock) > 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage(
                            "Insufficient stock. Available: " + stock + ", Required: " + qty,
                            "INSUFFICIENT_STOCK"))
                    .build();
        }

        VisitSundry row = new VisitSundry();
        row.patientVisitId = visitId;
        row.stockBatchId = null;
        row.itemId = item.id;
        row.itemName = item.title;
        row.unitOfMeasure = item.unitOfMeasure;
        row.quantityUsed = qty;
        row.unitSellingPrice = nz(item.sellingPrice);
        row.unitCostPrice = nz(item.costPrice);
        row.lineTotal = qty.multiply(row.unitSellingPrice);
        row.usedBy = usedBy;
        row.recordedAt = LocalDateTime.now();
        row.persist();

        shopItemService.updateItemStockAtHandAfterSelling(qty, item);

        invoiceService.syncInvoiceTotalsForVisit(visitId);
        return Response.ok(new VisitSundryDTO(row)).build();
    }

    public List<VisitSundryDTO> getVisitSundriesByVisitId(Long visitId) {
        if (visitId == null || visitId <= 0) {
            return List.of();
        }
        return VisitSundry.find("patientVisitId = ?1 ORDER BY id DESC", visitId)
                .<VisitSundry>list()
                .stream()
                .map(VisitSundryDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public Response deleteVisitSundry(Long id) {
        VisitSundry row = VisitSundry.findById(id);
        if (row == null) {
            return notFound("Visit sundry not found for ID: " + id);
        }

        BigDecimal qty = nz(row.quantityUsed);
        if (qty.compareTo(BigDecimal.ZERO) > 0) {
            if (row.stockBatchId != null) {
                StockBatch batch = stockBatchRepository.findById(row.stockBatchId);
                if (batch != null) {
                    shopItemService.addStockBatchWithTracking(
                            batch,
                            qty,
                            StockTrackingService.SRC_VISIT_SUNDRY_DELETE,
                            row.id,
                            StockTrackingService.REF_VISIT_SUNDRY);
                }
            } else if (row.itemId != null) {
                Item item = itemRepository.findById(row.itemId);
                if (item != null) {
                    shopItemService.updateItemStockAtHandAfterDeleting(qty, item);
                }
            }
        }

        Long visitId = row.patientVisitId;
        row.delete();
        invoiceService.syncInvoiceTotalsForVisit(visitId);
        return Response.ok(new ResponseMessage("Visit sundry removed and stock restored.", null)).build();
    }

    private Response badRequest(String message) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ResponseMessage(message, null))
                .build();
    }

    private Response notFound(String message) {
        return Response.status(Response.Status.NOT_FOUND)
                .entity(new ResponseMessage(message, null))
                .build();
    }
}
