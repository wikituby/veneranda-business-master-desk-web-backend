package org.example.client.services;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.element.IBlockElement;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import com.itextpdf.layout.property.VerticalAlignment;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.sqlclient.Pool;
import io.vertx.mutiny.sqlclient.Row;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicReference;
import org.example.client.domains.Patient;
import org.example.client.domains.PatientGroup;
import org.example.client.domains.repositories.PatientGroupRepository;
import org.example.client.domains.repositories.PatientRepository;
import org.example.client.services.DeletedPatientNosService;
import org.example.client.services.PatientGroupService;
import org.example.client.services.payloads.requests.PatientParametersRequest;
import org.example.client.services.payloads.requests.PatientRequest;
import org.example.client.services.payloads.requests.PatientUpdateRequest;
import org.example.client.services.payloads.responses.FullPatientResponse;
import org.example.client.services.payloads.responses.dtos.PatientDTO;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.configuration.security.AuthenticatedUserResolver;
import org.example.finance.invoice.domains.Invoice;
import org.example.finance.invoice.services.FooterHelperInvoice;
import org.example.procedure.procedureRequested.domains.ProcedureRequested;
import org.example.subscription.services.FacilityBranding;
import org.example.subscription.services.FacilityBrandingService;
import org.example.subscription.services.FacilityPdfLogoService;
import org.example.subscription.services.SpecialPrivilegeService;

@ApplicationScoped
public class PatientService {
    @Inject
    PatientRepository patientRepository;
    @Inject
    DeletedPatientNosService deletedPatientNosService;
    @Inject
    PatientGroupRepository patientGroupRepository;
    @Inject
    PatientGroupService patientGroupService;
    @Inject
    Pool client;
    @Inject
    FacilityBrandingService facilityBrandingService;
    @Inject
    FacilityPdfLogoService facilityPdfLogoService;
    @Inject
    SpecialPrivilegeService specialPrivilegeService;
    @Inject
    AuthenticatedUserResolver authenticatedUserResolver;
    public static final String NOT_FOUND = "Not found!";

    private FacilityBranding facilityBranding() {
        return this.facilityBrandingService.resolveDefaultBranding();
    }

    @Transactional
    public Response createNewPatient(PatientRequest request) {
        if (request != null) {
            request.normalizeClientAliases();
        }
        Patient existingPatient = this.patientRepository.findByFirstNameAndSecondName(request.patientFirstName, request.patientSecondName);
        if (existingPatient != null) {
            return Response.status(Response.Status.CONFLICT).entity(new ResponseMessage("A buyer with the same first and second names already exists", null)).build();
        }
        ResolvedGroups resolved;
        try {
            resolved = resolveGroups(request.patientGroupId, request.patientGroupIds);
        } catch (WebApplicationException ex) {
            return ex.getResponse();
        }
        Patient patient = new Patient();
        applyGroupMembership(patient, resolved);
        patient.patientFirstName = request.patientFirstName;
        patient.patientSecondName = request.patientSecondName;
        patient.patientAddress = request.patientAddress;
        patient.patientAge = request.patientAge;
        patient.patientContact = request.patientContact;
        patient.patientGender = request.patientGender;
        patient.occupation = request.occupation;
        patient.bloodGroup = request.bloodGroup;
        patient.patientDateOfBirth = request.patientDateOfBirth;
        patient.creationDate = LocalDate.now();
        patient.nextOfKinName = request.nextOfKinName;
        patient.nextOfKinAddress = request.nextOfKinAddress;
        patient.nextOfKinContact = request.nextOfKinContact;
        patient.relationship = request.relationship;
        patient.patientNo = this.patientRepository.generateNextPatientNo();
        patient.patientFileNo = "OPD" + patient.patientNo;
        this.patientRepository.persist(patient);
        return Response.status(Response.Status.CREATED).entity(new ResponseMessage("Patient created successfully", new PatientDTO(patient))).build();
    }

    public void updateTotalAmountDue(Patient buyer, BigDecimal totalAmountDue) {
        buyer.totalAmountDue = totalAmountDue;
        this.patientRepository.persist(buyer);
    }

