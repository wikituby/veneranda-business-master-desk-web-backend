package org.example.consultations.services.payloads.responses;

import jakarta.json.bind.annotation.JsonbDateFormat;
import org.example.consultations.domains.ComplaintOption;

import java.time.LocalDate;

public class ComplaintOptionDTO {
    public Long id;
    public String category;
    public String title;
    public String description;

    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate creationDate;

    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate updateDate;

    public ComplaintOptionDTO() {
    }

    public ComplaintOptionDTO(ComplaintOption entity) {
        this.id = entity.id;
        this.category = entity.category;
        this.title = entity.title;
        this.description = entity.description;
        this.creationDate = entity.creationDate;
        this.updateDate = entity.updateDate;
    }
}
