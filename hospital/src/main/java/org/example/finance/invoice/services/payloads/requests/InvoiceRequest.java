package org.example.finance.invoice.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class InvoiceRequest {

    @Schema(example = "301")
    public Long visitId;
}
