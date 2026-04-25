package org.example.inventory.item.services.payloads.responses.dtos;

import java.time.LocalDateTime;

import org.example.inventory.item.domain.Manufacturer;

public class ManufacturerDTO {
    public Long id;
    public String manufacturerName;
    public String abbreviation;
    public String description;
    public LocalDateTime creationDateTime;
    public LocalDateTime upDateTime;
    public String emailAddress;
    public String contact;
    public String physicalAddress;
    public String webSiteAddress;
    public String countryOfOrigin;

    public ManufacturerDTO(Manufacturer manufacturer) {
        this.id = manufacturer.id;
        this.manufacturerName = manufacturer.manufacturerName;
        this.abbreviation = manufacturer.abbreviation;
        this.description = manufacturer.description;
        this.contact = manufacturer.contact;
        this.emailAddress = manufacturer.emailAddress;
        this.physicalAddress = manufacturer.physicalAddress;
        this.webSiteAddress = manufacturer.webSiteAddress;
        this.countryOfOrigin = manufacturer.countryOfOrigin;
        this.creationDateTime = manufacturer.creationDateTime;
        this.upDateTime = manufacturer.upDateTime;
    }
}













