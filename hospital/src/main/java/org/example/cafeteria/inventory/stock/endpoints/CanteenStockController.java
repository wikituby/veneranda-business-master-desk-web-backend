package org.example.cafeteria.inventory.stock.endpoints;


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
import org.example.cafeteria.inventory.stock.services.CanteenStockService;
import org.example.cafeteria.inventory.stock.services.requests.CanteenStockTakeRequest;
import org.example.cafeteria.inventory.stock.services.responses.dtos.CanteenStockDTO;

@Path("/Cafeteria-stock-item")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Cafeteria stock Management Module", description = "Cafeteria stock Management")

public class CanteenStockController {

    @Inject
    CanteenStockService canteenStockService;

    @POST
    @Path("receive-new-stock-canteenItem")
    @Transactional
    @Operation(summary = "receive-new-stock", description = "receive-new-stock")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = CanteenStockDTO.class)))
    public Response receiveNewCanteenStock(CanteenStockTakeRequest request) {
        return canteenStockService.receiveCanteenStock(request);
    }

    @GET
    @Path("/get-all-stock-receives-canteenItem")
    @Transactional
    @Operation(summary = "get all canteen stock receives", description = "get all canteen stock receives.")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = CanteenStockDTO.class)))
    public Response getAllStockReceives() {
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label,canteenStockService.getAllCanteenStockReceives())).build();
    }

    @DELETE
    @Path("delete-stock-received-canteenItem/{id}")
    //@RolesAllowed({"ADMIN"})
    @Transactional
    @Operation(summary = "delete canteen stock received by id ", description = "delete canteen stock received by id")
    @APIResponse(description = "Successful", responseCode = "200")
    public Response deleteCanteenStockReceivedById(@PathParam("id") Long id){
        return canteenStockService.deleteCanteenStockReceivedById(id);

    }

}
