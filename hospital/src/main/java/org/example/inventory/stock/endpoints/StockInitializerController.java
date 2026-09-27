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
import org.example.inventory.stock.services.StockInitializerService;
import org.example.inventory.stock.services.payloads.requests.StockInitializerRequest;
import org.example.inventory.stock.services.payloads.responses.dtos.StockInitializerDashboardDTO;

@Path("/stock-initializer")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Hospital Management Module", description = "First-time stock setup and opening balances")
public class StockInitializerController {

    @Inject
    StockInitializerService stockInitializerService;

    @GET
    @Path("/dashboard")
    @Transactional
    @Operation(summary = "Initializer dashboard", description = "Pending and completed opening stock counts")
    @APIResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = StockInitializerDashboardDTO.class)))
    public Response getDashboard(@QueryParam("storeId") Long storeId) {
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label,
                stockInitializerService.getDashboard(storeId))).build();
    }

    @POST
    @Path("/save-draft")
    @Transactional
    @Operation(summary = "Save draft quantity", description = "Stores a draft count before final initialization")
    public Response saveDraft(StockInitializerRequest request) {
        return stockInitializerService.saveDraft(request);
    }

    @POST
    @Path("/finalize")
    @Transactional
    @Operation(summary = "Count and initialize", description = "Applies opening stock and marks the item as done")
    public Response finalizeInitialization(StockInitializerRequest request) {
        return stockInitializerService.finalize(request);
    }
}
