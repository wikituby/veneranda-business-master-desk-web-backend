package org.example.consultations.services.payloads.responses;

import jakarta.json.bind.annotation.JsonbDateFormat;
import org.example.consultations.domains.ComplaintType;

import java.time.LocalDate;

public class ComplaintTypeDTO {
    public Long id;
    public String title;
    public String description;

    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate creationDate;

    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate updateDate;

    public ComplaintTypeDTO(ComplaintType complaintType) {
        this.id = complaintType.id;
        this.title = complaintType.title;
        this.description = complaintType.description;
        this.creationDate = complaintType.creationDate;
        this.updateDate = complaintType.updateDate;
    }

    public ComplaintTypeDTO() {
    }
}






