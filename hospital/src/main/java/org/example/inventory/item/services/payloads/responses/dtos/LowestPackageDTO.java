package org.example.inventory.item.services.payloads.responses.dtos;

import org.example.inventory.item.domain.LowestPackage;

public class LowestPackageDTO {

    public Long id;
    public String title;
    public String description;
    public String standardAbbreviation;



    public LowestPackageDTO(LowestPackage lowestPackage) {
        this.id = lowestPackage.id;
        this.title = lowestPackage.title;
        this.standardAbbreviation = lowestPackage.standardAbbreviation;
        this.description = lowestPackage.description;


    }
}
