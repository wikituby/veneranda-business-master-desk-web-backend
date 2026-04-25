package org.example.cafeteria.sales.saleDay.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class SaleDayStatusUpdateRequest {
    @Schema(example = "open")
    public String visitStatus;
}






