package org.example.inventory.stock.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.inventory.stock.domains.PurchaseOrder;
import org.example.inventory.stock.domains.PurchaseOrderLine;
import org.example.inventory.stock.services.payloads.requests.PurchaseOrderLineRequest;
import org.example.inventory.stock.services.payloads.requests.PurchaseOrderRequest;
import org.example.inventory.stock.services.payloads.responses.dtos.PurchaseOrderDTO;
import org.example.inventory.stock.services.payloads.responses.dtos.PurchaseOrderLineDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class PurchaseOrderService {

    @Transactional
    public Response create(PurchaseOrderRequest request) {
        List<PurchaseOrderLineRequest> incoming = request == null ? null : request.lines;
        if (incoming == null || incoming.isEmpty()) {
            return badRequest("Add at least one product that is below its reorder level.");
        }

        PurchaseOrder order = new PurchaseOrder();
        order.orderNo = "PO-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmssSSS"));
        order.supplierId = request.supplierId;
        order.supplierName = blankToNull(request.supplierName);
        order.notes = blankToNull(request.notes);
        order.status = "draft";
        order.createdAt = LocalDateTime.now();
        order.persistAndFlush();

        int saved = 0;
        for (PurchaseOrderLineRequest lineRequest : incoming) {
            if (!keepLine(lineRequest)) {
                continue;
            }
            PurchaseOrderLine line = newLine(order.id, lineRequest);
            line.persist();
            saved++;
        }
        if (saved == 0) {
            order.delete();
            return badRequest("Each line needs a product name and a quantity greater than zero.");
        }
        return Response.ok(new ResponseMessage(ActionMessages.SAVED.label, toDto(order))).build();
    }

    public List<PurchaseOrderDTO> list() {
        List<PurchaseOrder> orders = PurchaseOrder.find("ORDER BY id DESC").list();
        List<PurchaseOrderDTO> result = new ArrayList<>();
        for (PurchaseOrder order : orders) {
            result.add(toDto(order));
        }
        return result;
    }

    public Response get(Long id) {
        PurchaseOrder order = PurchaseOrder.findById(id);
        if (order == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Purchase order not found.", null))
                    .build();
        }
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, toDto(order))).build();
    }

    @Transactional
    public Response update(Long id, PurchaseOrderRequest request) {
        PurchaseOrder order = PurchaseOrder.findById(id);
        if (order == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Purchase order not found.", null))
                    .build();
        }
        if (request == null) {
            return badRequest("Purchase order details are required.");
        }

        order.supplierId = request.supplierId;
        order.supplierName = blankToNull(request.supplierName);
        order.notes = blankToNull(request.notes);

        List<PurchaseOrderLine> existing = PurchaseOrderLine.find("purchaseOrderId", order.id).list();
        Set<Long> kept = new HashSet<>();
        List<PurchaseOrderLineRequest> incoming = request.lines == null ? List.of() : request.lines;

        for (PurchaseOrderLineRequest lineRequest : incoming) {
            if (lineRequest == null) {
                continue;
            }
            if (lineRequest.id == null) {
                if (keepLine(lineRequest)) {
                    newLine(order.id, lineRequest).persist();
                }
                continue;
            }
            PurchaseOrderLine line = findOwned(existing, lineRequest.id);
            if (line == null || !keepLine(lineRequest)) {
                continue;
            }
            applyLine(line, lineRequest);
            kept.add(line.id);
        }

        for (PurchaseOrderLine line : existing) {
            if (!kept.contains(line.id)) {
                line.delete();
            }
        }

        return Response.ok(new ResponseMessage(ActionMessages.UPDATED.label, toDto(order))).build();
    }

    private PurchaseOrderLine findOwned(List<PurchaseOrderLine> existing, Long id) {
        for (PurchaseOrderLine line : existing) {
            if (id.equals(line.id)) {
                return line;
            }
        }
        return null;
    }

    private boolean keepLine(PurchaseOrderLineRequest lineRequest) {
        if (lineRequest == null || blankToNull(lineRequest.stockItemName) == null) {
            return false;
        }
        return lineRequest.quantityOrdered != null && lineRequest.quantityOrdered.compareTo(BigDecimal.ZERO) > 0;
    }

    private PurchaseOrderLine newLine(Long purchaseOrderId, PurchaseOrderLineRequest lineRequest) {
        PurchaseOrderLine line = new PurchaseOrderLine();
        line.purchaseOrderId = purchaseOrderId;
        applyLine(line, lineRequest);
        return line;
    }

    private void applyLine(PurchaseOrderLine line, PurchaseOrderLineRequest lineRequest) {
        line.stockItemId = lineRequest.stockItemId;
        line.stockItemName = blankToNull(lineRequest.stockItemName);
        line.storeName = blankToNull(lineRequest.storeName);
        line.batchNumber = blankToNull(lineRequest.batchNumber);
        line.inStock = lineRequest.inStock;
        line.reorderTo = lineRequest.reorderTo;
        line.quantityOrdered = lineRequest.quantityOrdered;
        line.unitPrice = lineRequest.unitPrice;
        line.lineNotes = blankToNull(lineRequest.lineNotes);
    }

    private PurchaseOrderDTO toDto(PurchaseOrder order) {
        PurchaseOrderDTO dto = new PurchaseOrderDTO(order);
        List<PurchaseOrderLine> lines = PurchaseOrderLine.find("purchaseOrderId = ?1 ORDER BY id", order.id).list();
        for (PurchaseOrderLine line : lines) {
            dto.lines.add(new PurchaseOrderLineDTO(line));
        }
        return dto;
    }

    private Response badRequest(String message) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ResponseMessage(message, null))
                .build();
    }

    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
