package org.example.inventory.stock.endpoints;

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
import org.example.inventory.stock.services.StockSupplierService;
import org.example.inventory.stock.services.payloads.requests.StockSupplierRequest;
import org.example.inventory.stock.services.payloads.responses.dtos.StockSupplierDTO;

@Path("/Hospital-item")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Hospital Management Module", description = "Manage hospital stock suppliers")
public class StockSupplierController {

    @Inject
    StockSupplierService stockSupplierService;

    // ✅ CREATE SUPPLIER
    @POST
    @Path("/add-new-stock-supplier")
    @Transactional
    @Operation(summary = "Add new supplier", description = "Register a new stock supplier")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = StockSupplierDTO.class)))
    public Response addNewStockSupplier(StockSupplierRequest request) {
        return stockSupplierService.addNewStockSupplier(request);
    }

    // ✅ UPDATE SUPPLIER
    @PUT
    @Path("/update-stock-supplier/{id}")
    @Transactional
    @Operation(summary = "Update supplier", description = "Update an existing supplier by ID")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = StockSupplierDTO.class)))
    public Response updateStockSupplier(@PathParam("id") Long id, StockSupplierRequest request) {
        return stockSupplierService.updateStockSupplier(id, request);
    }

    // ✅ DELETE SUPPLIER
    @DELETE
    @Path("/delete-stock-supplier/{id}")
    @Transactional
    @Operation(summary = "Delete supplier", description = "Delete a supplier by ID")
    @APIResponse(description = "Successful", responseCode = "200")
    public Response deleteStockSupplier(@PathParam("id") Long id) {
        return stockSupplierService.deleteStockSupplier(id);
    }

    // ✅ GET ALL SUPPLIERS
    @GET
    @Path("/get-all-stock-suppliers")
    @Transactional
    @Operation(summary = "Get all suppliers", description = "Retrieve all registered stock suppliers")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = StockSupplierDTO.class)))
    public Response getAllStockSuppliers() {
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, stockSupplierService.getAllStockSuppliers())).build();
    }

    // ✅ GET SUPPLIER BY ID
    @GET
    @Path("/get-stock-supplier/{id}")
    @Transactional
    @Operation(summary = "Get supplier by ID", description = "Retrieve supplier details by ID")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = StockSupplierDTO.class)))
    public Response getStockSupplierById(@PathParam("id") Long id) {
        return stockSupplierService.getStockSupplierById(id);
    }
}
