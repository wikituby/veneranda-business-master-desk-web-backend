package org.example.consultations.services.payloads.responses;

import jakarta.json.bind.annotation.JsonbDateFormat;
import org.example.consultations.domains.ComplaintSite;

import java.time.LocalDate;

public class ComplaintSiteDTO {
    public Long id;
    public String title;
    public String description;

    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate creationDate;

    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate updateDate;

    public ComplaintSiteDTO(ComplaintSite complaintSite) {
        this.id = complaintSite.id;
        this.title = complaintSite.title;
        this.description = complaintSite.description;
        this.creationDate = complaintSite.creationDate;
        this.updateDate = complaintSite.updateDate;
    }

    public ComplaintSiteDTO() {
    }
}






















