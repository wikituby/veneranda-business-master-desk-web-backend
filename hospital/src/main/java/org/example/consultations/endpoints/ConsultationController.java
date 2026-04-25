package org.example.consultations.endpoints;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
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
import org.example.consultations.services.payloads.responses.ConsultationDTO;
import org.example.consultations.services.payloads.requests.ConsultationRequest;
import org.example.consultations.services.ConsultationService;
import org.example.vitals.services.payloads.responses.InitialTriageVitalsDTO;

import java.util.List;

@Path("Patient-management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Patient Management Module - consultations", description = "Patient Management")


public class ConsultationController {

    @Inject
    ConsultationService consultationService;

    @POST
    @Path("create-new-Consultation/{id}")
    @Transactional
    @Operation(summary = "new-Consultation", description = "new-Consultation")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ConsultationDTO.class)))
    public Response createConsultation(@PathParam("id") Long id, ConsultationRequest request){
        return consultationService.createNewConsultation(id, request);
    }

    @GET
    @Path("get-consultations-visit-by-id/{id}")
    @Operation(summary = "Get the first consultation by visit ID", description = "Fetches the most recent consultation for a given visit ID")
    @APIResponse(
            description = "Successful",
            responseCode = "200",
            content = @Content(schema = @Schema(implementation = ConsultationDTO.class))
    )
    public Response getConsultationsById(@PathParam("id") Long id) {
        // Call the service method to get the first ConsultationDTO for the given visitId
        ConsultationDTO consultation = consultationService.getFirstConsultationByVisitId(id);

        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, consultation)).build();
    }

    @GET
    @Transactional
    @Path("/get-all-consultations")
    // @RolesAllowed({"ADMIN"})
    @Operation(summary = "Get all consultations", description = "How to Retrieve a list of all consultations")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ConsultationDTO.class, type = SchemaType.ARRAY)))
    public Response getAllConsultations() {
        List<ConsultationDTO> consultations = consultationService.getAllConsultations();
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, consultations)).build();
    }


}






