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
import org.example.inventory.stock.domains.StockTracking;
import org.example.inventory.stock.services.StockTrackingService;
import org.example.inventory.stock.services.payloads.requests.StockTrackingRequest;
import org.example.inventory.stock.services.payloads.responses.dtos.StockTrackingDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Path("/stock-tracking")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Stock Management Module", description = "Track stock movements (in/out)")
public class StockTrackingController {

    @Inject
    StockTrackingService stockTrackingService;

    @POST
    @Path("/create")
    @Transactional
    @Operation(summary = "Create stock tracking record", description = "Record a stock transaction (in/out)")
    @APIResponse(description = "Created successfully", responseCode = "200",
            content = @Content(schema = @Schema(implementation = StockTrackingDTO.class)))
    public Response create(StockTrackingRequest request) {
        StockTracking entity = stockTrackingService.createStockTracking(
                request.stockItemId,
                request.stockBeforeTransaction,
                request.transactionType,
                request.quantityChanged,
                request.stockAfterTransaction
        );
        return Response.ok(new ResponseMessage(ActionMessages.SAVED.label, new StockTrackingDTO(entity))).build();
    }

    @GET
    @Path("/all")
    @Operation(summary = "Get all stock tracking records", description = "Retrieve all stock movement records")
    @APIResponse(description = "Successful", responseCode = "200")
    public Response getAll() {
        List<StockTrackingDTO> list = stockTrackingService.getAll()
                .stream().map(StockTrackingDTO::new).collect(Collectors.toList());
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, list)).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get stock tracking record by ID", description = "Fetch a single stock tracking record")
    @APIResponse(description = "Successful", responseCode = "200")
    public Response getById(@PathParam("id") Long id) {
        StockTracking entity = stockTrackingService.getById(id);
        if (entity == null)
            throw new NotFoundException("Record not found");
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, new StockTrackingDTO(entity))).build();
    }

    @GET
    @Path("/item/{stockItemId}")
    @Operation(summary = "Get stock tracking by item", description = "Fetch all stock transactions for a specific item")
    @APIResponse(description = "Successful", responseCode = "200")
    public Response getByStockItem(@PathParam("stockItemId") Long stockItemId) {
        List<StockTrackingDTO> list = stockTrackingService.getByStockItem(stockItemId)
                .stream().map(StockTrackingDTO::new).collect(Collectors.toList());
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, list)).build();
    }

    @GET
    @Path("/total-in/{stockItemId}")
    @Operation(summary = "Get total stock in", description = "Calculate total stock added for a specific item")
    @APIResponse(description = "Successful", responseCode = "200",
            content = @Content(schema = @Schema(implementation = BigDecimal.class)))
    public Response getTotalIn(@PathParam("stockItemId") Long stockItemId) {
        BigDecimal total = stockTrackingService.getTotalStockIn(stockItemId);
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, total)).build();
    }

    @GET
    @Path("/total-out/{stockItemId}")
    @Operation(summary = "Get total stock out", description = "Calculate total stock removed for a specific item")
    @APIResponse(description = "Successful", responseCode = "200",
            content = @Content(schema = @Schema(implementation = BigDecimal.class)))
    public Response getTotalOut(@PathParam("stockItemId") Long stockItemId) {
        BigDecimal total = stockTrackingService.getTotalStockOut(stockItemId);
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, total)).build();
    }

    @DELETE
    @Path("/delete/{id}")
    @Transactional
    @Operation(summary = "Delete stock tracking record", description = "Remove a specific stock tracking entry by ID")
    @APIResponse(description = "Deleted successfully", responseCode = "200")
    public Response delete(@PathParam("id") Long id) {
        boolean deleted = stockTrackingService.delete(id);
        if (!deleted)
            throw new NotFoundException("Record not found");
        return Response.ok(new ResponseMessage(ActionMessages.DELETED.label, null)).build();
    }
}
