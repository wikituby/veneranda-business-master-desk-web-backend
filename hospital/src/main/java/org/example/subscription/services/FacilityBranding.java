package org.example.subscription.services;

import org.example.subscription.services.payloads.FacilityBrandingDTO;

/**
 * Resolved branding for PDF/report headers and footers.
 */
public class FacilityBranding {

    public final String facilityName;
    public final String facilityLogoUrl;
    public final String facilityAddress;
    public final String subsidiaryLabel;
    public final String phonePrimary;
    public final String phoneSecondary;
    public final String email;
    public final String financeDepartmentName;
    public final String medicalRecordsDepartmentName;
    public final String diagnosticsDepartmentName;
    public final String bankName;
    public final String ugxAccountNumber;
    public final String usdAccountNumber;
    public final String thankYouMessage;
    public final String invoiceFooterContactLine;

    public FacilityBranding(
            String facilityName,
            String facilityLogoUrl,
            String facilityAddress,
            String subsidiaryLabel,
            String phonePrimary,
            String phoneSecondary,
            String email,
            String financeDepartmentName,
            String medicalRecordsDepartmentName,
            String diagnosticsDepartmentName,
            String bankName,
            String ugxAccountNumber,
            String usdAccountNumber,
            String thankYouMessage,
            String invoiceFooterContactLine
    ) {
        this.facilityName = safe(facilityName, "Health Facility");
        this.facilityLogoUrl = facilityLogoUrl != null ? facilityLogoUrl.trim() : "";
        this.facilityAddress = safe(facilityAddress, "");
        this.subsidiaryLabel = safe(subsidiaryLabel, "");
        this.phonePrimary = safe(phonePrimary, "");
        this.phoneSecondary = safe(phoneSecondary, "");
        this.email = safe(email, "");
        this.financeDepartmentName = safe(financeDepartmentName, "Department of Finance");
        this.medicalRecordsDepartmentName = safe(medicalRecordsDepartmentName, "Department of Medical Records");
        this.diagnosticsDepartmentName = safe(diagnosticsDepartmentName, "Department of Medical Diagnostics");
        this.bankName = bankName != null ? bankName.trim() : "";
        this.ugxAccountNumber = ugxAccountNumber != null ? ugxAccountNumber.trim() : "";
        this.usdAccountNumber = usdAccountNumber != null ? usdAccountNumber.trim() : "";
        this.thankYouMessage = thankYouMessage != null ? thankYouMessage.trim() : "";
        this.invoiceFooterContactLine = invoiceFooterContactLine != null ? invoiceFooterContactLine.trim() : "";
    }

    private static String safe(String v, String fallback) {
        if (v == null || v.isBlank()) {
            return fallback;
        }
        return v.trim();
    }

    private String subsidiarySuffix() {
        if (subsidiaryLabel.isBlank()) {
            return "";
        }
        String s = subsidiaryLabel.trim();
        if (!s.startsWith("(")) {
            s = "(" + s + ")";
        }
        return " " + s;
    }

    public String financeHeaderLine() {
        return financeDepartmentName + " - " + facilityName + subsidiarySuffix();
    }

    public String medicalRecordsHeaderLine() {
        String addr = facilityAddress.isBlank() ? "" : facilityAddress;
        return medicalRecordsDepartmentName + "-" + facilityName + subsidiarySuffix()
                + (addr.isBlank() ? "." : " Address:" + addr + ".");
    }

    public String diagnosticsHeaderLine() {
        String addr = facilityAddress.isBlank() ? "" : facilityAddress;
        return diagnosticsDepartmentName + "-" + facilityName + subsidiarySuffix()
                + (addr.isBlank() ? "" : " Address:" + addr + ".")
                + "\n " + contactInquiryLine();
    }

    public String contactInquiryLine() {
        String phones = joinPhones(phonePrimary, phoneSecondary);
        StringBuilder sb = new StringBuilder("For inquiries / suggestions call:");
        if (!phones.isBlank()) {
            sb.append(" ").append(phones);
        }
        if (!email.isBlank()) {
            sb.append(". Email:").append(email);
        }
        return sb.toString();
    }

    public String medicalRecordsTitleBlock() {
        return medicalRecordsHeaderLine() + "\n " + contactInquiryLine();
    }

    public String invoiceFooterMainLine() {
        String base = "- " + financeDepartmentName + " - " + facilityName + subsidiarySuffix() + " - System Generated -";
        if (hasBankAccounts()) {
            return base + "\n " + bankName + ": [ UGX ACC. NO: " + ugxAccountNumber + "] [USD ACC.NO: " + usdAccountNumber + "]";
        }
        return base;
    }

    public String invoiceFooterContactResolved() {
        if (!invoiceFooterContactLine.isBlank()) {
            return invoiceFooterContactLine;
        }
        String phones = joinPhones(phonePrimary, phoneSecondary);
        StringBuilder sb = new StringBuilder();
        if (!facilityAddress.isBlank()) {
            sb.append(facilityAddress);
        }
        if (!phones.isBlank()) {
            if (!sb.isEmpty()) {
                sb.append(" | ");
            }
            sb.append("Tel: ").append(phones);
        }
        if (!email.isBlank()) {
            if (!sb.isEmpty()) {
                sb.append(" | ");
            }
            sb.append("Email: ").append(email);
        }
        return sb.toString();
    }

    public String thankYouLine() {
        if (!thankYouMessage.isBlank()) {
            return thankYouMessage;
        }
        return "Thank you for choosing " + facilityName + "!";
    }

    public boolean hasBankAccounts() {
        return !bankName.isBlank() && (!ugxAccountNumber.isBlank() || !usdAccountNumber.isBlank());
    }

    private static String joinPhones(String a, String b) {
        boolean ha = a != null && !a.isBlank();
        boolean hb = b != null && !b.isBlank();
        if (ha && hb) {
            return a.trim() + " / " + b.trim();
        }
        if (ha) {
            return a.trim();
        }
        if (hb) {
            return b.trim();
        }
        return "";
    }

    public FacilityBrandingDTO toDto(Long facilityId) {
        FacilityBrandingDTO dto = new FacilityBrandingDTO();
        dto.facilityId = facilityId;
        dto.facilityLogoUrl = facilityLogoUrl.isBlank() ? null : facilityLogoUrl;
        dto.facilityName = facilityName;
        dto.facilityAddress = facilityAddress;
        dto.subsidiaryLabel = subsidiaryLabel;
        dto.phonePrimary = phonePrimary;
        dto.phoneSecondary = phoneSecondary;
        dto.email = email;
        dto.financeDepartmentName = financeDepartmentName;
        dto.medicalRecordsDepartmentName = medicalRecordsDepartmentName;
        dto.diagnosticsDepartmentName = diagnosticsDepartmentName;
        dto.bankName = bankName;
        dto.ugxAccountNumber = ugxAccountNumber;
        dto.usdAccountNumber = usdAccountNumber;
        dto.thankYouMessage = thankYouMessage;
        dto.invoiceFooterContactLine = invoiceFooterContactLine;
        dto.financeHeaderLine = financeHeaderLine();
        dto.medicalRecordsHeaderLine = medicalRecordsHeaderLine();
        dto.diagnosticsHeaderLine = diagnosticsHeaderLine();
        dto.contactInquiryLine = contactInquiryLine();
        dto.invoiceFooterMainLine = invoiceFooterMainLine();
        dto.thankYouLine = thankYouLine();
        return dto;
    }
}
