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
import org.example.inventory.item.services.CompositionService;
import org.example.inventory.item.services.ProductVariantService;
import org.example.inventory.item.services.payloads.requests.CompositionRequest;
import org.example.inventory.item.services.payloads.requests.ProductVariantRequest;
import org.example.inventory.item.services.payloads.responses.basicResponses.CompositionResponse;
import org.example.inventory.item.services.payloads.responses.basicResponses.ProductVariantResponse;
import org.example.inventory.item.services.payloads.responses.dtos.ProductVariantDTO;

@Path("/Hospital-item")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Hospital Management Module", description = "Hospital Management")

public class ProductVariantController {

    @Inject
    ProductVariantService productVariantService;

    @POST
    @Path("/add-new-variant")
    //@RolesAllowed({"ADMIN"})
    @Transactional
    @Operation(summary = "add a new variant", description = "add a new variant.")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ProductVariantResponse.class)))
    public Response addNewVariant(ProductVariantRequest request) {
        return Response.ok(new ResponseMessage(ActionMessages.SAVED.label, productVariantService.createVariant(request))).build();
    }

    @GET
    @Path("/get-all-variants")
    @Transactional
    //@RolesAllowed({"ADMIN"})
    @Operation(summary = "Get all product variants", description = "Retrieve a list of all product variants")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ProductVariantDTO.class, type = SchemaType.ARRAY)))
    public Response getAllVariants() {
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, productVariantService.getAllVariants())).build();
    }

    @GET
    @Path("/get-variant/{id}")
    @Transactional
    //@RolesAllowed({"ADMIN"})
    @Operation(summary = "Get product variant by ID", description = "Retrieve product variant details by ID")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ProductVariantDTO.class)))
    public Response getVariantById(@PathParam("id") Long id) {
        return productVariantService.getVariantById(id);
    }

    @PUT
    @Path("/update-variant/{id}")
    @Transactional
    //@RolesAllowed({"ADMIN"})
    @Operation(summary = "Update product variant", description = "Update an existing product variant by ID")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ProductVariantDTO.class)))
    public Response updateVariant(@PathParam("id") Long id, ProductVariantRequest request) {
        return productVariantService.updateVariant(id, request);
    }

    @DELETE
    @Path("/delete-variant/{id}")
    @Transactional
    //@RolesAllowed({"ADMIN"})
    @Operation(summary = "Delete product variant", description = "Delete a product variant by ID")
    @APIResponse(description = "Successful", responseCode = "200")
    public Response deleteVariant(@PathParam("id") Long id) {
        return productVariantService.deleteVariant(id);
    }
}
