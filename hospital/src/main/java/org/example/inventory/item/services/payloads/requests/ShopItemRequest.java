package org.example.inventory.item.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;

public class ShopItemRequest {

    @Schema(example = "1", description = "Item category ID (ItemCategory)")
    public Long categoryId;

    @Schema(example = "2", description = "Optional parent item category ID")
    public Long parentCategoryId;

    @Schema(example = "50")
    public Integer reOrderTo;

    @Schema(example = "anti-malarial", description = "Optional subcategory label")
    public String subCategory;

    @Schema(example = "Artesunate 30mg IV")
    public String title;

    @Schema(example = "This is the best and cheapest ladies hand bag ont he market ang that arches with most dresse")
    public String description;

    @Schema(example = "https://firebasestorage.googleapis.com/v0/b/newstorageforuplodapp.appspot.com/o/images%2Fcomputer.jpg?alt=media&token=54728992-5370-4be3-91d2-05e54bac6042")
    public String image;

    @Schema(example = "Vial")
    public String unitOfMeasure;

    @Schema(example = "mg")
    public String lastUnitOfMeasure;

    @Schema(example = "250")
    public BigDecimal lastUnitValue;

    @Schema(example = "50")
    public Integer reOrderLevel;


    @Schema(example = "500")
    public BigDecimal costPrice;

    @Schema(example = "1000")
    public BigDecimal sellingPrice;

    @Schema(example = "1000")
    public BigDecimal stockAtHand;

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




