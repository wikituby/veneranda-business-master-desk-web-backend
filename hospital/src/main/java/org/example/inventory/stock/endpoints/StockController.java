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
import org.example.inventory.stock.services.StockService;
import org.example.inventory.stock.services.payloads.requests.StockTakeRequest;


import org.example.inventory.stock.services.payloads.responses.dtos.StockDTO;

@Path("/shop-item")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Hospital Management Module", description = "Hospital Management")

public class StockController {

    @Inject
    StockService stockService;

    @POST
    @Path("receive-new-stock")
    @Transactional
    @Operation(summary = "receive-new-stock", description = "receive-new-stock")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = StockDTO.class)))
    public Response receiveNewStock(StockTakeRequest request) {
        return stockService.receiveStock(request);
    }

    @GET
    @Path("/get-all-stock-receives")
    @Transactional
    @Operation(summary = "get all stock receives", description = "get all stock receives.")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = StockDTO.class)))
    public Response getAllStockReceives() {
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label,stockService.getAllStockReceives())).build();
    }

    @DELETE
    @Path("delete-stock-received/{id}")
    //@RolesAllowed({"ADMIN"})
    @Transactional
    @Operation(summary = "delete stock received by id ", description = "delete stock received by id")
    @APIResponse(description = "Successful", responseCode = "200")
    public Response deleteStockReceivedById(@PathParam("id") Long id){
        return stockService.deleteStockReceivedById(id);

    }

}







