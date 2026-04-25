package org.example.client.services;

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
import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.mysqlclient.MySQLPool;
import io.vertx.mutiny.sqlclient.Row;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.example.client.domains.DeletedPatient;
import org.example.client.domains.repositories.PatientRepository;
import org.example.client.domains.Patient;
import org.example.client.domains.PatientGroup;
import org.example.client.domains.repositories.PatientGroupRepository;
import org.example.client.services.payloads.requests.PatientParametersRequest;
import org.example.client.services.payloads.requests.PatientRequest;
import org.example.client.services.payloads.requests.PatientUpdateRequest;
import org.example.client.services.payloads.responses.FullPatientResponse;
import org.example.client.services.payloads.responses.dtos.PatientDTO;
import org.example.client.services.payloads.responses.dtos.PatientGroupDTO;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.consultations.services.payloads.responses.ConsultationDTO;
import org.example.finance.invoice.domains.Invoice;
import org.example.finance.invoice.services.FooterHelperInvoice;
import org.example.procedure.procedureRequested.domains.ProcedureRequested;
import org.example.treatment.domains.TreatmentRequested;
import org.example.treatment.services.payloads.responses.TreatmentRequestedDTO;
import org.example.visit.domains.PatientVisit;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicReference;

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
    MySQLPool client;


    public static final String NOT_FOUND = "Not found!";

    @Transactional
    public Response createNewPatient(PatientRequest request) {

        // Check if a buyer with the same first and second names already exists
        Patient existingPatient = patientRepository.findByFirstNameAndSecondName(
                request.patientFirstName, request.patientSecondName);

        if (existingPatient != null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ResponseMessage("A buyer with the same first and second names already exists", null))
                    .build();
        }


        PatientGroup patientGroup = null;
        if (request.patientGroupId != null) {
            patientGroup = patientGroupRepository.findById(request.patientGroupId);

            if (patientGroup == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ResponseMessage("Patient group not found for ID: " + request.patientGroupId, null))
                        .build();
            }

            // Check if group is "veneranda medical" and role is not "md"
            if ("veneranda medical".equalsIgnoreCase(patientGroup.groupName) &&
                    (request.userRole == null || !request.userRole.equalsIgnoreCase("md"))) {

                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ResponseMessage("You need admin approval to add any patient in Veneranda Medical group", null))
                        .build();
            }
        }


        // Create new Patient entity and set basic information
        Patient patient = new Patient();
        patient.patientGroup = patientGroup;
        patient.patientFirstName = request.patientFirstName;
        patient.patientSecondName = request.patientSecondName;
        patient.patientAddress = request.patientAddress;
        patient.patientAge = request.patientAge;
        patient.patientContact = request.patientContact;
        patient.patientGender = request.patientGender;
        patient.occupation = request.occupation;


//      buyer.patientProfilePic = "https://firebasestorage.googleapis.com/v0/b/newstorageforuplodapp.appspot.com/o/images%2Fplaceholder.jpg?alt=media&token=caade802-c591-4dee-b590-a040c694553b";

        patient.patientDateOfBirth = request.patientDateOfBirth;
        patient.creationDate = LocalDate.now();

        // Set Next of Kin information
        patient.nextOfKinName = request.nextOfKinName;
        patient.nextOfKinAddress = request.nextOfKinAddress;
        patient.nextOfKinContact = request.nextOfKinContact;
        patient.relationship = request.relationship;

