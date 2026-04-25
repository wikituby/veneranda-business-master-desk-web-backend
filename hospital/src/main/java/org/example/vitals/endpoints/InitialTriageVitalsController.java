package org.example.vitals.endpoints;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.vitals.services.payloads.responses.InitialTriageVitalsDTO;
import org.example.vitals.services.payloads.requests.InitialTriageVitalsRequest;
import org.example.vitals.services.InitialTriageVitalsService;
import org.example.vitals.services.payloads.requests.InitialVitalUpdateRequest;

import java.util.List;

@Path("Patient-management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Patient Management Module", description = "Patient Management")

public class InitialTriageVitalsController {

    @Inject
    InitialTriageVitalsService initialTriageVitalsService;

    @POST
    @Path("create-new-InitialTriageVitals/{id}")
    @Transactional
    @Operation(summary = "new-InitialTriageVitals", description = "new-InitialTriageVitals")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = InitialTriageVitalsDTO.class)))
    public Response createInitialTriageVitalsVisit(@PathParam("id") Long id, InitialTriageVitalsRequest request){
        return initialTriageVitalsService.createNewInitialTriageVitals(id, request);
    }

    @GET
    @Transactional
    @Path("/get-all-InitialTriageVitals")
    // @RolesAllowed({"ADMIN"})
    @Operation(summary = "Get all InitialTriageVitals", description = "Retrieve a list of all InitialTriageVitals")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = InitialTriageVitalsDTO.class, type = SchemaType.ARRAY)))
    public Response getAllPatients() {
        List<InitialTriageVitalsDTO> initialTriageVitalsList = initialTriageVitalsService.getAllInitialTriageVitals();
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, initialTriageVitalsList)).build();
    }

    @GET
    @Path("get-Initial-TriageVitals-visit-by-id/{id}")
    @Operation(summary = "Get InitialTriageVitals where visit id", description = "Get InitialTriageVitals where visit id")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = InitialTriageVitalsDTO.class)))
    public Response getInitialTriageVitalsById(@PathParam("id") Long id) {
        // Call the service method to get a list of InitialTriageVitalsDTO for the given visitId
        List<InitialTriageVitalsDTO> initialTriageVitals = initialTriageVitalsService.getInitialTriageVitalsByVisitId(id);

        // Return a successful response with the list of DTOs
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, initialTriageVitals)).build();
    }

    @PUT
    @Path("update-vital/{id}")
    //@RolesAllowed({"ADMIN","CUSTOMER"})
    @Transactional
    @Operation(summary = "Update vital", description = "Update vital")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = InitialTriageVitalsDTO.class)))
    public Response updateVital(@PathParam("id") Long id, InitialVitalUpdateRequest request){
        return initialTriageVitalsService.updateInitialVitalById(id, request);
    }

    @DELETE
    @Path("/delete-vital-by-id/{id}")
    //@RolesAllowed({"ADMIN"})
    @Transactional
    @Operation(summary = "delete vital by id", description = "delete vital by id")
    @APIResponse(description = "Successful", responseCode = "200")
    public Response deleteVitalById(@PathParam("id") Long id){
        return initialTriageVitalsService.deleteVitalById(id);
    }


}






