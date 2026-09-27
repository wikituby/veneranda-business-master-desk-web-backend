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
import org.example.inventory.stock.services.StockBatchService;
import org.example.inventory.stock.services.payloads.requests.StockBatchFieldsUpdateRequest;
import org.example.inventory.stock.services.payloads.responses.dtos.StockBatchDTO;

@Path("/stock-records")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Stock Management Module", description = "Manage stock records and inventory")
public class StockBatchController {

    @Inject
    StockBatchService stockBatchService;

    @GET
    @Path("/get-all-stock-batches")
    @Transactional
    @Operation(summary = "Get all stock batches", description = "Retrieves all stock batches sorted by descending ID. Optionally filter by storeId.")
    @APIResponse(description = "Successful retrieval of all stock batches.", responseCode = "200", content = @Content(schema = @Schema(implementation = StockBatchDTO.class)))
    public Response getAllStockBatches(@QueryParam("storeId") Long storeId) {
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, stockBatchService.getAll(storeId))).build();
    }

    @PUT
    @Path("/update-stock-batch-fields/{id}")
    @Transactional
    @Operation(summary = "Update stock batch fields", description = "Updates specific fields: stockAtHand, unitCostPrice, unitSellingPrice, expiryDate, shelfNumber, profit margins (retail/wholesale/special), reOrderLevel, reOrderQuantity, reOrderTo, unitOfMeasure, lastUnitValue, lastUnitOfMeasure. Only non-null request fields are applied.")
    @APIResponse(description = "Stock batch fields updated successfully.", responseCode = "200", content = @Content(schema = @Schema(implementation = StockBatchDTO.class)))
    public Response updateStockBatchFields(@PathParam("id") Long id, StockBatchFieldsUpdateRequest request) {
        return stockBatchService.updateStockBatchFields(id, request);
    }

    @DELETE
    @Path("/delete-stock-batch/{id}")
    @Transactional
    @Operation(summary = "Delete stock batch", description = "Deletes a batch when stock at hand is zero and no treatment request lines reference this batch. Receive/transfer/expiry history may still mention this batch id.")
    @APIResponse(description = "Deleted successfully", responseCode = "200")
    @APIResponse(description = "Batch not found or cannot be deleted", responseCode = "404")
    @APIResponse(description = "Validation failed (stock or dependencies)", responseCode = "400")
    public Response deleteStockBatch(@PathParam("id") Long id) {
        return stockBatchService.deleteStockBatch(id);
    }

}
