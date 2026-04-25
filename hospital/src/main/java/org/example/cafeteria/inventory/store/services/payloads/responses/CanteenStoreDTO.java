package org.example.cafeteria.inventory.store.services.payloads.responses;

import jakarta.json.bind.annotation.JsonbDateFormat;
import org.example.cafeteria.inventory.store.domains.CanteenStore;

import java.time.LocalDate;

public class CanteenStoreDTO {

    public Long id;
    public String name;
    public String location;
    public String description;

    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate creationDate;

    // Constructor to map from Store entity
    public CanteenStoreDTO(CanteenStore store) {
        this.id = store.id;
        this.name = store.name;
        this.location = store.location;
        this.description = store.description;
        this.creationDate = store.creationDate;
    }
}






