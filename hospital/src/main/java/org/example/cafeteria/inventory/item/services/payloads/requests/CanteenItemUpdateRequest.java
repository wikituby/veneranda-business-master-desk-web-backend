package org.example.cafeteria.inventory.item.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;

public class CanteenItemUpdateRequest {

    @Schema(example = "drug")
    public String category;

    @Schema(example = "anti-malarial")
    public String subCategory;

    @Schema(example = "Artesunate 60mg")
    public String title;

    @Schema(example = "Great outerwear jackets for Spring Autumn Winter, suitable for many occasions, such as working, hiking, camping, mountain rock climbing,")
    public String description;

    @Schema(example = "200")
    public BigDecimal costPrice;

    @Schema(example = "200")
    public BigDecimal sellingPrice;

    @Schema(example = "https://euro.montbell.com/products/prod_img/zoom/z_2301368_bric.jpg")
    public String image;

    @Schema(example = "50")
    public Integer reOrderLevel;

    @Schema(example = "50")
    public BigDecimal stockAtHand;

    @Schema(example = "each")
    public String unitOfMeasure;


}
