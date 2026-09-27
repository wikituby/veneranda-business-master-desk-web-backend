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
import org.example.inventory.stock.services.StockDosageRuleService;
import org.example.inventory.stock.services.StockItemService;
import org.example.inventory.stock.services.UnitSellingModelService;
import org.example.inventory.stock.services.payloads.requests.StockDosageRuleRequest;
import org.example.inventory.stock.services.payloads.requests.StockItemRequest;
import org.example.inventory.stock.services.payloads.requests.UnitSellingModelRequest;
import org.example.inventory.stock.services.payloads.responses.basicResponses.StockItemResponse;
import org.example.inventory.stock.services.payloads.responses.dtos.StockDosageRuleDTO;
import org.example.inventory.stock.services.payloads.responses.dtos.StockItemDTO;
import org.example.inventory.stock.services.payloads.responses.dtos.UnitSellingModelDTO;

import java.math.BigDecimal;
import java.util.List;

@Path("/stock-item")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Inventory Management - Stock-Item", description = "Endpoints for managing stock items in the inventory.")
public class StockItemController {

    @Inject
    StockItemService stockItemService;

    @Inject
    StockDosageRuleService stockDosageRuleService;

    @Inject
    UnitSellingModelService unitSellingModelService;

    @POST
    @Path("/create-or-update-stock-item")
    @Transactional
    @Operation(summary = "Create or Update Stock Item", description = "Creates a new StockItem or updates an existing one based on name and brand.")
    @APIResponse(description = "Stock item successfully created or updated.",responseCode = "200",content = @Content(schema = @Schema(implementation = StockItemResponse.class)))
    public Response createOrUpdateStockItem(StockItemRequest request) {
        return stockItemService.createOrUpdateStockItem(request);
    }

