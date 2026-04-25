package org.example.cafeteria.client.endpoints;

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
import org.example.cafeteria.client.services.BuyerGroupService;
import org.example.cafeteria.client.services.payloads.requests.BuyerGroupRequest;
import org.example.cafeteria.client.services.payloads.requests.BuyerGroupUpdateRequest;
import org.example.cafeteria.client.services.payloads.responses.dtos.PatientGroupDTO;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;

import java.util.List;

@Path("cafeteria-buyer-group-management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "cafeteria buyer group Management Module", description = "cafeteria buyer group Management")
public class BuyerGroupController {

    @Inject
    BuyerGroupService buyerGroupService;

    @POST
    @Path("create-new-buyer-group")
    @Transactional
    @Operation(summary = "new-buyer group", description = "new-buyer group")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = PatientGroupDTO.class)))
    public Response createNewPatientGroup(BuyerGroupRequest request) {
        return buyerGroupService.createNewPatientGroup(request);
    }

    @GET
    @Transactional
    @Path("/get-all-buyer-groups")
    // @RolesAllowed({"ADMIN"})
    @Operation(summary = "Get all buyer groups", description = "Retrieve a list of all buyer groups")
    @APIResponse(
            description = "Successful",
            responseCode = "200",
            content = @Content(schema = @Schema(implementation = PatientGroupDTO.class, type = SchemaType.ARRAY))
    )
    public Response getAllPatientGroups() {
        List<PatientGroupDTO> patientGroupList = buyerGroupService.getAllPatientGroups();
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, patientGroupList)).build();
    }

    @PUT
    @Path("update-buyer-group/{id}")
    //@RolesAllowed({"ADMIN","CUSTOMER"})
    @Transactional
    @Operation(summary = "Update buyer group", description = "Update buyer group")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = PatientGroupDTO.class)))
    public Response updatePatientGroup(@PathParam("id") Long id, BuyerGroupUpdateRequest request){
        return Response.ok(new ResponseMessage(ActionMessages.UPDATED.label, buyerGroupService.updatePatientGroupById(id, request) )).build();
    }

    @GET
    @Path("get-buyer-group-by-id/{id}")
    //@RolesAllowed({"ADMIN"})
    @Operation(summary = "Get buyer group by id", description = "Get buyer group by id")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = PatientGroupDTO.class)))
    public Response getPatientGroupById(@PathParam("id") Long id) {
        PatientGroupDTO patientGroup = buyerGroupService.getPatientGroupById(id);
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, patientGroup)).build();
    }

    @DELETE
    @Path("delete-buyer-group/{id}")
    //@RolesAllowed({"ADMIN"})
    @Transactional
    @Operation(summary = "delete buyer group by id ", description = "delete buyer group by id.")
    @APIResponse(description = "Successful", responseCode = "200")
    public Response deleteGroupById(@PathParam("id") Long id){
        return buyerGroupService.deleteGroupById(id);

    }
}






