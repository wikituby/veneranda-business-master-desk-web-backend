package org.example.inventory.item.services.payloads.responses.dtos;

import org.example.inventory.item.domain.Brand;

public class BrandDTO {

    public Long id;
    public String name;
    public String manufacturer;
    public String manufacturerAddress;
    public String countryOfOrigin;
    public String description;

    public BrandDTO(Brand brand) {
        this.id = brand.id;
        this.name = brand.name;
        this.manufacturer = brand.manufacturer;
        this.manufacturerAddress = brand.manufacturerAddress;
        this.countryOfOrigin = brand.countryOfOrigin;
        this.description = brand.description;
    }
}
