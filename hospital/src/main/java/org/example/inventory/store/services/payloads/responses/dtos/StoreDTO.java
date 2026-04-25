package org.example.inventory.store.services.payloads.responses.dtos;

import jakarta.json.bind.annotation.JsonbDateFormat;
import org.example.inventory.store.domains.Store;

import java.time.LocalDate;

public class StoreDTO {

    public Long id;
    public String name;
    public String location;
    public String description;
    public String defaultStatus;

    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate creationDate;

    // Constructor to map from Store entity
    public StoreDTO(Store store) {
        this.id = store.id;
        this.name = store.name;
        this.location = store.location;
        this.description = store.description;
        this.defaultStatus = store.defaultStatus;
        this.creationDate = store.creationDate;
    }
}
