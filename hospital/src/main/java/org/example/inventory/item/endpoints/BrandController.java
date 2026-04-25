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
import org.example.inventory.item.domain.Brand;
import org.example.inventory.item.services.BrandService;
import org.example.inventory.item.services.payloads.requests.BrandRequest;
import org.example.inventory.item.services.payloads.responses.dtos.BrandDTO;

import java.util.List;
import java.util.stream.Collectors;

@Path("/Hospital-item")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Hospital Management Module", description = "Hospital Management")
public class BrandController {

    @Inject
    BrandService brandService;

    // CREATE
    @POST
    @Path("/add-new-brand")
    @Transactional
    @Operation(summary = "Add new brand", description = "Register a new brand")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = BrandDTO.class)))
    public Response addBrand(BrandRequest request) {
        ResponseMessage resp = brandService.addBrand(request);
        return Response.ok(resp).build();
    }

    // UPDATE
    @PUT
    @Path("/update-brand/{id}")
    @Transactional
    @Operation(summary = "Update brand", description = "Update an existing brand by ID")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = BrandDTO.class)))
    public Response updateBrand(@PathParam("id") Long id, BrandRequest request) {
        ResponseMessage resp = brandService.updateBrand(id, request);
        return Response.ok(resp).build();
    }

    // DELETE
    @DELETE
    @Path("/delete-brand/{id}")
    @Transactional
    @Operation(summary = "Delete brand", description = "Delete a brand by ID")
    @APIResponse(description = "Successful", responseCode = "200")
    public Response deleteBrand(@PathParam("id") Long id) {
        ResponseMessage resp = brandService.deleteBrand(id);
        return Response.ok(resp).build();
    }

    // GET ALL
    @GET
    @Path("/get-all-brands")
    @Operation(summary = "Get all brands", description = "Retrieve all registered brands")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = BrandDTO.class)))
    public Response getAllBrands() {
        List<Brand> brands = brandService.getAllBrands();
        List<BrandDTO> dtos = brands.stream().map(BrandDTO::new).collect(Collectors.toList());
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, dtos)).build();
    }

    // GET BY ID
    @GET
    @Path("/get-brand/{id}")
    @Operation(summary = "Get brand by ID", description = "Retrieve brand details by ID")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = BrandDTO.class)))
    public Response getBrandById(@PathParam("id") Long id) {
        Brand brand = brandService.getBrandById(id);
        if (brand == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Brand not found for ID: " + id))
                    .build();
        }
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, new BrandDTO(brand))).build();
    }
}
