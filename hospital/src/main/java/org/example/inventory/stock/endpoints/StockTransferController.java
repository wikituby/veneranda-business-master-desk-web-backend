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
import org.example.inventory.stock.services.StockTransferService;
import org.example.inventory.stock.services.payloads.requests.StockTransferRequest;
import org.example.inventory.stock.services.payloads.responses.dtos.StockTransferDTO;

import java.math.BigDecimal;
import java.util.List;


@Path("/Hospital-item")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Hospital Management Module", description = "Hospital Management")

public class StockTransferController {

    @Inject
    StockTransferService stockTransferService;

    @POST
    @Path("transfer-stock")
    @Transactional
    @Operation(summary = "transfer", description = "Transfer stock between stores")
    @APIResponse(description = "Successful", responseCode = "200",content = @Content(schema = @Schema(implementation = StockTransferDTO.class)))
    public StockTransferDTO transferStock(StockTransferRequest request) {
        return stockTransferService.transferStock(request);
    }

    @GET
    @Path("stock-transfers")
    @Transactional
    @Operation(summary = "List stock transfers", description = "Returns all recorded stock transfers between stores")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = StockTransferDTO[].class)))
    public List<StockTransferDTO> listStockTransfers() {
        return stockTransferService.findAllTransfers();
    }


    @GET
    @Path("total-item-stock/{id}")
    @Transactional
    @Operation(summary = "get total items stocked by id", description = "get total items stocked by id")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = BigDecimal.class)))
    public Response getTotalItemsStocked(@PathParam("id") Long id) {
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label,stockTransferService.getTotalItemStock(id))).build();
    }
}
