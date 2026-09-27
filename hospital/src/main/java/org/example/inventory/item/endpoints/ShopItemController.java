package org.example.inventory.item.endpoints;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.inventory.item.domain.Item;
import org.example.inventory.item.domain.repositories.ItemRepository;
import org.example.inventory.item.services.ItemDosageRuleService;
import org.example.inventory.item.services.ItemUnitSellingModelService;
import org.example.inventory.item.services.ShopItemService;
import org.example.inventory.item.services.payloads.requests.ShopItemParametersRequest;
import org.example.inventory.item.services.payloads.requests.ShopItemReceiveRequest;
import org.example.inventory.item.services.payloads.requests.ShopItemRequest;
import org.example.inventory.item.services.payloads.requests.ShopItemUpdateRequest;
import org.example.inventory.item.services.payloads.responses.ItemDTO;
import org.example.inventory.item.services.payloads.responses.ItemQuantityDto;
import org.example.inventory.item.services.payloads.responses.ShopItemResponse;
import org.example.inventory.item.services.payloads.responses.dtos.ItemDosageRuleDTO;
import org.example.inventory.item.services.payloads.responses.dtos.ItemUnitSellingModelDTO;
import org.example.inventory.stock.services.payloads.requests.StockDosageRuleRequest;
import org.example.inventory.stock.services.payloads.requests.UnitSellingModelRequest;

import java.math.BigDecimal;
import java.util.List;

@Path("/shop-item")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Hospital Management Module", description = "Hospital Management")
public class ShopItemController {


    @Inject
    ShopItemService shopItemService;

    @Inject
    ItemRepository shopItemRepository;

    @Inject
    ItemUnitSellingModelService itemUnitSellingModelService;

    @Inject
    ItemDosageRuleService itemDosageRuleService;

