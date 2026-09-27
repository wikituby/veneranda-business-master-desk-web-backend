package org.example.finance.invoice.services;

//import com.itextpdf.io.IOException;
import java.io.IOException;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;

import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import com.itextpdf.layout.property.VerticalAlignment;


import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.example.client.domains.Patient;
import org.example.client.domains.PatientGroup;
import org.example.client.services.PatientService;
import org.example.client.services.payloads.responses.dtos.PatientDTO;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.consultations.domains.Consultation;
import org.example.consultations.domains.PresentingComplaint;
import org.example.consultations.services.ConsultationService;
import org.example.consultations.services.payloads.responses.ConsultationDTO;
import org.example.procedure.procedureRequested.domains.ProcedureRequested;
import org.example.pharmacy.sundry.domains.VisitSundry;
import org.example.inventory.item.domain.Item;
import org.example.inventory.stock.domains.StockBatch;
import org.example.treatment.domains.TreatmentRequested;
import org.example.visit.domains.repositories.PatientVisitRepository;
import org.example.finance.invoice.domains.repositories.InvoiceRepository;
import org.example.finance.invoice.domains.Invoice;
import org.example.finance.invoice.services.payloads.responses.InvoiceDTO;
import org.example.finance.invoice.services.payloads.requests.InvoiceUpdateRequest;
import org.example.finance.invoice.services.payloads.requests.StatementPdfRequest;
import org.example.finance.payments.cash.services.PaymentService;
import org.example.subscription.services.FacilityBranding;
import org.example.subscription.services.FacilityBrandingService;
import org.example.subscription.services.FacilityPdfLogoService;
import org.example.visit.domains.PatientVisit;
import org.example.vitals.domains.InitialTriageVitals;
import org.example.lab.cbc.domains.Cbc;
import org.example.lab.generalReport.domains.GeneralLabReport;
import org.example.lab.hepatitisB.domains.HepatitisB;
import org.example.lab.hepatitisC.domains.HepatitisC;
import org.example.lab.hpylori.domains.Hpylori;
import org.example.lab.parasitologyStool.domains.ParasitologyStool;
import org.example.lab.singleStatementReport.hiv.domains.Hiv;
import org.example.lab.singleStatementReport.malaria.domains.Malaria;
import org.example.lab.singleStatementReport.randomGlucose.domains.Rbs;
import org.example.lab.singleStatementReport.urineHcg.domains.UrineHcg;
import org.example.lab.stoolExam.domains.StoolExam;
import org.example.lab.urinalysis.domains.Urinalysis;
import org.example.lab.widal.domains.Widal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;

@ApplicationScoped
public class InvoiceService {

    @Inject
    InvoiceRepository invoiceRepository;

    @Inject
    PatientVisitRepository patientVisitRepository;

    @Inject
    PaymentService paymentService;

    @Inject
    PatientService patientService;

    @Inject
    ConsultationService consultationService;

    @Inject
    FacilityBrandingService facilityBrandingService;

    @Inject
    FacilityPdfLogoService facilityPdfLogoService;

    private FacilityBranding facilityBranding() {
        return facilityBrandingService.resolveDefaultBranding();
    }

    private static final String NOT_FOUND = "Not found!";

    private static final DeviceRgb LAB_SECTION_BLUE = new DeviceRgb(30, 90, 180);

    private static String plainNumber(BigDecimal value) {
        if (value == null) {
            return "";
        }
        return value.stripTrailingZeros().toPlainString();
    }

    private boolean resolveIncludeNextOfKin(StatementPdfRequest request) {
        if (request == null || request.includeNextOfKin == null) {
            return true;
        }
        return request.includeNextOfKin;
    }

    private boolean resolveIncludePaymentTable(StatementPdfRequest request) {
        if (request == null || request.includePaymentTable == null) {
            return true;
        }
        return request.includePaymentTable;
    }

    private boolean resolveIncludeAmountColumns(StatementPdfRequest request) {
        if (request == null || request.includeAmountColumns == null) {
            return true;
        }
        return request.includeAmountColumns;
    }

    private boolean resolveIncludeFooter(StatementPdfRequest request) {
        if (request == null || request.includeFooter == null) {
            return true;
        }
        return request.includeFooter;
    }

    private boolean resolveIncludeFlag(Boolean value) {
        return value == null || value;
    }

    private String formatProcedureDateTime(ProcedureRequested procedure) {
        if (procedure == null) {
            return "";
        }
        String datePart = procedure.dateOfProcedure != null ? procedure.dateOfProcedure.toString() : "";
        String timePart = procedure.timeOfProcedure != null
                ? procedure.timeOfProcedure.withNano(0).toString()
                : "";
        if (datePart.isEmpty() && timePart.isEmpty()) {
            return "";
        }
        if (datePart.isEmpty()) {
            return timePart;
        }
        if (timePart.isEmpty()) {
            return datePart;
        }
        return datePart + " " + timePart;
    }

    private Cell statementHeaderCell(String text, TextAlignment alignment) {
        return createCell(text, 1, alignment)
                .setBold()
                .setFontSize(7)
                .setFontColor(ColorConstants.WHITE)
                .setBackgroundColor(ColorConstants.DARK_GRAY)
                .setBorder(Border.NO_BORDER);
    }