// Determine buyer number
       // DeletedPatient deletedPatientInQue = deletedPatientNosService.findFirstDeletedPatient();

        patient.patientNo = patientRepository.generateNextPatientNo();


        // Generate buyer file number
        patient.patientFileNo = "VMD" + patient.patientNo;

        // Persist the new Patient entity
        patientRepository.persist(patient);

        // Remove the deleted buyer number from the queue
        /*if (deletedPatientInQue.patientNo != 0) {
            deletedPatientNosService.deleteByDeletedPatient(deletedPatientInQue);
        }*/

        // Return a success response with the created PatientDTO
        return Response.status(Response.Status.CREATED)
                .entity(new ResponseMessage("Patient created successfully", new PatientDTO(patient)))
                .build();
    }


    public void updateTotalAmountDue(Patient buyer, BigDecimal totalAmountDue){

        buyer.totalAmountDue = totalAmountDue;

        patientRepository.persist(buyer);

    }




    @Transactional
    public Response createMultiplePatients(List<PatientRequest> requests) {
        List<PatientDTO> createdPatients = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (PatientRequest request : requests) {
            try {

                // Check for existing buyer
                Patient existingPatient = patientRepository.findByFirstNameAndSecondName(
                        request.patientFirstName, request.patientSecondName);
                if (existingPatient != null) {
                    errors.add("Duplicate buyer: " + request.patientFirstName + " " + request.patientSecondName);
                    continue;
                }

                // Check buyer group
                PatientGroup patientGroup = null;
                if (request.patientGroupId != null) {
                    patientGroup = patientGroupRepository.findById(request.patientGroupId);

                    if (patientGroup == null) {
                        return Response.status(Response.Status.NOT_FOUND)
                                .entity(new ResponseMessage("Patient group not found for ID: " + request.patientGroupId, null))
                                .build();
                    }

                    // Check if group is "veneranda medical" and role is not "md"
                    if ("veneranda medical".equalsIgnoreCase(patientGroup.groupName) &&
                            (request.userRole == null || !request.userRole.equalsIgnoreCase("md"))) {

                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity(new ResponseMessage("You need admin approval to add any patient in Veneranda Medical group", null))
                                .build();
                    }
                }

                // Create and populate Patient
                Patient patient = new Patient();
                patient.patientGroup = patientGroup;
                patient.patientFirstName = request.patientFirstName;
                patient.patientSecondName = request.patientSecondName;
                patient.patientAddress = request.patientAddress;
                patient.patientAge = request.patientAge;
                patient.patientContact = request.patientContact;
                patient.patientGender = request.patientGender;
                patient.occupation = request.occupation;
                patient.patientProfilePic = request.patientProfilePic;
                patient.patientDateOfBirth = request.patientDateOfBirth;
                patient.creationDate = LocalDate.now();

                // Set next of kin info
                patient.nextOfKinName = request.nextOfKinName;

                patient.nextOfKinAddress = request.nextOfKinAddress;
                patient.nextOfKinContact = request.nextOfKinContact;
                patient.relationship = request.relationship;

                // Assign buyer number
                // Determine buyer number
               /* DeletedPatient deletedPatientInQue = deletedPatientNosService.findFirstDeletedPatient();

                if (deletedPatientInQue != null) {
                    buyer.patientNo = deletedPatientInQue.patientNo;
                    //deletedPatientNosService.delete(deletedPatientInQue); // remove used number
                } else {
                    buyer.patientNo = patientRepository.generateNextPatientNo();
                }*/

                patient.patientNo = patientRepository.generateNextPatientNo();

                patient.patientFileNo = "VMD" + patient.patientNo;

                patientRepository.persist(patient);

                // Remove number from deleted queue
                /*assert deletedPatientInQue != null;
                if (deletedPatientInQue.patientNo != 0) {
                    deletedPatientNosService.deleteByDeletedPatient(deletedPatientInQue);
                }*/

                createdPatients.add(new PatientDTO(patient));

            } catch (Exception ex) {
                errors.add("Error creating buyer: " + request.patientFirstName + " " + request.patientSecondName);
            }
        }

        if (createdPatients.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("No patients created", errors))
                    .build();
        }

        return Response.status(Response.Status.CREATED)
                .entity(new ResponseMessage("Patients created successfully", createdPatients))
                .build();
    }










    /*@Transactional
    public List<Patient> getAllPatients() {
        return patientRepository.listAll(Sort.descending("patientNo"));
    }*/

    @Transactional
    public List<PatientDTO> getAllPatients() {
        return patientRepository.listAll(Sort.descending("patientNo"))
                .stream()
                .map(PatientDTO::new)
                .toList();
    }

    @Transactional
    public List<PatientDTO> getAllPatientsWithDebt() {
        return patientRepository.listAll(Sort.descending("patientNo"))
                .stream()
                .filter(buyer -> buyer.getTotalBalanceDue() != null &&
                        buyer.getTotalBalanceDue().compareTo(BigDecimal.ZERO) > 0)
                .map(PatientDTO::new)
                .toList();
    }


    public static Image getLogo() {
        try {
            // Load the image as a stream from the classpath
            InputStream logoStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("logo.png");

            if (logoStream == null) {
                throw new RuntimeException("Logo image not found in classpath.");
            }

            byte[] imageBytes = logoStream.readAllBytes(); // Java 9+; use IOUtils for Java 8
            ImageData imageData = ImageDataFactory.create(imageBytes);

            Image logo = new Image(imageData);
            logo.scaleToFit(80, 80);
            logo.setHorizontalAlignment(HorizontalAlignment.CENTER);

            return logo;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load the logo image.", e);
        }
    }
