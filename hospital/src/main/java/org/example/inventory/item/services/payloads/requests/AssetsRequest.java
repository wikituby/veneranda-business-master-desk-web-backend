package org.example.inventory.item.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDate;

public class AssetsRequest {

    @Schema(
            example = "Ultrasound Machine",
            description = "The name or title of the asset."
    )
    public String title;

    @Schema(
            example = "10.5",
            description = "Annual depreciation rate (%) for the asset."
    )
    public double depreciationRate;

    @Schema(
            example = "60",
            description = "The useful life of the asset in months before it becomes fully depreciated."
    )
    public int usefulLifeMonths;

    @Schema(
            example = "2023-08-15",
            description = "The date the asset was acquired."
    )
    public LocalDate acquisitionDate;

    @Schema(
            example = "SN-ULTRA-2023-001",
            description = "Unique serial number identifying the asset."
    )
    public String serialNumber;

    @Schema(
            example = "This is a high-resolution Doppler ultrasound machine used for obstetric and abdominal imaging.",
            description = "Detailed description or additional information about the asset."
    )
    public String description;
}
