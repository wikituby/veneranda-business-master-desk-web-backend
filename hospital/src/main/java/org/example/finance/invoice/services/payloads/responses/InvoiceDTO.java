package org.example.finance.invoice.services.payloads.responses;

import jakarta.json.bind.annotation.JsonbDateFormat;
import org.example.client.services.payloads.responses.dtos.PatientDTO;
import org.example.finance.invoice.domains.Invoice;
import org.example.finance.payments.cash.services.payloads.responses.PaymentDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class InvoiceDTO {

    public Long id;
    public Long visitId;
    public LocalTime timeOfCreation;
    public String invoiceNo;
    public int invoicePlainNo;
    public Long patientId;
    
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate dateOfInvoice;
    
    public BigDecimal balanceDue;
    public String tin;
    public String companyLogo;
    public String documentTitle;
    public String fromName;
    public String toName;
    public String fromAddress;
    public String toAddress;
    public String reference;
    public BigDecimal subTotal;
    public BigDecimal discount;
    public BigDecimal tax;
    public BigDecimal totalAmount;
    public BigDecimal amountPaid;
    public String notes;
    
    public PatientDTO patient;
    public List<PaymentDTO> payments;

    public InvoiceDTO(Invoice invoice) {
        if (invoice != null) {
            this.id = invoice.getId();
            this.visitId = invoice.visit != null ? invoice.visit.id : null;
            this.patientId = invoice.patient != null ? invoice.patient.id : null;
            this.timeOfCreation = invoice.timeOfCreation != null ? invoice.timeOfCreation : LocalTime.now();
            this.invoiceNo = invoice.invoiceNo != null ? invoice.invoiceNo : "N/A";
            this.invoicePlainNo = invoice.invoicePlainNo;
            this.dateOfInvoice = invoice.dateOfInvoice != null ? invoice.dateOfInvoice : LocalDate.now();
            this.balanceDue = invoice.balanceDue != null ? invoice.balanceDue : BigDecimal.ZERO;
            this.tin = invoice.tin != null ? invoice.tin : "N/A";
            this.companyLogo = invoice.companyLogo != null ? invoice.companyLogo : "default_logo_url";
            this.documentTitle = invoice.documentTitle != null ? invoice.documentTitle : "Invoice";
            this.fromName = invoice.fromName != null ? invoice.fromName : "VENERANDA MEDICAL";
            this.toName = invoice.toName != null ? invoice.toName : "N/A";
            this.fromAddress = invoice.fromAddress != null ? invoice.fromAddress : "Bugogo Town Council";
            this.toAddress = invoice.toAddress != null ? invoice.toAddress : "Bugogo Town Council";
            this.reference = invoice.reference != null ? invoice.reference : generateRandomReferenceNo(20);
            this.subTotal = invoice.subTotal != null ? invoice.subTotal : BigDecimal.ZERO;
            this.discount = invoice.discount != null ? invoice.discount : BigDecimal.ZERO;
            this.tax = invoice.tax != null ? invoice.tax : BigDecimal.ZERO;
            this.totalAmount = invoice.totalAmount != null ? invoice.totalAmount : BigDecimal.ZERO;
            this.amountPaid = invoice.amountPaid != null ? invoice.amountPaid : BigDecimal.ZERO;
            this.notes = invoice.notes != null ? invoice.notes : "No Notes";
            
            this.patient = invoice.patient != null ? new PatientDTO(invoice.patient) : null;
            
            this.payments = Optional.ofNullable(invoice.getPayments())
                    .orElse(Collections.emptyList())
                    .stream()
                    .map(PaymentDTO::new)
                    .collect(Collectors.toList());
        } else {
            this.id = null;
            this.visitId = null;
            this.timeOfCreation = LocalTime.now();
            this.invoiceNo = "N/A";
            this.invoicePlainNo = 0;
            this.dateOfInvoice = LocalDate.now();
            this.balanceDue = BigDecimal.ZERO;
            this.tin = "N/A";
            this.companyLogo = "default_logo_url";
            this.documentTitle = "Invoice";
            this.fromName = "VENERANDA MEDICAL";
            this.toName = "N/A";
            this.fromAddress = "Bugogo Town Council";
            this.toAddress = "Bugogo Town Council";
            this.reference = generateRandomReferenceNo(20);
            this.subTotal = BigDecimal.ZERO;
            this.discount = BigDecimal.ZERO;
            this.tax = BigDecimal.ZERO;
            this.totalAmount = BigDecimal.ZERO;
            this.amountPaid = BigDecimal.ZERO;
            this.notes = "No Notes";
            this.patient = null;
            this.payments = null;
        }
    }

    private String generateRandomReferenceNo(int length) {
        return "REF" + Math.random();
    }

    public Long getId() {
        return this.id;
    }
}







