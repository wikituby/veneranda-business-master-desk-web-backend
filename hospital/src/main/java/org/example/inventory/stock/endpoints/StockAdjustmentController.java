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
import org.example.inventory.stock.services.StockAdjustmentService;
import org.example.inventory.stock.services.payloads.requests.StockAdjustmentRequest;
import org.example.inventory.stock.services.payloads.responses.dtos.StockAdjustmentDTO;

@Path("/stock-adjustment")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Hospital Management Module", description = "Stock quantity adjustments (damage, recount, etc.)")
public class StockAdjustmentController {

    @Inject
    StockAdjustmentService stockAdjustmentService;

    @POST
    @Path("/add-new-stock-adjustment")
    @Transactional
    @Operation(summary = "Record adjustment", description = "Applies signed quantityChanged to the batch and writes stock tracking.")
    @APIResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = StockAdjustmentDTO.class)))
    public Response addNew(StockAdjustmentRequest request) {
        return stockAdjustmentService.addNew(request);
    }

    @GET
    @Path("/get-all-stock-adjustments")
    @Transactional
    @Operation(summary = "List all stock adjustments")
    @APIResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = StockAdjustmentDTO.class)))
    public Response getAll() {
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, stockAdjustmentService.getAll())).build();
    }

    @GET
    @Path("/get-stock-adjustment/{id}")
    @Transactional
    @Operation(summary = "Get one stock adjustment")
    @APIResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = StockAdjustmentDTO.class)))
    public Response getById(@PathParam("id") Long id) {
        return stockAdjustmentService.getById(id);
    }

    @GET
    @Path("/get-stock-adjustments-by-batch/{stockBatchId}")
    @Transactional
    @Operation(summary = "List adjustments for a batch")
    @APIResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = StockAdjustmentDTO.class)))
    public Response getByBatch(@PathParam("stockBatchId") Long stockBatchId) {
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label,
                stockAdjustmentService.getByStockBatchId(stockBatchId))).build();
    }
}
