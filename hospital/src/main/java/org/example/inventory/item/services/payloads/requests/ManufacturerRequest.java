package org.example.inventory.item.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class ManufacturerRequest {

    // Backend field names
    @Schema(example = "Bliss GVS Pharma Ltd.")
    public String manufacturerName;

    @Schema(example = "bliss")
    public String abbreviation;

    @Schema(example = "+256708403223")
    public String contact;

    @Schema(example = "Plot 23 Industrial Area, Nairobi, Kenya")
    public String physicalAddress;

    @Schema(example = "blisspharma@gmail.com")
    public String emailAddress;

    @Schema(example = "blisspharma.com")
    public String webSiteAddress;

    @Schema(example = "India")
    public String countryOfOrigin;

    @Schema(example = "Leading pharmaceutical manufacturer")
    public String description;

    // Frontend field names (for compatibility)
    @Schema(example = "Bliss GVS Pharma Ltd.")
    public String name;

    @Schema(example = "+256708403223")
    public String phone;

    @Schema(example = "Plot 23 Industrial Area, Nairobi, Kenya")
    public String address;

    @Schema(example = "blisspharma@gmail.com")
    public String email;

    @Schema(example = "India")
    public String country;

    @Schema(example = "John Doe")
    public String contactPerson;
}

