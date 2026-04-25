package org.example.client.endpoints;

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
import org.example.client.services.payloads.responses.dtos.PatientGroupDTO;
import org.example.client.services.payloads.requests.PatientGroupRequest;
import org.example.client.services.PatientGroupService;
import org.example.client.services.payloads.requests.PatientGroupUpdateRequest;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;

import java.util.List;

@Path("Patient-management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Patient Management Module", description = "Patient Management")
public class PatientGroupController {

    @Inject
    PatientGroupService patientGroupService;

    @POST
    @Path("create-new-patient-group")
    @Transactional
    @Operation(summary = "new-patient group", description = "new-patient group")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = PatientGroupDTO.class)))
    public Response createNewPatientGroup(PatientGroupRequest request) {
        return patientGroupService.createNewPatientGroup(request);
    }

    @GET
    @Transactional
    @Path("/get-all-patient-groups")
    // @RolesAllowed({"ADMIN"})
    @Operation(summary = "Get all patient groups", description = "Retrieve a list of all patient groups")
    @APIResponse(
            description = "Successful",
            responseCode = "200",
            content = @Content(schema = @Schema(implementation = PatientGroupDTO.class, type = SchemaType.ARRAY))
    )
    public Response getAllPatientGroups() {
        List<PatientGroupDTO> patientGroupList = patientGroupService.getAllPatientGroups();
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, patientGroupList)).build();
    }

    @PUT
    @Path("update-patient-group/{id}")
    //@RolesAllowed({"ADMIN","CUSTOMER"})
    @Transactional
    @Operation(summary = "Update patient group", description = "Update patient group")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = PatientGroupDTO.class)))
    public Response updatePatientGroup(@PathParam("id") Long id, PatientGroupUpdateRequest request){
        return Response.ok(new ResponseMessage(ActionMessages.UPDATED.label,patientGroupService.updatePatientGroupById(id, request) )).build();
    }

    @GET
    @Path("get-patient-group-by-id/{id}")
    //@RolesAllowed({"ADMIN"})
    @Operation(summary = "Get patient group by id", description = "Get patient group by id")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = PatientGroupDTO.class)))
    public Response getPatientGroupById(@PathParam("id") Long id) {
        PatientGroupDTO patientGroup = patientGroupService.getPatientGroupById(id);
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, patientGroup)).build();
    }

    @DELETE
    @Path("delete-group/{id}")
    //@RolesAllowed({"ADMIN"})
    @Transactional
    @Operation(summary = "delete group by id ", description = "delete group by id.")
    @APIResponse(description = "Successful", responseCode = "200")
    public Response deleteGroupById(@PathParam("id") Long id){
        return patientGroupService.deleteGroupById(id);

    }
}






