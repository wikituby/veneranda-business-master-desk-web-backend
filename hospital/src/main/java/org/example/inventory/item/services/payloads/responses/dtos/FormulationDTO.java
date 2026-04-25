package org.example.inventory.item.services.payloads.responses.dtos;

import org.example.inventory.item.domain.Formulation;

public class FormulationDTO {
    public Long id;
    public String name;
    public String description;

    public FormulationDTO(Formulation formulation) {
        this.id = formulation.id;
        this.name = formulation.name;
        this.description = formulation.description;
    }
}
