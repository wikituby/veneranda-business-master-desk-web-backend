package org.example.inventory.stock.endpoints;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.inventory.stock.services.StockVerificationService;
import org.example.inventory.stock.services.payloads.requests.StockVerificationApprovalRequest;
import org.example.inventory.stock.services.payloads.requests.StockVerificationRequest;
import org.example.inventory.stock.services.payloads.responses.dtos.StockVerificationDTO;
import org.example.inventory.stock.services.payloads.responses.dtos.StockVerificationDashboardDTO;
import org.example.inventory.stock.services.payloads.responses.dtos.StockVerificationPreviewDTO;

@Path("/stock-verification")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Hospital Management Module", description = "Stock verification layer over stock tracking")
public class StockVerificationController {

    @Inject
    StockVerificationService stockVerificationService;

    @GET
    @Path("/catalog")
    @Transactional
    @Operation(summary = "Verification catalog", description = "Statuses, reasons, and confidence levels")
    public Response getCatalog() {
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, stockVerificationService.getCatalog())).build();
    }

    @GET
    @Path("/dashboard")
    @Transactional
    @Operation(summary = "Verification dashboard", description = "Stock at hand with last verification and live preview")
    @APIResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = StockVerificationDashboardDTO.class)))
    public Response getDashboard(@QueryParam("storeId") Long storeId) {
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, stockVerificationService.getDashboard(storeId))).build();
    }

    @GET
    @Path("/preview/{stockBatchId}")
    @Transactional
    @Operation(summary = "Preview verification", description = "Expected stock and confidence before recording")
    @APIResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = StockVerificationPreviewDTO.class)))
    public Response preview(@PathParam("stockBatchId") Long stockBatchId) {
        StockVerificationPreviewDTO preview = stockVerificationService.preview(stockBatchId);
        if (preview == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Stock batch not found", null))
                    .build();
        }
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, preview)).build();
    }

    @GET
    @Path("/preview-item/{itemId}")
    @Transactional
    @Operation(summary = "Preview item verification", description = "Expected stock for shop-item mode")
    @APIResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = StockVerificationPreviewDTO.class)))
    public Response previewItem(@PathParam("itemId") Long itemId) {
        StockVerificationPreviewDTO preview = stockVerificationService.previewItem(itemId);
        if (preview == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Item not found", null))
                    .build();
        }
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, preview)).build();
    }

    @POST
    @Path("/create")
    @Transactional
    @Operation(summary = "Record verification", description = "Creates an immutable verification snapshot")
    @APIResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = StockVerificationDTO.class)))
    public Response create(StockVerificationRequest request) {
        return stockVerificationService.create(request);
    }

    @POST
    @Path("/approve/{id}")
    @Transactional
    @Operation(summary = "Approve verification")
    @APIResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = StockVerificationDTO.class)))
    public Response approve(@PathParam("id") Long id, StockVerificationApprovalRequest request) {
        return stockVerificationService.approve(id, request);
    }

    @GET
    @Path("/get-all")
    @Transactional
    @Operation(summary = "List verifications")
    @APIResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = StockVerificationDTO.class)))
    public Response getAll(@QueryParam("storeId") Long storeId) {
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, stockVerificationService.getAll(storeId))).build();
    }

    @GET
    @Path("/get/{id}")
    @Transactional
    @Operation(summary = "Get verification by id")
    @APIResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = StockVerificationDTO.class)))
    public Response getById(@PathParam("id") Long id) {
        return stockVerificationService.getById(id);
    }

    @GET
    @Path("/by-batch/{stockBatchId}")
    @Transactional
    @Operation(summary = "Verification history for a batch")
    @APIResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = StockVerificationDTO.class)))
    public Response getByBatch(@PathParam("stockBatchId") Long stockBatchId) {
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, stockVerificationService.getByBatch(stockBatchId))).build();
    }

    @GET
    @Path("/by-item/{itemId}")
    @Transactional
    @Operation(summary = "Verification history for a shop item")
    @APIResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = StockVerificationDTO.class)))
    public Response getByItem(@PathParam("itemId") Long itemId) {
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, stockVerificationService.getByItem(itemId))).build();
    }
}
