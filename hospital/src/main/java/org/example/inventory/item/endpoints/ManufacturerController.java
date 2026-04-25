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
import org.example.inventory.item.services.ManufacturerService;
import org.example.inventory.item.services.payloads.requests.ManufacturerRequest;
import org.example.inventory.item.services.payloads.responses.dtos.ManufacturerDTO;

@Path("/Hospital-item")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Hospital Management Module", description = "Manage hospital manufacturers")
public class ManufacturerController {

    @Inject
    ManufacturerService manufacturerService;

    // ✅ CREATE MANUFACTURER
    @POST
    @Path("/add-new-manufacturer")
    @Transactional
    @Operation(summary = "Add new manufacturer", description = "Register a new manufacturer")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ManufacturerDTO.class)))
    public Response addNewManufacturer(ManufacturerRequest request) {
        return manufacturerService.addNewManufacturer(request);
    }

    // ✅ UPDATE MANUFACTURER
    @PUT
    @Path("/update-manufacturer/{id}")
    @Transactional
    @Operation(summary = "Update manufacturer", description = "Update an existing manufacturer by ID")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ManufacturerDTO.class)))
    public Response updateManufacturer(@PathParam("id") Long id, ManufacturerRequest request) {
        return manufacturerService.updateManufacturer(id, request);
    }

    // ✅ DELETE MANUFACTURER
    @DELETE
    @Path("/delete-manufacturer/{id}")
    @Transactional
    @Operation(summary = "Delete manufacturer", description = "Delete a manufacturer by ID")
    @APIResponse(description = "Successful", responseCode = "200")
    public Response deleteManufacturer(@PathParam("id") Long id) {
        return manufacturerService.deleteManufacturer(id);
    }

    // ✅ GET ALL MANUFACTURERS
    @GET
    @Path("/get-all-manufacturers")
    @Transactional
    @Operation(summary = "Get all manufacturers", description = "Retrieve all registered manufacturers")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ManufacturerDTO.class)))
    public Response getAllManufacturers() {
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, manufacturerService.getAllManufacturers())).build();
    }

    // ✅ GET MANUFACTURER BY ID
    @GET
    @Path("/get-manufacturer/{id}")
    @Transactional
    @Operation(summary = "Get manufacturer by ID", description = "Retrieve manufacturer details by ID")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ManufacturerDTO.class)))
    public Response getManufacturerById(@PathParam("id") Long id) {
        return manufacturerService.getManufacturerById(id);
    }
}













