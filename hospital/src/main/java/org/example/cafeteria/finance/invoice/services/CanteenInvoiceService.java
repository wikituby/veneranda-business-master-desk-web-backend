package org.example.cafeteria.finance.invoice.services;

import java.io.IOException;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;

import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
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
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.cafeteria.client.domains.BuyerGroup;
import org.example.cafeteria.client.services.BuyerService;
import org.example.cafeteria.finance.invoice.domains.CanteenInvoice;
import org.example.cafeteria.finance.invoice.domains.repositories.CanteenInvoiceRepository;
import org.example.cafeteria.finance.invoice.services.payloads.requests.CanteenInvoiceUpdateRequest;
import org.example.cafeteria.finance.invoice.services.payloads.responses.CanteenInvoiceDTO;
import org.example.cafeteria.finance.payments.cash.services.CanteenPaymentService;
import org.example.cafeteria.sales.saleDay.domains.SaleDay;
import org.example.cafeteria.sales.saleDay.domains.repository.SaleDayRepository;
import org.example.cafeteria.sales.saleDone.domains.Sale;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.subscription.services.FacilityPdfLogoService;
import org.example.procedure.procedureRequested.domains.ProcedureRequested;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

@ApplicationScoped
public class CanteenInvoiceService {

    @Inject
    CanteenInvoiceRepository canteenInvoiceRepository;

    @Inject
    SaleDayRepository saleDayRepository;

    @Inject
    CanteenPaymentService canteenPaymentService;

    @Inject
    BuyerService buyerService;

    @Inject
    FacilityPdfLogoService facilityPdfLogoService;

    private static final String NOT_FOUND = "Not found!";

    @Transactional
    public CanteenInvoice createInvoice(Long visitId) {
        // Check if the visit already has an invoice
        List<CanteenInvoice> existingInvoice = CanteenInvoice.find(
                "visit.id = ?1 ORDER BY id DESC",
                visitId
        ).list();

        // Check if the list is NOT empty
        if (!existingInvoice.isEmpty()) {
            throw new IllegalArgumentException("Invoice already exists for this visit.");
        }

        // Find the patient visit
        SaleDay saleDay = SaleDay.findById(visitId);

        // Throw exception if the visit is not found
        if (saleDay == null) {
            throw new IllegalArgumentException("Visit not found.");
        }

        // Create the invoice
        CanteenInvoice invoice = new CanteenInvoice();
        invoice.saleDay = saleDay;
        invoice.buyer = saleDay.patient;
        invoice.tin = "185 7564 3489";
        invoice.notes = "Type in a brief note";
        invoice.discount = BigDecimal.valueOf(0.00);
        invoice.tax = BigDecimal.valueOf(0.00);
        invoice.dateOfInvoice = LocalDate.now();
        invoice.timeOfCreation = LocalTime.now();
        invoice.toName = saleDay.patient.patientFirstName + " " + saleDay.patient.patientSecondName;
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

        // Persist the invoice
        canteenInvoiceRepository.persist(invoice);

        //updateSubTotal(subTotalCalculated, visitId);

        return invoice;
    }


    public BigDecimal calculateTotalBalanceDueForClosedVisits(Long patientId) {
        List<CanteenInvoice> invoices = CanteenInvoice.find(
                "visit.patient.id = ?1 and visit.visitStatus = ?2", patientId, "closed"
        ).list();

        return invoices.stream()
                .map(invoice -> invoice.balanceDue)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }




