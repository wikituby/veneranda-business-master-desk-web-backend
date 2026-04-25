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
import org.example.inventory.item.services.RouteOfAdminService;
import org.example.inventory.item.services.payloads.requests.RouteOfAdminRequest;
import org.example.inventory.item.services.payloads.responses.basicResponses.RouteOfAdminResponse;
import org.example.inventory.item.services.payloads.responses.dtos.RouteOfAdminDTO;

@Path("/Hospital-item")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Hospital Management Module", description = "Hospital Management")

public class RouteOfAdminController {

    @Inject
    RouteOfAdminService routeOfAdminService;

    @POST
    @Path("/add-new-route-of-admin")
    //@RolesAllowed({"ADMIN"})
    @Transactional
    @Operation(summary = "add a new route of admin", description = "add a new route of admin")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = RouteOfAdminResponse.class)))
    public Response addNewRouteOfAdmin(RouteOfAdminRequest request) {
        return Response.ok(new ResponseMessage(ActionMessages.SAVED.label, routeOfAdminService.addNewRouteOfAdmin(request))).build();
    }

    @GET
    @Path("/get-all-route-of-admin")
    @Transactional
    //@RolesAllowed({"ADMIN"})
    @Operation(summary = "Get all route of admin", description = "Retrieve a list of all route of admin")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = RouteOfAdminDTO.class, type = SchemaType.ARRAY)))
    public Response getAllRouteOfAdmin() {
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, routeOfAdminService.getAllRoutesOfAdmin())).build();
    }

    @GET
    @Path("/get-route-of-admin/{id}")
    @Transactional
    //@RolesAllowed({"ADMIN"})
    @Operation(summary = "Get route of admin by ID", description = "Retrieve route of admin details by ID")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = RouteOfAdminDTO.class)))
    public Response getRouteOfAdminById(@PathParam("id") Long id) {
        return routeOfAdminService.getRouteOfAdminById(id);
    }

    @PUT
    @Path("/update-route-of-admin/{id}")
    @Transactional
    //@RolesAllowed({"ADMIN"})
    @Operation(summary = "Update route of admin", description = "Update an existing route of admin by ID")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = RouteOfAdminDTO.class)))
    public Response updateRouteOfAdmin(@PathParam("id") Long id, RouteOfAdminRequest request) {
        return routeOfAdminService.updateRouteOfAdmin(id, request);
    }

    @DELETE
    @Path("/delete-route-of-admin/{id}")
    @Transactional
    //@RolesAllowed({"ADMIN"})
    @Operation(summary = "Delete route of admin", description = "Delete a route of admin by ID")
    @APIResponse(description = "Successful", responseCode = "200")
    public Response deleteRouteOfAdmin(@PathParam("id") Long id) {
        return routeOfAdminService.deleteRouteOfAdmin(id);
    }
}
