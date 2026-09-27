package org.example.pharmacy.otc.services.payloads.responses;

import org.example.pharmacy.otc.domains.OtcPharmacySaleLine;
import org.example.treatment.domains.TreatmentRequested;
import org.example.visit.domains.PatientVisit;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/** Flat row for pharmacy sales history tables (OTC counter + doctor prescriptions). */
public class OtcPharmacySaleLineDTO {
    /** Short label: OTC or RX */
    public String saleSource;
    /** OTC: {@link OtcPharmacySaleLine} id; RX: {@link TreatmentRequested} id. */
    public Long lineId;
    public Long saleId;
    public String saleNo;
    public LocalDate saleDate;
    public LocalTime saleTime;
    public Long visitId;
    public String patientName;
    public Long stockBatchId;
    public Long itemId;
    public String itemName;
    public String batchNumber;
    public String storeName;
    public BigDecimal quantity;
    public BigDecimal unitBuy;
    public BigDecimal unitSellingPrice;
    public BigDecimal totalAmount;
    public BigDecimal stockAtHandAfter;
    /** Combined dose label, e.g. 200mg */
    public String dosage;
    public BigDecimal amountPerFrequencyValue;
    public String amountPerFrequencyUnit;
    public BigDecimal frequencyValue;
    public Integer frequencyUnit;
    public BigDecimal durationValue;
    public Integer durationUnit;
    public BigDecimal totalUnits;
    public String instructions;
    public String route;
    public boolean reversed;
    /** Sale-level totals for receipt views. */
    public BigDecimal saleTotalAmount;
    public BigDecimal amountReceived;
    public BigDecimal changeAmount;
    public String paymentForm;
    public String receivedBy;
    public String notes;
    public Long patientId;
    /** quotation | sales_order | proforma | sales_invoice | receipt | delivery_note */
    public String documentType;
    /** From linked visit — true when goods delivery was confirmed. */
    public Boolean goodsDelivered;
    public String deliveryNote;
    /** False when the order/sale has been deactivated. */
    public Boolean active;

    public OtcPharmacySaleLineDTO(OtcPharmacySaleLine line) {
        if (line == null) {
            return;
        }
        this.lineId = line.id;
        this.reversed = line.reversed;
        if (line.sale != null) {
            this.saleId = line.sale.id;
            this.saleNo = line.sale.saleNo;
            this.saleDate = line.sale.saleDate;
            this.saleTime = line.sale.saleTime;
            this.saleTotalAmount = line.sale.totalAmount;
            this.amountReceived = line.sale.amountReceived;
            this.changeAmount = line.sale.changeAmount;
            this.paymentForm = line.sale.paymentForm;
            this.receivedBy = line.sale.receivedBy;
            this.notes = line.sale.notes;
            this.patientId = line.sale.patientId;
            this.patientName = line.sale.patientName;
            this.documentType = line.sale.documentType;
            this.active = line.sale.active;
            if (line.sale.visitId != null) {
                this.saleSource = "RX";
                this.visitId = line.sale.visitId;
                attachVisitDelivery(line.sale.visitId);
            } else {
                this.saleSource = "OTC";
            }
        } else {
            this.saleSource = "OTC";
            this.active = true;
        }
        if (line.stockBatch != null) {
            this.stockBatchId = line.stockBatch.id;
        }
        this.itemId = line.itemId;
        this.itemName = line.itemName;
        this.batchNumber = line.batchNumber;
        this.storeName = line.storeName;
        this.quantity = line.quantity;
        this.unitBuy = line.unitBuy;
        this.unitSellingPrice = line.unitSellingPrice;
        this.totalAmount = line.totalAmount;
        this.stockAtHandAfter = line.stockAtHandAfter;
        this.amountPerFrequencyValue = line.amountPerFrequencyValue;
        this.amountPerFrequencyUnit = line.amountPerFrequencyUnit;
        this.dosage = formatDosage(line.amountPerFrequencyValue, line.amountPerFrequencyUnit);
        this.frequencyValue = line.frequencyValue;
        this.frequencyUnit = line.frequencyUnit;
        this.durationValue = line.durationValue;
        this.durationUnit = line.durationUnit;
        this.totalUnits = line.totalUnits;
        this.instructions = line.instructions;
        this.route = line.route;
    }

    public OtcPharmacySaleLineDTO(TreatmentRequested treatment) {
        if (treatment == null || treatment.visit == null) {
            return;
        }
        this.saleSource = "RX";
        this.lineId = treatment.id;
        this.saleId = treatment.id;
        this.saleNo = treatment.visit.visitNumber > 0
                ? "ORDER-" + treatment.visit.visitNumber
                : "ORDER-" + treatment.visit.id;
        this.saleDate = treatment.visit.visitDate;
        this.saleTime = treatment.visit.visitTime;
        this.visitId = treatment.visit.id;
        this.goodsDelivered = Boolean.TRUE.equals(treatment.visit.goodsDelivered);
        this.deliveryNote = treatment.visit.deliveryNote;
        this.patientName = resolvePatientName(treatment);
        if (treatment.stockBatch != null) {
            this.stockBatchId = treatment.stockBatch.id;
        }
        this.itemName = treatment.itemName;
        this.quantity = treatment.quantity;
        this.unitBuy = treatment.unitBuy;
        this.unitSellingPrice = treatment.unitSellingPrice;
        this.totalAmount = treatment.totalAmount;
        this.amountPerFrequencyValue = treatment.amountPerFrequencyValue;
        this.amountPerFrequencyUnit = treatment.amountPerFrequencyUnit;
        this.dosage = formatDosage(treatment.amountPerFrequencyValue, treatment.amountPerFrequencyUnit);
        this.frequencyValue = treatment.frequencyValue;
        this.frequencyUnit = parseFrequencyUnit(treatment.frequencyUnit);
        this.durationValue = treatment.durationValue;
        this.durationUnit = parseDurationUnit(treatment.durationUnit);
        this.totalUnits = treatment.totalUnits;
        this.instructions = treatment.instructions;
        this.route = treatment.route;
        this.active = true;
    }

    private static Integer parseFrequencyUnit(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Integer parseDurationUnit(String value) {
        return parseFrequencyUnit(value);
    }

    private void attachVisitDelivery(Long visitId) {
        if (visitId == null || visitId <= 0) {
            return;
        }
        PatientVisit visit = PatientVisit.findById(visitId);
        if (visit == null) {
            return;
        }
        this.goodsDelivered = Boolean.TRUE.equals(visit.goodsDelivered);
        this.deliveryNote = visit.deliveryNote;
    }

    private static String resolvePatientName(TreatmentRequested treatment) {
        if (treatment.visit.patientName != null && !treatment.visit.patientName.isBlank()) {
            return treatment.visit.patientName.trim();
        }
        if (treatment.visit.patient != null) {
            String first = treatment.visit.patient.patientFirstName != null
                    ? treatment.visit.patient.patientFirstName : "";
            String second = treatment.visit.patient.patientSecondName != null
                    ? treatment.visit.patient.patientSecondName : "";
            return (first + " " + second).trim();
        }
        return null;
    }

    private static String formatDosage(BigDecimal value, String unit) {
        if (value != null && value.compareTo(BigDecimal.ZERO) == 0) {
            value = null;
        }
        if (value == null && (unit == null || unit.isBlank())) {
            return null;
        }
        if (value == null) {
            return unit.trim();
        }
        String amount = value.stripTrailingZeros().toPlainString();
        if (unit == null || unit.isBlank()) {
            return amount;
        }
        return amount + unit.trim();
    }
}
