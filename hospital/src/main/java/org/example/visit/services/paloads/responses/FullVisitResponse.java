package org.example.visit.services.paloads.responses;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class FullVisitResponse {

    public Long id;
    public Long patientId;
    public String visitName;
    public Integer visitNumber;
    public String visitReason;
    public String visitStatus;
    public String visitType;
    public LocalDate visitDate;
    public LocalDate visitLastUpdatedDate;
    public LocalTime visitTime;
    public BigDecimal subTotal;
    public BigDecimal totalAmount;

    public BigDecimal amountPaid;
    public BigDecimal balanceDue;
    public String visitGroup;




}