    @GET
    @Path("/search")
    //@RolesAllowed({"ADMIN","AGENT"})
    @Transactional
    @Operation(summary = "search", description = "search.")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ShopItemResponse.class)))
    public List<Item> searchItems(
            @QueryParam("category") String category,
            @QueryParam("title") String title) {
        return shopItemService.searchItems(category, title);
    }


    @POST
    @Path("/add-new-Items")
    //@RolesAllowed({"ADMIN"})
    @Transactional
    @Operation(summary = "add a new shopItem", description = "add a new shopItem.")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ShopItemResponse.class)))
    public Response addShopItem(ShopItemRequest request) {
        return Response.ok(new ResponseMessage(ActionMessages.SAVED.label,shopItemService.addShopItem(request))).build();
    }


    @POST
    @Path("/add-new-bulk-items")  // Tip: lowercase "items" for consistency
    //@RolesAllowed({"ADMIN"})
    @Transactional
    @Operation(summary = "Add multiple shop items",description = "Adds a list of new shop items.")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ShopItemResponse.class))// You can use an array schema if needed
    )
    public Response addShopItems(List<ShopItemRequest> requests) {
        return Response.ok(
                new ResponseMessage(ActionMessages.SAVED.label, shopItemService.addShopItems(requests))
        ).build();
    }


    @PUT
    @Path("/update-bulk-items-after-service-order")  // Tip: lowercase "items" for consistency
    @Transactional
    @Operation(summary = "Add multiple items", description = "Adds a list of new items.")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ShopItemResponse.class)) // You can use an array schema if needed
    )
    public Response updateItemsAfterServiceOrder(List<ItemQuantityDto> requests) {
        return Response.ok(
                new ResponseMessage(ActionMessages.SAVED.label, shopItemService.updateItemStockAtHandAfterService(requests))
        ).build();
    }


    @GET
    @Path("/get-all-Items")
    @Transactional
    @Operation(summary = "get all shopItems", description = "get all shopItems.")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = Item.class)))
    public Response getShopItems() {
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label,shopItemService.listLatestFirst())).build();
    }

    @GET
    @Path("/get-all-Items-stock-below-reorder")
    @Transactional
    @Operation(summary = "get-all-Items-stock-below-reorder", description = "get-all-Items-stock-below-reorder")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = Item.class)))
    public Response getShopItemsWithStockBelowReorder() {
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label,shopItemService.getAllItemsWithStockAtHandBelowReOrderLevels())).build();
    }

    @GET
    @Path("/get-Items-advanced-search")
    //@RolesAllowed({"ADMIN","USER","AGENT"})
    @Operation(summary = "get shop items advanced search", description = "get shop items advanced search.")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = Item.class)))
    public Response getShopItemsAdvancedFilter(@BeanParam ShopItemParametersRequest request){
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label,shopItemService.getShopItemsAdvancedFilter(request))).build();
    }

    @GET
    @Path("{id}")
    @Transactional
    @Operation(summary = "get shopItem by id", description = "get shopItem by id")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ShopItemResponse.class)))
    public Response getShopItemById(@PathParam("id") Long id) {
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label,shopItemService.getShopItemById(id))).build();
    }

    @DELETE
    @Transactional
    @RolesAllowed({"ADMIN"})
    @Operation(summary = "delete all shopItems", description = "delete all shopItems.")
    @APIResponse(description = "Successful", responseCode = "200")
    public Response deleteAllItems(){
        shopItemService.deleteAllShopItems();
        return Response.ok(new ResponseMessage(ActionMessages.DELETED.label)).build();

    }


    @GET
    @Path("get-drugs")
    //@RolesAllowed({"ADMIN","CUSTOMER"})
    @Transactional
    @Operation(summary = "get drugs", description = "get drugs")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ItemDTO.class)))
    public Response getDrugs(){
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label,shopItemService.listLatestFirst() )).build();
    }



    @POST
    @Path("/receive-stock")
    @Transactional
    @Operation(summary = "Receive stock for a shop item", description = "Increments stock at hand for a shop item.")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ItemDTO.class)))
    public Response receiveStock(ShopItemReceiveRequest request) {
        return Response.ok(new ResponseMessage(ActionMessages.SAVED.label, shopItemService.receiveStock(request))).build();
    }

    @PUT
    @Path("update-item/{id}")
   // @RolesAllowed({"ADMIN","AGENT"})
    @Transactional
    @Operation(summary = "Update shopItem by Id", description = "Update shopItem by Id")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ItemDTO.class)))
    public Response updateShopItem(@PathParam("id") Long id, ShopItemUpdateRequest request){
        return Response.ok(new ResponseMessage(ActionMessages.UPDATED.label,shopItemService.updateShopItemById(id, request) )).build();
    }

    @PUT
    @Path("/update-shelfNumbers")
