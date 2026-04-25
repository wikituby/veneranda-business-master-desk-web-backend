package org.example.inventory.item.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;

public class ShopItemUpdateRequest {

    @Schema(example = "1", description = "Item category ID (ItemCategory)")
    public Long categoryId;

    @Schema(example = "2", description = "Optional parent item category ID")
    public Long parentCategoryId;

    @Schema(example = "anti-malarial", description = "Optional subcategory label")
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
    public Integer reOrderTo;

    @Schema(example = "50")
    public BigDecimal stockAtHand;

    @Schema(example = "each")
    public String unitOfMeasure;

    @Schema(example = "mg")
    public String lastUnitOfMeasure;

    @Schema(example = "250")
    public BigDecimal lastUnitValue;

    // -----------------------------
    // Medication-like fields (optional)
    // -----------------------------

    @Schema(example = "500", description = "Amount per dose")
    public BigDecimal dosage;

    @Schema(example = "mg", description = "Dosage unit")
    public String dosageUnit;

    @Schema(example = "2", description = "How often (value)")
    public BigDecimal frequency;

    @Schema(example = "per Day", description = "How often (unit)")
    public String frequencyUnit;

    @Schema(example = "5", description = "How long (value)")
    public BigDecimal duration;

    @Schema(example = "Days", description = "How long (unit)")
    public String durationUnit;

    @Schema(example = "Oral", description = "Route of administration")
    public String route;


}