    @GET
    @Path("/get-all-stock-items")
    @Transactional
    @Operation(summary = "Get all Stock Items", description = "Retrieves all stock items sorted by descending ID.")
    @APIResponse(description = "Successful retrieval of all stock items.",responseCode = "200",content = @Content(schema = @Schema(implementation = StockItemDTO.class)) )
    public Response getAllStockItems() {
        var items = stockItemService.getAllStockItems();
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, items)).build();
    }

    @GET
    @Path("/get-stock-item-by-id/{id}")
    @Transactional
    @Operation(summary = "Get Stock Item by ID", description = "Retrieves a single stock item using its ID.")
    @APIResponse(
            description = "Stock item found successfully.",
            responseCode = "200",
            content = @Content(schema = @Schema(implementation = StockItemDTO.class))
    )
    public Response getStockItemById(@PathParam("id") Long id) {
        return stockItemService.getStockItemById(id);
    }

    @PUT
    @Path("/update/{id}")
    @Transactional
    @Operation(summary = "Update Stock Item by ID", description = "Updates an existing stock item by its ID.")
    @APIResponse(
            description = "Stock item updated successfully.",
            responseCode = "200",
            content = @Content(schema = @Schema(implementation = StockItemDTO.class))
    )
    public Response updateStockItem(@PathParam("id") Long id, StockItemRequest request) {
        return stockItemService.updateStockItem(id, request);
    }

    @DELETE
    @Path("/delete/{id}")
    @Transactional
    @Operation(summary = "Delete Stock Item", description = "Deletes a stock item by its ID.")
    @APIResponse(
            description = "Stock item deleted successfully.",
            responseCode = "200"
    )
    public Response deleteStockItem(@PathParam("id") Long id) {
        return stockItemService.deleteStockItem(id);
    }

    // (Optional) If you want a filter endpoint by brandId or category
    @GET
    @Path("/filter")
    @Transactional
    @Operation(summary = "Filter Stock Items", description = "Filters stock items by optional brand or category.")
    @APIResponse(
            description = "Filtered stock items list.",
            responseCode = "200",
            content = @Content(schema = @Schema(implementation = StockItemDTO.class))
    )
    public Response filterStockItems(
            @QueryParam("brandId") Long brandId,
            @QueryParam("categoryId") Long categoryId
    ) {
        // Example filter — you can extend this logic in your service
        List<StockItemDTO> filteredItems = stockItemService.getAllStockItems().stream()
                .filter(i -> (brandId == null || i.brandId.equals(brandId)) &&
                        (categoryId == null || i.itemCategoryId.equals(categoryId)))
                .toList();

        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, filteredItems)).build();
    }

    @PUT
    @Path("/update-missing-names-titles")
    @Transactional
    @Operation(summary = "Update Missing Names and Titles", description = "Updates all existing StockItems that have IDs but are missing names/titles by fetching them from their respective repositories.")
    @APIResponse(
            description = "Stock items updated successfully with missing names/titles.",
            responseCode = "200"
    )
    public Response updateMissingNamesAndTitles() {
        return stockItemService.updateMissingNamesAndTitles();
    }

    @POST
    @Path("/{stockItemId}/dosage-rules")
    @Transactional
    @Operation(summary = "Create Dosage Rule", description = "Creates a standard dosage rule linked to a stock item/drug.")
    @APIResponse(description = "Dosage rule created successfully.", responseCode = "200", content = @Content(schema = @Schema(implementation = StockDosageRuleDTO.class)))
    public Response createDosageRule(@PathParam("stockItemId") Long stockItemId, StockDosageRuleRequest request) {
        return stockDosageRuleService.createDosageRule(stockItemId, request);
    }

    @GET
    @Path("/{stockItemId}/dosage-rules")
    @Transactional
    @Operation(summary = "List Dosage Rules", description = "Lists dosage rules linked to a stock item/drug.")
    @APIResponse(description = "Dosage rules fetched successfully.", responseCode = "200", content = @Content(schema = @Schema(implementation = StockDosageRuleDTO.class)))
    public Response getDosageRulesByStockItem(
            @PathParam("stockItemId") Long stockItemId,
            @QueryParam("activeOnly") @DefaultValue("true") boolean activeOnly
    ) {
        var rules = activeOnly
                ? stockDosageRuleService.getActiveDosageRulesByStockItem(stockItemId)
                : stockDosageRuleService.getDosageRulesByStockItem(stockItemId);
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, rules)).build();
    }

    @PUT
    @Path("/dosage-rules/{ruleId}")
    @Transactional
    @Operation(summary = "Update Dosage Rule", description = "Updates an existing dosage rule.")
    @APIResponse(description = "Dosage rule updated successfully.", responseCode = "200", content = @Content(schema = @Schema(implementation = StockDosageRuleDTO.class)))
    public Response updateDosageRule(@PathParam("ruleId") Long ruleId, StockDosageRuleRequest request) {
        return stockDosageRuleService.updateDosageRule(ruleId, request);
    }

    @DELETE
    @Path("/dosage-rules/{ruleId}")
    @Transactional
    @Operation(summary = "Delete Dosage Rule", description = "Deletes a dosage rule.")
    @APIResponse(description = "Dosage rule deleted successfully.", responseCode = "200")
    public Response deleteDosageRule(@PathParam("ruleId") Long ruleId) {
        return stockDosageRuleService.deleteDosageRule(ruleId);
    }

    @POST
    @Path("/{stockItemId}/unit-selling-models")
    @Transactional
    @Operation(summary = "Create Unit Selling Model", description = "Creates a unit selling model linked to a stock item.")
    @APIResponse(description = "Unit selling model created successfully.", responseCode = "200", content = @Content(schema = @Schema(implementation = UnitSellingModelDTO.class)))
    public Response createUnitSellingModel(
            @PathParam("stockItemId") Long stockItemId,
            UnitSellingModelRequest request
    ) {
        return unitSellingModelService.createModel(stockItemId, request);
    }

    @GET
    @Path("/{stockItemId}/unit-selling-models")
    @Transactional
    @Operation(summary = "List Unit Selling Models", description = "Lists unit selling models linked to a stock item.")
    @APIResponse(description = "Unit selling models fetched successfully.", responseCode = "200", content = @Content(schema = @Schema(implementation = UnitSellingModelDTO.class)))
    public Response getUnitSellingModelsByStockItem(
            @PathParam("stockItemId") Long stockItemId,
            @QueryParam("unitCostPrice") BigDecimal unitCostPrice
    ) {
        var models = unitSellingModelService.listByStockItem(stockItemId, unitCostPrice);
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, models)).build();
    }

    @PUT
    @Path("/unit-selling-models/{modelId}")
    @Transactional
    @Operation(summary = "Update Unit Selling Model", description = "Updates an existing unit selling model.")
    @APIResponse(description = "Unit selling model updated successfully.", responseCode = "200", content = @Content(schema = @Schema(implementation = UnitSellingModelDTO.class)))
    public Response updateUnitSellingModel(
            @PathParam("modelId") Long modelId,
            UnitSellingModelRequest request
    ) {
        return unitSellingModelService.updateModel(modelId, request);
    }

    @DELETE
    @Path("/unit-selling-models/{modelId}")
    @Transactional
    @Operation(summary = "Delete Unit Selling Model", description = "Deletes a unit selling model.")
    @APIResponse(description = "Unit selling model deleted successfully.", responseCode = "200")
    public Response deleteUnitSellingModel(@PathParam("modelId") Long modelId) {
        return unitSellingModelService.deleteModel(modelId);
    }
}
