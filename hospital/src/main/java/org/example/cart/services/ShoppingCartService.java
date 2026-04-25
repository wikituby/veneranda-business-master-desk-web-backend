package org.example.cart.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.cart.domains.ShoppingCart;
import org.example.cart.services.payloads.requests.ShoppingCartRequest;
import org.example.cart.services.payloads.responses.ShoppingCartResponse;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.inventory.item.domain.Item;
import org.example.user.domains.User;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class ShoppingCartService {


    @Transactional
    public Response addToCart(ShoppingCartRequest request) {
        User user = User.findById(request.userId);
        Item shopItem = Item.findById(request.shopItemId);

        if (user == null || shopItem == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("User or Product not found").build();

        }

        ShoppingCart shoppingCart = ShoppingCart.find("user = ?1 and shopItem = ?2", user, shopItem).firstResult();

        if (shoppingCart == null) {
            shoppingCart = new ShoppingCart();
            shoppingCart.user = user;
            shoppingCart.shopItem = shopItem;
            shoppingCart.quantity = 1;
            shoppingCart.persist();
        } else {
            shoppingCart.quantity += 1;
        }

        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label,getAllCart(request.userId))).build();


    }


    @Transactional
    public Response updateCartQuantity(Long shopItemId, int newQuantity) {
        ShoppingCart shoppingCart = ShoppingCart.findById(shopItemId);

        if (shoppingCart == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        shoppingCart.quantity = newQuantity;
        return Response.ok().build();
    }


    @Transactional
    public Response removeFromCart(Long shopItemId) {
        ShoppingCart shoppingCart = ShoppingCart.findById(shopItemId);

        if (shoppingCart != null) {
            shoppingCart.delete();
            return Response.ok().build();
        }

        return Response.status(Response.Status.NOT_FOUND).build();
    }


    public List<Item> getCart(Long userId) {
        return ShoppingCart.find("user.id", userId).list();

    }

    public ShoppingCartResponse getAllCart(Long userId) {
        // Retrieve all shopping cart items for the user
        List<ShoppingCart> cartItems = ShoppingCart.find("user.id", userId).list();

        // Extract the ShopItem objects from the ShoppingCart entries
        List<Item> shopItems = cartItems.stream()
                .map(cartItem -> cartItem.shopItem)
                .collect(Collectors.toList());

        // Calculate the total quantity by summing the quantity field of each cart item
        int totalQuantity = cartItems.stream()
                .mapToInt(cartItem -> cartItem.quantity)
                .sum();

        // Return the list of ShopItems and the total quantity as a CartResponse object
        return new ShoppingCartResponse(shopItems, totalQuantity);
    }





}







