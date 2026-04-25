package org.example.consultations.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class ComplaintSiteRequest {
    @Schema(example = "Chest", description = "Title of the complaint site")
    public String title;

    @Schema(example = "Chest area of the body", description = "Description of the complaint site")
    public String description;
}






