// @RolesAllowed({"ADMIN","AGENT"}) // uncomment if needed
    @Transactional
    @Operation(summary = "Assign shelf numbers to unnumbered items", description = "Finds the highest existing shelf number, then assigns sequential shelf numbers to all items without one.")
    @APIResponse(description = "Shelf numbers updated successfully",responseCode = "200")
    public Response updateShelfNumbers() {
        shopItemService.assignShelfNumbersToUnnumberedItems();
        return Response.ok(new ResponseMessage(ActionMessages.UPDATED.label)).build();
    }

    @DELETE
    @Path("delete-item/{id}")
    //@RolesAllowed({"ADMIN"})
    @Transactional
    @Operation(summary = "delete shopItem by id ", description = "delete shopItem by id.")
    @APIResponse(description = "Successful", responseCode = "200")
    public Response deleteItemById(@PathParam("id") Long id){
        return shopItemService.deleteShopItemById(id);

    }

    @GET
    //@RolesAllowed({"ADMIN"})
    @Transactional
    @Path("/generate-pdf")
    @Operation(summary = "pdf", description = "pdf download")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    public Response generateAndReturnPdf(@BeanParam ShopItemParametersRequest request) {
        return shopItemService.generateAndReturnPdf(request);
    }

    @POST
    @Path("/{itemId}/dosage-rules")
    @Transactional
    @Operation(summary = "Create Item Dosage Rule", description = "Creates a standard dosage rule linked to a shop item.")
    @APIResponse(description = "Dosage rule created successfully.", responseCode = "200", content = @Content(schema = @Schema(implementation = ItemDosageRuleDTO.class)))
    public Response createDosageRule(@PathParam("itemId") Long itemId, StockDosageRuleRequest request) {
        return itemDosageRuleService.createDosageRule(itemId, request);
    }

    @GET
    @Path("/{itemId}/dosage-rules")
    @Transactional
    @Operation(summary = "List Item Dosage Rules", description = "Lists dosage rules linked to a shop item.")
    @APIResponse(description = "Dosage rules fetched successfully.", responseCode = "200", content = @Content(schema = @Schema(implementation = ItemDosageRuleDTO.class)))
    public Response getDosageRulesByItem(
            @PathParam("itemId") Long itemId,
            @QueryParam("activeOnly") @DefaultValue("true") boolean activeOnly
    ) {
        var rules = activeOnly
                ? itemDosageRuleService.getActiveDosageRulesByItem(itemId)
                : itemDosageRuleService.getDosageRulesByItem(itemId);
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, rules)).build();
    }

    @PUT
    @Path("/dosage-rules/{ruleId}")
    @Transactional
    @Operation(summary = "Update Item Dosage Rule", description = "Updates an existing shop item dosage rule.")
    @APIResponse(description = "Dosage rule updated successfully.", responseCode = "200", content = @Content(schema = @Schema(implementation = ItemDosageRuleDTO.class)))
    public Response updateDosageRule(@PathParam("ruleId") Long ruleId, StockDosageRuleRequest request) {
        return itemDosageRuleService.updateDosageRule(ruleId, request);
    }

    @DELETE
    @Path("/dosage-rules/{ruleId}")
    @Transactional
    @Operation(summary = "Delete Item Dosage Rule", description = "Deletes a shop item dosage rule.")
    @APIResponse(description = "Dosage rule deleted successfully.", responseCode = "200")
    public Response deleteDosageRule(@PathParam("ruleId") Long ruleId) {
        return itemDosageRuleService.deleteDosageRule(ruleId);
    }

    @POST
    @Path("/{itemId}/unit-selling-models")
    @Transactional
    @Operation(summary = "Create Item Unit Selling Model", description = "Creates a unit selling model linked to a shop item.")
    @APIResponse(description = "Unit selling model created successfully.", responseCode = "200", content = @Content(schema = @Schema(implementation = ItemUnitSellingModelDTO.class)))
    public Response createUnitSellingModel(
            @PathParam("itemId") Long itemId,
            UnitSellingModelRequest request
    ) {
        return itemUnitSellingModelService.createModel(itemId, request);
    }

    @GET
    @Path("/{itemId}/unit-selling-models")
    @Transactional
    @Operation(summary = "List Item Unit Selling Models", description = "Lists unit selling models linked to a shop item.")
    @APIResponse(description = "Unit selling models fetched successfully.", responseCode = "200", content = @Content(schema = @Schema(implementation = ItemUnitSellingModelDTO.class)))
    public Response getUnitSellingModelsByItem(
            @PathParam("itemId") Long itemId,
            @QueryParam("unitCostPrice") BigDecimal unitCostPrice
    ) {
        var models = itemUnitSellingModelService.listByItem(itemId, unitCostPrice);
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, models)).build();
    }

    @PUT
    @Path("/unit-selling-models/{modelId}")
    @Transactional
    @Operation(summary = "Update Item Unit Selling Model", description = "Updates an existing shop item unit selling model.")
    @APIResponse(description = "Unit selling model updated successfully.", responseCode = "200", content = @Content(schema = @Schema(implementation = ItemUnitSellingModelDTO.class)))
    public Response updateUnitSellingModel(
            @PathParam("modelId") Long modelId,
            UnitSellingModelRequest request
    ) {
        return itemUnitSellingModelService.updateModel(modelId, request);
    }

    @DELETE
    @Path("/unit-selling-models/{modelId}")
    @Transactional
    @Operation(summary = "Delete Item Unit Selling Model", description = "Deletes a shop item unit selling model.")
    @APIResponse(description = "Unit selling model deleted successfully.", responseCode = "200")
    public Response deleteUnitSellingModel(@PathParam("modelId") Long modelId) {
        return itemUnitSellingModelService.deleteModel(modelId);
    }

}


