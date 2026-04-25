package org.example.inventory.item.services.payloads.responses.dtos;

import org.example.inventory.item.domain.Assets;

public class AssetsDTO {

    public Long id;
    public String title;
    public double depreciationRate;
    public int usefulLifeMonths;
    public String serialNumber;
    public String description;
    public java.time.LocalDate acquisitionDate;
    public java.time.LocalDate creationDate;
    public java.time.LocalDate updateDate;

    public AssetsDTO(Assets assets) {
        this.id = assets.id;
        this.title = assets.title;
        this.depreciationRate = assets.depreciationRate;
        this.usefulLifeMonths = assets.usefulLifeMonths;
        this.serialNumber = assets.serialNumber;
        this.description = assets.description;
        this.acquisitionDate = assets.acquisitionDate;
        this.creationDate = assets.creationDate;
        this.updateDate = assets.updateDate;
    }
}
