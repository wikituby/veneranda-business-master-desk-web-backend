package org.example.treatment.endpoints;

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
import org.example.treatment.services.TreatmentRequestService;
import org.example.treatment.services.payloads.responses.TreatmentRequestedDTO;
import org.example.treatment.services.payloads.requests.TreatmentRequestedRequest;
import org.example.treatment.services.payloads.requests.TreatmentStatusUpdateRequest;

import java.util.List;

@Path("Client-management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Client Management Module", description = "Client Management")

public class TreatmentRequestedController {

    @Inject
    TreatmentRequestService treatmentRequestService;

    @POST
    @Path("create-new-treatmentRequest/{id}")
    @Transactional
    @Operation(summary = "new-treatmentRequest", description = "new-treatmentRequest")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = TreatmentRequestedDTO.class)))
    public Response createNewTreatmentRequested(@PathParam("id") Long visitId, TreatmentRequestedRequest request){
        return treatmentRequestService.createNewTreatmentRequested(visitId, request);
    }


    @GET
    @Path("get-treatment-requested-by-visit-id/{id}")
    @Operation(summary = "get-treatment-requested-by-visit-id", description = "get-treatment-requested-by-visit-id")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = TreatmentRequestedDTO.class)))
    public Response getTreatmentByVisitId(@PathParam("id") Long visitId) {
        List<TreatmentRequestedDTO> treatmentRequested = treatmentRequestService.getTreatmentRequestedByVisit(visitId);

        // Return a successful response with the list of DTOs
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, treatmentRequested)).build();
    }

    @PUT
    @Path("update-treatmentRequest/{id}")
    @Transactional
    @Operation(summary = "update-treatmentRequest", description = "update-treatmentRequest")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = TreatmentRequestedDTO.class)))
    public Response updateTreatmentRequested(@PathParam("id") Long treatmentId, TreatmentRequestedRequest request){
        return treatmentRequestService.updateTreatmentRequested(treatmentId, request);
    }

    @PATCH
    @Path("update-treatment-status/{id}")
    @Transactional
    @Operation(summary = "Update prescription line status (pharmacist dispense)")
    @APIResponse(description = "Successful", responseCode = "200")
    public Response updateTreatmentStatus(@PathParam("id") Long treatmentId, TreatmentStatusUpdateRequest request) {
        return treatmentRequestService.updateTreatmentStatus(treatmentId, request);
    }

    @DELETE
    @Path("/delete-requested-treatment-by-id/{id}")
    //@RolesAllowed({"ADMIN"})
    @Transactional
    @Operation(summary = "delete requested treatment by id", description = "delete requested treatment by id")
    @APIResponse(description = "Successful", responseCode = "200")
    public Response deleteRequestedTreatmentById(@PathParam("id") Long id){
        return treatmentRequestService.deleteTreatmentRequestById(id);
    }
}




