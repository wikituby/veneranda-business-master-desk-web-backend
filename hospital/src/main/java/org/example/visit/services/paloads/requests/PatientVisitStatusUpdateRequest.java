package org.example.visit.services.paloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class PatientVisitStatusUpdateRequest {
    @Schema(example = "open")
    public String visitStatus;

    @Schema(example = "admin")
    public String userRole;
}






