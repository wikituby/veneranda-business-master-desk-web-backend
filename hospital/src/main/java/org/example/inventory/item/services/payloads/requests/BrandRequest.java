package org.example.inventory.item.services.payloads.requests;


import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class BrandRequest {

    @Schema(example = "P-Alaxin")
    public String name;

    @Schema(example = "Bliss GVS Pharma Ltd.")
    public String manufacturer;

    @Schema(example = "Plot 23 Industrial Area, Nairobi, Kenya")
    public String manufacturerAddress;

    @Schema(example = "India")
    public String countryOfOrigin;

    @Schema(example = "Antimalarial combination of Artemether and Lumefantrine")
    public String description;
}
