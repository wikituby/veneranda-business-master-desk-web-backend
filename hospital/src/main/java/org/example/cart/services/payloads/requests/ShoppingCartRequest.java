package org.example.cart.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class ShoppingCartRequest {


    @Schema(example = "1")
    public Long userId;

    @Schema(example = "1")
    public Long shopItemId;

}