    @Transactional
    public Response createMultiplePatients(List<PatientRequest> requests) {
        ArrayList<PatientDTO> createdPatients = new ArrayList<PatientDTO>();
        ArrayList<String> errors = new ArrayList<String>();
        for (PatientRequest request : requests) {
            try {
                if (request != null) {
                    request.normalizeClientAliases();
                }
                Patient existingPatient = this.patientRepository.findByFirstNameAndSecondName(request.patientFirstName, request.patientSecondName);
                if (existingPatient != null) {
                    errors.add("Duplicate buyer: " + request.patientFirstName + " " + request.patientSecondName);
                    continue;
                }
                ResolvedGroups resolved;
                try {
                    resolved = resolveGroups(request.patientGroupId, request.patientGroupIds);
                } catch (WebApplicationException ex) {
                    return ex.getResponse();
                }
                Patient patient = new Patient();
                applyGroupMembership(patient, resolved);
                patient.patientFirstName = request.patientFirstName;
                patient.patientSecondName = request.patientSecondName;
                patient.patientAddress = request.patientAddress;
                patient.patientAge = request.patientAge;
                patient.patientContact = request.patientContact;
                patient.patientGender = request.patientGender;
                patient.occupation = request.occupation;
                patient.bloodGroup = request.bloodGroup;
                patient.patientProfilePic = request.patientProfilePic;
                patient.patientDateOfBirth = request.patientDateOfBirth;
                patient.creationDate = LocalDate.now();
                patient.nextOfKinName = request.nextOfKinName;
                patient.nextOfKinAddress = request.nextOfKinAddress;
                patient.nextOfKinContact = request.nextOfKinContact;
                patient.relationship = request.relationship;
                patient.patientNo = this.patientRepository.generateNextPatientNo();
                patient.patientFileNo = "OPD" + patient.patientNo;
                this.patientRepository.persist(patient);
                createdPatients.add(new PatientDTO(patient));
            }
            catch (Exception ex) {
                errors.add("Error creating buyer: " + request.patientFirstName + " " + request.patientSecondName);
            }
        }
        if (createdPatients.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new ResponseMessage("No patients created", errors)).build();
        }
        return Response.status(Response.Status.CREATED).entity(new ResponseMessage("Patients created successfully", createdPatients)).build();
    }

    @Transactional
    public List<PatientDTO> getAllPatients() {
        return this.patientRepository.listAll(Sort.descending((String[])new String[]{"patientNo"})).stream().map(PatientDTO::new).toList();
    }

    @Transactional
    public List<PatientDTO> getAllPatientsWithDebt() {
        return this.patientRepository.listAll(Sort.descending((String[])new String[]{"patientNo"})).stream().filter(buyer -> buyer.getTotalBalanceDue() != null && buyer.getTotalBalanceDue().compareTo(BigDecimal.ZERO) > 0).map(PatientDTO::new).toList();
    }

    private Image getLogo() {
        return this.facilityPdfLogoService.createLogoImage();
    }

    @Transactional
    public PatientDebtResult getAllPatientsWithDebtAndCompassion() {
        List<Patient> patients = this.patientRepository.listAll(Sort.descending((String[])new String[]{"patientNo"})).stream().filter(patient -> patient.getTotalBalanceDue() != null && patient.getTotalBalanceDue().compareTo(BigDecimal.ZERO) > 0 && patient.hasCompassionMembership()).toList();
        BigDecimal totalDebt = patients.stream().map(Patient::getTotalBalanceDue).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        List<PatientDTO> patientDTOs = patients.stream().map(PatientDTO::new).toList();
        return new PatientDebtResult(patientDTOs, totalDebt);
    }

    @Transactional
    public Response generateAndReturnInvoicePdfForListOfCompassionPatients() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter pdfWriter = new PdfWriter((OutputStream)baos);
            PdfDocument pdfDocument = new PdfDocument(pdfWriter);
            pdfDocument.addEventHandler("EndPdfPage", (IEventHandler)new FooterHelperInvoice(this.facilityBranding()));
            Document document = new Document(pdfDocument);
            document.setMargins(36.0f, 36.0f, 90.0f, 36.0f);
            Table invoiceTitle = new Table(new float[]{1.0f});
            invoiceTitle.setWidth(UnitValue.createPercentValue((float)100.0f));
            invoiceTitle.addCell((Cell)((Cell)new Cell().add((IBlockElement)((Div)((Div)new Div().setBorderBottom((Border)new SolidBorder(1.0f))).setPaddingBottom(2.0f)).add((IBlockElement)((Paragraph)((Paragraph)new Paragraph("invoice").setBold()).setFontSize(11.0f)).setTextAlignment(TextAlignment.CENTER))).add((IBlockElement)((Paragraph)((Paragraph)new Paragraph(this.facilityBranding().financeHeaderLine()).setFontSize(7.0f)).setMarginTop(3.0f)).setTextAlignment(TextAlignment.CENTER)).setBorder(Border.NO_BORDER)).setPaddingBottom(15.0f));
            document.add((IBlockElement)invoiceTitle);
            Table headerTable = new Table(new float[]{1.0f, 1.0f, 1.0f, 2.0f, 1.0f});
            headerTable.setWidth(UnitValue.createPercentValue((float)100.0f));
            headerTable.addCell((Cell)((Cell)((Cell)((Cell)((Cell)new Cell().add(this.getLogo().setWidth(79.0f).setHeight(68.0f)).setBorder(Border.NO_BORDER)).setHorizontalAlignment(HorizontalAlignment.LEFT)).setVerticalAlignment(VerticalAlignment.TOP)).setPaddingTop(-7.0f)).setPaddingLeft(-22.0f));
            headerTable.addCell((Cell)new Cell().add((IBlockElement)((Paragraph)new Paragraph("FROM: ").setFontSize(7.0f)).setTextAlignment(TextAlignment.LEFT)).add((IBlockElement)((Paragraph)new Paragraph("ADDRESS: ").setFontSize(7.0f)).setTextAlignment(TextAlignment.LEFT)).add((IBlockElement)((Paragraph)new Paragraph("TO: ").setFontSize(7.0f)).setTextAlignment(TextAlignment.LEFT)).add((IBlockElement)((Paragraph)new Paragraph("ADDRESS: ").setFontSize(7.0f)).setTextAlignment(TextAlignment.LEFT)).setBorder(Border.NO_BORDER));
            headerTable.addCell((Cell)new Cell().add((IBlockElement)((Paragraph)new Paragraph("VENERANDA MEDICAL").setFontSize(7.0f)).setTextAlignment(TextAlignment.LEFT)).add((IBlockElement)((Paragraph)new Paragraph("BUGOGO TOWN COUNCIL, KYEGEGWA DISTRICT").setFontSize(7.0f)).setTextAlignment(TextAlignment.LEFT)).add((IBlockElement)((Paragraph)new Paragraph("KATOMA DEVELOPMENT CENTER").setFontSize(7.0f)).setTextAlignment(TextAlignment.LEFT)).add((IBlockElement)((Paragraph)new Paragraph().add(Optional.of("KATOMA").map(String::toUpperCase).orElse("")).setFontSize(7.0f)).setTextAlignment(TextAlignment.LEFT)).setBorder(Border.NO_BORDER));
            headerTable.addCell((Cell)new Cell().add((IBlockElement)((Paragraph)new Paragraph("NUMBER: ").setFontSize(7.0f)).setTextAlignment(TextAlignment.LEFT)).add((IBlockElement)((Paragraph)new Paragraph("DUE DATE: ").setFontSize(7.0f)).setTextAlignment(TextAlignment.LEFT)).add((IBlockElement)((Paragraph)new Paragraph("DATE: ").setFontSize(7.0f)).setTextAlignment(TextAlignment.LEFT)).add((IBlockElement)((Paragraph)new Paragraph("BALANCE DUE: ").setFontSize(7.0f)).setTextAlignment(TextAlignment.LEFT)).setBorder(Border.NO_BORDER));
            headerTable.addCell((Cell)new Cell().add((IBlockElement)((Paragraph)new Paragraph("UG-503").setFontSize(7.0f)).setTextAlignment(TextAlignment.RIGHT)).add((IBlockElement)((Paragraph)new Paragraph(String.valueOf("8/25/2025")).setFontSize(7.0f)).setTextAlignment(TextAlignment.RIGHT)).add((IBlockElement)((Paragraph)new Paragraph(String.valueOf("8/25/2025")).setFontSize(7.0f)).setTextAlignment(TextAlignment.RIGHT)).add((IBlockElement)((Paragraph)new Paragraph(String.valueOf("5000")).setFontSize(7.0f)).setTextAlignment(TextAlignment.RIGHT)).setBorder(Border.NO_BORDER));
            document.add((IBlockElement)headerTable);
            float[] columnWidths = new float[]{4.0f, 1.0f, 2.0f, 2.0f};
            Table itemsTable = new Table(columnWidths);
            itemsTable.setWidth(UnitValue.createPercentValue((float)100.0f));
            itemsTable.addCell((Cell)((Cell)((Cell)((Cell)((Cell)this.createCell("CLIENT NAME", 1, TextAlignment.LEFT).setBold()).setFontSize(7.0f)).setFontColor(ColorConstants.WHITE)).setBackgroundColor(ColorConstants.BLACK)).setBorder(Border.NO_BORDER));
            itemsTable.addCell((Cell)((Cell)((Cell)((Cell)((Cell)this.createCell("SERVICE", 1, TextAlignment.RIGHT).setBold()).setFontSize(7.0f)).setFontColor(ColorConstants.WHITE)).setBackgroundColor(ColorConstants.BLACK)).setBorder(Border.NO_BORDER));
            itemsTable.addCell((Cell)((Cell)((Cell)((Cell)((Cell)this.createCell("AMOUNT TO PAY (UGX)", 1, TextAlignment.RIGHT).setBold()).setFontSize(7.0f)).setFontColor(ColorConstants.WHITE)).setBackgroundColor(ColorConstants.BLACK)).setBorder(Border.NO_BORDER));
            itemsTable.addCell((Cell)((Cell)((Cell)((Cell)((Cell)this.createCell("AMOUNT PAID (UGX)", 1, TextAlignment.RIGHT).setBold()).setFontSize(7.0f)).setFontColor(ColorConstants.WHITE)).setBackgroundColor(ColorConstants.BLACK)).setBorder(Border.NO_BORDER));
            itemsTable.addCell((Cell)((Cell)((Cell)((Cell)((Cell)this.createCell("BALANCE DUE (UGX)", 1, TextAlignment.RIGHT).setBold()).setFontSize(7.0f)).setFontColor(ColorConstants.WHITE)).setBackgroundColor(ColorConstants.BLACK)).setBorder(Border.NO_BORDER));
            boolean isEvenRow = false;
            PatientDebtResult result = this.getAllPatientsWithDebtAndCompassion();
            List<PatientDTO> patientDTOs = result.patients();
            for (PatientDTO patientDto : patientDTOs) {
                Color rowColor = isEvenRow ? ColorConstants.WHITE : ColorConstants.LIGHT_GRAY;
                itemsTable.addCell((Cell)((Cell)((Cell)((Cell)this.createCell(patientDto.patientFirstName.toUpperCase() + " " + patientDto.patientSecondName.toUpperCase(), 1, TextAlignment.LEFT).setFontSize(7.0f)).setBackgroundColor(rowColor)).setBorder(Border.NO_BORDER)).setBorderBottom((Border)new SolidBorder(1.0f)));
                itemsTable.addCell((Cell)((Cell)((Cell)((Cell)this.createCell(String.valueOf("MEDICAL BILLS"), 1, TextAlignment.RIGHT).setFontSize(7.0f)).setBackgroundColor(rowColor)).setBorder(Border.NO_BORDER)).setBorderBottom((Border)new SolidBorder(1.0f)));
                itemsTable.addCell((Cell)((Cell)((Cell)((Cell)this.createCell(String.valueOf(patientDto.patientGender), 1, TextAlignment.RIGHT).setFontSize(7.0f)).setBackgroundColor(rowColor)).setBorder(Border.NO_BORDER)).setBorderBottom((Border)new SolidBorder(1.0f)));
                itemsTable.addCell((Cell)((Cell)((Cell)((Cell)this.createCell(String.valueOf(patientDto.patientGender), 1, TextAlignment.RIGHT).setFontSize(7.0f)).setBackgroundColor(rowColor)).setBorder(Border.NO_BORDER)).setBorderBottom((Border)new SolidBorder(1.0f)));
                itemsTable.addCell((Cell)((Cell)((Cell)((Cell)this.createCell(String.valueOf(patientDto.totalAmountDue), 1, TextAlignment.RIGHT).setFontSize(7.0f)).setBackgroundColor(rowColor)).setBorder(Border.NO_BORDER)).setBorderBottom((Border)new SolidBorder(1.0f)));
                isEvenRow = !isEvenRow;
            }
            document.add((IBlockElement)itemsTable);
            Table totalsTable = new Table(new float[]{4.0f, 2.0f, 2.0f});
            totalsTable.setWidth(UnitValue.createPercentValue((float)100.0f));
            Cell notesCell1 = (Cell)((Cell)((Cell)((Cell)new Cell(6, 1).add((IBlockElement)new Paragraph("\n IMPRESSION / DIAGNOSIS: ")).setTextAlignment(TextAlignment.LEFT)).setFontSize(7.0f)).setBorder(Border.NO_BORDER)).setVerticalAlignment(VerticalAlignment.TOP);
            totalsTable.addCell(notesCell1);
            BigDecimal totalDebt = result.totalDebt();
            totalsTable.addCell((Cell)((Cell)((Cell)((Cell)((Cell)this.createCell("SUBTOTAL:", 1, TextAlignment.LEFT).setFontSize(7.0f)).setBorder(Border.NO_BORDER)).setBorderBottom((Border)new SolidBorder(1.0f))).setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            totalsTable.addCell((Cell)((Cell)((Cell)((Cell)((Cell)this.createCell(String.valueOf(totalDebt), 1, TextAlignment.RIGHT).setFontSize(7.0f)).setBorder(Border.NO_BORDER)).setBorderBottom((Border)new SolidBorder(1.0f))).setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            totalsTable.addCell((Cell)((Cell)((Cell)this.createCell("DISCOUNT:", 1, TextAlignment.LEFT).setFontSize(7.0f)).setBorder(Border.NO_BORDER)).setBorderBottom((Border)new SolidBorder(1.0f)));
            totalsTable.addCell((Cell)((Cell)((Cell)this.createCell("50000", 1, TextAlignment.RIGHT).setFontSize(7.0f)).setBorder(Border.NO_BORDER)).setBorderBottom((Border)new SolidBorder(1.0f)));
            totalsTable.addCell((Cell)((Cell)((Cell)this.createCell("TAX:", 1, TextAlignment.LEFT).setFontSize(7.0f)).setBorder(Border.NO_BORDER)).setBorderBottom((Border)new SolidBorder(1.0f)));
            totalsTable.addCell((Cell)((Cell)((Cell)this.createCell("1000000", 1, TextAlignment.RIGHT).setFontSize(7.0f)).setBorder(Border.NO_BORDER)).setBorderBottom((Border)new SolidBorder(1.0f)));
            totalsTable.addCell((Cell)((Cell)((Cell)((Cell)((Cell)this.createCell("TOTAL AMOUNT:", 1, TextAlignment.LEFT).setBold()).setFontSize(7.0f)).setBorder(Border.NO_BORDER)).setBorderBottom((Border)new SolidBorder(1.0f))).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            totalsTable.addCell((Cell)((Cell)((Cell)((Cell)((Cell)this.createCell("500000", 1, TextAlignment.RIGHT).setBold()).setFontSize(7.0f)).setBorder(Border.NO_BORDER)).setBorderBottom((Border)new SolidBorder(1.0f))).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            totalsTable.addCell((Cell)((Cell)((Cell)this.createCell("AMOUNT PAID:", 1, TextAlignment.LEFT).setFontSize(7.0f)).setBorder(Border.NO_BORDER)).setBorderBottom((Border)new SolidBorder(1.0f)));
            totalsTable.addCell((Cell)((Cell)((Cell)this.createCell("700000", 1, TextAlignment.RIGHT).setFontSize(7.0f)).setBorder(Border.NO_BORDER)).setBorderBottom((Border)new SolidBorder(1.0f)));
            totalsTable.addCell((Cell)((Cell)((Cell)((Cell)((Cell)this.createCell("BALANCE DUE:", 1, TextAlignment.LEFT).setBold()).setFontSize(7.0f)).setBorder(Border.NO_BORDER)).setBorderBottom((Border)new SolidBorder(1.0f))).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            totalsTable.addCell((Cell)((Cell)((Cell)((Cell)((Cell)this.createCell("1200000", 1, TextAlignment.RIGHT).setBold()).setFontSize(7.0f)).setBorder(Border.NO_BORDER)).setBorderBottom((Border)new SolidBorder(1.0f))).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            document.add((IBlockElement)totalsTable);
            document.close();
            byte[] pdfBytes = baos.toByteArray();
            return Response.ok(new ByteArrayInputStream(pdfBytes)).header("Content-Disposition", (Object)"attachment; filename=invoice.pdf").type("application/pdf").build();
        }
        catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    public Response generateInvoicePdfWithLogo(Long invoiceId) {
        Response response;
        Invoice invoice = (Invoice)Invoice.findById(invoiceId);
        if (invoice == null) {
            throw new IllegalArgumentException("Invoice not found for ID: " + invoiceId);
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            PdfWriter writer = new PdfWriter((OutputStream)baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);
            try {
                String logoPath = "src/main/resources/logo.png";
                ImageData imageData = ImageDataFactory.create((String)logoPath);
                Image logo = new Image(imageData);
                logo.scaleToFit(80.0f, 80.0f);
                logo.setHorizontalAlignment(HorizontalAlignment.CENTER);
            }
            catch (IOException e) {
                document.close();
                throw new RuntimeException("Failed to load the logo image.", e);
            }
            Paragraph title = (Paragraph)((Paragraph)((Paragraph)new Paragraph("INVOICE").setFontSize(18.0f)).setBold()).setTextAlignment(TextAlignment.RIGHT);
            document.add((IBlockElement)title);
            Table headerTable = (Table)new Table(UnitValue.createPercentArray((float[])new float[]{2.0f, 3.0f})).useAllAvailableWidth().setMarginTop(10.0f);
            headerTable.addCell((Cell)this.createCell("Number:", 3, TextAlignment.LEFT).setBold());
            headerTable.addCell(this.createCell(invoice.invoiceNo, 3, TextAlignment.LEFT));
            headerTable.addCell((Cell)this.createCell("Reference:", 3, TextAlignment.LEFT).setBold());
            headerTable.addCell(this.createCell(invoice.reference, 3, TextAlignment.LEFT));
            headerTable.addCell((Cell)this.createCell("Date:", 3, TextAlignment.LEFT).setBold());
            headerTable.addCell(this.createCell(invoice.dateOfInvoice.toString(), 3, TextAlignment.LEFT));
            headerTable.addCell((Cell)this.createCell("Due Date:", 3, TextAlignment.LEFT).setBold());
            headerTable.addCell(this.createCell(invoice.upDateOfInvoice.toString(), 3, TextAlignment.LEFT));
            document.add((IBlockElement)headerTable);
            Table infoTable = (Table)new Table(UnitValue.createPercentArray((float[])new float[]{1.0f, 2.0f, 1.0f, 2.0f})).useAllAvailableWidth().setMarginTop(15.0f);
            infoTable.addCell((Cell)this.createCell("FROM:", 3, TextAlignment.LEFT).setBorder(Border.NO_BORDER));
            infoTable.addCell((Cell)((Cell)this.createCell("TO:", 3, TextAlignment.LEFT).setBold()).setBorder(Border.NO_BORDER));
            infoTable.addCell((Cell)this.createCell("VENERANDA MEDICAL", 3, TextAlignment.LEFT).setBorder(Border.NO_BORDER));
            infoTable.addCell((Cell)this.createCell("TIN: ", 3, TextAlignment.LEFT).setBorder(Border.NO_BORDER));
            infoTable.addCell((Cell)this.createCell(invoice.toName, 3, TextAlignment.LEFT).setBorder(Border.NO_BORDER));
            infoTable.addCell((Cell)this.createCell("EMAIL: ", 3, TextAlignment.LEFT).setBorder(Border.NO_BORDER));
            document.add((IBlockElement)infoTable);
            Table itemsTable = (Table)((Table)new Table(UnitValue.createPercentArray((float[])new float[]{4.0f, 1.0f, 1.0f, 1.0f})).useAllAvailableWidth().setMarginTop(15.0f)).setBackgroundColor(ColorConstants.LIGHT_GRAY);
            itemsTable.addHeaderCell("Item");
            itemsTable.addHeaderCell("Qty");
            itemsTable.addHeaderCell("Unit Price (UGX)");
            itemsTable.addHeaderCell("Total (UGX)");
            for (ProcedureRequested procedureRequested : invoice.visit.getProceduresRequested()) {
                itemsTable.addCell(this.createCell(procedureRequested.procedureRequestedType, 3, TextAlignment.LEFT));
                itemsTable.addCell(this.createCell(String.valueOf(procedureRequested.quantity), 3, TextAlignment.RIGHT));
                itemsTable.addCell(this.createCell(String.valueOf(procedureRequested.unitSellingPrice), 3, TextAlignment.RIGHT));
                itemsTable.addCell(this.createCell(String.valueOf(procedureRequested.totalAmount), 3, TextAlignment.RIGHT));
            }
            document.add((IBlockElement)itemsTable);
            Table summaryTable = (Table)new Table(UnitValue.createPercentArray((float[])new float[]{2.0f, 1.0f})).useAllAvailableWidth().setMarginTop(10.0f);
            summaryTable.addCell((Cell)this.createCell("Discount", 3, TextAlignment.LEFT).setBold());
            summaryTable.addCell(this.createCell(invoice.discount.toString(), 3, TextAlignment.RIGHT));
            summaryTable.addCell((Cell)this.createCell("Total", 3, TextAlignment.LEFT).setBold());
            summaryTable.addCell(this.createCell(invoice.totalAmount.toString(), 3, TextAlignment.RIGHT));
            document.add((IBlockElement)summaryTable);
            if (invoice.notes != null && !invoice.notes.isEmpty()) {
                document.add((IBlockElement)((Paragraph)new Paragraph("NOTES").setBold()).setMarginTop(10.0f));
                document.add((IBlockElement)new Paragraph(invoice.notes));
            }
            document.close();
            byte[] pdfBytes = baos.toByteArray();
            response = Response.ok(new ByteArrayInputStream(pdfBytes)).header("Content-Disposition", (Object)("attachment; filename=invoice_" + invoice.invoiceNo + ".pdf")).type("application/pdf").build();
        }
        catch (Throwable throwable) {
            try {
                try {
                    baos.close();
                }
                catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            }
            catch (Exception e) {
                throw new RuntimeException("Error generating invoice PDF", e);
            }
        }
        return response;
    }

    private Cell createCell(String content, int i, TextAlignment alignment) {
        Cell cell = new Cell().add((IBlockElement)new Paragraph(content));
        cell.setTextAlignment(alignment);
        return cell;
    }

    @Transactional
    public List<PatientDTO> getAllPatientsByGroupId(Long groupId) {
        return this.patientRepository.listAll(Sort.descending((String[])new String[]{"patientNo"})).stream()
                .filter(buyer -> buyer.isMemberOfGroupId(groupId))
                .map(PatientDTO::new)
                .toList();
    }

    public PatientDTO getPatientById(Long id) {
        return this.patientRepository.findByIdOptional(id).map(PatientDTO::new).orElseThrow(() -> new WebApplicationException("Patient not found", 404));
    }

    @Transactional
    public Response updatePatientById(Long id, PatientUpdateRequest request) {
        if (request != null) {
            request.normalizeClientAliases();
        }
        ResolvedGroups resolved;
        try {
            resolved = resolveGroups(request.patientGroupId, request.patientGroupIds);
        } catch (WebApplicationException ex) {
            return ex.getResponse();
        }
        Patient patient = (Patient)(this.patientRepository.findById(id));
        patient.patientFirstName = request.patientFirstName;
        patient.patientSecondName = request.patientSecondName;
        patient.patientAddress = request.patientAddress;
        patient.patientContact = request.patientContact;
        patient.patientGender = request.patientGender;
        patient.patientAge = request.patientAge;
        patient.occupation = request.occupation;
        patient.bloodGroup = request.bloodGroup;
        applyGroupMembership(patient, resolved);
        patient.nextOfKinName = request.nextOfKinName;
        patient.nextOfKinContact = request.nextOfKinContact;
        patient.relationship = request.relationship;
        patient.nextOfKinAddress = request.nextOfKinAddress;
        patient.patientDateOfBirth = request.patientDateOfBirth;
        patient.patientLastUpdatedDate = LocalDate.now();
        this.patientRepository.persist(patient);
        return Response.ok(new ResponseMessage("Patient Details Updated successfully", new PatientDTO(patient))).build();
    }

    @Transactional
    public Object findMaxPatientNo() {
        return this.patientRepository.listAll(Sort.descending((String[])new String[]{"patientNo"})).stream().findFirst().orElseThrow(() -> new WebApplicationException(NOT_FOUND, 404));
    }

    @Transactional
    public int findMaxPatientFileNoReturnInt() {
        return this.patientRepository.listAll(Sort.descending((String[])new String[]{"patientNo"})).stream().map(buyer -> buyer.patientNo).findFirst().orElse(0);
    }

    @Transactional
    public Response deletePatientById(Long id) {
        Patient deletedPatient = (Patient)(this.patientRepository.findById(id));
        if (deletedPatient == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        this.deletedPatientNosService.saveDeletedPatientNo(deletedPatient);
        this.patientRepository.delete(deletedPatient);
        return Response.ok(new ResponseMessage(ActionMessages.DELETED.label)).build();
    }

    public List<FullPatientResponse> getPatientsAdvancedFilter(PatientParametersRequest request) {
        StringJoiner whereClause = this.getStringJoiner(request);
        String sql = "SELECT\n    id,\n    group_id,\n    nextOfKinAddress,\n    nextOfKinContact,\n    nextOfKinName,\n    patientAddress,\n    patientAge,\n    patientContact,\n    patientDateOfBirth,\n    patientFileNo,\n    patientFirstName,\n    patientGender,\n    occupation,\n    bloodGroup,\n    patientLastUpdatedDate,\n    patientNo,\n    patientProfilePic,\n    patientSecondName,\n    relationship,\n    totalAmountDue\nFROM Patient\n%s\nORDER BY id DESC;\n".formatted(whereClause);
        return this.client.query(sql).execute().onItem().transformToMulti(rows -> Multi.createFrom().iterable(rows)).onItem().transform(row -> this.from(row)).collect().asList().await().indefinitely();
    }

    private FullPatientResponse from(Row row) {
        FullPatientResponse response = new FullPatientResponse();
        response.id = row.getLong("id");
        response.group_id = row.getLong("group_id");
        response.nextOfKinAddress = row.getString("nextOfKinAddress");
        response.nextOfKinContact = row.getString("nextOfKinContact");
        response.nextOfKinName = row.getString("nextOfKinName");
        response.patientAddress = row.getString("patientAddress");
        response.occupation = row.getString("occupation");
        response.bloodGroup = row.getString("bloodGroup");
        response.patientAge = row.getBigDecimal("patientAge");
        response.totalAmountDue = row.getBigDecimal("totalAmountDue");
        response.patientNo = row.getInteger("patientNo");
        response.patientContact = row.getString("patientContact");
        response.patientFileNo = row.getString("patientFileNo");
        response.patientFirstName = row.getString("patientFirstName");
        response.patientGender = row.getString("patientGender");
        response.patientProfilePic = row.getString("patientProfilePic");
        response.patientSecondName = row.getString("patientSecondName");
        response.patientDateOfBirth = row.getLocalDate("patientDateOfBirth");
        response.patientLastUpdatedDate = row.getLocalDate("patientLastUpdatedDate");
        return response;
    }

    private StringJoiner getStringJoiner(PatientParametersRequest request) {
        AtomicReference<Boolean> hasSearchCriteria = new AtomicReference<Boolean>(Boolean.FALSE);
        ArrayList<String> conditions = new ArrayList<String>();
        if (request.group_id != null) {
            conditions.add("(group_id = " + request.group_id
                    + " OR id IN (SELECT patient_id FROM patient_patient_group WHERE group_id = "
                    + request.group_id + "))");
            hasSearchCriteria.set(Boolean.TRUE);
        }
        if (request.patientAddress != null && !request.patientAddress.isEmpty()) {
            conditions.add("patientAddress = '" + request.patientAddress + "'");
            hasSearchCriteria.set(Boolean.TRUE);
        }
        if (request.patientGender != null && !request.patientGender.isEmpty()) {
            conditions.add("patientGender = '" + request.patientGender + "'");
            hasSearchCriteria.set(Boolean.TRUE);
        }
        StringJoiner whereClause = new StringJoiner(" AND ", "WHERE ", "");
        conditions.forEach(whereClause::add);
        if (Boolean.FALSE.equals(hasSearchCriteria.get())) {
            whereClause.add("1 = 1");
        }
        return whereClause;
    }

    public record PatientDebtResult(List<PatientDTO> patients, BigDecimal totalDebt) {
    }

    private record ResolvedGroups(PatientGroup primary, Set<PatientGroup> all) {
    }

    private ResolvedGroups resolveGroups(Long primaryId, List<Long> groupIds) {
        LinkedHashSet<Long> ids = new LinkedHashSet<>();
        if (groupIds != null) {
            for (Long id : groupIds) {
                if (id != null) {
                    ids.add(id);
                }
            }
        }
        if (ids.isEmpty() && primaryId != null) {
            ids.add(primaryId);
        }
        if (primaryId != null) {
            // Ensure primary is present and ordered first.
            LinkedHashSet<Long> ordered = new LinkedHashSet<>();
            ordered.add(primaryId);
            ordered.addAll(ids);
            ids = ordered;
        }

        LinkedHashSet<PatientGroup> groups = new LinkedHashSet<>();
        for (Long id : ids) {
            PatientGroup group = this.patientGroupRepository.findById(id);
            if (group == null) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                        .entity(new ResponseMessage("Patient group not found for ID: " + id, null))
                        .build());
            }
            if (this.specialPrivilegeService.requiresCreditGroupAssignPrivilege(group, null)
                    && !this.specialPrivilegeService.hasPrivilege(
                    this.authenticatedUserResolver.requireCurrentUser(),
                    SpecialPrivilegeService.Privilege.ADD_VENERANDA_MEDICAL_PATIENT)) {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ResponseMessage(
                                "You need MD approval to add a patient to a debt-permitted group", null))
                        .build());
            }
            groups.add(group);
        }

        PatientGroup primary = null;
        if (primaryId != null) {
            for (PatientGroup g : groups) {
                if (primaryId.equals(g.id)) {
                    primary = g;
                    break;
                }
            }
        }
        if (primary == null && !groups.isEmpty()) {
            primary = groups.iterator().next();
        }
        return new ResolvedGroups(primary, groups);
    }

    private void applyGroupMembership(Patient patient, ResolvedGroups resolved) {
        patient.patientGroup = resolved.primary();
        if (patient.patientGroups == null) {
            patient.patientGroups = new HashSet<>();
        } else {
            patient.patientGroups.clear();
        }
        patient.patientGroups.addAll(resolved.all());
    }
}
