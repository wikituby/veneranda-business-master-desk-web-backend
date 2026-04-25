package org.example.inventory.item.services.payloads.responses.basicResponses;

import java.math.BigDecimal;
import java.time.LocalDate;

public class FullItemResponse {

    public Long id;

    public String category;

    public String subCategory;

    public String title;

    public String description;

    public String unitOfMeasure;

    public LocalDate creationDate;

    public String number;

    public String image;

    public BigDecimal price;

    public BigDecimal costPrice;

    public BigDecimal sellingPrice;
}
