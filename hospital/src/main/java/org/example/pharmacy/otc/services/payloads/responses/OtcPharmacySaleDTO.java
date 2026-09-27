package org.example.pharmacy.otc.services.payloads.responses;

import org.example.pharmacy.otc.domains.OtcPharmacySale;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

public class OtcPharmacySaleDTO {
    public Long id;
    public String saleNo;
    public LocalDate saleDate;
    public LocalTime saleTime;
    public BigDecimal totalAmount;
    public BigDecimal amountReceived;
    public BigDecimal changeAmount;
    public String paymentForm;
    public String receivedBy;
    public String notes;
    public Long visitId;
    public Long patientId;
    public String patientName;
    /** quotation | sales_order | proforma | sales_invoice | receipt */
    public String documentType;
    /** False when the order/sale has been deactivated. */
    public Boolean active;
    public List<OtcPharmacySaleLineDTO> lines;

    public OtcPharmacySaleDTO(OtcPharmacySale sale) {
        if (sale == null) {
            return;
        }
        this.id = sale.id;
        this.saleNo = sale.saleNo;
        this.saleDate = sale.saleDate;
        this.saleTime = sale.saleTime;
        this.totalAmount = sale.totalAmount;
        this.amountReceived = sale.amountReceived;
        this.changeAmount = sale.changeAmount;
        this.paymentForm = sale.paymentForm;
        this.receivedBy = sale.receivedBy;
        this.notes = sale.notes;
        this.visitId = sale.visitId;
        this.patientId = sale.patientId;
        this.patientName = sale.patientName;
        this.documentType = sale.documentType;
        this.active = sale.active;
        if (sale.lines != null) {
            this.lines = sale.lines.stream().map(OtcPharmacySaleLineDTO::new).collect(Collectors.toList());
        }
    }
}
