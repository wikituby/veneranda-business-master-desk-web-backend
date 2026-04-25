package org.example.cafeteria.inventory.item.endpoints;

import jakarta.annotation.security.RolesAllowed;
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
import org.example.cafeteria.inventory.item.domains.CanteenItem;
import org.example.cafeteria.inventory.item.domains.repositories.CanteenItemRepository;
import org.example.cafeteria.inventory.item.services.CanteenItemService;
import org.example.cafeteria.inventory.item.services.payloads.requests.CanteenItemParametersRequest;
import org.example.cafeteria.inventory.item.services.payloads.requests.CanteenItemRequest;
import org.example.cafeteria.inventory.item.services.payloads.requests.CanteenItemUpdateRequest;
import org.example.cafeteria.inventory.item.services.payloads.responses.CanteenItemDTO;
import org.example.cafeteria.inventory.item.services.payloads.responses.CanteenItemQuantityDto;
import org.example.cafeteria.inventory.item.services.payloads.responses.CanteenItemResponse;

import java.util.List;

@Path("/Cafeteria-inventory-item")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Cafeteria inventory Management Module", description = "Cafeteria inventory Management")
public class CanteenItemController {


    @Inject
    CanteenItemService canteenItemService;

    @Inject
    CanteenItemRepository canteenItemRepository;

    @GET
    @Path("/search-direct")
    //@RolesAllowed({"ADMIN","AGENT"})
    @Transactional
    @Operation(summary = "search", description = "search.")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = CanteenItemResponse.class)))
    public List<CanteenItem> searchItems(
            @QueryParam("category") String category,
            @QueryParam("title") String title) {
        return canteenItemService.searchItems(category, title);
    }


    @POST
    @Path("/add-new-canteenItem")
    //@RolesAllowed({"ADMIN"})
    @Transactional
    @Operation(summary = "add a new canteenItem", description = "add a new canteenItem.")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = CanteenItemResponse.class)))
    public Response addCanteenItem(CanteenItemRequest request) {
        return Response.ok(new ResponseMessage(ActionMessages.SAVED.label,canteenItemService.addCanteenItem(request))).build();
    }



    @POST
    @Path("/add-new-bulk-canteenItem")  // Tip: lowercase "items" for consistency
    //@RolesAllowed({"ADMIN"})
    @Transactional
    @Operation(summary = "Add multiple canteen items", description = "Adds a list of new canteen items.")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = CanteenItemResponse.class)) // You can use an array schema if needed
    )
    public Response addCanteenItems(List<CanteenItemRequest> requests) {
        return Response.ok(
                new ResponseMessage(ActionMessages.SAVED.label, canteenItemService.addCanteenItems(requests))
        ).build();
    }



    @PUT
    @Path("/update-bulk-canteenItem-after-service-order")  // Tip: lowercase "items" for consistency
    @Transactional
    @Operation(summary = "Add multiple items", description = "Adds a list of new items.")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = CanteenItemResponse.class)) // You can use an array schema if needed
    )
    public Response updateCanteenItemsAfterServiceOrder(List<CanteenItemQuantityDto> requests) {
        return Response.ok(
                new ResponseMessage(ActionMessages.SAVED.label, canteenItemService.updateCanteenItemStockAtHandAfterService(requests))
        ).build();
    }


    @GET
    @Path("/get-all-canteenItem")
    @Transactional
    @Operation(summary = "get all canteenItems", description = "get all canteenItems.")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = CanteenItem.class)))
    public Response getCanteenItems() {
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label,canteenItemService.listLatestFirst())).build();
    }

    @GET
    @Path("/get-canteenItem-advanced-search")
    //@RolesAllowed({"ADMIN","USER","AGENT"})
    @Operation(summary = "get canteen items advanced search", description = "get canteen items advanced search.")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = CanteenItem.class)))
    public Response getCanteenItemsAdvancedFilter(@BeanParam CanteenItemParametersRequest request){
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label,canteenItemService.getCanteenItemsAdvancedFilter(request))).build();
    }

    @GET
    @Path("get-canteenItem/{id}")
    @Transactional
    @Operation(summary = "get canteenItem by id", description = "get canteenItem by id")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = CanteenItemResponse.class)))
    public Response getCanteenItemById(@PathParam("id") Long id) {
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label,canteenItemService.getCanteenItemById(id))).build();
    }

    @DELETE
    @Transactional
    @RolesAllowed({"ADMIN"})
    @Operation(summary = "delete all canteenItems", description = "delete all canteenItems.")
    @APIResponse(description = "Successful", responseCode = "200")
    public Response deleteAllItems(){
        canteenItemService.deleteAllCanteenItems();
        return Response.ok(new ResponseMessage(ActionMessages.DELETED.label)).build();

    }


    @GET
    @Path("get-canteenItem-drugs")
    //@RolesAllowed({"ADMIN","CUSTOMER"})
    @Transactional
    @Operation(summary = "get drugs", description = "get drugs")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = CanteenItemDTO.class)))
    public Response getDrugs(){
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label,canteenItemService.listLatestFirst() )).build();
    }



    @PUT
    @Path("update-canteenItem/{id}")
    // @RolesAllowed({"ADMIN","AGENT"})
    @Transactional
    @Operation(summary = "Update canteenItem by Id", description = "Update canteenItem by Id")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = CanteenItemDTO.class)))
    public Response updateCanteenItem(@PathParam("id") Long id, CanteenItemUpdateRequest request){
        return Response.ok(new ResponseMessage(ActionMessages.UPDATED.label,canteenItemService.updateCanteenItemById(id, request) )).build();
    }

    @DELETE
    @Path("delete-canteenItem/{id}")
    //@RolesAllowed({"ADMIN"})
    @Transactional
    @Operation(summary = "delete canteenItem by id ", description = "delete canteenItem by id.")
    @APIResponse(description = "Successful", responseCode = "200")
    public Response deleteCanteenItemById(@PathParam("id") Long id){
        return canteenItemService.deleteCanteenItemById(id);

    }

    @GET
    //@RolesAllowed({"ADMIN"})
    @Transactional
    @Path("file-canteenItem/generate-pdf")
    @Operation(summary = "pdf", description = "pdf download")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    public Response generateAndReturnPdf(@BeanParam CanteenItemParametersRequest request) {
        return canteenItemService.generateAndReturnPdf(request);
    }


}

