package org.example.inventory.item.endpoints;

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
import org.example.inventory.item.services.FormulationService;
import org.example.inventory.item.services.payloads.requests.FormulationRequest;
import org.example.inventory.item.services.payloads.responses.basicResponses.FormulationResponse;
import org.example.inventory.item.services.payloads.responses.dtos.FormulationDTO;

import java.util.List;

@Path("/Hospital-item")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Hospital Management Module", description = "Hospital Management")
public class FormulationController {

    @Inject
    FormulationService formulationService;

    @POST
    @Path("/add-new-formulation")
    @Transactional
    @Operation(summary = "Add a new formulation", description = "Add a new formulation for product variants")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = FormulationResponse.class)))
    public Response addFormulation(FormulationRequest request) {
        return formulationService.createFormulation(request);
    }


    @GET
    @Path("/all-formulations")
    @Operation(summary = "List all formulations", description = "Get all existing formulations")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = FormulationDTO.class)))
    public Response getAllFormulations() {
        List<FormulationDTO> dtos = formulationService.getAllFormulations();
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, dtos)).build();
    }

    @GET
    @Path("/formulation/{id}")
    @Operation(summary = "Get formulation by ID", description = "Fetch a formulation by its ID")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = FormulationDTO.class)))
    public Response getFormulationById(@PathParam("id") Long id) {
        FormulationDTO dto = formulationService.getFormulationById(id);
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, dto)).build();
    }

    @PUT
    @Path("/update-formulation/{id}")
    @Transactional
    //@RolesAllowed({"ADMIN"})
    @Operation(summary = "Update formulation", description = "Update an existing formulation by ID")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = FormulationDTO.class)))
    public Response updateFormulation(@PathParam("id") Long id, FormulationRequest request) {
        return formulationService.updateFormulation(id, request);
    }

    @DELETE
    @Path("/delete-formulation/{id}")
    @Transactional
    //@RolesAllowed({"ADMIN"})
    @Operation(summary = "Delete formulation", description = "Delete a formulation by ID")
    @APIResponse(description = "Successful", responseCode = "200")
    public Response deleteFormulation(@PathParam("id") Long id) {
        return formulationService.deleteFormulation(id);
    }
}
