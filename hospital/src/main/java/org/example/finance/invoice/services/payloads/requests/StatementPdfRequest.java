package org.example.finance.invoice.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

public class StatementPdfRequest {

    @Schema(description = "Treatment request IDs to include in the statement. Omit or null to include all.")
    public List<Long> treatmentRequestedIds;

    @Schema(description = "Procedure / service request IDs to include in the statement. Omit or null to include all.")
    public List<Long> procedureRequestedIds;

    @Schema(description = "When false, next of kin is omitted from the statement header. Defaults to true.")
    public Boolean includeNextOfKin;

    @Schema(description = "When false, the payment / totals table is omitted from the statement. Defaults to true.")
    public Boolean includePaymentTable;

    @Schema(description = "When true, show price columns on services and qty / unit / total columns on drugs. Defaults to true.")
    public Boolean includeAmountColumns;

    @Schema(description = "When false, the page footer is omitted from the statement. Defaults to true.")
    public Boolean includeFooter;

    @Schema(description = "When false, patient full name is omitted from the statement header. Defaults to true.")
    public Boolean includePatientName;

    @Schema(description = "When false, patient address is omitted from the statement header. Defaults to true.")
    public Boolean includePatientAddress;

    @Schema(description = "When false, patient gender is omitted from the statement header. Defaults to true.")
    public Boolean includePatientGender;

    @Schema(description = "When false, patient age is omitted from the statement header. Defaults to true.")
    public Boolean includePatientAge;

    @Schema(description = "When false, patient contact is omitted from the statement header. Defaults to true.")
    public Boolean includePatientContact;

    @Schema(description = "When false, visit date is omitted from the statement header. Defaults to true.")
    public Boolean includeVisitDate;

    @Schema(description = "When false, balance due is omitted from the statement header. Defaults to true.")
    public Boolean includeBalanceDue;

    @Schema(
            description = "Document heading type: MEDICAL_REPORT, DISCHARGE_FORM, or MEDICAL_FORM. Defaults to MEDICAL_REPORT.",
            enumeration = {"MEDICAL_REPORT", "DISCHARGE_FORM", "MEDICAL_FORM"}
    )
    public String documentType;

    public List<Long> getTreatmentRequestedIds() {
        return treatmentRequestedIds;
    }

    public void setTreatmentRequestedIds(List<Long> treatmentRequestedIds) {
        this.treatmentRequestedIds = treatmentRequestedIds;
    }

    public List<Long> getProcedureRequestedIds() {
        return procedureRequestedIds;
    }

    public void setProcedureRequestedIds(List<Long> procedureRequestedIds) {
        this.procedureRequestedIds = procedureRequestedIds;
    }

    public Boolean getIncludeNextOfKin() {
        return includeNextOfKin;
    }

    public void setIncludeNextOfKin(Boolean includeNextOfKin) {
        this.includeNextOfKin = includeNextOfKin;
    }

    public Boolean getIncludePaymentTable() {
        return includePaymentTable;
    }

    public void setIncludePaymentTable(Boolean includePaymentTable) {
        this.includePaymentTable = includePaymentTable;
    }

    public Boolean getIncludeAmountColumns() {
        return includeAmountColumns;
    }

    public void setIncludeAmountColumns(Boolean includeAmountColumns) {
        this.includeAmountColumns = includeAmountColumns;
    }

    public Boolean getIncludeFooter() {
        return includeFooter;
    }

    public void setIncludeFooter(Boolean includeFooter) {
        this.includeFooter = includeFooter;
    }

    public Boolean getIncludePatientName() {
        return includePatientName;
    }

    public void setIncludePatientName(Boolean includePatientName) {
        this.includePatientName = includePatientName;
    }

    public Boolean getIncludePatientAddress() {
        return includePatientAddress;
    }

    public void setIncludePatientAddress(Boolean includePatientAddress) {
        this.includePatientAddress = includePatientAddress;
    }

    public Boolean getIncludePatientGender() {
        return includePatientGender;
    }

    public void setIncludePatientGender(Boolean includePatientGender) {
        this.includePatientGender = includePatientGender;
    }

    public Boolean getIncludePatientAge() {
        return includePatientAge;
    }

    public void setIncludePatientAge(Boolean includePatientAge) {
        this.includePatientAge = includePatientAge;
    }

    public Boolean getIncludePatientContact() {
        return includePatientContact;
    }

    public void setIncludePatientContact(Boolean includePatientContact) {
        this.includePatientContact = includePatientContact;
    }

    public Boolean getIncludeVisitDate() {
        return includeVisitDate;
    }

    public void setIncludeVisitDate(Boolean includeVisitDate) {
        this.includeVisitDate = includeVisitDate;
    }

    public Boolean getIncludeBalanceDue() {
        return includeBalanceDue;
    }

    public void setIncludeBalanceDue(Boolean includeBalanceDue) {
        this.includeBalanceDue = includeBalanceDue;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }
}
