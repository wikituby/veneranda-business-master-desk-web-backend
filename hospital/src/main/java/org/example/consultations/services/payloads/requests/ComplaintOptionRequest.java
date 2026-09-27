package org.example.consultations.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class ComplaintOptionRequest {
    @Schema(example = "nature", description = "Option category: nature, aggravating, alleviating, or associated")
    public String category;

    @Schema(example = "Sharp", description = "Title of the complaint option")
    public String title;

    @Schema(example = "Sharp character of pain", description = "Optional description")
    public String description;
}
