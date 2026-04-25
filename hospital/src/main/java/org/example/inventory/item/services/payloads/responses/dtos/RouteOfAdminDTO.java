package org.example.inventory.item.services.payloads.responses.dtos;

import org.example.inventory.item.domain.RouteOfAdmin;

public class RouteOfAdminDTO {

    public Long id;
    public String title;
    public String description;
    public String standardAbbreviation;



    public RouteOfAdminDTO(RouteOfAdmin routeOfAdmin) {
        this.id = routeOfAdmin.id;
        this.title = routeOfAdmin.title;
        this.standardAbbreviation = routeOfAdmin.standardAbbreviation;
        this.description = routeOfAdmin.description;


    }
}
