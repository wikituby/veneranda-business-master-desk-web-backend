package org.example.inventory.item.services.payloads.requests;

import jakarta.ws.rs.QueryParam;

import java.time.LocalDate;

public class ShopItemParametersRequest {

    @QueryParam("category")
    public String category;

    @QueryParam("subCategory")
    public String subCategory;

    @QueryParam("title")
    public String title;

    @QueryParam("datefrom")
    public LocalDate datefrom;

    @QueryParam("dateto")
    public LocalDate dateto;

}
