package org.example.inventory.item.endpoints;

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
import org.example.inventory.item.services.LowestPackageService;
import org.example.inventory.item.services.RouteOfAdminService;
import org.example.inventory.item.services.payloads.requests.LowestPackageRequest;
import org.example.inventory.item.services.payloads.requests.RouteOfAdminRequest;
import org.example.inventory.item.services.payloads.responses.basicResponses.LowestPackageResponse;
import org.example.inventory.item.services.payloads.responses.basicResponses.RouteOfAdminResponse;
import org.example.inventory.item.services.payloads.responses.dtos.LowestPackageDTO;

@Path("/Hospital-item")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Hospital Management Module", description = "Hospital Management")

public class LowestPackageController {

    @Inject
    LowestPackageService lowestPackageService;

    @POST
    @Path("/add-new-lowest-package")
    //@RolesAllowed({"ADMIN"})
    @Transactional
    @Operation(summary = "add a new lowest package", description = "add a new lowest package")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = LowestPackageResponse.class)))
    public Response addNewLowestPackage(LowestPackageRequest request) {
        return Response.ok(new ResponseMessage(ActionMessages.SAVED.label, lowestPackageService.addNewLowestPackage(request))).build();
    }

    @GET
    @Path("/get-all-lowest-packages")
    @Operation(summary = "Get all lowest packages", description = "Retrieve a list of all lowest packages")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = LowestPackageDTO.class, type = SchemaType.ARRAY)))
    public Response getAllLowestPackages() {
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, lowestPackageService.getAllLowestPackages())).build();
    }

    @PUT
    @Path("/update-lowest-package/{id}")
    @Transactional
    @Operation(summary = "Update lowest package", description = "Update an existing lowest package by ID")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = LowestPackageResponse.class)))
    public Response updateLowestPackage(@PathParam("id") Long id, LowestPackageRequest request) {
        return lowestPackageService.updateLowestPackage(id, request);
    }

    @DELETE
    @Path("/delete-lowest-package/{id}")
    @Transactional
    @Operation(summary = "Delete lowest package", description = "Delete a lowest package by ID")
    @APIResponse(description = "Successful", responseCode = "200")
    public Response deleteLowestPackage(@PathParam("id") Long id) {
        return lowestPackageService.deleteLowestPackage(id);
    }
}
