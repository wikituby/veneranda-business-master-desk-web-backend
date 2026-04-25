package org.example.cart.endpoints;

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
import org.example.cart.services.payloads.requests.ShoppingCartRequest;
import org.example.cart.services.payloads.responses.ShoppingCartResponse;
import org.example.cart.services.ShoppingCartService;
import org.example.cart.domains.ShoppingCart;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;

@Path("/shop-cart")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Tag(name = "Shopping Cart Module", description = "Shopping Cart")
    public class ShoppingCartController {

        @Inject
        ShoppingCartService shoppingCartService;


        @POST
        @Path("/add-to-cart")
        //@RolesAllowed({"ADMIN"})
        @Transactional
        @Operation(summary = "add a new shopItem to cart", description = "add a new shopItem to cart.")
        @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ShoppingCartResponse.class)))
        public Response addItemToShoppingCart(ShoppingCartRequest request) {
            return Response.ok(new ResponseMessage(ActionMessages.SAVED.label,shoppingCartService.addToCart(request))).build();
        }

        @GET
        @Path("/get-cart-items/{id}")
        @Transactional
        @Operation(summary = "get all cart items", description = "get all cart items.")
        @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ShoppingCart.class)))
        public Response getCartItems(@PathParam("id") Long userId) {
            return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label,shoppingCartService.getCart(userId))).build();
        }

        @GET
        @Path("/get-All-cart-items/{id}")
        @Transactional
        @Operation(summary = "get all cart items", description = "get all cart items.")
        @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ShoppingCartResponse.class)))
        public Response getAllCartItems(@PathParam("id") Long userId) {
            return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label,shoppingCartService.getAllCart(userId))).build();
        }

        /*@GET
        @Path("/search")
        @RolesAllowed({"ADMIN","AGENT"})
        @Transactional
        @Operation(summary = "search", description = "search.")
        @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ShopItemResponse.class)))
        public List<ShopItem> searchItems(
                @QueryParam("category") String category,
                @QueryParam("title") String title) {
            return shopItemService.searchItems(category, title);
        }


        @POST
        @Path("/add-new-Items to cart")
        @RolesAllowed({"ADMIN"})
        @Transactional
        @Operation(summary = "add a new shopItem to cart", description = "add a new shopItem to cart.")
        @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ShopItemResponse.class)))
        public Response addShopItem(ShopItemRequest request) {
            return Response.ok(new ResponseMessage(ActionMessages.SAVED.label,shopItemService.addShopItem(request))).build();
        }

        @GET
        @Path("/get-all-Items")
        @Transactional
        @Operation(summary = "get all shopItems", description = "get all shopItems.")
        @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ShopItem.class)))
        public Response getShopItems() {
            return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label,shopItemService.listLatestFirst())).build();
        }

        @GET
        @Path("/get-Items-advanced-search")
        //@RolesAllowed({"ADMIN","USER","AGENT"})
        @Operation(summary = "get shop items advanced search", description = "get shop items advanced search.")
        @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ShopItem.class)))
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

        @PUT
        @Path("{id}")
        @RolesAllowed({"ADMIN","AGENT"})
        @Transactional
        @Operation(summary = "Update shopItem by Id", description = "Update shopItem by Id")
        @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = User.class)))
        public Response updateShopItem(@PathParam("id") Long id, ShopItemUpdateRequest request){
            return Response.ok(new ResponseMessage(ActionMessages.UPDATED.label,shopItemService.updateShopItemById(id, request) )).build();
        }

        @DELETE
        @Path("{id}")
        @RolesAllowed({"ADMIN"})
        @Transactional
        @Operation(summary = "delete shopItem by id ", description = "delete shopItem by id.")
        @APIResponse(description = "Successful", responseCode = "200")
        public Response deleteItemById(@PathParam("id") Long id){
            shopItemService.deleteShopItemById(id);
            return Response.ok(new ResponseMessage(ActionMessages.DELETED.label)).build();

        }

        @GET
        //@RolesAllowed({"ADMIN"})
        @Transactional
        @Path("/generate-pdf")
        @Operation(summary = "pdf", description = "pdf download")
        @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
        public Response generateAndReturnPdf(@BeanParam ShopItemParametersRequest request) {
            return shopItemService.generateAndReturnPdf(request);
        }*/


    }








