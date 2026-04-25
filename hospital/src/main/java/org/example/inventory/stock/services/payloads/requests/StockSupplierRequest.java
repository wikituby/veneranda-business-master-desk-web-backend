package org.example.inventory.stock.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class StockSupplierRequest {

    @Schema(example = "mackie pharmacy")
    public String supplierName;

    @Schema(example = "mackie")
    public String abbreviation;

    @Schema(example = "+256708403223")
    public String contact;

    @Schema(example = "mubende town")
    public String physicalAddress;

    @Schema(example = "mackiePharmacy@gmail.com")
    public String emailAddress;

    @Schema(example = "mackiepharmacy.com")
    public String webSiteAddress;

    @Schema(example = "the only pharmacy that can deliver to bugogo")
    public String description;
}
