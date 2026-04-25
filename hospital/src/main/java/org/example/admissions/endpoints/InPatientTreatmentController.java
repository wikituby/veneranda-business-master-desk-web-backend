package org.example.admissions.endpoints;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.example.admissions.services.payloads.responses.InPatientTreatmentDTO;
import org.example.admissions.services.payloads.requests.InPatientTreatmentRequest;
import org.example.admissions.services.InPatientTreatmentService;
import org.example.configuration.handler.ResponseMessage;
import org.example.statics.StatusTypes;

@Path("Patient-management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Patient Management Module", description = "Patient Management")

public class InPatientTreatmentController {

    @Inject
    InPatientTreatmentService inPatientTreatmentService;

    @POST
    @Path("create-new-InPatientTreatmentGiven")
    @Transactional
    @Operation(summary = "create-new-InPatientTreatmentGiven", description = "create-new-InPatientTreatmentGiven")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = InPatientTreatmentDTO.class)))
    public Response createInPatientTreatmentGiven(InPatientTreatmentRequest request){
        return Response.ok(new ResponseMessage(StatusTypes.CREATED.label,inPatientTreatmentService.createNewInPatientTreatment(request) )).build();
    }
}






