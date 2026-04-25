package org.example.finance.payments.cash.services.payloads.requests;

import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDate;

public class PaymentParametersRequest {
    @QueryParam("paymentForm")
    @Schema(example = "cash at hand")
    public String paymentForm;

    @QueryParam("receivedBy")
    @Schema(example = "John Doe")
    public String receivedBy;

    @QueryParam("paidBy")
    @Schema(example = "Jane Smith")
    public String paidBy;

    @QueryParam("datefrom")
    @Schema(example = "2024-01-01")
    public LocalDate datefrom;

    @QueryParam("dateto")
    @Schema(example = "2024-12-31")
    public LocalDate dateto;
}










