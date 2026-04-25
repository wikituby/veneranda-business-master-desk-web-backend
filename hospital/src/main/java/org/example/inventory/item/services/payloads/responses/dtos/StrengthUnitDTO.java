package org.example.inventory.item.services.payloads.responses.dtos;

import org.example.inventory.item.domain.StrengthUnit;

public class StrengthUnitDTO {

    public Long id;
    public String title;
    public String description;
    public String standardAbbreviation;



    public StrengthUnitDTO(StrengthUnit strengthUnit) {
        this.id = strengthUnit.id;
        this.title = strengthUnit.title;
        this.standardAbbreviation = strengthUnit.standardAbbreviation;
        this.description = strengthUnit.description;


    }
}
