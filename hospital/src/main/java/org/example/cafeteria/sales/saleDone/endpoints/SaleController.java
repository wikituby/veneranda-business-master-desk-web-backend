package org.example.cafeteria.sales.saleDone.endpoints;

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
import org.example.cafeteria.sales.saleDone.services.SaleService;
import org.example.cafeteria.sales.saleDone.services.payloads.requests.SaleRequest;
import org.example.cafeteria.sales.saleDone.services.payloads.responses.SaleDTO;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;

import java.util.List;

@Path("Cafeteria-sale-done-management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Cafeteria sale done Management Module", description = "Cafeteria sale done Management")

public class SaleController {

    @Inject
    SaleService saleService;

    @POST
    @Path("create-new-sale-done-/{id}")
    @Transactional
    @Operation(summary = "new-treatmentRequest", description = "new-treatmentRequest")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = SaleDTO.class)))
    public Response createNewTreatmentRequested(@PathParam("id") Long visitId, SaleRequest request){
        return saleService.createNewTreatmentRequested(visitId, request);
    }


    @GET
    @Path("get-sale-done-requested-by-visit-id/{id}")
    @Operation(summary = "get-sale-requested-by-visit-id", description = "get-sale-requested-by-visit-id")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = SaleDTO.class)))
    public Response getTreatmentByVisitId(@PathParam("id") Long visitId) {
        List<SaleDTO> treatmentRequested = saleService.getTreatmentRequestedByVisit(visitId);

        // Return a successful response with the list of DTOs
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, treatmentRequested)).build();
    }

    @DELETE
    @Path("/delete-requested-sale-done-by-id/{id}")
    //@RolesAllowed({"ADMIN"})
    @Transactional
    @Operation(summary = "delete requested sale by id", description = "delete requested sale by id")
    @APIResponse(description = "Successful", responseCode = "200")
    public Response deleteRequestedTreatmentById(@PathParam("id") Long id){
        return saleService.deleteTreatmentRequestById(id);
    }
}






