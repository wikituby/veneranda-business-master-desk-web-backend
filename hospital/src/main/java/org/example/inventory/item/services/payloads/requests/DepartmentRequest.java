package org.example.inventory.item.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDate;

public class DepartmentRequest {

    @Schema(example = "Radiology Department")
    public String title;

    @Schema(example = "Handles all radiological imaging such as X-rays, CT scans, and ultrasounds.")
    public String description;

    @Schema(example = "2025/11/09")
    public LocalDate creationDate;

    @Schema(example = "2025/11/09")
    public LocalDate updateDate;
}
