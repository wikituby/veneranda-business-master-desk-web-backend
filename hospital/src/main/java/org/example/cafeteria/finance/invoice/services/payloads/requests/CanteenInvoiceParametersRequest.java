package org.example.cafeteria.finance.invoice.services.payloads.requests;

import jakarta.ws.rs.QueryParam;

public class CanteenInvoiceParametersRequest {
    @QueryParam("InvoiceId")
    public Long invoiceId;

}






