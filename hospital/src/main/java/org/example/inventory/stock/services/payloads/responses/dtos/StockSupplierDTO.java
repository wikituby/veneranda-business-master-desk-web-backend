package org.example.inventory.stock.services.payloads.responses.dtos;

import java.time.LocalDateTime;

import org.example.inventory.stock.domains.StockSupplier;

public class StockSupplierDTO {
    public Long id;
    public String supplierName;
    public String abbreviation;
    public String description;
    public LocalDateTime creationDateTime;
    public LocalDateTime upDateTime;
    public String emailAddress;
    public String contact;
    public String physicalAddress;
    public String webSiteAddress;


    public StockSupplierDTO(StockSupplier supplier) {
        this.id = supplier.id;
        this.supplierName = supplier.supplierName;
        this.abbreviation = supplier.abbreviation;
        this.description = supplier.description;
        this.contact = supplier.contact;
        this.emailAddress = supplier.emailAddress;
        this.physicalAddress = supplier.physicalAddress;
        this.webSiteAddress = supplier.webSiteAddress;
        this.creationDateTime = supplier.creationDateTime;
        this.upDateTime = supplier.upDateTime;
    }
}
