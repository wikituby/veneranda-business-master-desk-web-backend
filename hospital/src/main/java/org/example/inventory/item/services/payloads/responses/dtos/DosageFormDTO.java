package org.example.inventory.item.services.payloads.responses.dtos;

import org.example.inventory.item.domain.DosageForm;

public class DosageFormDTO {
    public Long id;
    public String name;
    public Long formulationId;
    public String description;

    public DosageFormDTO(DosageForm dosageForm) {
        this.id = dosageForm.id;
        this.name = dosageForm.name;
        this.formulationId = dosageForm.formulationId;
        this.description = dosageForm.description;
    }
}