    @Transactional
    public Response updateInvoice(Long invoiceId, CanteenInvoiceUpdateRequest request) {
        // Find the existing invoice
        CanteenInvoice invoice = CanteenInvoice.findById(invoiceId);

        if (invoice == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Invoice not found.", null))
                    .build();
        }

        if ("closed".equals(invoice.saleDay.visitStatus)) {
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

        // Assign 0.00 if discount or tax are null
        BigDecimal discount = (request.discount != null) ? request.discount : invoice.discount;
        BigDecimal tax = (request.tax != null) ? request.tax : invoice.tax;

        // Recalculate subtotal, total amount, and balance due
        Map<String, BigDecimal> invoiceSubTotalMap = getInvoiceSubTotal(invoice.saleDay.id);
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

        invoice.amountPaid = canteenPaymentService.getTotalPaymentOfInvoice(invoiceId);
        invoice.balanceDue = totalAmount.subtract(invoice.amountPaid);

        // Update other fields
        invoice.upDateOfInvoice = LocalDate.now();
        invoice.updateTimeOfCreation = LocalTime.now();
        invoice.notes = request.notes;
        invoice.discount = discount;
        invoice.tax = tax;

        // Persist
        canteenInvoiceRepository.persist(invoice);

        return Response.ok(new ResponseMessage("Invoice updated successfully", new CanteenInvoiceDTO(invoice))).build();
    }


    @Transactional
    public void updateInvoiceAmountPaid(CanteenInvoice invoice) {
        // Calculate the total payments made for the invoice
        if (invoice == null) {
            throw new IllegalArgumentException("Invoice not found.");
        }
        BigDecimal totalPayments = canteenPaymentService.getTotalPaymentOfInvoice(invoice.id);

        totalPayments = totalPayments != null ? totalPayments : BigDecimal.ZERO;

        // Update the invoice fields
        invoice.amountPaid = totalPayments;
        invoice.balanceDue = invoice.totalAmount.subtract(totalPayments);

        // Persist the updated invoice
        canteenInvoiceRepository.persist(invoice);
    }

    @Transactional
    public List<CanteenInvoiceDTO> getAllInvoices() {
        return canteenInvoiceRepository.listAll(Sort.descending("invoicePlainNo"))
                .stream()
                .map(CanteenInvoiceDTO::new)
                .toList();
    }

    @Transactional
    public Response getInvoiceByVisitId(Long visitId) {
        CanteenInvoice invoice = canteenInvoiceRepository.find("visit.id", visitId)
                .firstResult();

        if (invoice == null) {
            //throw new WebApplicationException("Invoice not found for visitId: " + visitId, 404);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Invoice not found for visitId: " + visitId, null))
                    .build();
        }

       // return new InvoiceDTO(invoice);
        return Response.ok(new ResponseMessage("Invoice updated successfully", new CanteenInvoiceDTO(invoice))).build();

    }


    @Transactional
    public int findMaxInvoiceNoReturnInt() {
        return canteenInvoiceRepository.listAll(Sort.descending("invoicePlainNo"))
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
    public Map<String, BigDecimal> getInvoiceSubTotal(Long visitId) {
        List<ProcedureRequested> scanProcedures = ProcedureRequested.find(
                "category = ?1 and visit.id = ?2 ORDER BY id DESC",
                "imaging",
                visitId
        ).list();

        List<ProcedureRequested> labTestsProcedures = ProcedureRequested.find(
                "category = ?1 and visit.id = ?2 ORDER BY id DESC",
                "labtest",
                visitId
        ).list();

        List<ProcedureRequested> consultationProcedures = ProcedureRequested.find(
                "category = ?1 and visit.id = ?2 ORDER BY id DESC",
                "consultation", // category filter for 'consultation'
                visitId         // Visit ID filter
        ).list();



        List<ProcedureRequested> otherProcedures = ProcedureRequested.find(
                "category NOT IN (?1, ?2, ?3) and visit.id = ?4 ORDER BY id DESC",
                "labtest",      // First category to exclude
                "imaging",      // Second category to exclude
                "consultation", // Third category to exclude
                visitId         // Visit ID filter
        ).list();


        List<Sale> treatmentGive = Sale.find(
                "visit.id = ?1 ORDER BY id DESC",
                visitId
        ).list();

       // BigDecimal consultationFee = checkIfConsultationWasDone(visitId);

        BigDecimal consultationFee = consultationProcedures.stream()
                .map(procedureRequested -> procedureRequested.totalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal ultrasoundTotalAmount = scanProcedures.stream()
                .map(procedureRequested -> procedureRequested.totalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal labTotalAmount = labTestsProcedures.stream()
                .map(procedureRequested -> procedureRequested.totalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal otherProcedureTotalAmount = otherProcedures.stream()
                .map(procedureRequested -> procedureRequested.totalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal treatmentTotalCost = treatmentGive.stream()
                .map(treatmentRequested -> treatmentRequested.totalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal invoiceSubtotal = labTotalAmount
                .add(ultrasoundTotalAmount)
                .add(treatmentTotalCost)
                .add(otherProcedureTotalAmount)
                .add(consultationFee);


        SaleDay saleDay = saleDayRepository.findById(visitId);
        if (saleDay == null) {
            throw new IllegalArgumentException("Visit not found.");
        }

        // Check if the visit has an invoice
        if (saleDay.invoice == null || saleDay.invoice.isEmpty()) {
            // Create a new invoice
            CanteenInvoice newInvoice = new CanteenInvoice();
            newInvoice.saleDay = saleDay; // Associate the invoice with the visit
            newInvoice.buyer = saleDay.patient;
            newInvoice.tin = "185 7564 3489";
            newInvoice.notes = "Type in a brief note";
            newInvoice.dateOfInvoice = LocalDate.now();
            newInvoice.timeOfCreation = LocalTime.now();
            newInvoice.toName = saleDay.patient.patientFirstName + " " + saleDay.patient.patientSecondName;
            newInvoice.fromName = "VENERANDA MEDICAL";
            newInvoice.fromAddress = "Bugogo Town Council-Kyegegwa District";
            newInvoice.toAddress = "Bugogo Town Council-Kyegegwa District";
            newInvoice.companyLogo = "https://firebasestorage.googleapis.com/v0/b/newstorageforuplodapp.appspot.com/o/images%2FAsset%201.png?alt=media&token=08b34d6a-0693-4dff-88b1-6e42b5c56f67";
            newInvoice.documentTitle = "INVOICE";
            newInvoice.invoicePlainNo = findMaxInvoiceNoReturnInt() + 1;
            newInvoice.invoiceNo = "VMDINV-" + findMaxInvoiceNoReturnInt() + 1;
            newInvoice.reference = generateRandomReferenceNo(20);
            newInvoice.subTotal = invoiceSubtotal; // Initialize subtotal
            newInvoice.discount = BigDecimal.ZERO; // Initialize discount
            newInvoice.tax = BigDecimal.ZERO; // Initialize tax
            newInvoice.totalAmount = invoiceSubtotal; // Initialize total amount
            BigDecimal totalAmountPaid = canteenPaymentService.getTotalPaymentOfVisit(visitId);
            newInvoice.amountPaid = totalAmountPaid; // Initialize amount paid
            newInvoice.balanceDue = invoiceSubtotal.subtract(totalAmountPaid); // Initialize balance due

            // Persist the new invoice
            canteenInvoiceRepository.persist(newInvoice);

            // Add the new invoice to the visit
            if (saleDay.invoice == null) {
                saleDay.invoice = new ArrayList<>();
            }
            saleDay.invoice.add(newInvoice);

            // Update the visit in the repository
            saleDayRepository.persist(saleDay);
        }

        // Return the patient associated with the visit

        //updateSubTotal(invoiceSubtotal, visitId);

        // Get the first invoice from the list (or handle multiple invoices as needed)
        CanteenInvoice invoiceUpdate = saleDay.invoice.get(0); // Assuming visit.invoice is a List<Invoice>

        // Update the invoice fields
        invoiceUpdate.subTotal = invoiceSubtotal;
        //BigDecimal totalAmount = invoiceSubtotal.subtract(invoiceUpdate.discount.add(invoiceUpdate.tax));
        //BigDecimal totalAmount = (invoiceSubtotal.subtract(invoiceUpdate.discount)).subtract(invoiceUpdate.tax);
        BigDecimal totalAmountDiscounted = invoiceSubtotal.subtract(invoiceUpdate.discount);

        BigDecimal totalAmount = totalAmountDiscounted.add(invoiceUpdate.tax);


        invoiceUpdate.totalAmount = totalAmount;

        invoiceUpdate.amountPaid = canteenPaymentService.getTotalPaymentOfInvoice(invoiceUpdate.id);
        invoiceUpdate.balanceDue = totalAmount.subtract(canteenPaymentService.getTotalPaymentOfInvoice(invoiceUpdate.id));

        // Persist the updated invoice
        canteenInvoiceRepository.persist(invoiceUpdate);

        List<CanteenInvoice> allInvoices = CanteenInvoice.find(
                "patient.id = ?1 ORDER BY id DESC",
                saleDay.getPatient().getId()
        ).list();



        CanteenInvoice invoiceWithVisitId = canteenInvoiceRepository.find(
                "visit.id", visitId
        ).firstResult();

        if (invoiceWithVisitId == null) {
            throw new IllegalArgumentException("invoice not found.");

        }

        BigDecimal invoiceBalanceDue = invoiceWithVisitId.balanceDue;

        Long invoiceId = invoiceWithVisitId.id;


        BigDecimal totalAmountDue = allInvoices.stream()
                .map(invoice -> invoice.balanceDue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);


        buyerService.updateTotalAmountDue(saleDay.patient, totalAmountDue);




        // Return as a map with keys for clarity
        Map<String, BigDecimal> totalCostMap = new HashMap<>();


        totalCostMap.put("LabTestTotal", labTotalAmount);
        totalCostMap.put("UltrasoundTotal", ultrasoundTotalAmount);
        totalCostMap.put("OtherProcedureCost", otherProcedureTotalAmount);
        totalCostMap.put("ConsultationFee", consultationFee);
        totalCostMap.put("TreatmentTotalCost", treatmentTotalCost);
        totalCostMap.put("InvoiceId", BigDecimal.valueOf(invoiceId));


        totalCostMap.put("InvoiceSubtotal", invoiceSubtotal);

        totalCostMap.put("TotalAmountDue", totalAmountDue);

        // from invoice

        totalCostMap.put("Discount", invoiceWithVisitId.discount);
        totalCostMap.put("Tax", invoiceWithVisitId.tax);
        totalCostMap.put("TotalAmount", invoiceWithVisitId.totalAmount);
        totalCostMap.put("AmountPaid", invoiceWithVisitId.amountPaid);
        totalCostMap.put("BalanceDue", invoiceBalanceDue);



        return totalCostMap;
        //return Response.ok(new ResponseMessage("Invoice updated successfully", new InvoiceDTO(invoice))).build();

    }

    @Transactional
    public Response deleteInvoice(Long id) {
        try {
            // Execute the custom SQL query to delete the payment
            int rowsDeleted = canteenInvoiceRepository.deleteInvoiceById(id);

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

        List<CanteenInvoice> allInvoices = CanteenInvoice.find(
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
        } catch (IOException | MalformedURLException e) {
            throw new RuntimeException("Failed to load the logo image.", e);
        }
    }*/




    @Transactional
    public Response generateAndReturnInvoicePdf(Long visitId) {
        try {
            // Find the patient visit
            SaleDay visit = SaleDay.findById(visitId);
            if (visit == null) {
                throw new IllegalArgumentException("Visit not found.");
            }

            // Ensure visit.invoice is not null and contains at least one invoice
            if (visit.invoice == null || visit.invoice.isEmpty()) {
                CanteenInvoice invoice = createInvoice(visitId); // Create a new invoice
                visit.invoice.add(invoice); // Add invoice to visit (if applicable)
                visit.persist(); // Save the changes

            }

            // Get the first invoice from the list (or handle multiple invoices as needed)
            CanteenInvoice invoice = visit.invoice.get(0); // Assuming visit.invoice is a List<Invoice>

            // Ensure the invoice is not null
            if (invoice == null) {
                throw new IllegalArgumentException("Invoice not found.");
            }

            // Create the PDF document
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter pdfWriter = new PdfWriter(baos);
            PdfDocument pdfDocument = new PdfDocument(pdfWriter);
            Document document = new Document(pdfDocument);

            // Add invoice title
            Table invoiceTitle = new Table(new float[]{1});
            invoiceTitle.setWidth(UnitValue.createPercentValue(100));
            invoiceTitle.addCell(new Cell()
                    .add(new Paragraph("STATEMENT")
                            .setBold()
                            .setFontSize(11)
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
                    .setPaddingLeft(-27)
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
                    .add(new Paragraph("BUGOGO VILLAGE, KYEGEGWA").setFontSize(7).setTextAlignment(TextAlignment.LEFT))
                    .add(new Paragraph(invoice.saleDay.patient.patientFirstName.toUpperCase() + " " + invoice.saleDay.patient.patientSecondName.toUpperCase())
                            .setFontSize(7).setTextAlignment(TextAlignment.LEFT))
                    .add(new Paragraph()
                             .add(Optional.ofNullable(invoice.saleDay.patient.getPatientGroup())
                                     .map(BuyerGroup::getGroupName)
                                     .map(String::toUpperCase)
                                     .orElse(""))
                             .setFontSize(7)
                             .setTextAlignment(TextAlignment.LEFT))

                    .add(new Paragraph()
                             .add(Optional.ofNullable(invoice.saleDay.patient.patientAddress)
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
            /*for (Consultation consultation : invoice.saleDay.getConsultation()) {
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
            assert invoice.saleDay != null;
            /*for (ProcedureRequested procedureRequested : invoice.saleDay.getProceduresRequested()) {
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
            }*/

            // Add rows for TreatmentRequested
            for (Sale treatmentRequested : invoice.saleDay.getTreatmentRequested()) {
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


            Cell notesCell1 = new Cell(6, 1)
                    .add(new Paragraph("NOTES: " +"\n"+"PATIENT NAME: " +invoice.saleDay.patient.patientFirstName.toUpperCase()+" "+invoice.saleDay.patient.patientSecondName.toUpperCase() ))
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





    public Response generateInvoicePdfWithLogo(Long invoiceId) {
        // Retrieve the invoice
        CanteenInvoice invoice = CanteenInvoice.findById(invoiceId);
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
                document.close();
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

            /*for (ProcedureRequested procedureRequested : invoice.saleDay.getProceduresRequested()) {
                itemsTable.addCell(createCell(procedureRequested.procedureRequestedType, 3, TextAlignment.LEFT));
                itemsTable.addCell(createCell(String.valueOf(procedureRequested.quantity), 3, TextAlignment.RIGHT));
                itemsTable.addCell(createCell(String.valueOf(procedureRequested.unitSellingPrice), 3, TextAlignment.RIGHT));
                itemsTable.addCell(createCell(String.valueOf(procedureRequested.totalAmount), 3, TextAlignment.RIGHT));
            }*/

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