/*@Transactional
    public List<PatientDTO> getAllPatientsWithDebtAndCompassion() {
        return patientRepository.listAll(Sort.descending("patientNo"))
                .stream()
                .filter(patient ->
                        // patient must have debt
                        (patient.getTotalBalanceDue() != null &&
                                patient.getTotalBalanceDue().compareTo(BigDecimal.ZERO) > 0)
                                // AND must be under compassion group
                                && (patient.getPatientGroup() != null &&
                                "compassion".equalsIgnoreCase(patient.patientGroup.groupNameShortForm))
                )
                .map(PatientDTO::new)
                .toList();
    }*/

    @Transactional
    public PatientDebtResult getAllPatientsWithDebtAndCompassion() {
        List<Patient> patients = patientRepository.listAll(Sort.descending("patientNo"))
                .stream()
                .filter(patient ->
                        (patient.getTotalBalanceDue() != null &&
                                patient.getTotalBalanceDue().compareTo(BigDecimal.ZERO) > 0)
                                && (patient.getPatientGroup() != null &&
                                "compassion".equalsIgnoreCase(patient.patientGroup.groupNameShortForm))
                )
                .toList();

        // Calculate total debt
        BigDecimal totalDebt = patients.stream()
                .map(Patient::getTotalBalanceDue)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Convert to DTOs
        List<PatientDTO> patientDTOs = patients.stream()
                .map(PatientDTO::new)
                .toList();

        return new PatientDebtResult(patientDTOs, totalDebt);
    }

    // Create a wrapper class
    public record PatientDebtResult(List<PatientDTO> patients, BigDecimal totalDebt) {}








    @Transactional
    public Response generateAndReturnInvoicePdfForListOfCompassionPatients() {
        try {

            // Create the PDF document
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter pdfWriter = new PdfWriter(baos);
            PdfDocument pdfDocument = new PdfDocument(pdfWriter);
            pdfDocument.addEventHandler(PdfDocumentEvent.END_PAGE, new FooterHelperInvoice());

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
                    .add(new Paragraph("Department of Finance - Veneranda Medical (A subsidiary of Veneranda Hospital)")
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
            PatientDebtResult result = getAllPatientsWithDebtAndCompassion();

            List<PatientDTO> patientDTOs = result.patients();

            for (PatientDTO patientDto : patientDTOs) {
                com.itextpdf.kernel.colors.Color rowColor = isEvenRow
                        ? ColorConstants.WHITE
                        : ColorConstants.LIGHT_GRAY;

                itemsTable.addCell(createCell(patientDto.patientFirstName.toUpperCase() + " " + patientDto.patientSecondName.toUpperCase() , 1, TextAlignment.LEFT)
                        .setFontSize(7)
                        .setBackgroundColor(rowColor)
                        .setBorder(Border.NO_BORDER)
                        .setBorderBottom(new SolidBorder(1)));

                itemsTable.addCell(createCell(String.valueOf("MEDICAL BILLS"), 1, TextAlignment.RIGHT)
                        .setFontSize(7)
                        .setBackgroundColor(rowColor)
                        .setBorder(Border.NO_BORDER)
                        .setBorderBottom(new SolidBorder(1)));

                itemsTable.addCell(createCell(String.valueOf(patientDto.patientGender), 1, TextAlignment.RIGHT)
                        .setFontSize(7)
                        .setBackgroundColor(rowColor)
                        .setBorder(Border.NO_BORDER)
                        .setBorderBottom(new SolidBorder(1)));

                itemsTable.addCell(createCell(String.valueOf(patientDto.patientGender), 1, TextAlignment.RIGHT)
                        .setFontSize(7)
                        .setBackgroundColor(rowColor)
                        .setBorder(Border.NO_BORDER)
                        .setBorderBottom(new SolidBorder(1)));

                itemsTable.addCell(createCell(String.valueOf(patientDto.totalAmountDue), 1, TextAlignment.RIGHT)
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

            BigDecimal totalDebt = result.totalDebt();

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

            for (ProcedureRequested procedureRequested : invoice.visit.getProceduresRequested()) {
                itemsTable.addCell(createCell(procedureRequested.procedureRequestedType, 3, TextAlignment.LEFT));
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











    @Transactional
    public List<PatientDTO> getAllPatientsByGroupId(Long groupId) {
        return patientRepository.listAll(Sort.descending("patientNo"))
                .stream()
                .filter(buyer -> buyer.patientGroup != null &&
                        buyer.patientGroup.id.equals(groupId))
                .map(PatientDTO::new)
                .toList();
    }


    public PatientDTO getPatientById(Long id) {
        return patientRepository.findByIdOptional(id)
                .map(PatientDTO::new)  // Convert Patient entity to PatientDTO
                .orElseThrow(() -> new WebApplicationException("Patient not found", 404));
    }



    @Transactional
    public Response updatePatientById(Long id, PatientUpdateRequest request) {

                    PatientGroup patientGroup = null;

                    // Validate and assign patient group
                    if (request.patientGroupId != null) {
                        patientGroup = patientGroupRepository.findById(request.patientGroupId);
                        if (patientGroup == null) {
                            throw new WebApplicationException(
                                    Response.status(Response.Status.NOT_FOUND)
                                            .entity(new ResponseMessage("Patient group not found for ID: " + request.patientGroupId, null))
                                            .build()
                            );
                        }

                        if ("veneranda medical".equalsIgnoreCase(patientGroup.groupName) &&
                                (request.userRole == null || !request.userRole.equalsIgnoreCase("md"))) {
                            throw new WebApplicationException(
                                    Response.status(Response.Status.BAD_REQUEST)
                                            .entity(new ResponseMessage("You need admin approval to add any patient in Veneranda Medical group", null))
                                            .build()
                            );
                        }
                    }

                    Patient patient = patientRepository.findById(id);
                    // Update patient fields
                    patient.patientFirstName = request.patientFirstName;
                    patient.patientSecondName = request.patientSecondName;
                    patient.patientAddress = request.patientAddress;
                    patient.patientContact = request.patientContact;
                    patient.patientGender = request.patientGender;
                    patient.patientAge = request.patientAge;
                    patient.occupation = request.occupation;

                    patient.patientGroup = patientGroup;
                    patient.nextOfKinName = request.nextOfKinName;
                    patient.nextOfKinContact = request.nextOfKinContact;
                    patient.relationship = request.relationship;
                    patient.nextOfKinAddress = request.nextOfKinAddress;
                    patient.patientDateOfBirth = request.patientDateOfBirth;
                    patient.patientLastUpdatedDate = LocalDate.now();

                    patientRepository.persist(patient);


        return Response.ok(new ResponseMessage("Patient Details Updated successfully", new PatientDTO(patient))).build();


    }


    @Transactional
    public Object findMaxPatientNo() {
        return patientRepository.listAll(Sort.descending("patientNo"))
                .stream()
                .findFirst()
                .orElseThrow(() -> new WebApplicationException(NOT_FOUND,404));
    }

   // public static final String NOT_FOUND = "Not found!";


    @Transactional
    public int findMaxPatientFileNoReturnInt() {
        return patientRepository.listAll(Sort.descending("patientNo"))
                .stream()
                .map(buyer -> buyer.patientNo)
                .findFirst()
                .orElse(0);
    }

    @Transactional
    public Response deletePatientById(Long id) {

        Patient deletedPatient = patientRepository.findById(id);

        if (deletedPatient == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .build();
        }

        deletedPatientNosService.saveDeletedPatientNo(deletedPatient);
        
        patientRepository.delete(deletedPatient);

        return Response.ok(new ResponseMessage(ActionMessages.DELETED.label)).build();
    }





    public List<FullPatientResponse> getPatientsAdvancedFilter(PatientParametersRequest request) {
        StringJoiner whereClause = getStringJoiner(request);

        String sql = """
        SELECT
            id,
            group_id,
            nextOfKinAddress,
            nextOfKinContact,
            nextOfKinName,
            patientAddress,
            patientAge,
            patientContact,
            patientDateOfBirth,
            patientFileNo,
            patientFirstName,
            patientGender,
            occupation,
            patientLastUpdatedDate,
            patientNo,
            patientProfilePic,
            patientSecondName,
            relationship,
            totalAmountDue
        FROM vena.Patient
        %s
        ORDER BY id DESC;
        """.formatted(whereClause);

        return client.query(sql)
                .execute()
                .onItem()
                .transformToMulti(rows -> Multi.createFrom().iterable(rows))
                .onItem()
                .transform(this::from)
                .collect().asList()
                .await()
                .indefinitely();
    }


    private FullPatientResponse from(Row row){

        FullPatientResponse response = new FullPatientResponse();
        response.id = row.getLong("id");
        response.group_id = row.getLong("group_id");
        response.nextOfKinAddress = row.getString("nextOfKinAddress");
        response.nextOfKinContact = row.getString("nextOfKinContact");
        response.nextOfKinName = row.getString("nextOfKinName");
        response.patientAddress = row.getString("patientAddress");
        response.occupation = row.getString("occupation");
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
        AtomicReference<Boolean> hasSearchCriteria = new AtomicReference<>(Boolean.FALSE);

        List<String> conditions = new ArrayList<>();
        if (request.group_id != null) {
            conditions.add("group_id = " + request.group_id);
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

        /*if (request.datefrom != null && request.dateto != null) {
            conditions.add("dateOfPayment BETWEEN '" + request.datefrom + "' AND '" + request.dateto + "'");
            hasSearchCriteria.set(Boolean.TRUE);
        }*/

        StringJoiner whereClause = new StringJoiner(" AND ", "WHERE ", "");

        conditions.forEach(whereClause::add);

        if (Boolean.FALSE.equals(hasSearchCriteria.get())) {
            whereClause.add("1 = 1");
        }

        return whereClause;
    }









}






