package org.example.cafeteria.inventory.item.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;

public class CanteenItemRequest {

    @Schema(example = "drug")
    public String category;

    @Schema(example = "anti-malarial")
    public String subCategory;

    @Schema(example = "Artesunate 30mg IV")
    public String title;

    @Schema(example = "This is the best and cheapest ladies hand bag ont he market ang that arches with most dresse")
    public String description;

    @Schema(example = "https://firebasestorage.googleapis.com/v0/b/newstorageforuplodapp.appspot.com/o/images%2Fcomputer.jpg?alt=media&token=54728992-5370-4be3-91d2-05e54bac6042")
    public String image;

    @Schema(example = "Vial")
    public String unitOfMeasure;

    @Schema(example = "50")
    public Integer reOrderLevel;

    @Schema(example = "500")
    public BigDecimal costPrice;

    @Schema(example = "1000")
    public BigDecimal sellingPrice;

    @Schema(example = "1000")
    public BigDecimal stockAtHand;







}
