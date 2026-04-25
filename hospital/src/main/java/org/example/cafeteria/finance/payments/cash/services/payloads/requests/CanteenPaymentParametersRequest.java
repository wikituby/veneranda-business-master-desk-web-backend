package org.example.cafeteria.finance.payments.cash.services.payloads.requests;

import jakarta.ws.rs.QueryParam;

import java.time.LocalDate;

public class CanteenPaymentParametersRequest {

    @QueryParam("paymentForm")
    public String paymentForm;

    @QueryParam("receivedBy")
    public String receivedBy;

    @QueryParam("paidBy")
    public String paidBy;

    @QueryParam("datefrom")
    public LocalDate datefrom;

    @QueryParam("dateto")
    public LocalDate dateto;
}






