package org.example.store.services.payloads.responses;

import jakarta.json.bind.annotation.JsonbDateFormat;
import org.example.store.domains.Store;

import java.time.LocalDate;

public class StoreDTO {

    public Long id;
    public String name;
    public String location;
    public String description;

    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate creationDate;

    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate lastUpdatedDate;

    public StoreDTO(Store store) {
        this.id = store.id;
        this.name = store.name;
        this.location = store.location;
        this.description = store.description;
        this.creationDate = store.creationDate;
        this.lastUpdatedDate = store.lastUpdatedDate;
    }
}






