package org.example.consultations.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class ComplaintTypeRequest {
    @Schema(example = "Pain", description = "Title of the complaint type")
    public String title;

    @Schema(example = "Describes pain-related complaints", description = "Description of the complaint type")
    public String description;
}






















