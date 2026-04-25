package org.example.finance.invoice.services.payloads.requests;

import jakarta.ws.rs.QueryParam;

public class InvoiceParametersRequest {
    @QueryParam("InvoiceId")
    public Long invoiceId;

}