    private Cell statementBodyCell(String text, TextAlignment alignment, com.itextpdf.kernel.colors.Color rowColor) {
        return createCell(text != null ? text : "", 1, alignment)
                .setFontSize(7)
                .setBackgroundColor(rowColor)
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(1));
    }

    private void addStatementServicesTable(Document document, List<ProcedureRequested> procedures, boolean includeAmounts) {
        if (procedures == null || procedures.isEmpty()) {
            return;
        }
        float[] widths = includeAmounts
                ? new float[]{3.2f, 1.8f, 1.3f, 1.3f, 3.2f, 1.2f}
                : new float[]{3.5f, 2f, 1.5f, 1.5f, 3.5f};
        Table procedureTable = new Table(UnitValue.createPercentArray(widths));
        procedureTable.setWidth(UnitValue.createPercentValue(100));

        procedureTable.addCell(statementHeaderCell("SERVICES OFFERED", TextAlignment.LEFT));
        procedureTable.addCell(statementHeaderCell("DATE & TIME", TextAlignment.LEFT));
        procedureTable.addCell(statementHeaderCell("REQUESTED BY", TextAlignment.LEFT));
        procedureTable.addCell(statementHeaderCell("DONE BY", TextAlignment.LEFT));
        procedureTable.addCell(statementHeaderCell("SUMMARY OF RESULTS / CONCLUSION", TextAlignment.RIGHT));
        if (includeAmounts) {
            procedureTable.addCell(statementHeaderCell("PRICE (UGX)", TextAlignment.RIGHT));
        }

        for (ProcedureRequested procedureRequested : procedures) {
            com.itextpdf.kernel.colors.Color rowColor = ColorConstants.WHITE;
            procedureTable.addCell(statementBodyCell(
                    procedureRequested.procedureRequestedName != null
                            ? procedureRequested.procedureRequestedName.toUpperCase()
                            : "",
                    TextAlignment.LEFT,
                    rowColor));
            procedureTable.addCell(statementBodyCell(
                    formatProcedureDateTime(procedureRequested).toUpperCase(),
                    TextAlignment.LEFT,
                    rowColor));
            procedureTable.addCell(statementBodyCell(
                    ellipsizeWithTwoDots(procedureRequested.orderedBy, 12),
                    TextAlignment.LEFT,
                    rowColor));
            procedureTable.addCell(statementBodyCell(
                    ellipsizeWithTwoDots(procedureRequested.doneBy, 12),
                    TextAlignment.LEFT,
                    rowColor));
            String report = procedureRequested.report != null ? String.valueOf(procedureRequested.report).toUpperCase() : "";
            procedureTable.addCell(statementBodyCell(report, TextAlignment.RIGHT, rowColor));
            if (includeAmounts) {
                procedureTable.addCell(statementBodyCell(
                        plainNumber(procedureRequested.unitSellingPrice),
                        TextAlignment.RIGHT,
                        rowColor));
            }
        }
        document.add(procedureTable);
    }

    private void addStatementDrugsTable(Document document, List<TreatmentRequested> treatments, boolean includeAmounts) {
        if (treatments == null || treatments.isEmpty()) {
            return;
        }
        float[] widths = includeAmounts
                ? new float[]{4f, 3.2f, 1f, 1.4f, 1.4f}
                : new float[]{5f, 4f};
        Table treatmentTable = new Table(UnitValue.createPercentArray(widths));
        treatmentTable.setWidth(UnitValue.createPercentValue(100));

        treatmentTable.addCell(statementHeaderCell("DRUG / ITEM / TREATMENT", TextAlignment.LEFT));
        treatmentTable.addCell(statementHeaderCell("DESCRIPTION / NOTES", TextAlignment.RIGHT));
        if (includeAmounts) {
            treatmentTable.addCell(statementHeaderCell("QTY", TextAlignment.RIGHT));
            treatmentTable.addCell(statementHeaderCell("UNIT PRICE (UGX)", TextAlignment.RIGHT));
            treatmentTable.addCell(statementHeaderCell("TOTAL (UGX)", TextAlignment.RIGHT));
        }

        for (TreatmentRequested treatmentRequested : treatments) {
            com.itextpdf.kernel.colors.Color rowColor = ColorConstants.WHITE;
            String itemName = treatmentRequested.itemName != null
                    ? treatmentRequested.itemName.toUpperCase()
                    : "";
            treatmentTable.addCell(statementBodyCell(itemName, TextAlignment.LEFT, rowColor));
            String description = plainNumber(treatmentRequested.amountPerFrequencyValue) + " "
                    + treatmentRequested.amountPerFrequencyUnit + " "
                    + plainNumber(treatmentRequested.frequencyValue) + " "
                    + "X" + " " + treatmentRequested.frequencyUnit + " "
                    + "FOR" + " " + plainNumber(treatmentRequested.durationValue) + " "
                    + treatmentRequested.durationUnit;
            treatmentTable.addCell(statementBodyCell(description, TextAlignment.RIGHT, rowColor));
            if (includeAmounts) {
                treatmentTable.addCell(statementBodyCell(
                        plainNumber(treatmentRequested.quantity),
                        TextAlignment.RIGHT,
                        rowColor));
                treatmentTable.addCell(statementBodyCell(
                        plainNumber(treatmentRequested.unitSellingPrice),
                        TextAlignment.RIGHT,
                        rowColor));
                treatmentTable.addCell(statementBodyCell(
                        plainNumber(treatmentRequested.totalAmount),
                        TextAlignment.RIGHT,
                        rowColor));
            }
        }
        document.add(treatmentTable);
    }

    private boolean resolveIncludePatientName(StatementPdfRequest request) {
        return request == null || resolveIncludeFlag(request.includePatientName);
    }

    private boolean resolveIncludePatientAddress(StatementPdfRequest request) {
        return request == null || resolveIncludeFlag(request.includePatientAddress);
    }

    private boolean resolveIncludePatientGender(StatementPdfRequest request) {
        return request == null || resolveIncludeFlag(request.includePatientGender);
    }

    private boolean resolveIncludePatientAge(StatementPdfRequest request) {
        return request == null || resolveIncludeFlag(request.includePatientAge);
    }

    private boolean resolveIncludePatientContact(StatementPdfRequest request) {
        return request == null || resolveIncludeFlag(request.includePatientContact);
    }

    private boolean resolveIncludeVisitDate(StatementPdfRequest request) {
        return request == null || resolveIncludeFlag(request.includeVisitDate);
    }

    private boolean resolveIncludeBalanceDue(StatementPdfRequest request) {
        return request == null || resolveIncludeFlag(request.includeBalanceDue);
    }

    private String resolveStatementDocumentTitle(StatementPdfRequest request) {
        String type = "MEDICAL_REPORT";
        if (request != null && request.documentType != null && !request.documentType.trim().isEmpty()) {
            type = request.documentType.trim().toUpperCase().replace(' ', '_').replace('-', '_');
        }
        if ("DISCHARGE_FORM".equals(type) || "DISCHARGE".equals(type)) {
            return "SYSTEM GENERATED DISCHARGE FORM";
        }
        if ("MEDICAL_FORM".equals(type)) {
            return "SYSTEM GENERATED MEDICAL FORM";
        }
        return "SYSTEM GENERATED MEDICAL REPORT";
    }

    private List<TreatmentRequested> resolveStatementTreatments(PatientVisit visit, StatementPdfRequest request) {
        List<TreatmentRequested> all = visit.getTreatmentRequested();
        if (all == null || all.isEmpty()) {
            return List.of();
        }
        if (request == null || request.treatmentRequestedIds == null) {
            return all;
        }
        Set<Long> ids = new HashSet<>(request.treatmentRequestedIds);
        return all.stream().filter(t -> ids.contains(t.id)).toList();
    }

    private List<ProcedureRequested> resolveStatementProcedures(PatientVisit visit, StatementPdfRequest request) {
        List<ProcedureRequested> all = visit.getProceduresRequested();
        if (all == null || all.isEmpty()) {
            return List.of();
        }
        if (request == null || request.procedureRequestedIds == null) {
            return all;
        }
        Set<Long> ids = new HashSet<>(request.procedureRequestedIds);
        return all.stream().filter(p -> ids.contains(p.id)).toList();
    }

    @Transactional
    public Invoice createInvoice(Long visitId) {
        // Check if the visit already has an invoice
        List<Invoice> existingInvoice = Invoice.find(
                "visit.id = ?1 ORDER BY id DESC",
                visitId
        ).list();

        // Check if the list is NOT empty
        if (!existingInvoice.isEmpty()) {
            throw new IllegalArgumentException("Invoice already exists for this visit.");
        }

        // Find the patient visit
        PatientVisit patientVisit = PatientVisit.findById(visitId);

        // Throw exception if the visit is not found
        if (patientVisit == null) {
            throw new IllegalArgumentException("Visit not found.");
        }

        // Create the invoice
        Invoice invoice = new Invoice();
        invoice.visit = patientVisit;
        invoice.patient = patientVisit.patient;
        invoice.tin = "185 7564 3489";
        invoice.notes = "Type in a brief note";
        invoice.discount = BigDecimal.valueOf(0.00);
        invoice.tax = BigDecimal.valueOf(0.00);
        invoice.dateOfInvoice = LocalDate.now();
        invoice.timeOfCreation = LocalTime.now();
        invoice.toName = patientVisit.patient.patientFirstName + " " + patientVisit.patient.patientSecondName;
        invoice.fromName = "VENERANDA MEDICAL";
        invoice.fromAddress = "Bugogo Town Council-Kyegegwa District";
        invoice.toAddress = "Bugogo Town Council-Kyegegwa District";
        invoice.companyLogo = "https://firebasestorage.googleapis.com/v0/b/newstorageforuplodapp.appspot.com/o/images%2FAsset%201.png?alt=media&token=08b34d6a-0693-4dff-88b1-6e42b5c56f67";
        invoice.documentTitle = "INVOICE";
        invoice.invoicePlainNo = findMaxInvoiceNoReturnInt() + 1;
        invoice.invoiceNo = "VMDINV-" + invoice.invoicePlainNo;
        invoice.reference = generateRandomReferenceNo(20);
        invoice.subTotal = BigDecimal.valueOf(0.00);
        invoice.totalAmount = BigDecimal.valueOf(0.00);
        invoice.balanceDue = BigDecimal.valueOf(0.00);
        invoice.amountPaid = BigDecimal.valueOf(0.00);
        invoice.invoiceStatus = "INVOICE";

        // Persist the invoice
        invoiceRepository.persist(invoice);

        //updateSubTotal(subTotalCalculated, visitId);

        return invoice;
    }

    /**
     * Recomputes invoice subtotal, total amount, and balance due from all visit line items
     * (treatments, procedures, etc.). Safe to call before payment when totals may still be zero.
     */
    @Transactional
    public void syncInvoiceTotalsForVisit(Long visitId) {
        if (visitId == null) {
            return;
        }
        getInvoiceSubTotal(visitId);
    }


    public BigDecimal calculateTotalBalanceDueForClosedVisits(Long patientId) {
        List<Invoice> invoices = Invoice.find(
                "visit.patient.id = ?1 and visit.visitStatus = ?2", patientId, "closed"
        ).list();

        return invoices.stream()
                .map(invoice -> invoice.balanceDue)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Sum of balanceDue across all visits for a patient (open and closed). */
    public BigDecimal calculateTotalBalanceDueForAllVisits(Long patientId) {
        if (patientId == null) {
            return BigDecimal.ZERO;
        }
        @SuppressWarnings("unchecked")
        List<org.example.visit.domains.PatientVisit> visits =
                org.example.visit.domains.PatientVisit.find("patient.id", patientId).list();
        return visits.stream()
                .map(v -> v.balanceDue != null ? v.balanceDue : BigDecimal.ZERO)
                .filter(b -> b.compareTo(BigDecimal.ZERO) > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }



    @Transactional
    public Response updateInvoice(Long invoiceId, InvoiceUpdateRequest request) {
        // Find the existing invoice
        Invoice invoice = Invoice.findById(invoiceId);

        if (invoice == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Invoice not found.", null))
                    .build();
        }

        if ("closed".equals(invoice.visit.visitStatus)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Visit is closed. You cannot add anything. Open a new visit or contact Admin on 0784411848: ", null))
                    .build();
        }

        if (request.discount != null && request.discount.compareTo(BigDecimal.ZERO) < 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Discount must be greater than or equal to zero.", null))
                    .build();
        }

        if (request.tax != null && request.tax.compareTo(BigDecimal.ZERO) < 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Tax must be greater than or equal to zero.", null))
                    .build();
        }

        if (request.discount != null && request.discount.compareTo(invoice.subTotal) > 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Discount cannot be greater than the subtotal", null))
                    .build();
        }

        // Assign 0.00 if discount or tax are null
        BigDecimal discount = (request.discount != null) ? request.discount : invoice.discount;
        BigDecimal tax = (request.tax != null) ? request.tax : invoice.tax;

        // Recalculate subtotal, total amount, and balance due
        Map<String, BigDecimal> invoiceSubTotalMap = getInvoiceSubTotal(invoice.visit.id);
        BigDecimal subTotalCalculated = invoiceSubTotalMap.get("InvoiceSubtotal");
        invoice.subTotal = subTotalCalculated;

        if (subTotalCalculated == null || subTotalCalculated.compareTo(BigDecimal.ZERO) <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("SubTotal Amount must be greater than zero.", null))
                    .build();
        }

        // Ensure subTotalCalculated is greater than the discount
        if (subTotalCalculated.compareTo(discount) <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("SubTotal Amount must be greater than the discount.", null))
                    .build();
        }

        // Now it's safe to subtract and add
        BigDecimal totalAmountDiscounted = subTotalCalculated.subtract(discount);
        BigDecimal totalAmount = totalAmountDiscounted.add(tax);
        invoice.totalAmount = totalAmount;

        invoice.amountPaid = paymentService.getTotalPaymentOfInvoice(invoiceId);
        invoice.balanceDue = totalAmount.subtract(invoice.amountPaid);

        // Update other fields
        invoice.upDateOfInvoice = LocalDate.now();
        invoice.updateTimeOfCreation = LocalTime.now();
        invoice.notes = request.notes;
        invoice.discount = discount;
        invoice.tax = tax;

        // Persist
        invoiceRepository.persist(invoice);

        PatientVisit patientVisit = invoice.visit;
        if (patientVisit == null) {
            throw new IllegalArgumentException("visit not found.");
        }

        patientVisit.balanceDue = invoice.balanceDue;
        patientVisit.amountPaid = invoice.amountPaid;
        patientVisit.subTotal = invoice.subTotal;
        patientVisit.totalAmount = invoice.totalAmount;
        BigDecimal syncedCost = invoiceSubTotalMap.get("TotalCostPrice");
        syncVisitFinancialBreakdown(
                patientVisit,
                invoice,
                nz(invoiceSubTotalMap.get("ProcedureSellingTotal")),
                nz(invoiceSubTotalMap.get("TreatmentSellingTotal")),
                nz(invoiceSubTotalMap.get("ProcedureCostTotal")),
                nz(invoiceSubTotalMap.get("TreatmentCostTotal")),
                syncedCost != null ? syncedCost : BigDecimal.ZERO);
        patientVisitRepository.persist(patientVisit);


        return Response.ok(new ResponseMessage("Invoice updated successfully", new InvoiceDTO(invoice))).build();
    }


    @Transactional
    public void updateInvoiceAmountPaid(Invoice invoice) {
        // Calculate the total payments made for the invoice
        if (invoice == null) {
            throw new IllegalArgumentException("Invoice not found.");
        }



        BigDecimal totalPayments = paymentService.getTotalPaymentOfInvoice(invoice.id);

        totalPayments = totalPayments != null ? totalPayments : BigDecimal.ZERO;

        // Update the invoice fields
        invoice.amountPaid = totalPayments;
        invoice.balanceDue = invoice.totalAmount.subtract(totalPayments);
        invoice.invoiceStatus = "RECEIPT";

        // Persist the updated invoice
        invoiceRepository.persist(invoice);

        PatientVisit patientVisit = invoice.visit;
        if (patientVisit == null) {
            throw new IllegalArgumentException("visit not found.");
        }

        patientVisit.balanceDue = invoice.balanceDue;
        patientVisit.amountPaid = invoice.amountPaid;
        patientVisit.subTotal = invoice.subTotal;
        patientVisit.totalAmount = invoice.totalAmount;
        patientVisit.totalSell = invoice.totalAmount;
        patientVisitRepository.persist(patientVisit);
    }

    @Transactional
    public List<InvoiceDTO> getAllInvoices() {
        return invoiceRepository.listAll(Sort.descending("invoicePlainNo"))
                .stream()
                .map(InvoiceDTO::new)
                .toList();
    }

    @Transactional
    public Response getInvoiceByVisitId(Long visitId) {
        Invoice invoice = invoiceRepository.find("visit.id", visitId)
                .firstResult();

        if (invoice == null) {
            //throw new WebApplicationException("Invoice not found for visitId: " + visitId, 404);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Invoice not found for visitId: " + visitId, null))
                    .build();
        }

       // return new InvoiceDTO(invoice);
        return Response.ok(new ResponseMessage("Invoice updated successfully", new InvoiceDTO(invoice))).build();

    }

    @Transactional
    public Response getCompassionInvoices() {
        List<Invoice> invoices = invoiceRepository.listAll()
                .stream()
                .filter(invoice ->
                        invoice.visit != null &&
                                invoice.visit.patient != null &&
                                invoice.visit.patient.getPatientGroup() != null &&
                                "compassion".equalsIgnoreCase(invoice.visit.patient.patientGroup.groupNameShortForm)
                )
                .toList();

        if (invoices.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("No invoices found for compassion group patients", null))
                    .build();
        }

        List<InvoiceDTO> invoiceDTOs = invoices.stream()
                .map(InvoiceDTO::new)
                .toList();

        return Response.ok(new ResponseMessage("Compassion invoices retrieved successfully", invoiceDTOs)).build();
    }





    @Transactional
    public CompassionDebtResult getAllInvoicesWithDebtAndCompassion() {
        List<Invoice> invoices = invoiceRepository.listAll()
                .stream()
                .filter(invoice ->
                        invoice.visit != null &&
                                invoice.visit.patient != null &&
                                invoice.visit.patient.getPatientGroup() != null &&
                                "compassion".equalsIgnoreCase(invoice.visit.patient.patientGroup.groupNameShortForm)
                )
                .toList();

        if (invoices.isEmpty()) {
            throw new RuntimeException("no invoice returned.");

        }

        // Calculate total debt
        BigDecimal totalBalanceDue = invoices.stream()
                .map(Invoice::getTotalBalanceDue)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Convert to DTOs
        List<InvoiceDTO> invoiceDTOs = invoices.stream()
                .map(InvoiceDTO::new)
                .toList();

        return new CompassionDebtResult(invoiceDTOs, totalBalanceDue);
    }

    // Create a wrapper class
    public record CompassionDebtResult(List<InvoiceDTO> invoices, BigDecimal totalBalanceDue) {}









    @Transactional
    public Response generateAndReturnInvoicePdfForListOfCompassionPatients() {
        try {



            // Create the PDF document
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter pdfWriter = new PdfWriter(baos);
            PdfDocument pdfDocument = new PdfDocument(pdfWriter);
            pdfDocument.addEventHandler(PdfDocumentEvent.END_PAGE, new FooterHelperInvoice(facilityBranding()));

            Document document = new Document(pdfDocument);
            document.setMargins(36, 36, 90, 36);



            // Add invoice title
            Table invoiceTitle = new Table(new float[]{1});
            invoiceTitle.setWidth(UnitValue.createPercentValue(100));
            invoiceTitle.addCell(new Cell()
                    .add(new Div()
                            .setBorderBottom(new SolidBorder(1)) // Underline (1px solid line)
                            .setPaddingBottom(2) // Space between text and underline
                            .add(new Paragraph("invoice")
                                    .setBold()
                                    .setFontSize(11)
                                    .setTextAlignment(TextAlignment.CENTER)
                            )
                    )
                    .add(new Paragraph(facilityBranding().financeHeaderLine())
                            .setFontSize(7)
                            //.setItalic()
                            .setMarginTop(3)
                            .setTextAlignment(TextAlignment.CENTER))
                    .setBorder(Border.NO_BORDER)
                    .setPaddingBottom(15)
            );
            document.add(invoiceTitle);

            // Add header: Logo and Invoice Details
            Table headerTable = new Table(new float[]{1, 1, 1, 2, 1});
            headerTable.setWidth(UnitValue.createPercentValue(100));

            // Add logo
            headerTable.addCell(new Cell()
                    .add(getLogo().setWidth(79).setHeight(68))
                    .setBorder(Border.NO_BORDER)
                    .setHorizontalAlignment(HorizontalAlignment.LEFT)
                    .setVerticalAlignment(VerticalAlignment.TOP)
                    .setPaddingTop(-7)
                    .setPaddingLeft(-22)
            );

            // Add invoice details
            headerTable.addCell(new Cell()
                    .add(new Paragraph("FROM: ").setFontSize(7).setTextAlignment(TextAlignment.LEFT))
                    .add(new Paragraph("ADDRESS: ").setFontSize(7).setTextAlignment(TextAlignment.LEFT))
                    .add(new Paragraph("TO: ").setFontSize(7).setTextAlignment(TextAlignment.LEFT))
                    .add(new Paragraph("ADDRESS: ").setFontSize(7).setTextAlignment(TextAlignment.LEFT))
                    .setBorder(Border.NO_BORDER)
            );

            headerTable.addCell(new Cell()
                    .add(new Paragraph("VENERANDA MEDICAL").setFontSize(7).setTextAlignment(TextAlignment.LEFT))
                    .add(new Paragraph("BUGOGO TOWN COUNCIL, KYEGEGWA DISTRICT").setFontSize(7).setTextAlignment(TextAlignment.LEFT))
                    .add(new Paragraph("KATOMA DEVELOPMENT CENTER")
                            .setFontSize(7).setTextAlignment(TextAlignment.LEFT))
                    .add(new Paragraph()
                            .add(Optional.of("KATOMA")
                                    .map(String::toUpperCase)
                                    .orElse(""))
                            .setFontSize(7)
                            .setTextAlignment(TextAlignment.LEFT)
                    )





                    .setBorder(Border.NO_BORDER)
            );

            headerTable.addCell(new Cell()
                    .add(new Paragraph("NUMBER: ").setFontSize(7).setTextAlignment(TextAlignment.LEFT))
                    .add(new Paragraph("DUE DATE: ").setFontSize(7).setTextAlignment(TextAlignment.LEFT))
                    .add(new Paragraph("DATE: ").setFontSize(7).setTextAlignment(TextAlignment.LEFT))
                    .add(new Paragraph("BALANCE DUE: ").setFontSize(7).setTextAlignment(TextAlignment.LEFT))
                    .setBorder(Border.NO_BORDER)
            );

            headerTable.addCell(new Cell()
                    .add(new Paragraph("UG-503").setFontSize(7).setTextAlignment(TextAlignment.RIGHT))
                    .add(new Paragraph(String.valueOf("8/25/2025")).setFontSize(7).setTextAlignment(TextAlignment.RIGHT))
                    .add(new Paragraph(String.valueOf("8/25/2025")).setFontSize(7).setTextAlignment(TextAlignment.RIGHT))
                    .add(new Paragraph(String.valueOf("5000")).setFontSize(7).setTextAlignment(TextAlignment.RIGHT))

                    .setBorder(Border.NO_BORDER)
            );

            document.add(headerTable);

            // Add items table
            float[] columnWidths = {4, 1, 2, 2};
            Table itemsTable = new Table(columnWidths);
            itemsTable.setWidth(UnitValue.createPercentValue(100));

            // Add header with no column lines
            itemsTable.addCell(createCell("CLIENT NAME", 1, TextAlignment.LEFT)
                    .setBold()
                    .setFontSize(7)
                    .setFontColor(ColorConstants.WHITE)
                    .setBackgroundColor(ColorConstants.BLACK)
                    .setBorder(Border.NO_BORDER));

            itemsTable.addCell(createCell("SERVICE", 1, TextAlignment.RIGHT)
                    .setBold()
                    .setFontSize(7)
                    .setFontColor(ColorConstants.WHITE)
                    .setBackgroundColor(ColorConstants.BLACK)
                    .setBorder(Border.NO_BORDER));

            itemsTable.addCell(createCell("AMOUNT TO PAY (UGX)", 1, TextAlignment.RIGHT)
                    .setBold()
                    .setFontSize(7)
                    .setFontColor(ColorConstants.WHITE)
                    .setBackgroundColor(ColorConstants.BLACK)
                    .setBorder(Border.NO_BORDER));

            itemsTable.addCell(createCell("AMOUNT PAID (UGX)", 1, TextAlignment.RIGHT)
                    .setBold()
                    .setFontSize(7)
                    .setFontColor(ColorConstants.WHITE)
                    .setBackgroundColor(ColorConstants.BLACK)
                    .setBorder(Border.NO_BORDER));

            itemsTable.addCell(createCell("BALANCE DUE (UGX)", 1, TextAlignment.RIGHT)
                    .setBold()
                    .setFontSize(7)
                    .setFontColor(ColorConstants.WHITE)
                    .setBackgroundColor(ColorConstants.BLACK)
                    .setBorder(Border.NO_BORDER));



            boolean isEvenRow = false;
            CompassionDebtResult result = getAllInvoicesWithDebtAndCompassion();

            List<InvoiceDTO> invoiceDTOs = result.invoices();

            BigDecimal totalBalanceDue = result.totalBalanceDue();


            for (InvoiceDTO invoiceDto : invoiceDTOs) {
                com.itextpdf.kernel.colors.Color rowColor = isEvenRow
                        ? ColorConstants.WHITE
                        : ColorConstants.LIGHT_GRAY;

                itemsTable.addCell(createCell(invoiceDto.patient.patientFirstName.toUpperCase() + " " + invoiceDto.patient.patientSecondName.toUpperCase() , 1, TextAlignment.LEFT)
                        .setFontSize(7)
                        .setBackgroundColor(rowColor)
                        .setBorder(Border.NO_BORDER)
                        .setBorderBottom(new SolidBorder(1)));

                itemsTable.addCell(createCell(String.valueOf("MEDICAL BILLS"), 1, TextAlignment.RIGHT)
                        .setFontSize(7)
                        .setBackgroundColor(rowColor)
                        .setBorder(Border.NO_BORDER)
                        .setBorderBottom(new SolidBorder(1)));

                itemsTable.addCell(createCell(String.valueOf(invoiceDto.patient.patientGender), 1, TextAlignment.RIGHT)
                        .setFontSize(7)
                        .setBackgroundColor(rowColor)
                        .setBorder(Border.NO_BORDER)
                        .setBorderBottom(new SolidBorder(1)));

                itemsTable.addCell(createCell(String.valueOf(invoiceDto.patient.patientGender), 1, TextAlignment.RIGHT)
                        .setFontSize(7)
                        .setBackgroundColor(rowColor)
                        .setBorder(Border.NO_BORDER)
                        .setBorderBottom(new SolidBorder(1)));

                itemsTable.addCell(createCell(String.valueOf(totalBalanceDue), 1, TextAlignment.RIGHT)
                        .setFontSize(7)
                        .setBackgroundColor(rowColor)
                        .setBorder(Border.NO_BORDER)
                        .setBorderBottom(new SolidBorder(1)));

                isEvenRow = !isEvenRow;
            }

            // Add rows for TreatmentRequested
            /*for (TreatmentRequested treatmentRequested : invoice.visit.getTreatmentRequested()) {
                com.itextpdf.kernel.colors.Color rowColor = isEvenRow
                        ? ColorConstants.WHITE
                        : ColorConstants.LIGHT_GRAY;

                itemsTable.addCell(createCell(treatmentRequested.itemName.toUpperCase(), 1, TextAlignment.LEFT)
                        .setFontSize(7)
                        .setBackgroundColor(rowColor)
                        .setBorder(Border.NO_BORDER)
                        .setBorderBottom(new SolidBorder(1)));

                itemsTable.addCell(createCell(treatmentRequested.quantity.toString(), 1, TextAlignment.RIGHT)
                        .setFontSize(7)
                        .setBackgroundColor(rowColor)
                        .setBorder(Border.NO_BORDER)
                        .setBorderBottom(new SolidBorder(1)));


                itemsTable.addCell(createCell(String.valueOf(treatmentRequested.unitSellingPrice), 1, TextAlignment.RIGHT)
                        .setFontSize(7)
                        .setBackgroundColor(rowColor)
                        .setBorder(Border.NO_BORDER)
                        .setBorderBottom(new SolidBorder(1)));

                itemsTable.addCell(createCell(String.valueOf(treatmentRequested.totalAmount), 1, TextAlignment.RIGHT)
                        .setFontSize(7)
                        .setBackgroundColor(rowColor)
                        .setBorder(Border.NO_BORDER)
                        .setBorderBottom(new SolidBorder(1)));

                isEvenRow = !isEvenRow;
            }*/

            document.add(itemsTable);



            // Add totals table
            Table totalsTable = new Table(new float[]{4, 2, 2});
            totalsTable.setWidth(UnitValue.createPercentValue(100));

            //PatientGroupDTO patientDTO = patientGroupService.getPatientGroupById(1);


            Cell notesCell1 = new Cell(6, 1)
                    .add(new Paragraph("\n IMPRESSION / DIAGNOSIS: " ))
                    .setTextAlignment(TextAlignment.LEFT)
                    .setFontSize(7)
                    .setBorder(Border.NO_BORDER)
                    .setVerticalAlignment(VerticalAlignment.TOP);
            totalsTable.addCell(notesCell1);

            BigDecimal totalDebt = result.totalBalanceDue();

            // Add subtotal row
            totalsTable.addCell(createCell("SUBTOTAL:", 1, TextAlignment.LEFT)
                    .setFontSize(7)
                    .setBorder(Border.NO_BORDER)
                    .setBorderBottom(new SolidBorder(1))
                    .setBold()
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY));
            totalsTable.addCell(createCell(String.valueOf(totalDebt), 1, TextAlignment.RIGHT)
                    .setFontSize(7)
                    .setBorder(Border.NO_BORDER)
                    .setBorderBottom(new SolidBorder(1))
                    .setBold()
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY));

            // Add discount row
            totalsTable.addCell(createCell("DISCOUNT:", 1, TextAlignment.LEFT)
                    .setFontSize(7)
                    .setBorder(Border.NO_BORDER)
                    .setBorderBottom(new SolidBorder(1)));
            totalsTable.addCell(createCell("50000", 1, TextAlignment.RIGHT)
                    .setFontSize(7)
                    .setBorder(Border.NO_BORDER)
                    .setBorderBottom(new SolidBorder(1)));

            // Add tax row
            totalsTable.addCell(createCell("TAX:", 1, TextAlignment.LEFT)
                    .setFontSize(7)
                    .setBorder(Border.NO_BORDER)
                    .setBorderBottom(new SolidBorder(1)));
            totalsTable.addCell(createCell("1000000", 1, TextAlignment.RIGHT)
                    .setFontSize(7)
                    .setBorder(Border.NO_BORDER)
                    .setBorderBottom(new SolidBorder(1)));

            // Add total amount row
            totalsTable.addCell(createCell("TOTAL AMOUNT:", 1, TextAlignment.LEFT)
                    .setBold()
                    .setFontSize(7)
                    .setBorder(Border.NO_BORDER)
                    .setBorderBottom(new SolidBorder(1))
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY));
            totalsTable.addCell(createCell("500000", 1, TextAlignment.RIGHT)
                    .setBold()
                    .setFontSize(7)
                    .setBorder(Border.NO_BORDER)
                    .setBorderBottom(new SolidBorder(1))
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY));

            // Add amount paid row
            totalsTable.addCell(createCell("AMOUNT PAID:", 1, TextAlignment.LEFT)
                    .setFontSize(7)
                    .setBorder(Border.NO_BORDER)
                    .setBorderBottom(new SolidBorder(1)));
            totalsTable.addCell(createCell("700000", 1, TextAlignment.RIGHT)
                    .setFontSize(7)
                    .setBorder(Border.NO_BORDER)
                    .setBorderBottom(new SolidBorder(1)));

            // Add balance due row
            totalsTable.addCell(createCell("BALANCE DUE:", 1, TextAlignment.LEFT)
                    .setBold()
                    .setFontSize(7)
                    .setBorder(Border.NO_BORDER)
                    .setBorderBottom(new SolidBorder(1))
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY));
            totalsTable.addCell(createCell("1200000", 1, TextAlignment.RIGHT)
                    .setBold()
                    .setFontSize(7)
                    .setBorder(Border.NO_BORDER)
                    .setBorderBottom(new SolidBorder(1))
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY));

            document.add(totalsTable);

            // Close the document
            document.close();

            // Return the PDF as a response
            byte[] pdfBytes = baos.toByteArray();
            return Response.ok(new ByteArrayInputStream(pdfBytes))
                    .header("Content-Disposition", "attachment; filename=invoice.pdf")
                    .type("application/pdf")
                    .build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }























































    @Transactional
    public int findMaxInvoiceNoReturnInt() {
        return invoiceRepository.listAll(Sort.descending("invoicePlainNo"))
                .stream()
                .map(invoice -> invoice.invoicePlainNo)
                .findFirst()
                .orElse(0);
    }

    @Transactional
    public String generateRandomReferenceNo(int length) {
        // Define characters that can be used in the password
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder reference = new StringBuilder();
        // Generate a random reference of the specified length
        for (int i = 0; i < length; i++) {
            int randomIndex = (int) (Math.random() * characters.length());
            reference.append(characters.charAt(randomIndex));
        }

        return reference.toString();
    }

    @Transactional
    public BigDecimal checkIfConsultationWasDone(Long visitId) {
        // Check if initial vitals were taken
        boolean vitalsTaken = InitialTriageVitals.find("visit.id = ?1", visitId).firstResultOptional().isPresent();

        // Check if any treatments were given
        boolean treatmentsGiven = TreatmentRequested.find("visit.id = ?1", visitId).firstResultOptional().isPresent();

        // Check if any procedures were requested
        boolean proceduresDone = ProcedureRequested.find("visit.id = ?1", visitId).firstResultOptional().isPresent();

        boolean consultation = Consultation.find("visit.id = ?1", visitId).firstResultOptional().isPresent();

        // If any of the above conditions are true, return 10,000 shillings
        if (consultation) {
            return new BigDecimal("10000");
        }

        // Otherwise, return 0
        return BigDecimal.ZERO;
    }
    @Transactional
    public Map<String, BigDecimal> getInvoiceSubTotal(Long visitId) {
    
        /* =========================
           FETCH DATA
           ========================= */
    
        List<ProcedureRequested> scanProcedures = ProcedureRequested.find(
                "category = ?1 and visit.id = ?2 ORDER BY id DESC",
                "imaging", visitId
        ).list();
    
        List<ProcedureRequested> labTestsProcedures = ProcedureRequested.find(
                "category = ?1 and visit.id = ?2 ORDER BY id DESC",
                "labtest", visitId
        ).list();
    
        List<ProcedureRequested> consultationProcedures = ProcedureRequested.find(
                "category = ?1 and visit.id = ?2 ORDER BY id DESC",
                "consultation", visitId
        ).list();
    
        List<ProcedureRequested> allProcedures = ProcedureRequested.find(
                "visit.id = ?1 ORDER BY id DESC",
                visitId
        ).list();
    
        List<ProcedureRequested> otherProcedures = ProcedureRequested.find(
                "category NOT IN (?1, ?2, ?3) and visit.id = ?4 ORDER BY id DESC",
                "labtest", "imaging", "consultation", visitId
        ).list();
    
        List<TreatmentRequested> treatmentGive = TreatmentRequested.find(
                "visit.id = ?1 ORDER BY id DESC",
                visitId
        ).list();

        List<VisitSundry> visitSundries = VisitSundry.find(
                "patientVisitId = ?1 ORDER BY id DESC",
                visitId
        ).list();
    
    
        /* =========================
           SELLING TOTALS (PATIENT)
           ========================= */
    
        BigDecimal consultationFee = consultationProcedures.stream()
                .map(p -> p.totalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    
        BigDecimal ultrasoundTotalAmount = scanProcedures.stream()
                .map(p -> p.totalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    
        BigDecimal labTotalAmount = labTestsProcedures.stream()
                .map(p -> p.totalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    
        BigDecimal otherProcedureTotalAmount = otherProcedures.stream()
                .map(p -> p.totalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    
        BigDecimal treatmentSellingTotal = treatmentGive.stream()
                .filter(TreatmentRequested::countsTowardInvoice)
                .map(t -> t.totalAmount != null ? t.totalAmount : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal sundriesSellingTotal = visitSundries.stream()
                .map(this::resolveVisitSundrySellingTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal sundriesCostTotal = visitSundries.stream()
                .map(this::resolveVisitSundryCostTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        treatmentSellingTotal = treatmentSellingTotal.add(sundriesSellingTotal);
    
        BigDecimal invoiceSubtotal = labTotalAmount
                .add(ultrasoundTotalAmount)
                .add(consultationFee)
                .add(otherProcedureTotalAmount)
                .add(treatmentSellingTotal);
    
    
        /* =========================
           COST TOTALS (INTERNAL)
           ========================= */
    
        BigDecimal procedureCostTotal = allProcedures.stream()
                .map(p -> {
                    // Use zero if procedure is null or unitCostPrice is null
                    if (p.procedure != null && p.procedure.unitCostPrice != null) {
                        return p.procedure.unitCostPrice.multiply(BigDecimal.valueOf(p.quantity));
                    }
                    return BigDecimal.ZERO; // Return zero when unitCostPrice is null
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    
        BigDecimal treatmentCostTotal = treatmentGive.stream()
                .filter(TreatmentRequested::countsTowardInvoice)
                .map(t -> {
                    // Use zero if unitBuy or quantity is null
                    if (t.unitBuy != null && t.quantity != null) {
                        return t.unitBuy.multiply(t.quantity);
                    }
                    return BigDecimal.ZERO; // Return zero when unitBuy is null
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .add(sundriesCostTotal);
    
        BigDecimal grandCostTotal = procedureCostTotal.add(treatmentCostTotal);

        BigDecimal procedureSellingTotal = labTotalAmount
                .add(ultrasoundTotalAmount)
                .add(consultationFee)
                .add(otherProcedureTotalAmount);
    
    
        /* =========================
           PROFIT
           ========================= */
    
        
    
    
        /* =========================
           VISIT & INVOICE
           ========================= */
    
        PatientVisit visit = patientVisitRepository.findById(visitId);
        if (visit == null) {
            throw new IllegalArgumentException("Visit not found.");
        }
    
        Invoice invoice;
        if (visit.invoice == null || visit.invoice.isEmpty()) {
    
            invoice = new Invoice();
            invoice.visit = visit;
            invoice.patient = visit.patient;
            invoice.tin = "185 7564 3489";
            invoice.notes = "Type in a brief note";
            invoice.dateOfInvoice = LocalDate.now();
            invoice.timeOfCreation = LocalTime.now();
            invoice.toName = visit.patient.patientFirstName + " " + visit.patient.patientSecondName;
            invoice.fromName = "VENERANDA MEDICAL";
            invoice.fromAddress = "Bugogo Town Council-Kyegegwa District";
            invoice.toAddress = "Bugogo Town Council-Kyegegwa District";
            invoice.companyLogo = "https://firebasestorage.googleapis.com/v0/b/newstorageforuplodapp.appspot.com/o/images%2FAsset%201.png";
            invoice.documentTitle = "INVOICE";
            invoice.invoicePlainNo = findMaxInvoiceNoReturnInt() + 1;
            invoice.invoiceNo = "VMDINV-" + invoice.invoicePlainNo;
            invoice.reference = generateRandomReferenceNo(20);
    
            invoice.discount = BigDecimal.ZERO;
            invoice.tax = BigDecimal.ZERO;
    
            invoiceRepository.persist(invoice);
    
            if (visit.invoice == null) {
                visit.invoice = new ArrayList<>();
            }
            visit.invoice.add(invoice);
            patientVisitRepository.persist(visit);
        }
    
        invoice = visit.invoice.get(0);
    
    
        /* =========================
           UPDATE INVOICE
           ========================= */
    
        invoice.subTotal = invoiceSubtotal;
    
        BigDecimal totalAfterDiscount = invoiceSubtotal.subtract(invoice.discount);
        BigDecimal totalAmount = totalAfterDiscount.add(invoice.tax);
    
        invoice.totalAmount = totalAmount;
        invoice.amountPaid = paymentService.getTotalPaymentOfInvoice(invoice.id);
        invoice.balanceDue = totalAmount.subtract(invoice.amountPaid);
    
        invoiceRepository.persist(invoice);
    
    
        /* =========================
           UPDATE VISIT
           ========================= */
    
        visit.subTotal = invoice.subTotal;
        visit.totalAmount = invoice.totalAmount;
        visit.amountPaid = invoice.amountPaid;
        visit.balanceDue = invoice.balanceDue;
        syncVisitFinancialBreakdown(
                visit,
                invoice,
                procedureSellingTotal,
                treatmentSellingTotal,
                procedureCostTotal,
                treatmentCostTotal,
                grandCostTotal);
    
        patientVisitRepository.persist(visit);
    
    
        /* =========================
           PATIENT TOTAL DUE
           ========================= */
    
        List<Invoice> allInvoices = Invoice.find(
                "patient.id = ?1 ORDER BY id DESC",
                visit.patient.id
        ).list();
    
        BigDecimal totalAmountDue = allInvoices.stream()
                .map(i -> i.balanceDue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    
        patientService.updateTotalAmountDue(visit.patient, totalAmountDue);

        BigDecimal grossProfit = totalAmount.subtract(grandCostTotal);
    
    
        /* =========================
           RESPONSE MAP
           ========================= */
    
        Map<String, BigDecimal> totalCostMap = new HashMap<>();
    
        // Selling
        totalCostMap.put("InvoiceSubtotal", invoiceSubtotal);
        totalCostMap.put("TotalAmount", invoice.totalAmount);
        totalCostMap.put("Discount", invoice.discount);
        totalCostMap.put("Tax", invoice.tax);
        totalCostMap.put("AmountPaid", invoice.amountPaid);
        totalCostMap.put("BalanceDue", invoice.balanceDue);
    
        // Cost & Profit (ADMIN ONLY)
        totalCostMap.put("TotalCostPrice", grandCostTotal);
        totalCostMap.put("GrossProfit", grossProfit);
    
        totalCostMap.put("InvoiceId", BigDecimal.valueOf(invoice.id));
        totalCostMap.put("TotalAmountDue", totalAmountDue);
        totalCostMap.put("UltrasoundTotal", ultrasoundTotalAmount);
        totalCostMap.put("LabTestTotal", labTotalAmount);
        totalCostMap.put("ConsultationFee", consultationFee);
        totalCostMap.put("OtherProcedureCost", otherProcedureTotalAmount);
        totalCostMap.put("TreatmentTotalCost", treatmentSellingTotal);
        totalCostMap.put("ProcedureSellingTotal", procedureSellingTotal);
        totalCostMap.put("TreatmentSellingTotal", treatmentSellingTotal);
        totalCostMap.put("ProcedureCostTotal", procedureCostTotal);
        totalCostMap.put("TreatmentCostTotal", treatmentCostTotal);


        
    
        return totalCostMap;
    }

    private static BigDecimal nz(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private static void syncVisitFinancialBreakdown(
            PatientVisit visit,
            Invoice invoice,
            BigDecimal procedureSellingTotal,
            BigDecimal treatmentSellingTotal,
            BigDecimal procedureCostTotal,
            BigDecimal treatmentCostTotal,
            BigDecimal grandCostTotal
    ) {
        if (visit == null || invoice == null) {
            return;
        }
        visit.subTotal = invoice.subTotal;
        visit.totalAmount = invoice.totalAmount;
        visit.totalSell = invoice.totalAmount;
        visit.tax = nz(invoice.tax);
        visit.discount = nz(invoice.discount);
        visit.amountPaid = invoice.amountPaid;
        visit.balanceDue = invoice.balanceDue;
        visit.totalCost = nz(grandCostTotal);
        visit.treatmentTotalSell = nz(treatmentSellingTotal);
        visit.totalSellForProcedure = nz(procedureSellingTotal);
        visit.totalCostOfProcedures = nz(procedureCostTotal);
        visit.totalCostOfTreatment = nz(treatmentCostTotal);
    }

    private BigDecimal resolveVisitSundrySellingTotal(VisitSundry row) {
        if (row == null) {
            return BigDecimal.ZERO;
        }
        if (row.lineTotal != null && row.lineTotal.compareTo(BigDecimal.ZERO) > 0) {
            return row.lineTotal;
        }
        BigDecimal qty = nz(row.quantityUsed);
        if (row.unitSellingPrice != null && row.unitSellingPrice.compareTo(BigDecimal.ZERO) > 0) {
            return qty.multiply(row.unitSellingPrice);
        }
        if (row.stockBatchId != null) {
            StockBatch batch = StockBatch.findById(row.stockBatchId);
            if (batch != null && batch.unitSellingPrice != null) {
                return qty.multiply(batch.unitSellingPrice);
            }
        }
        if (row.itemId != null) {
            Item item = Item.findById(row.itemId);
            if (item != null && item.sellingPrice != null) {
                return qty.multiply(item.sellingPrice);
            }
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal resolveVisitSundryCostTotal(VisitSundry row) {
        if (row == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal qty = nz(row.quantityUsed);
        if (row.unitCostPrice != null && row.unitCostPrice.compareTo(BigDecimal.ZERO) > 0) {
            return qty.multiply(row.unitCostPrice);
        }
        if (row.stockBatchId != null) {
            StockBatch batch = StockBatch.findById(row.stockBatchId);
            if (batch != null && batch.unitCostPrice != null) {
                return qty.multiply(batch.unitCostPrice);
            }
        }
        if (row.itemId != null) {
            Item item = Item.findById(row.itemId);
            if (item != null && item.costPrice != null) {
                return qty.multiply(item.costPrice);
            }
        }
        return BigDecimal.ZERO;
    }
    

    @Transactional
    public Response deleteInvoice(Long id) {
        try {
            // Execute the custom SQL query to delete the payment
            int rowsDeleted = invoiceRepository.deleteInvoiceById(id);

            // Check if any rows were deleted
            if (rowsDeleted > 0) {
                return Response.ok(new ResponseMessage(ActionMessages.DELETED.label)).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ResponseMessage("invoice not found", null))
                        .build();
            }
        } catch (Exception e) {
            // Log the error and return a 500 response
            System.err.println("Error deleting invoice: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ResponseMessage("Failed to delete invoice: " + e.getMessage(), null))
                    .build();
        }
    }


    @Transactional
    public Map<String, BigDecimal> getTotalPatientBalanceDue(Long patientId) {

        List<Invoice> allInvoices = Invoice.find(
                "patient.id = ?1 ORDER BY id DESC",
                patientId
        ).list();

        BigDecimal totalAmountDue = allInvoices.stream()
                .map(invoice -> invoice.balanceDue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Return as a map with keys for clarity
        Map<String, BigDecimal> totalCostMap = new HashMap<>();

        totalCostMap.put("TotalAmountDue", totalAmountDue);

        return totalCostMap;
    }




    private Image getLogo() {
        return facilityPdfLogoService.createLogoImage();
    }

    private Image getLogoForInvoice(Invoice invoice) {
        if (invoice != null && invoice.companyLogo != null && !invoice.companyLogo.isBlank()) {
            return facilityPdfLogoService.createLogoImage(invoice.companyLogo);
        }
        return getLogo();
    }


   /* public static Image getLogo() {
        try {
            // Replace with your logo path or URL
            String logoPath = "src/main/resources/logo.png"; // Local file
            // String logoPath = "https://example.com/logo.png"; // External URL

            // Load the image
            ImageData imageData = ImageDataFactory.create(logoPath);

            // Create the Image object
            Image logo = new Image(imageData);

            // Scale and align the logo
            logo.scaleToFit(80, 80);
            logo.setHorizontalAlignment(HorizontalAlignment.CENTER);

            return logo;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load the logo image.", e);
        }
    }*/













        @Transactional
        public Response generateAndReturnInvoicePdf(Long visitId) {
            try {
                // Find the patient visit
                PatientVisit visit = PatientVisit.findById(visitId);
                if (visit == null) {
                    throw new IllegalArgumentException("Visit not found.");
                }
    
                // Ensure visit.invoice is not null and contains at least one invoice
                if (visit.invoice == null || visit.invoice.isEmpty()) {
                    Invoice invoice = createInvoice(visitId); // Create a new invoice
                    visit.invoice.add(invoice); // Add invoice to visit (if applicable)
                    visit.persist(); // Save the changes
    
                }
    
                // Get the first invoice from the list (or handle multiple invoices as needed)
                Invoice invoice = visit.invoice.get(0); // Assuming visit.invoice is a List<Invoice>
    
                // Ensure the invoice is not null
                if (invoice == null) {
                    throw new IllegalArgumentException("Invoice not found.");
                }
    
                BigDecimal totalPayments = paymentService.getTotalPaymentOfVisit(visitId);
    
                if (totalPayments != null && totalPayments.compareTo(BigDecimal.ZERO) > 0) {
                    invoice.invoiceStatus = "RECEIPT";
                    invoiceRepository.persist(invoice);
                } else {
                    invoice.invoiceStatus = "INVOICE";
                    invoiceRepository.persist(invoice);
                }
    
    
                // Create the PDF document
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PdfWriter pdfWriter = new PdfWriter(baos);
                PdfDocument pdfDocument = new PdfDocument(pdfWriter);
                pdfDocument.addEventHandler(PdfDocumentEvent.END_PAGE, new FooterHelperInvoice(facilityBranding()));
    
                Document document = new Document(pdfDocument);
                document.setMargins(36, 36, 90, 36);
    
    
    
                // Add invoice title
                Table invoiceTitle = new Table(new float[]{1});
                invoiceTitle.setWidth(UnitValue.createPercentValue(100));
                invoiceTitle.addCell(new Cell()
                        .add(new Div()
                                .setBorderBottom(new SolidBorder(1)) // Underline (1px solid line)
                                .setPaddingBottom(2) // Space between text and underline
                                .add(new Paragraph(invoice.invoiceStatus)
                                        .setBold()
                                        .setFontSize(11)
                                        .setTextAlignment(TextAlignment.CENTER)
                                )
                        )
                        .add(new Paragraph(facilityBranding().financeHeaderLine())
                                .setFontSize(7)
                                //.setItalic()
                                .setMarginTop(3)
                                .setTextAlignment(TextAlignment.CENTER))
                        .setBorder(Border.NO_BORDER)
                        .setPaddingBottom(15)
                );
                document.add(invoiceTitle);
    
                // Add header: Logo and Invoice Details
                Table headerTable = new Table(new float[]{1, 1, 1, 2, 1});
                headerTable.setWidth(UnitValue.createPercentValue(100));
    
                // Add logo
                headerTable.addCell(new Cell()
                        .add(getLogo().setWidth(79).setHeight(68))
                        .setBorder(Border.NO_BORDER)
                        .setHorizontalAlignment(HorizontalAlignment.LEFT)
                        .setVerticalAlignment(VerticalAlignment.TOP)
                        .setPaddingTop(-7)
                        .setPaddingLeft(-22)
                );
    
                // Add invoice details
                headerTable.addCell(new Cell()
                        .add(new Paragraph("FROM: ").setFontSize(7).setTextAlignment(TextAlignment.LEFT))
                        .add(new Paragraph("ADDRESS: ").setFontSize(7).setTextAlignment(TextAlignment.LEFT))
                        .add(new Paragraph("TO: ").setFontSize(7).setTextAlignment(TextAlignment.LEFT))
                        .add(new Paragraph("ADDRESS: ").setFontSize(7).setTextAlignment(TextAlignment.LEFT))
                        .setBorder(Border.NO_BORDER)
                );
    
                headerTable.addCell(new Cell()
                        .add(new Paragraph("VENERANDA MEDICAL").setFontSize(7).setTextAlignment(TextAlignment.LEFT))
                        .add(new Paragraph("BUGOGO TOWN COUNCIL, KYEGEGWA DISTRICT").setFontSize(7).setTextAlignment(TextAlignment.LEFT))
                        .add(new Paragraph(invoice.visit.patient.patientFirstName.toUpperCase() + " " + invoice.visit.patient.patientSecondName.toUpperCase())
                                .setFontSize(7).setTextAlignment(TextAlignment.LEFT))
                        .add(new Paragraph()
                                        .add(Optional.ofNullable(invoice.visit.patient.patientAddress)
                                                .map(String::toUpperCase)
                                                .orElse(""))
                                        .setFontSize(7)
                                        .setTextAlignment(TextAlignment.LEFT)
                                )
                        .add(new Paragraph()
                                 .add(Optional.ofNullable(invoice.visit.patient.getPatientGroup())
                                         .map(PatientGroup::getGroupName)
                                         .map(String::toUpperCase)
                                         .orElse(""))
                                 .setFontSize(7)
                                 .setTextAlignment(TextAlignment.LEFT))
    
    
    
    
    
    
                        .setBorder(Border.NO_BORDER)
                        );
    
                headerTable.addCell(new Cell()
                        .add(new Paragraph("NUMBER: ").setFontSize(7).setTextAlignment(TextAlignment.LEFT))
                        .add(new Paragraph("DUE DATE: ").setFontSize(7).setTextAlignment(TextAlignment.LEFT))
                        .add(new Paragraph("DATE: ").setFontSize(7).setTextAlignment(TextAlignment.LEFT))
                        .add(new Paragraph("BALANCE DUE: ").setFontSize(7).setTextAlignment(TextAlignment.LEFT))
                        .setBorder(Border.NO_BORDER)
                );
    
                headerTable.addCell(new Cell()
                        .add(new Paragraph(invoice.invoiceNo).setFontSize(7).setTextAlignment(TextAlignment.RIGHT))
                        .add(new Paragraph(String.valueOf(invoice.dateOfInvoice)).setFontSize(7).setTextAlignment(TextAlignment.RIGHT))
                        .add(new Paragraph(String.valueOf(invoice.dateOfInvoice)).setFontSize(7).setTextAlignment(TextAlignment.RIGHT))
                        .add(new Paragraph(String.valueOf(invoice.balanceDue)).setFontSize(7).setTextAlignment(TextAlignment.RIGHT))
    
                        .setBorder(Border.NO_BORDER)
                );
    
                document.add(headerTable);
    
                // Add items table
                float[] columnWidths = {4, 1, 2, 2};
                Table itemsTable = new Table(columnWidths);
                itemsTable.setWidth(UnitValue.createPercentValue(100));
    
                // Add header with no column lines
                itemsTable.addCell(createCell("ITEM", 1, TextAlignment.LEFT)
                        .setBold()
                        .setFontSize(7)
                        .setFontColor(ColorConstants.WHITE)
                        .setBackgroundColor(ColorConstants.BLACK)
                        .setBorder(Border.NO_BORDER));
    
                itemsTable.addCell(createCell("QTY", 1, TextAlignment.RIGHT)
                        .setBold()
                        .setFontSize(7)
                        .setFontColor(ColorConstants.WHITE)
                        .setBackgroundColor(ColorConstants.BLACK)
                        .setBorder(Border.NO_BORDER));
    
                itemsTable.addCell(createCell("UNIT PRICE (UGX)", 1, TextAlignment.RIGHT)
                        .setBold()
                        .setFontSize(7)
                        .setFontColor(ColorConstants.WHITE)
                        .setBackgroundColor(ColorConstants.BLACK)
                        .setBorder(Border.NO_BORDER));
    
                itemsTable.addCell(createCell("TOTAL (UGX)", 1, TextAlignment.RIGHT)
                        .setBold()
                        .setFontSize(7)
                        .setFontColor(ColorConstants.WHITE)
                        .setBackgroundColor(ColorConstants.BLACK)
                        .setBorder(Border.NO_BORDER));
    
                // Add table rows
                /*for (Consultation consultation : invoice.visit.getConsultation()) {
                    Border bottomBorder = new SolidBorder(1f);
    
                    itemsTable.addCell(createCell("CONSULTATION FEE", 1, TextAlignment.LEFT)
                            .setFontSize(7)
                            .setBorder(Border.NO_BORDER)
                            .setBorderBottom(bottomBorder));
    
                    itemsTable.addCell(createCell("1", 1, TextAlignment.RIGHT)
                            .setFontSize(7)
                            .setBorder(Border.NO_BORDER)
                            .setBorderBottom(bottomBorder));
    
                    itemsTable.addCell(createCell(String.valueOf(consultation.consultationFee), 1, TextAlignment.RIGHT)
                            .setFontSize(7)
                            .setBorder(Border.NO_BORDER)
                            .setBorderBottom(bottomBorder));
    
                    itemsTable.addCell(createCell(String.valueOf(consultation.consultationFee), 1, TextAlignment.RIGHT)
                            .setFontSize(7)
                            .setBorder(Border.NO_BORDER)
                            .setBorderBottom(bottomBorder));
                }*/
    
                // Add rows for ProcedureRequested
                boolean isEvenRow = false;
                assert invoice.visit != null;
                for (ProcedureRequested procedureRequested : invoice.visit.getProceduresRequested()) {
                    com.itextpdf.kernel.colors.Color rowColor = isEvenRow
                            ? ColorConstants.WHITE
                            : ColorConstants.LIGHT_GRAY;
    
                    itemsTable.addCell(createCell(procedureRequested.procedureRequestedName != null ? procedureRequested.procedureRequestedName.toUpperCase() : "", 1, TextAlignment.LEFT)
                            .setFontSize(7)
                            .setBackgroundColor(rowColor)
                            .setBorder(Border.NO_BORDER)
                            .setBorderBottom(new SolidBorder(1)));
    
                    itemsTable.addCell(createCell(String.valueOf(procedureRequested.quantity), 1, TextAlignment.RIGHT)
                            .setFontSize(7)
                            .setBackgroundColor(rowColor)
                            .setBorder(Border.NO_BORDER)
                            .setBorderBottom(new SolidBorder(1)));
    
                    itemsTable.addCell(createCell(String.valueOf(procedureRequested.unitSellingPrice), 1, TextAlignment.RIGHT)
                            .setFontSize(7)
                            .setBackgroundColor(rowColor)
                            .setBorder(Border.NO_BORDER)
                            .setBorderBottom(new SolidBorder(1)));
    
                    itemsTable.addCell(createCell(String.valueOf(procedureRequested.totalAmount), 1, TextAlignment.RIGHT)
                            .setFontSize(7)
                            .setBackgroundColor(rowColor)
                            .setBorder(Border.NO_BORDER)
                            .setBorderBottom(new SolidBorder(1)));
    
                    isEvenRow = !isEvenRow;
                }
    
                // Add rows for TreatmentRequested
                for (TreatmentRequested treatmentRequested : invoice.visit.getTreatmentRequested()) {
                    com.itextpdf.kernel.colors.Color rowColor = isEvenRow
                            ? ColorConstants.WHITE
                            : ColorConstants.LIGHT_GRAY;
    
                    itemsTable.addCell(createCell(treatmentRequested.itemName.toUpperCase(), 1, TextAlignment.LEFT)
                            .setFontSize(7)
                            .setBackgroundColor(rowColor)
                            .setBorder(Border.NO_BORDER)
                            .setBorderBottom(new SolidBorder(1)));
    
                    itemsTable.addCell(createCell(treatmentRequested.quantity.toString(), 1, TextAlignment.RIGHT)
                            .setFontSize(7)
                            .setBackgroundColor(rowColor)
                            .setBorder(Border.NO_BORDER)
                            .setBorderBottom(new SolidBorder(1)));
    
    
                    itemsTable.addCell(createCell(String.valueOf(treatmentRequested.unitSellingPrice), 1, TextAlignment.RIGHT)
                            .setFontSize(7)
                            .setBackgroundColor(rowColor)
                            .setBorder(Border.NO_BORDER)
                            .setBorderBottom(new SolidBorder(1)));
    
                    itemsTable.addCell(createCell(String.valueOf(treatmentRequested.totalAmount), 1, TextAlignment.RIGHT)
                            .setFontSize(7)
                            .setBackgroundColor(rowColor)
                            .setBorder(Border.NO_BORDER)
                            .setBorderBottom(new SolidBorder(1)));
    
                    isEvenRow = !isEvenRow;
                }
    
                document.add(itemsTable);
    
                // Add totals table
                Table totalsTable = new Table(new float[]{4, 2, 2});
                totalsTable.setWidth(UnitValue.createPercentValue(100));
    
                ConsultationDTO consultationDTO = consultationService.getFirstConsultationByVisitId(visitId);
    
    
    
    
                Cell notesCell1 = new Cell(6, 1)
                        .add(new Paragraph("\n IMPRESSION / DIAGNOSIS: " + consultationDTO.diagnosis.toUpperCase()))
                        .setTextAlignment(TextAlignment.LEFT)
                        .setFontSize(7)
                        .setBorder(Border.NO_BORDER)
                        .setVerticalAlignment(VerticalAlignment.TOP);
                totalsTable.addCell(notesCell1);
    
               /* for (Consultation consultation : invoice.visit.getConsultation()) {
    
                // Add notes
                Cell notesCell = new Cell(6, 1)
                        .add(("NOTES: " +"\n"+"PATIENT NAME: " +invoice.visit.patient.patientFirstName.toUpperCase()+" "+invoice.visit.patient.patientSecondName.toUpperCase() +"\n"+ "DIAGNOSIS: "+consultation.diagnosis.toUpperCase() +"\n"+ consultation.medicalHistory.toUpperCase() +"\n" + invoice.notes.toUpperCase()))
                        .setTextAlignment(TextAlignment.LEFT)
                        .setFontSize(7)
                        .setBorder(Border.NO_BORDER)
                        .setVerticalAlignment(VerticalAlignment.TOP);
                totalsTable.addCell(notesCell);
    
                }*/
    
                // Add subtotal row
                totalsTable.addCell(createCell("SUBTOTAL:", 1, TextAlignment.LEFT)
                        .setFontSize(7)
                        .setBorder(Border.NO_BORDER)
                        .setBorderBottom(new SolidBorder(1))
                        .setBold()
                        .setBackgroundColor(ColorConstants.LIGHT_GRAY));
                totalsTable.addCell(createCell(invoice.subTotal.toString(), 1, TextAlignment.RIGHT)
                        .setFontSize(7)
                        .setBorder(Border.NO_BORDER)
                        .setBorderBottom(new SolidBorder(1))
                        .setBold()
                        .setBackgroundColor(ColorConstants.LIGHT_GRAY));
    
                // Add discount row
                totalsTable.addCell(createCell("DISCOUNT:", 1, TextAlignment.LEFT)
                        .setFontSize(7)
                        .setBorder(Border.NO_BORDER)
                        .setBorderBottom(new SolidBorder(1)));
                totalsTable.addCell(createCell(invoice.discount.toString(), 1, TextAlignment.RIGHT)
                        .setFontSize(7)
                        .setBorder(Border.NO_BORDER)
                        .setBorderBottom(new SolidBorder(1)));
    
                // Add tax row
                totalsTable.addCell(createCell("TAX:", 1, TextAlignment.LEFT)
                        .setFontSize(7)
                        .setBorder(Border.NO_BORDER)
                        .setBorderBottom(new SolidBorder(1)));
                totalsTable.addCell(createCell(invoice.tax.toString(), 1, TextAlignment.RIGHT)
                        .setFontSize(7)
                        .setBorder(Border.NO_BORDER)
                        .setBorderBottom(new SolidBorder(1)));
    
                // Add total amount row
                totalsTable.addCell(createCell("TOTAL AMOUNT:", 1, TextAlignment.LEFT)
                        .setBold()
                        .setFontSize(7)
                        .setBorder(Border.NO_BORDER)
                        .setBorderBottom(new SolidBorder(1))
                        .setBackgroundColor(ColorConstants.LIGHT_GRAY));
                totalsTable.addCell(createCell(invoice.totalAmount.toString(), 1, TextAlignment.RIGHT)
                        .setBold()
                        .setFontSize(7)
                        .setBorder(Border.NO_BORDER)
                        .setBorderBottom(new SolidBorder(1))
                        .setBackgroundColor(ColorConstants.LIGHT_GRAY));
    
                // Add amount paid row
                totalsTable.addCell(createCell("AMOUNT PAID:", 1, TextAlignment.LEFT)
                        .setFontSize(7)
                        .setBorder(Border.NO_BORDER)
                        .setBorderBottom(new SolidBorder(1)));
                totalsTable.addCell(createCell(invoice.amountPaid.toString(), 1, TextAlignment.RIGHT)
                        .setFontSize(7)
                        .setBorder(Border.NO_BORDER)
                        .setBorderBottom(new SolidBorder(1)));
    
                // Add balance due row
                totalsTable.addCell(createCell("BALANCE DUE:", 1, TextAlignment.LEFT)
                        .setBold()
                        .setFontSize(7)
                        .setBorder(Border.NO_BORDER)
                        .setBorderBottom(new SolidBorder(1))
                        .setBackgroundColor(ColorConstants.LIGHT_GRAY));
                totalsTable.addCell(createCell(invoice.balanceDue.toString(), 1, TextAlignment.RIGHT)
                        .setBold()
                        .setFontSize(7)
                        .setBorder(Border.NO_BORDER)
                        .setBorderBottom(new SolidBorder(1))
                        .setBackgroundColor(ColorConstants.LIGHT_GRAY));
    
                document.add(totalsTable);
    
                // Close the document
                document.close();
    
                // Return the PDF as a response
                byte[] pdfBytes = baos.toByteArray();
                return Response.ok(new ByteArrayInputStream(pdfBytes))
                        .header("Content-Disposition", "attachment; filename=invoice.pdf")
                        .type("application/pdf")
                        .build();
    
            } catch (Exception e) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        }










        @Transactional
        public Response generateAndReturnMedicalStatementPdfForAvisit(Long visitId) {
            return generateAndReturnMedicalStatementPdfForAvisit(visitId, null);
        }

        @Transactional
        public Response generateAndReturnMedicalStatementPdfForAvisit(Long visitId, StatementPdfRequest request) {
            try {
                // Find the patient visit
                PatientVisit visit = PatientVisit.findById(visitId);
                if (visit == null) {
                    throw new IllegalArgumentException("Visit not found.");
                }
    
                // Ensure visit.invoice is not null and contains at least one invoice
                if (visit.invoice == null || visit.invoice.isEmpty()) {
                    Invoice invoice = createInvoice(visitId); // Create a new invoice
                    visit.invoice.add(invoice); // Add invoice to visit (if applicable)
                    visit.persist(); // Save the changes
    
                }
    
                   // Find the patient
                   Patient patient = Patient.findById(visit.patient.id);
                   if (patient == null) {
                       return Response.status(Response.Status.NOT_FOUND)
                               .entity(new ResponseMessage("patient not found for ID: " + visit.patient.id))
                               .build();
                   }
       
     
                   
    
    
                // Get the first invoice from the list (or handle multiple invoices as needed)
                Invoice invoice = visit.invoice.get(0); // Assuming visit.invoice is a List<Invoice>
    
                // Ensure the invoice is not null
                if (invoice == null) {
                    throw new IllegalArgumentException("Invoice not found.");
                }
    
                //BigDecimal totalPayments = paymentService.getTotalPaymentOfVisit(visitId);
    
                //if (totalPayments != null && totalPayments.compareTo(BigDecimal.ZERO) > 0) {
                    //invoice.invoiceStatus = "RECEIPT";
                    //invoiceRepository.persist(invoice);
               // } else {
                String documentTitle = resolveStatementDocumentTitle(request);
                invoice.invoiceStatus = documentTitle;
                invoice.documentTitle = documentTitle;
                invoiceRepository.persist(invoice);
                //}
    
    
                // Create the PDF document
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PdfWriter pdfWriter = new PdfWriter(baos);
                PdfDocument pdfDocument = new PdfDocument(pdfWriter);
                boolean includeFooter = resolveIncludeFooter(request);
                if (includeFooter) {
                    pdfDocument.addEventHandler(PdfDocumentEvent.END_PAGE, new FooterHelperInvoice(facilityBranding()));
                }
    
                Document document = new Document(pdfDocument);
                // Reserve bottom space for footer only when it is included
                document.setMargins(36, 36, includeFooter ? 90 : 36, 36);
    
    
    
                // Add invoice title
                Table invoiceTitle = new Table(new float[]{1});
                invoiceTitle.setWidth(UnitValue.createPercentValue(100));
                invoiceTitle.addCell(new Cell()
                        .add(new Div()
                                .setBorderBottom(new SolidBorder(1)) // Underline (1px solid line)
                                .setPaddingBottom(2) // Space between text and underline
                                .add(new Paragraph(documentTitle != null ? documentTitle : "SYSTEM GENERATED MEDICAL REPORT")
                                        .setBold()
                                        .setFontSize(11)
                                        .setTextAlignment(TextAlignment.CENTER)
                                )
                        )
                        .add(new Paragraph(facilityBranding().medicalRecordsTitleBlock())
                                .setFontSize(8)
                                //.setItalic()
                                .setMarginTop(3)
                                .setTextAlignment(TextAlignment.CENTER))
                        .setBorder(Border.NO_BORDER)
                        .setPaddingBottom(15)
                );
                document.add(invoiceTitle);
    
        
    
    
                // Header table (unchanged)
                Table headerTable = new Table(new float[]{1, 1, 1, 2, 1});
                headerTable.setWidth(UnitValue.createPercentValue(100));
                headerTable.addCell(new Cell()
                        .add(getLogo().setWidth(55).setHeight(47))
                        .setBorder(Border.NO_BORDER)
                        .setHorizontalAlignment(HorizontalAlignment.LEFT)
                        .setVerticalAlignment(VerticalAlignment.TOP)
                        .setPaddingTop(-7)
                        .setPaddingLeft(-12)
                        .setPaddingRight(0)
                );
                boolean includeNextOfKin = resolveIncludeNextOfKin(request);
                boolean includePatientName = resolveIncludePatientName(request);
                boolean includePatientAddress = resolveIncludePatientAddress(request);
                boolean includePatientGender = resolveIncludePatientGender(request);
                boolean includePatientAge = resolveIncludePatientAge(request);
                boolean includePatientContact = resolveIncludePatientContact(request);
                boolean includeVisitDate = resolveIncludeVisitDate(request);
                boolean includeBalanceDue = resolveIncludeBalanceDue(request);

                Cell headerLabelCell = new Cell().setBorder(Border.NO_BORDER);
                Cell headerValueCell = new Cell().setBorder(Border.NO_BORDER);
                Cell rightLabelCell = new Cell().setBorder(Border.NO_BORDER);
                Cell rightValueCell = new Cell().setBorder(Border.NO_BORDER);

                String patientName = patient.patientFirstName.toUpperCase() + " " + patient.patientSecondName.toUpperCase();
                String nextOfKeen = patient.nextOfKinName != null ? patient.nextOfKinName.toUpperCase() : "N/A";
                BigDecimal balance = patient.totalAmountDue;
                String gender = patient.patientGender != null ? patient.patientGender.toUpperCase() : "N/A";
                String ageText = patient.patientAge != null
                        ? patient.patientAge.toPlainString() + " YRS"
                        : "N/A";
                String addressText = Optional.ofNullable(invoice.visit.patient)
                        .map(p -> p.patientAddress +
                                (p.getPatientGroup() != null
                                        ? " (" + p.patientGroup.groupNameShortForm + ")"
                                        : ""))
                        .map(String::toUpperCase)
                        .orElse("");

                if (includePatientName) {
                    headerLabelCell.add(new Paragraph("CLIENT NAME: ").setFontSize(8).setTextAlignment(TextAlignment.LEFT));
                    headerValueCell.add(new Paragraph(patientName).setFontSize(8).setTextAlignment(TextAlignment.LEFT));
                }
                if (includePatientAddress) {
                    headerLabelCell.add(new Paragraph("ADDRESS: ").setFontSize(8).setTextAlignment(TextAlignment.LEFT));
                    headerValueCell.add(new Paragraph(addressText).setFontSize(8));
                }
                if (includeNextOfKin) {
                    headerLabelCell.add(new Paragraph("NEXT OF KEEN: ").setFontSize(8).setTextAlignment(TextAlignment.LEFT));
                    headerValueCell.add(new Paragraph(nextOfKeen).setFontSize(8).setTextAlignment(TextAlignment.LEFT));
                }
                if (includePatientGender) {
                    rightLabelCell.add(new Paragraph("GENDER: ").setFontSize(8));
                    rightValueCell.add(new Paragraph(gender).setFontSize(8).setTextAlignment(TextAlignment.RIGHT));
                }
                if (includePatientAge) {
                    rightLabelCell.add(new Paragraph("AGE: ").setFontSize(8));
                    rightValueCell.add(new Paragraph(ageText).setFontSize(8).setTextAlignment(TextAlignment.RIGHT));
                }
                if (includePatientContact) {
                    rightLabelCell.add(new Paragraph("CONTACT: ").setFontSize(8));
                    rightValueCell.add(new Paragraph(String.valueOf(patient.patientContact)).setFontSize(8).setTextAlignment(TextAlignment.RIGHT));
                }
                if (includeVisitDate) {
                    rightLabelCell.add(new Paragraph("VISIT DATE: ").setFontSize(8));
                    rightValueCell.add(new Paragraph(String.valueOf(visit.visitDate)).setFontSize(8).setTextAlignment(TextAlignment.RIGHT));
                }
                if (includeBalanceDue) {
                    rightLabelCell.add(new Paragraph("BALANCE DUE: ").setFontSize(8));
                    rightValueCell.add(new Paragraph(String.valueOf(balance)).setFontSize(8).setTextAlignment(TextAlignment.RIGHT));
                }

                headerTable.addCell(headerLabelCell);
                headerTable.addCell(headerValueCell);
                headerTable.addCell(rightLabelCell);
                headerTable.addCell(rightValueCell);
        
                headerTable.setBorderBottom(new SolidBorder(1));
                document.add(headerTable);
    
    
    
    
    
                assert invoice.visit != null;
    
                String vitalsSummary = formatVisitVitalsSummary(invoice.visit);
                boolean hasComplaintOrVitals = invoice.visit.getConsultation().stream()
                        .anyMatch(consultation ->
                                consultation.chiefComplaint != null && !consultation.chiefComplaint.trim().isEmpty())
                        || !vitalsSummary.isEmpty();
    
                // First table: presenting complaints vs vitals (only when data exists)
                if (hasComplaintOrVitals) {
                    float[] complaintsColumnWidths = {5f, 4f};
                    Table complaintsTable = new Table(UnitValue.createPercentArray(complaintsColumnWidths));
                    complaintsTable.setWidth(UnitValue.createPercentValue(100));
    
                    complaintsTable.addCell(createCell("SUMMARY OF PRESENTING COMPLAINTS", 1, TextAlignment.LEFT)
                            .setBold()
                            .setFontSize(7)
                            .setFontColor(ColorConstants.WHITE)
                            .setBackgroundColor(ColorConstants.DARK_GRAY)
                            .setBorder(Border.NO_BORDER));
    
                    complaintsTable.addCell(createCell("VITALS (TRIAGE)", 1, TextAlignment.RIGHT)
                            .setBold()
                            .setFontSize(7)
                            .setFontColor(ColorConstants.WHITE)
                            .setBackgroundColor(ColorConstants.DARK_GRAY)
                            .setBorder(Border.NO_BORDER));
    
                    for (Consultation consultation : invoice.visit.getConsultation()) {
                        String pcText = formatPresentingComplaints(consultation.presentingComplaints);
                        if (pcText.isEmpty()) {
                            pcText = consultation.chiefComplaint != null
                                    ? consultation.chiefComplaint.trim()
                                    : "";
                        }
                        String vitals = vitalsSummary;

                        if (pcText.isEmpty() && vitals.isEmpty()) {
                            continue;
                        }

                        com.itextpdf.kernel.colors.Color rowColor = ColorConstants.WHITE;

                        complaintsTable.addCell(createCell(pcText.toUpperCase(), 1, TextAlignment.LEFT)
                                .setFontSize(7)
                                .setBackgroundColor(rowColor)
                                .setBorder(Border.NO_BORDER)
                                .setBorderBottom(new SolidBorder(1)));
    
                        complaintsTable.addCell(createCell(vitals.toUpperCase(), 1, TextAlignment.RIGHT)
                                .setFontSize(7)
                                .setBackgroundColor(rowColor)
                                .setBorder(Border.NO_BORDER)
                                .setBorderBottom(new SolidBorder(1)));
    
                    }
                    document.add(complaintsTable);
                }
    
                List<ProcedureRequested> statementProcedures = resolveStatementProcedures(invoice.visit, request);
                boolean includeAmountColumns = resolveIncludeAmountColumns(request);
                addStatementServicesTable(document, statementProcedures, includeAmountColumns);
    
                // Diagnosis table (placed after services requested table)
                boolean hasDiagnosisRows = invoice.visit.getConsultation() != null
                        && invoice.visit.getConsultation().stream()
                        .anyMatch(consultation ->
                                consultation.diagnosis != null && !consultation.diagnosis.trim().isEmpty());
                if (hasDiagnosisRows) {
                    Table diagnosisTable = new Table(UnitValue.createPercentArray(new float[]{1f}));
                    diagnosisTable.setWidth(UnitValue.createPercentValue(100));
    
                    diagnosisTable.addCell(createCell("DIAGNOSIS", 1, TextAlignment.LEFT)
                            .setBold()
                            .setFontSize(7)
                            .setFontColor(ColorConstants.WHITE)
                            .setBackgroundColor(ColorConstants.DARK_GRAY)
                            .setBorder(Border.NO_BORDER));
    
                    for (Consultation consultation : invoice.visit.getConsultation()) {
                        String diagnosis = consultation.diagnosis != null
                                ? consultation.diagnosis.trim()
                                : "";
                        if (diagnosis.isEmpty()) {
                            continue;
                        }
    
                        diagnosisTable.addCell(createCell(diagnosis.toUpperCase(), 1, TextAlignment.LEFT)
                                .setFontSize(7)
                                .setBackgroundColor(ColorConstants.WHITE)
                                .setBorder(Border.NO_BORDER)
                                .setBorderBottom(new SolidBorder(1)));
                    }
    
                    document.add(diagnosisTable);
                }
    
                List<TreatmentRequested> statementTreatments = resolveStatementTreatments(invoice.visit, request);
                addStatementDrugsTable(document, statementTreatments, includeAmountColumns);
    
                // Final table: finances and payments (one row of values)
                boolean includePaymentTable = resolveIncludePaymentTable(request);
                if (includePaymentTable) {
                    Table totalsTable = new Table(UnitValue.createPercentArray(new float[]{2f, 2f, 2f, 2f, 2f, 2f}));
                    totalsTable.setWidth(UnitValue.createPercentValue(100));

                    totalsTable.addCell(createCell("SUBTOTAL", 1, TextAlignment.CENTER)
                            .setBold()
                            .setFontSize(7)
                            .setFontColor(ColorConstants.WHITE)
                            .setBackgroundColor(ColorConstants.DARK_GRAY)
                            .setBorder(Border.NO_BORDER));
                    totalsTable.addCell(createCell("TOTAL AMOUNT", 1, TextAlignment.CENTER)
                            .setBold()
                            .setFontSize(7)
                            .setFontColor(ColorConstants.WHITE)
                            .setBackgroundColor(ColorConstants.DARK_GRAY)
                            .setBorder(Border.NO_BORDER));
                    totalsTable.addCell(createCell("DISCOUNT", 1, TextAlignment.CENTER)
                            .setBold()
                            .setFontSize(7)
                            .setFontColor(ColorConstants.WHITE)
                            .setBackgroundColor(ColorConstants.DARK_GRAY)
                            .setBorder(Border.NO_BORDER));
                    totalsTable.addCell(createCell("TAX", 1, TextAlignment.CENTER)
                            .setBold()
                            .setFontSize(7)
                            .setFontColor(ColorConstants.WHITE)
                            .setBackgroundColor(ColorConstants.DARK_GRAY)
                            .setBorder(Border.NO_BORDER));
                    totalsTable.addCell(createCell("AMOUNT PAID", 1, TextAlignment.CENTER)
                            .setBold()
                            .setFontSize(7)
                            .setFontColor(ColorConstants.WHITE)
                            .setBackgroundColor(ColorConstants.DARK_GRAY)
                            .setBorder(Border.NO_BORDER));
                    totalsTable.addCell(createCell("BALANCE", 1, TextAlignment.CENTER)
                            .setBold()
                            .setFontSize(7)
                            .setFontColor(ColorConstants.WHITE)
                            .setBackgroundColor(ColorConstants.DARK_GRAY)
                            .setBorder(Border.NO_BORDER));

                    totalsTable.addCell(createCell(plainNumber(invoice.subTotal), 1, TextAlignment.CENTER)
                            .setFontSize(7)
                            .setBackgroundColor(ColorConstants.WHITE)
                            .setBorder(Border.NO_BORDER)
                            .setBorderBottom(new SolidBorder(1)));
                    totalsTable.addCell(createCell(plainNumber(invoice.totalAmount), 1, TextAlignment.CENTER)
                            .setFontSize(7)
                            .setBackgroundColor(ColorConstants.WHITE)
                            .setBorder(Border.NO_BORDER)
                            .setBorderBottom(new SolidBorder(1)));
                    totalsTable.addCell(createCell(plainNumber(invoice.discount), 1, TextAlignment.CENTER)
                            .setFontSize(7)
                            .setBackgroundColor(ColorConstants.WHITE)
                            .setBorder(Border.NO_BORDER)
                            .setBorderBottom(new SolidBorder(1)));
                    totalsTable.addCell(createCell(plainNumber(invoice.tax), 1, TextAlignment.CENTER)
                            .setFontSize(7)
                            .setBackgroundColor(ColorConstants.WHITE)
                            .setBorder(Border.NO_BORDER)
                            .setBorderBottom(new SolidBorder(1)));
                    totalsTable.addCell(createCell(plainNumber(invoice.amountPaid), 1, TextAlignment.CENTER)
                            .setFontSize(7)
                            .setBackgroundColor(ColorConstants.WHITE)
                            .setBorder(Border.NO_BORDER)
                            .setBorderBottom(new SolidBorder(1)));
                    totalsTable.addCell(createCell(plainNumber(invoice.balanceDue), 1, TextAlignment.CENTER)
                            .setFontSize(7)
                            .setBackgroundColor(ColorConstants.WHITE)
                            .setBorder(Border.NO_BORDER)
                            .setBorderBottom(new SolidBorder(1)));

                    document.add(totalsTable);
                }
    
                // Attending clinician / doctor table
                String attendingClinician = "N/A";
                if (invoice.visit.getConsultation() != null) {
                    attendingClinician = invoice.visit.getConsultation().stream()
                            .map(consultation -> consultation.doneBy)
                            .filter(Objects::nonNull)
                            .map(String::trim)
                            .filter(name -> !name.isEmpty())
                            .findFirst()
                            .orElse("N/A");
                }
    
                Table clinicianTable = new Table(UnitValue.createPercentArray(new float[]{3f, 3f}));
                clinicianTable.setWidth(UnitValue.createPercentValue(100));
                clinicianTable.setMarginTop(8);
    
                clinicianTable.addCell(createCell("ATTENDING CLINICIAN / DOCTOR", 1, TextAlignment.LEFT)
                        .setBold()
                        .setFontSize(7)
                        .setFontColor(ColorConstants.WHITE)
                        .setBackgroundColor(ColorConstants.DARK_GRAY)
                        .setBorder(Border.NO_BORDER));
                clinicianTable.addCell(createCell("SIGNATURE & STAMP", 1, TextAlignment.RIGHT)
                        .setBold()
                        .setFontSize(7)
                        .setFontColor(ColorConstants.WHITE)
                        .setBackgroundColor(ColorConstants.DARK_GRAY)
                        .setBorder(Border.NO_BORDER));
    
                clinicianTable.addCell(createCell("NAME:", 1, TextAlignment.LEFT)
                        .setFontSize(7)
                        .setBackgroundColor(ColorConstants.WHITE)
                        .setBorder(Border.NO_BORDER)
                        .setBorderBottom(new SolidBorder(1)));
                clinicianTable.addCell(createCell("", 1, TextAlignment.RIGHT)
                        .setFontSize(7)
                        .setBackgroundColor(ColorConstants.WHITE)
                        .setBorder(Border.NO_BORDER)
                        .setBorderBottom(new SolidBorder(1)));
    
                document.add(clinicianTable);
    
                // Close the document
                document.close();
    
                // Return the PDF as a response
                byte[] pdfBytes = baos.toByteArray();
                return Response.ok(new ByteArrayInputStream(pdfBytes))
                        .header("Content-Disposition", "attachment; filename=invoice.pdf")
                        .type("application/pdf")
                        .build();
    
            } catch (Exception e) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        }





















    @Transactional
    public Response generateAndReturnMedicalStatementPdfForAvisitForInvoice(Long visitId) {
        try {
            // Find the patient visit
            PatientVisit visit = PatientVisit.findById(visitId);
            if (visit == null) {
                throw new IllegalArgumentException("Visit not found.");
            }

            // Ensure visit.invoice is not null and contains at least one invoice
            if (visit.invoice == null || visit.invoice.isEmpty()) {
                Invoice invoice = createInvoice(visitId); // Create a new invoice
                visit.invoice.add(invoice); // Add invoice to visit (if applicable)
                visit.persist(); // Save the changes

            }

               // Find the patient
               Patient patient = Patient.findById(visit.patient.id);
               if (patient == null) {
                   return Response.status(Response.Status.NOT_FOUND)
                           .entity(new ResponseMessage("patient not found for ID: " + visit.patient.id))
                           .build();
               }
   
 
               


            // Get the first invoice from the list (or handle multiple invoices as needed)
            Invoice invoice = visit.invoice.get(0); // Assuming visit.invoice is a List<Invoice>

            // Ensure the invoice is not null
            if (invoice == null) {
                throw new IllegalArgumentException("Invoice not found.");
            }

            //BigDecimal totalPayments = paymentService.getTotalPaymentOfVisit(visitId);

            //if (totalPayments != null && totalPayments.compareTo(BigDecimal.ZERO) > 0) {
                //invoice.invoiceStatus = "RECEIPT";
                //invoiceRepository.persist(invoice);
           // } else {
                invoice.invoiceStatus = "SYSTEM GENERATED MEDICAL INVOICE";
                invoiceRepository.persist(invoice);
            //}


            // Create the PDF document
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter pdfWriter = new PdfWriter(baos);
            PdfDocument pdfDocument = new PdfDocument(pdfWriter);
            pdfDocument.addEventHandler(PdfDocumentEvent.END_PAGE, new FooterHelperInvoice(facilityBranding()));

            Document document = new Document(pdfDocument);
            document.setMargins(36, 36, 90, 36);



            // Add invoice title
            Table invoiceTitle = new Table(new float[]{1});
            invoiceTitle.setWidth(UnitValue.createPercentValue(100));
            invoiceTitle.addCell(new Cell()
                    .add(new Div()
                            .setBorderBottom(new SolidBorder(1)) // Underline (1px solid line)
                            .setPaddingBottom(2) // Space between text and underline
                            .add(new Paragraph(invoice.invoiceStatus)
                                    .setBold()
                                    .setFontSize(11)
                                    .setTextAlignment(TextAlignment.CENTER)
                            )
                    )
                    .add(new Paragraph(facilityBranding().medicalRecordsTitleBlock())
                            .setFontSize(8)
                            //.setItalic()
                            .setMarginTop(3)
                            .setTextAlignment(TextAlignment.CENTER))
                    .setBorder(Border.NO_BORDER)
                    .setPaddingBottom(15)
            );
            document.add(invoiceTitle);

    


            // Header table (unchanged)
            Table headerTable = new Table(new float[]{1, 1, 1, 2, 1});
            headerTable.setWidth(UnitValue.createPercentValue(100));
            headerTable.addCell(new Cell()
                    .add(getLogo().setWidth(55).setHeight(47))
                    .setBorder(Border.NO_BORDER)
                    .setHorizontalAlignment(HorizontalAlignment.LEFT)
                    .setVerticalAlignment(VerticalAlignment.TOP)
                    .setPaddingTop(-7)
                    .setPaddingLeft(-12)
                    .setPaddingRight(0)
            );
            headerTable.addCell(new Cell()
                    .add(new Paragraph("CLIENT NAME: ").setFontSize(8).setTextAlignment(TextAlignment.LEFT))
                    //.add(new Paragraph("BALANCE DUE: ").setFontSize(8).setTextAlignment(TextAlignment.LEFT))
                    .add(new Paragraph("ADDRESS: ").setFontSize(8).setTextAlignment(TextAlignment.LEFT))
                    .add(new Paragraph("NEXT OF KEEN: ").setFontSize(8).setTextAlignment(TextAlignment.LEFT))

                    .setBorder(Border.NO_BORDER)
            );
            String patientName = patient.patientFirstName.toUpperCase() + " " + patient.patientSecondName.toUpperCase();
            String nextOfKeen = patient.nextOfKinName != null ? patient.nextOfKinName.toUpperCase() : "N/A";
            if(nextOfKeen.equals(null)){
                nextOfKeen = "N/A";
            }

            BigDecimal balance = patient.totalAmountDue;
            String gender = patient.patientGender != null ? patient.patientGender.toUpperCase() : "N/A";
            
            headerTable.addCell(new Cell()
                    .add(new Paragraph(patientName).setFontSize(8).setTextAlignment(TextAlignment.LEFT))
                    //.add(new Paragraph(String.valueOf(balance)).setFontSize(8))
                    .add(new Paragraph(Optional.ofNullable(invoice.visit.patient)
                            .map(p -> p.patientAddress +
                                    (p.getPatientGroup() != null
                                            ? " (" + p.patientGroup.groupNameShortForm + ")"
                                            : ""))
                            .map(String::toUpperCase)
                            .orElse("")).setFontSize(8))
                    .add(new Paragraph(nextOfKeen).setFontSize(8).setTextAlignment(TextAlignment.LEFT))

                    .setBorder(Border.NO_BORDER)
            );
            headerTable.addCell(new Cell()
                    .add(new Paragraph("GENDER: " + gender).setFontSize(8))
                    .add(new Paragraph("CONTACT: ").setFontSize(8))
                    .add(new Paragraph("VISIT DATE: ").setFontSize(8))
                    .add(new Paragraph("BALANCE DUE: ").setFontSize(8))

                    .setBorder(Border.NO_BORDER)
            );
            String ageText = patient.patientAge != null 
                    ? patient.patientAge.toPlainString() + " YRS"
                    : "N/A";
            
            headerTable.addCell(new Cell()
                    .add(new Paragraph("AGE: " + ageText).setFontSize(8).setTextAlignment(TextAlignment.RIGHT))
                    .add(new Paragraph(String.valueOf(patient.patientContact)).setFontSize(8).setTextAlignment(TextAlignment.RIGHT))
                    .add(new Paragraph(String.valueOf(visit.visitDate)).setFontSize(8).setTextAlignment(TextAlignment.RIGHT))
                    .add(new Paragraph(String.valueOf(balance)).setFontSize(8).setTextAlignment(TextAlignment.RIGHT))

                    .setBorder(Border.NO_BORDER)
                    
            );
    
            headerTable.setBorderBottom(new SolidBorder(1));
            document.add(headerTable);





            assert invoice.visit != null;

            String vitalsSummary = formatVisitVitalsSummary(invoice.visit);
            boolean hasComplaintOrVitals = invoice.visit.getConsultation().stream()
                    .anyMatch(consultation ->
                            consultation.chiefComplaint != null && !consultation.chiefComplaint.trim().isEmpty())
                    || !vitalsSummary.isEmpty();

            // First table: presenting complaints vs vitals (only when data exists)
            if (hasComplaintOrVitals) {
                float[] complaintsColumnWidths = {5f, 4f};
                Table complaintsTable = new Table(UnitValue.createPercentArray(complaintsColumnWidths));
                complaintsTable.setWidth(UnitValue.createPercentValue(100));

                complaintsTable.addCell(createCell("SUMMARY OF PRESENTING COMPLAINTS", 1, TextAlignment.LEFT)
                        .setBold()
                        .setFontSize(7)
                        .setFontColor(ColorConstants.WHITE)
                        .setBackgroundColor(ColorConstants.DARK_GRAY)
                        .setBorder(Border.NO_BORDER));

                complaintsTable.addCell(createCell("VITALS (TRIAGE)", 1, TextAlignment.RIGHT)
                        .setBold()
                        .setFontSize(7)
                        .setFontColor(ColorConstants.WHITE)
                        .setBackgroundColor(ColorConstants.DARK_GRAY)
                        .setBorder(Border.NO_BORDER));

                for (Consultation consultation : invoice.visit.getConsultation()) {
                    String pcText = formatPresentingComplaints(consultation.presentingComplaints);
                    if (pcText.isEmpty()) {
                        pcText = consultation.chiefComplaint != null
                                ? consultation.chiefComplaint.trim()
                                : "";
                    }
                    String vitals = vitalsSummary;

                    if (pcText.isEmpty() && vitals.isEmpty()) {
                        continue;
                    }

                    com.itextpdf.kernel.colors.Color rowColor = ColorConstants.WHITE;

                    complaintsTable.addCell(createCell(pcText.toUpperCase(), 1, TextAlignment.LEFT)
                            .setFontSize(7)
                            .setBackgroundColor(rowColor)
                            .setBorder(Border.NO_BORDER)
                            .setBorderBottom(new SolidBorder(1)));

                    complaintsTable.addCell(createCell(vitals.toUpperCase(), 1, TextAlignment.RIGHT)
                            .setFontSize(7)
                            .setBackgroundColor(rowColor)
                            .setBorder(Border.NO_BORDER)
                            .setBorderBottom(new SolidBorder(1)));

                }
                document.add(complaintsTable);
            }

            List<ProcedureRequested> invoiceProcedures = invoice.visit.getProceduresRequested() != null
                    ? invoice.visit.getProceduresRequested()
                    : List.of();
            addStatementServicesTable(document, invoiceProcedures, true);

            // Diagnosis table (placed after services requested table)
            boolean hasDiagnosisRows = invoice.visit.getConsultation() != null
                    && invoice.visit.getConsultation().stream()
                    .anyMatch(consultation ->
                            consultation.diagnosis != null && !consultation.diagnosis.trim().isEmpty());
            if (hasDiagnosisRows) {
                Table diagnosisTable = new Table(UnitValue.createPercentArray(new float[]{1f}));
                diagnosisTable.setWidth(UnitValue.createPercentValue(100));

                diagnosisTable.addCell(createCell("DIAGNOSIS", 1, TextAlignment.LEFT)
                        .setBold()
                        .setFontSize(7)
                        .setFontColor(ColorConstants.WHITE)
                        .setBackgroundColor(ColorConstants.DARK_GRAY)
                        .setBorder(Border.NO_BORDER));

                for (Consultation consultation : invoice.visit.getConsultation()) {
                    String diagnosis = consultation.diagnosis != null
                            ? consultation.diagnosis.trim()
                            : "";
                    if (diagnosis.isEmpty()) {
                        continue;
                    }

                    diagnosisTable.addCell(createCell(diagnosis.toUpperCase(), 1, TextAlignment.LEFT)
                            .setFontSize(7)
                            .setBackgroundColor(ColorConstants.WHITE)
                            .setBorder(Border.NO_BORDER)
                            .setBorderBottom(new SolidBorder(1)));
                }

                document.add(diagnosisTable);
            }

            List<TreatmentRequested> invoiceTreatments = invoice.visit.getTreatmentRequested() != null
                    ? invoice.visit.getTreatmentRequested()
                    : List.of();
            addStatementDrugsTable(document, invoiceTreatments, true);

            // Final table: finances and payments (one row of values)
            Table totalsTable = new Table(UnitValue.createPercentArray(new float[]{2f, 2f, 2f, 2f, 2f, 2f}));
            totalsTable.setWidth(UnitValue.createPercentValue(100));

            totalsTable.addCell(createCell("SUBTOTAL", 1, TextAlignment.CENTER)
                    .setBold()
                    .setFontSize(7)
                    .setFontColor(ColorConstants.WHITE)
                    .setBackgroundColor(ColorConstants.DARK_GRAY)
                    .setBorder(Border.NO_BORDER));
            totalsTable.addCell(createCell("TOTAL AMOUNT", 1, TextAlignment.CENTER)
                    .setBold()
                    .setFontSize(7)
                    .setFontColor(ColorConstants.WHITE)
                    .setBackgroundColor(ColorConstants.DARK_GRAY)
                    .setBorder(Border.NO_BORDER));
            totalsTable.addCell(createCell("DISCOUNT", 1, TextAlignment.CENTER)
                    .setBold()
                    .setFontSize(7)
                    .setFontColor(ColorConstants.WHITE)
                    .setBackgroundColor(ColorConstants.DARK_GRAY)
                    .setBorder(Border.NO_BORDER));
            totalsTable.addCell(createCell("TAX", 1, TextAlignment.CENTER)
                    .setBold()
                    .setFontSize(7)
                    .setFontColor(ColorConstants.WHITE)
                    .setBackgroundColor(ColorConstants.DARK_GRAY)
                    .setBorder(Border.NO_BORDER));
            totalsTable.addCell(createCell("AMOUNT PAID", 1, TextAlignment.CENTER)
                    .setBold()
                    .setFontSize(7)
                    .setFontColor(ColorConstants.WHITE)
                    .setBackgroundColor(ColorConstants.DARK_GRAY)
                    .setBorder(Border.NO_BORDER));
            totalsTable.addCell(createCell("BALANCE", 1, TextAlignment.CENTER)
                    .setBold()
                    .setFontSize(7)
                    .setFontColor(ColorConstants.WHITE)
                    .setBackgroundColor(ColorConstants.DARK_GRAY)
                    .setBorder(Border.NO_BORDER));

            totalsTable.addCell(createCell(plainNumber(invoice.subTotal), 1, TextAlignment.CENTER)
                    .setFontSize(7)
                    .setBackgroundColor(ColorConstants.WHITE)
                    .setBorder(Border.NO_BORDER)
                    .setBorderBottom(new SolidBorder(1)));
            totalsTable.addCell(createCell(plainNumber(invoice.totalAmount), 1, TextAlignment.CENTER)
                    .setFontSize(7)
                    .setBackgroundColor(ColorConstants.WHITE)
                    .setBorder(Border.NO_BORDER)
                    .setBorderBottom(new SolidBorder(1)));
            totalsTable.addCell(createCell(plainNumber(invoice.discount), 1, TextAlignment.CENTER)
                    .setFontSize(7)
                    .setBackgroundColor(ColorConstants.WHITE)
                    .setBorder(Border.NO_BORDER)
                    .setBorderBottom(new SolidBorder(1)));
            totalsTable.addCell(createCell(plainNumber(invoice.tax), 1, TextAlignment.CENTER)
                    .setFontSize(7)
                    .setBackgroundColor(ColorConstants.WHITE)
                    .setBorder(Border.NO_BORDER)
                    .setBorderBottom(new SolidBorder(1)));
            totalsTable.addCell(createCell(plainNumber(invoice.amountPaid), 1, TextAlignment.CENTER)
                    .setFontSize(7)
                    .setBackgroundColor(ColorConstants.WHITE)
                    .setBorder(Border.NO_BORDER)
                    .setBorderBottom(new SolidBorder(1)));
            totalsTable.addCell(createCell(plainNumber(invoice.balanceDue), 1, TextAlignment.CENTER)
                    .setFontSize(7)
                    .setBackgroundColor(ColorConstants.WHITE)
                    .setBorder(Border.NO_BORDER)
                    .setBorderBottom(new SolidBorder(1)));

            document.add(totalsTable);

            // Attending clinician / doctor table
            String attendingClinician = "N/A";
            if (invoice.visit.getConsultation() != null) {
                attendingClinician = invoice.visit.getConsultation().stream()
                        .map(consultation -> consultation.doneBy)
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(name -> !name.isEmpty())
                        .findFirst()
                        .orElse("N/A");
            }

            Table clinicianTable = new Table(UnitValue.createPercentArray(new float[]{3f, 3f}));
            clinicianTable.setWidth(UnitValue.createPercentValue(100));
            clinicianTable.setMarginTop(8);

            clinicianTable.addCell(createCell("ATTENDING CLINICIAN / DOCTOR", 1, TextAlignment.LEFT)
                    .setBold()
                    .setFontSize(7)
                    .setFontColor(ColorConstants.WHITE)
                    .setBackgroundColor(ColorConstants.DARK_GRAY)
                    .setBorder(Border.NO_BORDER));
            clinicianTable.addCell(createCell("SIGNATURE & STAMP", 1, TextAlignment.RIGHT)
                    .setBold()
                    .setFontSize(7)
                    .setFontColor(ColorConstants.WHITE)
                    .setBackgroundColor(ColorConstants.DARK_GRAY)
                    .setBorder(Border.NO_BORDER));

            clinicianTable.addCell(createCell("NAME:", 1, TextAlignment.LEFT)
                    .setFontSize(7)
                    .setBackgroundColor(ColorConstants.WHITE)
                    .setBorder(Border.NO_BORDER)
                    .setBorderBottom(new SolidBorder(1)));
            clinicianTable.addCell(createCell("", 1, TextAlignment.RIGHT)
                    .setFontSize(7)
                    .setBackgroundColor(ColorConstants.WHITE)
                    .setBorder(Border.NO_BORDER)
                    .setBorderBottom(new SolidBorder(1)));

            document.add(clinicianTable);

            // Close the document
            document.close();

            // Return the PDF as a response
            byte[] pdfBytes = baos.toByteArray();
            return Response.ok(new ByteArrayInputStream(pdfBytes))
                    .header("Content-Disposition", "attachment; filename=invoice.pdf")
                    .type("application/pdf")
                    .build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Transactional
    public Response generateVisitLabResultsPdf(Long visitId) {
        try {
            PatientVisit visit = PatientVisit.findById(visitId);
            if (visit == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ResponseMessage("Visit not found for ID: " + visitId))
                        .build();
            }
            Patient patient = Patient.findById(visit.patient.id);
            if (patient == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ResponseMessage("Patient not found for this visit."))
                        .build();
            }

            List<Cbc> cbcs = Cbc.find("visit.id = ?1 ORDER BY id ASC", visitId).list();
            List<ParasitologyStool> parasitologyStools =
                    ParasitologyStool.find("visit.id = ?1 ORDER BY id ASC", visitId).list();
            List<Urinalysis> urinalyses = Urinalysis.find("visit.id = ?1 ORDER BY id ASC", visitId).list();
            List<GeneralLabReport> generalLabReports =
                    GeneralLabReport.find("visit.id = ?1 ORDER BY id ASC", visitId).list();
            List<Malaria> malarias = Malaria.find("visit.id = ?1 ORDER BY id ASC", visitId).list();
            List<Hiv> hivs = Hiv.find("visit.id = ?1 ORDER BY id ASC", visitId).list();
            List<HepatitisB> hepatitisBs = HepatitisB.find("visit.id = ?1 ORDER BY id ASC", visitId).list();
            List<HepatitisC> hepatitisCs = HepatitisC.find("visit.id = ?1 ORDER BY id ASC", visitId).list();
            List<Hpylori> hpyloris = Hpylori.find("visit.id = ?1 ORDER BY id ASC", visitId).list();
            List<Rbs> rbss = Rbs.find("visit.id = ?1 ORDER BY id ASC", visitId).list();
            List<UrineHcg> urineHcgs = UrineHcg.find("visit.id = ?1 ORDER BY id ASC", visitId).list();
            List<Widal> widals = Widal.find("visit.id = ?1 ORDER BY id ASC", visitId).list();
            List<StoolExam> stoolExams = StoolExam.find("visit.id = ?1 ORDER BY id ASC", visitId).list();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter pdfWriter = new PdfWriter(baos);
            PdfDocument pdfDocument = new PdfDocument(pdfWriter);
            pdfDocument.addEventHandler(PdfDocumentEvent.END_PAGE, new FooterHelperInvoice(facilityBranding()));

            Document document = new Document(pdfDocument);
            // Tighter margins than statement PDF so lab tables fit one page when possible
            document.setMargins(24, 24, 56, 24);

            addVisitLabResultsTitleAndHeader(document, visit, patient);

            boolean wrote = false;
            if (!cbcs.isEmpty()) {
                appendCbcReportsToVisitLabPdf(document, cbcs);
                wrote = true;
            }
            if (!parasitologyStools.isEmpty()) {
                appendParasitologyStoolReportsToVisitLabPdf(document, parasitologyStools);
                wrote = true;
            }
            for (Urinalysis u : urinalyses) {
                if (appendUrinalysisToVisitLabPdf(document, u)) {
                    wrote = true;
                }
            }
            for (GeneralLabReport g : generalLabReports) {
                if (appendGeneralLabReportToVisitLabPdf(document, g)) {
                    wrote = true;
                }
            }
            for (Malaria m : malarias) {
                if (appendMalariaToVisitLabPdf(document, m)) {
                    wrote = true;
                }
            }
            for (Hiv h : hivs) {
                if (appendSimpleTreatmentLabToVisitLabPdf(document, "HIV", h.treatmentRequested, h.result, h.notes)) {
                    wrote = true;
                }
            }
            for (HepatitisB h : hepatitisBs) {
                if (appendSimpleTreatmentLabToVisitLabPdf(document, "HEPATITIS B", h.treatmentRequested, h.result, h.notes)) {
                    wrote = true;
                }
            }
            for (HepatitisC h : hepatitisCs) {
                if (appendSimpleTreatmentLabToVisitLabPdf(document, "HEPATITIS C", h.treatmentRequested, h.result, h.notes)) {
                    wrote = true;
                }
            }
            for (Hpylori h : hpyloris) {
                if (appendSimpleTreatmentLabToVisitLabPdf(document, "H. PYLORI", h.treatmentRequested, h.result, h.notes)) {
                    wrote = true;
                }
            }
            for (Rbs r : rbss) {
                if (appendSimpleTreatmentLabToVisitLabPdf(document, "RANDOM BLOOD SUGAR (RBS)", r.treatmentRequested, r.result, r.notes)) {
                    wrote = true;
                }
            }
            for (UrineHcg u : urineHcgs) {
                if (appendSimpleTreatmentLabToVisitLabPdf(document, "URINE HCG", u.treatmentRequested, u.result, u.notes)) {
                    wrote = true;
                }
            }
            for (Widal w : widals) {
                if (appendSimpleTreatmentLabToVisitLabPdf(document, "WIDAL", w.treatmentRequested, w.result, w.notes)) {
                    wrote = true;
                }
            }
            for (StoolExam s : stoolExams) {
                if (appendSimpleTreatmentLabToVisitLabPdf(document, "STOOL EXAMINATION", s.treatmentRequested, s.result, s.notes)) {
                    wrote = true;
                }
            }

            if (!wrote) {
                document.add(new Paragraph("No lab results recorded for this visit.")
                        .setFontSize(7)
                        .setMarginTop(4));
            }

            document.close();
            byte[] pdfBytes = baos.toByteArray();
            return Response.ok(new ByteArrayInputStream(pdfBytes))
                    .header("Content-Disposition", "attachment; filename=visit-lab-results-" + visitId + ".pdf")
                    .type("application/pdf")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    private void addVisitLabResultsTitleAndHeader(Document document, PatientVisit visit, Patient patient) {
        Table docTitle = new Table(new float[]{1});
        docTitle.setWidth(UnitValue.createPercentValue(100));
        docTitle.addCell(new Cell()
                .add(new Div()
                        .setBorderBottom(new SolidBorder(0.75f))
                        .setPaddingBottom(1)
                        .add(new Paragraph("LAB RESULTS SUMMARY (VISIT)")
                                .setBold()
                                .setFontSize(9)
                                .setFixedLeading(10)
                                .setTextAlignment(TextAlignment.CENTER)
                        )
                )
                .add(new Paragraph(facilityBranding().medicalRecordsTitleBlock())
                        .setFontSize(6)
                        .setFixedLeading(7)
                        .setMarginTop(1)
                        .setTextAlignment(TextAlignment.CENTER))
                .setBorder(Border.NO_BORDER)
                .setPaddingBottom(4));
        document.add(docTitle);

        Table headerTable = new Table(new float[]{1, 1, 1, 2, 1});
        headerTable.setWidth(UnitValue.createPercentValue(100));
        headerTable.addCell(new Cell()
                .add(getLogo().setWidth(52).setHeight(44))
                .setBorder(Border.NO_BORDER)
                .setHorizontalAlignment(HorizontalAlignment.LEFT)
                .setVerticalAlignment(VerticalAlignment.TOP)
                .setPaddingTop(-4)
                .setPaddingLeft(-14));
        headerTable.addCell(new Cell()
                .add(new Paragraph("CLIENT NAME: ").setFontSize(6).setFixedLeading(7).setTextAlignment(TextAlignment.LEFT))
                .add(new Paragraph("ADDRESS: ").setFontSize(6).setFixedLeading(7).setTextAlignment(TextAlignment.LEFT))
                .add(new Paragraph("NEXT OF KEEN: ").setFontSize(6).setFixedLeading(7).setTextAlignment(TextAlignment.LEFT))
                .setBorder(Border.NO_BORDER));

        String patientName = patient.patientFirstName.toUpperCase() + " " + patient.patientSecondName.toUpperCase();
        String nextOfKeen = patient.nextOfKinName != null ? patient.nextOfKinName.toUpperCase() : "N/A";

        headerTable.addCell(new Cell()
                .add(new Paragraph(patientName).setFontSize(6).setFixedLeading(7).setTextAlignment(TextAlignment.LEFT))
                .add(new Paragraph(Optional.ofNullable(visit.patient)
                        .map(p -> p.patientAddress +
                                (p.getPatientGroup() != null
                                        ? " (" + p.patientGroup.groupNameShortForm + ")"
                                        : ""))
                        .map(String::toUpperCase)
                        .orElse("")).setFontSize(6).setFixedLeading(7))
                .add(new Paragraph(nextOfKeen).setFontSize(6).setFixedLeading(7).setTextAlignment(TextAlignment.LEFT))
                .setBorder(Border.NO_BORDER));

        String gender = patient.patientGender != null ? patient.patientGender.toUpperCase() : "N/A";
        headerTable.addCell(new Cell()
                .add(new Paragraph("GENDER: " + gender).setFontSize(6).setFixedLeading(7))
                .add(new Paragraph("CONTACT: ").setFontSize(6).setFixedLeading(7))
                .add(new Paragraph("VISIT DATE: ").setFontSize(6).setFixedLeading(7))
                .setBorder(Border.NO_BORDER));

        String ageText = patient.patientAge != null
                ? patient.patientAge.toPlainString() + " YRS"
                : "N/A";
        headerTable.addCell(new Cell()
                .add(new Paragraph("AGE: " + ageText).setFontSize(6).setFixedLeading(7).setTextAlignment(TextAlignment.RIGHT))
                .add(new Paragraph(String.valueOf(patient.patientContact)).setFontSize(6).setFixedLeading(7).setTextAlignment(TextAlignment.RIGHT))
                .add(new Paragraph(String.valueOf(visit.visitDate)).setFontSize(6).setFixedLeading(7).setTextAlignment(TextAlignment.RIGHT))
                .setBorder(Border.NO_BORDER));

        headerTable.setBorderBottom(new SolidBorder(0.75f));
        document.add(headerTable);
    }

    private void addLabBlueBanner(Document document, String title) {
        Table banner = new Table(new float[]{1})
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginTop(3)
                .setMarginBottom(0);
        Cell cell = new Cell()
                .add(new Paragraph(title).setBold().setFontSize(7).setFixedLeading(8).setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(LAB_SECTION_BLUE)
                .setBorder(Border.NO_BORDER)
                .setPadding(2)
                .setPaddingLeft(3)
                .setPaddingRight(3);
        banner.addCell(cell);
        document.add(banner);
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> parseJsonObjectToStringMap(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try (Jsonb jsonb = JsonbBuilder.create()) {
            Map<String, Object> raw = jsonb.fromJson(json.trim(), HashMap.class);
            if (raw == null || raw.isEmpty()) {
                return Map.of();
            }
            Map<String, String> out = new HashMap<>();
            for (Map.Entry<String, Object> e : raw.entrySet()) {
                if (e.getValue() == null) {
                    continue;
                }
                out.put(e.getKey(), String.valueOf(e.getValue()));
            }
            return out;
        } catch (Exception e) {
            return Map.of();
        }
    }

    private static boolean labTextBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String labNz(String s) {
        return s == null ? "" : s;
    }

    private Cell labGridHeaderCell(String text) {
        return new Cell()
                .add(new Paragraph(text).setBold().setFontSize(5.5f).setFixedLeading(6.5f).setTextAlignment(TextAlignment.CENTER))
                .setFontColor(ColorConstants.WHITE)
                .setBackgroundColor(ColorConstants.DARK_GRAY)
                .setBorder(new SolidBorder(0.35f))
                .setPadding(1)
                .setPaddingLeft(2)
                .setPaddingRight(2);
    }

    private Cell labGridValueCell(String text, TextAlignment alignment) {
        Paragraph p = new Paragraph(labNz(text)).setFontSize(5.5f).setFixedLeading(6.5f).setTextAlignment(alignment);
        return new Cell()
                .add(p)
                .setBackgroundColor(ColorConstants.WHITE)
                .setBorder(new SolidBorder(0.35f))
                .setPadding(1)
                .setPaddingLeft(2)
                .setPaddingRight(2);
    }

    private void appendCbcReportsToVisitLabPdf(Document document, List<Cbc> cbcs) {
        for (Cbc cbc : cbcs) {
            addLabBlueBanner(document, "FULL BLOOD COUNT (FBC)(CBC) Results");
            if (cbc.procedureRequested != null && !labTextBlank(cbc.procedureRequested.procedureRequestedName)) {
                document.add(new Paragraph("Service: " + cbc.procedureRequested.procedureRequestedName.toUpperCase())
                        .setFontSize(6)
                        .setFixedLeading(7)
                        .setMarginBottom(1));
            }
            document.add(new Paragraph("Overall Result / Lab Report Title")
                    .setBold()
                    .setFontSize(6)
                    .setFixedLeading(7)
                    .setMarginTop(1));
            document.add(new Paragraph(labNz(cbc.labReportTitle)).setFontSize(6).setFixedLeading(7).setMarginBottom(2));

            Map<String, String> interp = parseJsonObjectToStringMap(cbc.interpretations);
            String[][] allCbcRows = cbcPdfRowDefinitions();
            int mid = (allCbcRows.length + 1) / 2;
            String[][] leftRows = Arrays.copyOfRange(allCbcRows, 0, mid);
            String[][] rightRows = Arrays.copyOfRange(allCbcRows, mid, allCbcRows.length);

            Table leftCbc = buildCbcParameterGridTable(cbc, interp, leftRows);
            Table rightCbc = buildCbcParameterGridTable(cbc, interp, rightRows);

            Table cbcPair = new Table(UnitValue.createPercentArray(new float[]{1f, 1f}))
                    .setWidth(UnitValue.createPercentValue(100));
            cbcPair.addCell(new Cell()
                    .setBorder(Border.NO_BORDER)
                    .setPadding(0)
                    .setPaddingRight(2)
                    .add(leftCbc));
            cbcPair.addCell(new Cell()
                    .setBorder(Border.NO_BORDER)
                    .setPadding(0)
                    .setPaddingLeft(2)
                    .add(rightCbc));
            document.add(cbcPair);
        }
    }

    private Table buildCbcParameterGridTable(Cbc cbc, Map<String, String> interp, String[][] rows) {
        float[] colWidths = {2.2f, 1.4f, 1.4f, 2.2f, 1.8f};
        Table table = new Table(UnitValue.createPercentArray(colWidths))
                .setWidth(UnitValue.createPercentValue(100));

        table.addCell(labGridHeaderCell("PARAMETER"));
        table.addCell(labGridHeaderCell("RESULT"));
        table.addCell(labGridHeaderCell("UNITS"));
        table.addCell(labGridHeaderCell("REF RANGES"));
        table.addCell(labGridHeaderCell("INTERPRETATION"));

        for (String[] row : rows) {
            String jsonKey = row[1];
            table.addCell(labGridValueCell(row[0], TextAlignment.LEFT));
            table.addCell(labGridValueCell(cbcValueForKey(cbc, jsonKey), TextAlignment.CENTER));
            table.addCell(labGridValueCell(row[2], TextAlignment.CENTER));
            table.addCell(labGridValueCell(row[3], TextAlignment.CENTER));
            table.addCell(labGridValueCell(interp.get(jsonKey), TextAlignment.CENTER));
        }
        return table;
    }

    private static String[][] cbcPdfRowDefinitions() {
        return new String[][]{
                {"WBC", "wbc", "10^9/L", "4.0 - 11.0"},
                {"LYMPH", "lymph", "10^9/L", "0.8 - 4.0"},
                {"MID", "mid", "10^9/L", "0.1 - 1.5"},
                {"GRAN", "gran", "10^9/L", "2.0 - 7.5"},
                {"LYMPH%", "lymphPercent", "%", "20 - 40"},
                {"MID%", "midPercent", "%", "3 - 10"},
                {"GRAN%", "granPercent", "%", "50 - 70"},
                {"HGB", "hgb", "g/dL", "12.0 - 17.0"},
                {"RBC", "rbc", "10^12/L", "4.5 - 5.9"},
                {"HCT", "hct", "%", "36 - 50"},
                {"MCV", "mcv", "fL", "80 - 100"},
                {"MCH", "mch", "pg", "27 - 33"},
                {"MCHC", "mchc", "g/L", "320 - 360"},
                {"RDW-CV", "rdwCv", "%", "11.5 - 14.5"},
                {"RDW-SD", "rdwSd", "fL", "35 - 56"},
                {"PLT", "plt", "10^9/L", "150 - 400"},
                {"MPV", "mpv", "fL", "7.0 - 13.0"},
                {"PDW", "pdw", "", ""},
                {"PCT", "pct", "%", "0.15 - 0.32"},
        };
    }

    private static String cbcValueForKey(Cbc cbc, String key) {
        return switch (key) {
            case "wbc" -> labNz(cbc.wbc);
            case "lymph" -> labNz(cbc.lymph);
            case "mid" -> labNz(cbc.mid);
            case "gran" -> labNz(cbc.gran);
            case "lymphPercent" -> labNz(cbc.lymphPercent);
            case "midPercent" -> labNz(cbc.midPercent);
            case "granPercent" -> labNz(cbc.granPercent);
            case "hgb" -> labNz(cbc.hgb);
            case "rbc" -> labNz(cbc.rbc);
            case "hct" -> labNz(cbc.hct);
            case "mcv" -> labNz(cbc.mcv);
            case "mch" -> labNz(cbc.mch);
            case "mchc" -> labNz(cbc.mchc);
            case "rdwCv" -> labNz(cbc.rdwCv);
            case "rdwSd" -> labNz(cbc.rdwSd);
            case "plt" -> labNz(cbc.plt);
            case "mpv" -> labNz(cbc.mpv);
            case "pdw" -> labNz(cbc.pdw);
            case "pct" -> labNz(cbc.pct);
            default -> "";
        };
    }

    private Cell parasitologyLabeledCell(String label, String value) {
        Div div = new Div();
        div.add(new Paragraph(label).setBold().setFontSize(5.5f).setFixedLeading(6.5f));
        div.add(new Paragraph(labNz(value)).setFontSize(5.5f).setFixedLeading(6.5f));
        return new Cell()
                .add(div)
                .setBorder(new SolidBorder(0.35f))
                .setPadding(1.5f)
                .setBackgroundColor(ColorConstants.WHITE);
    }

    private void appendParasitologyStoolReportsToVisitLabPdf(Document document, List<ParasitologyStool> reports) {
        for (ParasitologyStool p : reports) {
            addLabBlueBanner(document, "PARASITOLOGY STOOL");
            if (p.procedureRequested != null && !labTextBlank(p.procedureRequested.procedureRequestedName)) {
                document.add(new Paragraph("Service: " + p.procedureRequested.procedureRequestedName.toUpperCase())
                        .setFontSize(6)
                        .setFixedLeading(7)
                        .setMarginBottom(1));
            }

            addLabBlueBanner(document, "Microscopy");
            Table microscopy = new Table(UnitValue.createPercentArray(new float[]{1f, 1f, 1f}))
                    .setWidth(UnitValue.createPercentValue(100));
            microscopy.addCell(parasitologyLabeledCell("Ova", p.ova));
            microscopy.addCell(parasitologyLabeledCell("Cysts", p.cysts));
            microscopy.addCell(parasitologyLabeledCell("Larvae", p.larvae));
            document.add(microscopy);

            addLabBlueBanner(document, "Macroscopy");
            Table macroscopy = new Table(UnitValue.createPercentArray(new float[]{1f, 1f, 1f}))
                    .setWidth(UnitValue.createPercentValue(100));
            macroscopy.addCell(parasitologyLabeledCell("Color", p.color));
            macroscopy.addCell(parasitologyLabeledCell("Consistency", p.consistency));
            macroscopy.addCell(parasitologyLabeledCell("Blood", p.blood));
            macroscopy.addCell(parasitologyLabeledCell("Mucus", p.mucus));
            macroscopy.addCell(parasitologyLabeledCell("Visible Parasites", p.visibleParasites));
            macroscopy.addCell(parasitologyLabeledCell("Others", p.others));
            document.add(macroscopy);

            document.add(new Paragraph("Parasitology Overall Result / Lab Report Title")
                    .setBold()
                    .setFontSize(6)
                    .setFixedLeading(7)
                    .setMarginTop(2));
            document.add(new Paragraph(labNz(p.labReportTitle)).setFontSize(6).setFixedLeading(7).setMarginBottom(1));

            Map<String, String> interpMap = parseJsonObjectToStringMap(p.interpretations);
            if (!interpMap.isEmpty()) {
                for (Map.Entry<String, String> e : interpMap.entrySet()) {
                    document.add(new Paragraph(e.getKey() + ": " + e.getValue()).setFontSize(5.5f).setFixedLeading(6.5f));
                }
            } else if (!labTextBlank(p.interpretations)) {
                document.add(new Paragraph(p.interpretations).setFontSize(5.5f).setFixedLeading(6.5f));
            }
        }
    }

    private Cell labGrayHeaderCell(String text, TextAlignment align) {
        return new Cell()
                .add(new Paragraph(text).setBold().setFontSize(5.5f).setFixedLeading(6.5f).setTextAlignment(align))
                .setFontColor(ColorConstants.WHITE)
                .setBackgroundColor(ColorConstants.DARK_GRAY)
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(0.75f))
                .setPadding(1)
                .setPaddingLeft(2)
                .setPaddingRight(2);
    }

    private Cell labWhiteDataCell(String text, TextAlignment align) {
        return new Cell()
                .add(new Paragraph(labNz(text)).setFontSize(5.5f).setFixedLeading(6.5f).setTextAlignment(align))
                .setBackgroundColor(ColorConstants.WHITE)
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(0.75f))
                .setPadding(1)
                .setPaddingLeft(2)
                .setPaddingRight(2);
    }

    private void appendGrayTwoColumnLabTable(Document document, String blueBannerTitle, String procedureLine,
                                               List<String[]> rows) {
        if (rows.isEmpty()) {
            return;
        }
        addLabBlueBanner(document, blueBannerTitle);
        if (!labTextBlank(procedureLine)) {
            document.add(new Paragraph(procedureLine).setFontSize(6).setFixedLeading(7).setItalic().setMarginBottom(1));
        }
        Table table = new Table(UnitValue.createPercentArray(new float[]{2f, 3f}))
                .setWidth(UnitValue.createPercentValue(100));
        table.addCell(labGrayHeaderCell("PARAMETER", TextAlignment.LEFT));
        table.addCell(labGrayHeaderCell("RESULT", TextAlignment.LEFT));
        for (String[] row : rows) {
            table.addCell(labWhiteDataCell(row[0], TextAlignment.LEFT));
            table.addCell(labWhiteDataCell(row[1], TextAlignment.LEFT));
        }
        document.add(table);
    }

    private static void addRowIfValuePresent(List<String[]> rows, String label, String value) {
        if (!labTextBlank(value)) {
            rows.add(new String[]{label, value});
        }
    }

    private boolean appendUrinalysisToVisitLabPdf(Document document, Urinalysis u) {
        List<String[]> rows = new ArrayList<>();
        addRowIfValuePresent(rows, "PH", u.ph);
        addRowIfValuePresent(rows, "SPECIFIC GRAVITY (SG)", u.sg);
        addRowIfValuePresent(rows, "PROTEIN", u.protein);
        addRowIfValuePresent(rows, "GLUCOSE", u.glucose);
        addRowIfValuePresent(rows, "KETONES", u.ketones);
        addRowIfValuePresent(rows, "BLOOD", u.blood);
        addRowIfValuePresent(rows, "BILIRUBIN", u.bilirubin);
        addRowIfValuePresent(rows, "UROBILINOGEN", u.urobilinogen);
        addRowIfValuePresent(rows, "NITRITE", u.nitrite);
        addRowIfValuePresent(rows, "LEUKOCYTE ESTERASE", u.leukocyteE);
        addRowIfValuePresent(rows, "EPITHELIAL CELLS", u.epithelialCells);
        addRowIfValuePresent(rows, "PUS CELLS / WBCs", u.pusCellsWbcs);
        addRowIfValuePresent(rows, "CASTS", u.casts);
        addRowIfValuePresent(rows, "RED CELLS", u.redCells);
        addRowIfValuePresent(rows, "CRYSTALS", u.crystals);
        addRowIfValuePresent(rows, "COLOR", u.color);
        addRowIfValuePresent(rows, "APPEARANCE", u.appearance);
        addRowIfValuePresent(rows, "VOLUME", u.volume);
        addRowIfValuePresent(rows, "OTHERS", u.others);

        Map<String, String> interp = parseJsonObjectToStringMap(u.interpretations);
        for (Map.Entry<String, String> e : interp.entrySet()) {
            rows.add(new String[]{"INTERPRETATION (" + e.getKey() + ")", e.getValue()});
        }
        if (interp.isEmpty() && !labTextBlank(u.interpretations)) {
            addRowIfValuePresent(rows, "INTERPRETATIONS", u.interpretations);
        }
        addRowIfValuePresent(rows, "LAB REPORT TITLE", u.labReportTitle);

        if (rows.isEmpty()) {
            return false;
        }
        String title = labTextBlank(u.test) ? "URINALYSIS" : u.test.toUpperCase();
        String proc = u.procedureRequested != null && !labTextBlank(u.procedureRequested.procedureRequestedName)
                ? "Service: " + u.procedureRequested.procedureRequestedName.toUpperCase()
                : "";
        appendGrayTwoColumnLabTable(document, title, proc, rows);
        return true;
    }

    private boolean appendGeneralLabReportToVisitLabPdf(Document document, GeneralLabReport g) {
        List<String[]> rows = new ArrayList<>();
        addRowIfValuePresent(rows, "TEST", g.test);
        addRowIfValuePresent(rows, "RESULT", g.result);
        addRowIfValuePresent(rows, "NOTES", g.notes);
        addRowIfValuePresent(rows, "RECOMMENDATION", g.recommendation);
        if (rows.isEmpty()) {
            return false;
        }
        String banner = !labTextBlank(g.labReportTitle) ? g.labReportTitle.toUpperCase() : "GENERAL LAB REPORT";
        String proc = g.procedureRequested != null && !labTextBlank(g.procedureRequested.procedureRequestedName)
                ? "Service: " + g.procedureRequested.procedureRequestedName.toUpperCase()
                : "";
        appendGrayTwoColumnLabTable(document, banner, proc, rows);
        return true;
    }

    private boolean appendMalariaToVisitLabPdf(Document document, Malaria m) {
        List<String[]> rows = new ArrayList<>();
        addRowIfValuePresent(rows, "BLOOD SMEAR (BS)", m.bs);
        addRowIfValuePresent(rows, "MRDT", m.mrdt);
        addRowIfValuePresent(rows, "NOTES", m.notes);
        addRowIfValuePresent(rows, "RECOMMENDATION", m.recommendation);
        if (rows.isEmpty()) {
            return false;
        }
        String banner = !labTextBlank(m.labReportTitle) ? m.labReportTitle.toUpperCase() : "MALARIA";
        String proc = m.procedureRequested != null && !labTextBlank(m.procedureRequested.procedureRequestedName)
                ? "Service: " + m.procedureRequested.procedureRequestedName.toUpperCase()
                : "";
        appendGrayTwoColumnLabTable(document, banner, proc, rows);
        return true;
    }

    private boolean appendSimpleTreatmentLabToVisitLabPdf(Document document, String testTitle,
                                                            org.example.treatment.domains.TreatmentRequested treatment,
                                                            String result, String notes) {
        List<String[]> rows = new ArrayList<>();
        addRowIfValuePresent(rows, "RESULT", result);
        addRowIfValuePresent(rows, "NOTES", notes);
        if (rows.isEmpty()) {
            return false;
        }
        String proc = "";
        if (treatment != null && !labTextBlank(treatment.itemName)) {
            proc = "Linked treatment / item: " + treatment.itemName.toUpperCase();
        }
        appendGrayTwoColumnLabTable(document, testTitle, proc, rows);
        return true;
    }

    public Response generateInvoicePdfWithLogo(Long invoiceId) {
        // Retrieve the invoice
        Invoice invoice = Invoice.findById(invoiceId);
        if (invoice == null) {
            throw new IllegalArgumentException("Invoice not found for ID: " + invoiceId);
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // Create PDF writer and document
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Add the logo
            try {
                // Replace with your logo path or URL
                String logoPath = "src/main/resources/logo.png"; // Local file
                // String logoPath = "https://example.com/logo.png"; // External URL

                // Load the image
                ImageData imageData = ImageDataFactory.create(logoPath);

                // Create the Image object
                Image logo = new Image(imageData);

                // Scale and align the logo
                logo.scaleToFit(80, 80);
                logo.setHorizontalAlignment(HorizontalAlignment.CENTER);

            } catch (IOException e) {
                document.close(); // Close document before throwing exception
                throw new RuntimeException("Failed to load the logo image.", e);
            }

            // Title
            Paragraph title = new Paragraph("INVOICE")
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.RIGHT);
            document.add(title);

            // Invoice Details
            Table headerTable = new Table(UnitValue.createPercentArray(new float[]{2, 3}))
                    .useAllAvailableWidth()
                    .setMarginTop(10);
            headerTable.addCell(createCell("Number:", 3, TextAlignment.LEFT).setBold());
            headerTable.addCell(createCell(invoice.invoiceNo, 3, TextAlignment.LEFT));
            headerTable.addCell(createCell("Reference:", 3, TextAlignment.LEFT).setBold());
            headerTable.addCell(createCell(invoice.reference, 3, TextAlignment.LEFT));
            headerTable.addCell(createCell("Date:", 3, TextAlignment.LEFT).setBold());
            headerTable.addCell(createCell(invoice.dateOfInvoice.toString(), 3, TextAlignment.LEFT));
            headerTable.addCell(createCell("Due Date:", 3, TextAlignment.LEFT).setBold());
            headerTable.addCell(createCell(invoice.upDateOfInvoice.toString(), 3, TextAlignment.LEFT));
            document.add(headerTable);

            // Sender and Receiver Information
            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 2, 1, 2}))
                    .useAllAvailableWidth()
                    .setMarginTop(15);
            infoTable.addCell(createCell("FROM:", 3, TextAlignment.LEFT).setBorder(Border.NO_BORDER));
            infoTable.addCell(createCell("TO:", 3, TextAlignment.LEFT).setBold().setBorder(Border.NO_BORDER));
            infoTable.addCell(createCell("VENERANDA MEDICAL", 3, TextAlignment.LEFT).setBorder(Border.NO_BORDER));
            infoTable.addCell(createCell("TIN: ", 3, TextAlignment.LEFT).setBorder(Border.NO_BORDER));
            infoTable.addCell(createCell(invoice.toName, 3, TextAlignment.LEFT).setBorder(Border.NO_BORDER));
            infoTable.addCell(createCell("EMAIL: ", 3, TextAlignment.LEFT).setBorder(Border.NO_BORDER));
            document.add(infoTable);

            // Invoice Items
            Table itemsTable = new Table(UnitValue.createPercentArray(new float[]{4, 1, 1, 1}))
                    .useAllAvailableWidth()
                    .setMarginTop(15)
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY);
            itemsTable.addHeaderCell("Item");
            itemsTable.addHeaderCell("Qty");
            itemsTable.addHeaderCell("Unit Price (UGX)");
            itemsTable.addHeaderCell("Total (UGX)");

            for (ProcedureRequested procedureRequested : invoice.visit.getProceduresRequested()) {
                itemsTable.addCell(createCell(procedureRequested.procedureRequestedName, 3, TextAlignment.LEFT));
                itemsTable.addCell(createCell(String.valueOf(procedureRequested.quantity), 3, TextAlignment.RIGHT));
                itemsTable.addCell(createCell(String.valueOf(procedureRequested.unitSellingPrice), 3, TextAlignment.RIGHT));
                itemsTable.addCell(createCell(String.valueOf(procedureRequested.totalAmount), 3, TextAlignment.RIGHT));
            }

            document.add(itemsTable);

            // Summary
            Table summaryTable = new Table(UnitValue.createPercentArray(new float[]{2, 1}))
                    .useAllAvailableWidth()
                    .setMarginTop(10);
            summaryTable.addCell(createCell("Discount", 3, TextAlignment.LEFT).setBold());
            summaryTable.addCell(createCell(invoice.discount.toString(), 3, TextAlignment.RIGHT));
            summaryTable.addCell(createCell("Total", 3, TextAlignment.LEFT).setBold());
            summaryTable.addCell(createCell(invoice.totalAmount.toString(), 3, TextAlignment.RIGHT));
            document.add(summaryTable);

            // Notes
            if (invoice.notes != null && !invoice.notes.isEmpty()) {
                document.add(new Paragraph("NOTES").setBold().setMarginTop(10));
                document.add(new Paragraph(invoice.notes));
            }

            // Close the document
            document.close();

            // Convert to byte array and return response
            byte[] pdfBytes = baos.toByteArray();
            return Response.ok(new ByteArrayInputStream(pdfBytes))
                    .header("Content-Disposition", "attachment; filename=invoice_" + invoice.invoiceNo + ".pdf")
                    .type("application/pdf")
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Error generating invoice PDF", e);
        }
    }

    // Utility method to create cells with alignment
    private Cell createCell(String content, int i, TextAlignment alignment) {
        Cell cell = new Cell().add(new Paragraph(content));
        cell.setTextAlignment(alignment);
        return cell;
    }

    private String ellipsizeWithTwoDots(String value, int maxChars) {
        if (value == null) {
            return "";
        }
        String normalized = value.trim().toUpperCase();
        if (normalized.length() <= maxChars) {
            return normalized;
        }
        return normalized.substring(0, maxChars) + "..";
    }

    private String formatPresentingComplaints(List<PresentingComplaint> pcs) {
        if (pcs == null || pcs.isEmpty()) {
            return "";
        }
        StringJoiner joiner = new StringJoiner("; ");
        for (PresentingComplaint pc : pcs) {
            StringBuilder sb = new StringBuilder();
            if (pc.severity != null && !pc.severity.trim().isEmpty()) {
                sb.append(pc.severity.trim());
            }
            if (pc.site != null && !pc.site.trim().isEmpty()) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(pc.site.trim());
            }
            if (pc.complaint != null && !pc.complaint.trim().isEmpty()) {
                if (sb.length() > 0) {
                    sb.append(" ");
                }
                sb.append(pc.complaint.trim());
            }
            if (pc.durationValue != null && pc.durationUnit != null
                    && !pc.durationUnit.trim().isEmpty()) {
                sb.append(" FOR ");
                sb.append(pc.durationValue);
                sb.append(" ");
                sb.append(pc.durationValue == 1 ? pc.durationUnit.trim() : pc.durationUnit.trim() + "s");
            } else if (pc.duration != null && !pc.duration.trim().isEmpty()) {
                sb.append(" FOR ");
                sb.append(pc.duration.trim());
            }
            if (sb.length() > 0) {
                joiner.add(sb.toString());
            }
        }
        return joiner.toString();
    }

    private String formatVisitVitalsSummary(PatientVisit visit) {
        if (visit == null || visit.getInitialTriageVitals() == null || visit.getInitialTriageVitals().isEmpty()) {
            return "";
        }

        InitialTriageVitals latestVitals = visit.getInitialTriageVitals()
                .get(visit.getInitialTriageVitals().size() - 1);

        StringJoiner joiner = new StringJoiner(", ");
        if (latestVitals.bloodPressure != null && !latestVitals.bloodPressure.trim().isEmpty()) {
            joiner.add("BP: " + latestVitals.bloodPressure.trim() + " mmHg");
        }
        if (latestVitals.temperature != null) {
            joiner.add("TEMP: " + plainNumber(BigDecimal.valueOf(latestVitals.temperature)) + " 0^C");
        }
        if (latestVitals.pulseRate != null) {
            joiner.add("PULSE: " + plainNumber(BigDecimal.valueOf(latestVitals.pulseRate)) + " bpm");
        }
        if (latestVitals.respiratoryRate != null) {
            joiner.add("RR: " + latestVitals.respiratoryRate + " breaths/min");
        }
        if (latestVitals.spO2 != null) {
            joiner.add("SPO2: " + plainNumber(BigDecimal.valueOf(latestVitals.spO2)) + " %");
        }
        if (latestVitals.muac != null) {
            joiner.add("MUAC: " + plainNumber(latestVitals.muac) + " cm");
        }

        return joiner.toString();
    }


   /* @Transactional
    public Response generateAndReturnInvoicePdf(Long invoiceId) {
        Invoice invoice = Invoice.findById(invoiceId);

        if (invoice == null) {
            throw new IllegalArgumentException("Invoice not found.");
        }

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            PdfWriter pdfWriter = new PdfWriter(baos);
            PdfDocument pdfDocument = new PdfDocument(pdfWriter);

            Document document = new Document(pdfDocument);

            Table table = new Table(6);
            table.setWidth(UnitValue.createPercentValue(100));

            Cell[] headerCells = {
                    createCell("Number"),
                    createCell("Category"),
                    createCell("Title"),
                    createCell("Description"),
                    createCell("CostPrice"),
                    createCell("Creation Date")

            };

            for (Cell cell : headerCells) {
                cell.setTextAlignment(TextAlignment.CENTER);
                table.addCell(cell);
            }

            for (FullShopItemResponse Item : getShopItemsAdvancedFilter(request)) {
                table.addCell(createCell(Item.number));
                table.addCell(createCell(Item.category));
                table.addCell(createCell(Item.title));
                table.addCell(createCell(Item.description));
                table.addCell(createCell("$" + Item.costPrice.toString()));
                table.addCell(createCell(Item.creationDate.toString()));
            }

            document.add(table);

            document.close();

            byte[] pdfBytes = baos.toByteArray();

            return Response.ok(new ByteArrayInputStream(pdfBytes))
                    .header("Content-Disposition", "attachment; filename=shop_items.pdf")
                    .type("application/pdf")
                    .build();
        } catch (IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    private Cell createCell(String content) {
        return new Cell().add(content);
    }



    public List<FullShopItemResponse> getShopItemsAdvancedFilter(ShopItemParametersRequest request) {
        StringJoiner whereClause = getStringJoiner(request);

        String sql = """
                 
                    SELECT
                    id,
                    category,
                    number,
                    image,
                    title,
                    costPrice,
                    sellingPrice,
                    creationDate,
                    unitOfMeasure,
                    description
                    FROM item
                    %s
                    ORDER BY creationDate DESC;                             
                    """.formatted(whereClause);

        return client.query(sql)
                .execute()
                .onItem()
                .transformToMulti(rows -> Multi.createFrom().iterable(rows))
                .onItem()
                .transform(this::from)
                .collect().asList().await()
                .indefinitely();

    }

    private FullShopItemResponse from(Row row){

        FullShopItemResponse response = new FullShopItemResponse();
        response.id = row.getLong("id");
        response.description = row.getString("description");
        response.image = row.getString("image");
        response.number = row.getString("number");
        response.category = row.getString("category");
        response.title = row.getString("title");
        response.costPrice = row.getBigDecimal("costPrice");
        response.sellingPrice = row.getBigDecimal("sellingPrice");
        response.unitOfMeasure = row.getString("unitOfMeasure");
        response.creationDate = row.getLocalDate("creationDate");

        return response;
    }

    private FullShopItemResponse fullShopItemDTO(Item Item){
        FullShopItemResponse response = new FullShopItemResponse();
        response.id = Item.id;
        response.number = Item.number;
        response.costPrice = Item.costPrice;
        response.sellingPrice = Item.sellingPrice;
        response.description = Item.description;
        response.category = Item.category;
        response.unitOfMeasure = Item.unitOfMeasure;
        response.title = Item.title;
        response.creationDate =Item.creationDate;
        response.image = Item.image;

        return response;
    }

    private StringJoiner getStringJoiner(ShopItemParametersRequest request) {
        AtomicReference<Boolean> hasSearchCriteria = new AtomicReference<>(Boolean.FALSE);

        List<String> conditions = new ArrayList<>();
        if (request.category != null && !request.category.isEmpty()) {
            conditions.add("category = '" + request.category + "'");
            hasSearchCriteria.set(Boolean.TRUE);
        }

        if (request.title != null && !request.title.isEmpty()) {
            conditions.add("title = '" + request.title + "'");
            hasSearchCriteria.set(Boolean.TRUE);
        }

        if (request.datefrom != null && request.dateto != null) {
            conditions.add("creationDate BETWEEN '" + request.datefrom + "' AND '" + request.dateto + "'");
            hasSearchCriteria.set(Boolean.TRUE);
        }

        StringJoiner whereClause = new StringJoiner(" AND ", "WHERE ", "");

        conditions.forEach(whereClause::add);

        if (Boolean.FALSE.equals(hasSearchCriteria.get())) {
            whereClause.add("1 = 1");
        }

        return whereClause;
    }*/

















}
