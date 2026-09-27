package org.example.diagnostics.ultrasoundScan.generalUs.services;

import java.io.IOException;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;

import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;

import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.element.IBlockElement;
import com.itextpdf.layout.element.IElement;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import com.itextpdf.layout.property.VerticalAlignment;

import com.itextpdf.html2pdf.HtmlConverter;

import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;



import io.quarkus.panache.common.Sort;
import io.vertx.mutiny.sqlclient.Pool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.client.domains.Patient;



import org.example.client.domains.PatientGroup;
import org.example.configuration.handler.ResponseMessage;
import org.example.consultations.services.payloads.responses.ConsultationDTO;
import org.example.diagnostics.ultrasoundScan.generalUs.domains.repositories.GeneralUsRepository;
import org.example.subscription.services.FacilityBranding;
import org.example.subscription.services.FacilityBrandingService;
import org.example.subscription.services.FacilityPdfLogoService;
import org.example.diagnostics.ultrasoundScan.generalUs.services.Payloads.requests.GeneralUsUpdateRequest;
import org.example.diagnostics.ultrasoundScan.generalUs.services.Payloads.responses.GeneralUsDTO;
import org.example.diagnostics.ultrasoundScan.generalUs.domains.GeneralUs;
import org.example.diagnostics.ultrasoundScan.generalUs.services.Payloads.requests.GeneralUsRequest;
import org.example.finance.invoice.domains.Invoice;
import org.example.finance.invoice.services.payloads.responses.InvoiceDTO;
import org.example.procedure.procedureRequested.domains.ProcedureRequested;
import org.example.procedure.procedureRequested.domains.repositories.ProcedureRequestedRepository;
import org.example.visit.domains.PatientVisit;
import org.example.user.domains.User;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


@ApplicationScoped
public class GeneralUsService {

    @Inject
    GeneralUsRepository generalUsRepository;

    @Inject
    ProcedureRequestedRepository procedureRequestedRepository;

    @Inject
    Pool client;

    @Inject
    FacilityBrandingService facilityBrandingService;

    @Inject
    FacilityPdfLogoService facilityPdfLogoService;

    private FacilityBranding facilityBranding() {
        return facilityBrandingService.resolveDefaultBranding();
    }

    public static final String NOT_FOUND = "Not found!";

        @Transactional
        public void createGeneralUsReport(ProcedureRequested procedureRequested){

            PatientVisit patientVisit = procedureRequested.visit; // ✅ correct

            Patient patient = patientVisit.patient;

            GeneralUs generalUs = new GeneralUs();
            generalUs.patientName = patient.patientFirstName +" "+ patient.patientSecondName;
            generalUs.gender = patient.patientGender;
            generalUs.patientAge = patient.patientAge;
            generalUs.visit = procedureRequested.visit;
            generalUs.procedureRequested = procedureRequested;
            generalUs.indication = "";
            generalUs.doneBy = "";
            generalUs.recommendation = "";
            generalUs.scanReportTitle = "";

            generalUs.exam = procedureRequested.procedureRequestedName;
            generalUs.findings = "";
            generalUs.impression = "";

            generalUs.scanPerformingDate = LocalDate.now();
            generalUs.upDatedDate = LocalDate.now();
            generalUs.timeOfProcedure = LocalTime.now();
            generalUs.scanRequestDate = procedureRequested.dateOfProcedure;


            generalUsRepository.persist(generalUs);


            procedureRequestedRepository.persist(procedureRequested);

        }

    public List<GeneralUsDTO> getAllGeneralUs() {
        return generalUsRepository.listAll(Sort.descending("id"))
                .stream()
                .map(GeneralUsDTO::new)
                .toList();
    }

    @Transactional
    public Response updateScanReportById(Long id, GeneralUsUpdateRequest request) {
        GeneralUs generalUs = GeneralUs.findById(id); // ✅ correct
        if (generalUs == null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ResponseMessage("Scan Report not found for ID: " + id))
                    .build();
        }

        ProcedureRequested procedureRequested = ProcedureRequested.findById(generalUs.procedureRequested.id);

        generalUs.indication = request.indication;
        generalUs.doneBy = request.doneBy;
        generalUs.recommendation = request.recommendation;
        generalUs.scanReportTitle = request.scanReportTitle;
        generalUs.exam = request.exam;
        generalUs.findings = request.findings;
        generalUs.impression = request.impression;
        generalUs.scanPerformingDate = LocalDate.now();
        generalUs.timeOfProcedure = LocalTime.now();

        generalUs.upDatedDate = LocalDate.now();

        generalUsRepository.persist(generalUs);

        procedureRequested.report = generalUs.scanReportTitle;

        if (generalUs.findings == null || generalUs.findings.isEmpty() ||
            generalUs.impression == null || generalUs.impression.isEmpty() ||
            generalUs.indication == null || generalUs.indication.isEmpty() ||
            generalUs.scanReportTitle == null || generalUs.scanReportTitle.isEmpty())
                 {
                     procedureRequested.status = "Pending";
        }else{
            procedureRequested.procedureRequestedType = generalUs.exam;
            procedureRequested.status = "Done";
            procedureRequested.doneBy = request.doneBy;
            procedureRequested.bgColor = "rgb(11, 155, 112)";
        }

        procedureRequestedRepository.persist(procedureRequested);

        return Response.status(Response.Status.CREATED)
                .entity(new ResponseMessage("Scan report updated successfully", new GeneralUsDTO(generalUs)))
                .build();


    }



    @Transactional
    public Response getScanReportByRequestId(Long procedureRequestedId){
        GeneralUs generalUs = GeneralUs.find("procedureRequested.id", procedureRequestedId).firstResult();
        if (generalUs == null) {
            ProcedureRequested procedureRequested = ProcedureRequested.findById(procedureRequestedId);
            if (procedureRequested == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ResponseMessage("Procedure request not found for ID: " + procedureRequestedId))
                        .build();
            }
            createGeneralUsReport(procedureRequested);
            generalUs = GeneralUs.find("procedureRequested.id", procedureRequestedId).firstResult();
        }
        /*return Response.status(Response.Status.FOUND)
                .entity(new ResponseMessage("scan report fetched successfully: ", new GeneralUsDTO(generalUs)))
                .build();*/
        return Response.ok(new ResponseMessage("scan report fetched successfully", new GeneralUsDTO(generalUs))).build();
    }

    @Transactional
    public Response generateAndReturnScanReportPdf(Long procedureRequestedId) {
    
        try {
            // Find the patient visit
            ProcedureRequested procedureRequestedScan = ProcedureRequested.findById(procedureRequestedId);
            if (procedureRequestedScan == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ResponseMessage("procedure requested not found for ID: " + procedureRequestedId))
                        .build();
            }
            GeneralUs generalUs = GeneralUs.find("procedureRequested.id", procedureRequestedId).firstResult();
            if (generalUs == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ResponseMessage("Scan Report not found for procedure ID: " + procedureRequestedId))
                        .build();
            }
    
            // Find the patient
            Patient patient = Patient.findById(generalUs.visit.patient.id);
            if (patient == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ResponseMessage("patient not found for ID: " + generalUs.visit.patient.id))
                        .build();
            }

            User user = User.find("role = ?1 and status = ?2", "sono", "active").firstResult();
            
            String performedBy = "N/A";
            String qualification = "";
            
            if (user != null) {
                performedBy = user.username != null ? user.username : user.email;
                qualification = user.qualification != null ? user.qualification : "";
                generalUs.doneBy = performedBy;
            }

            // Find the visit
            PatientVisit visit = PatientVisit.findById(generalUs.visit.id);
            if (visit == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ResponseMessage("visit not found for ID: " + generalUs.visit.id))
                        .build();
            }
    
            // Check invoice exists before accessing
            if (visit.invoice == null || visit.invoice.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ResponseMessage("No invoice found for this visit"))
                        .build();
            }
            
            Invoice invoice = visit.invoice.get(0);
    
            // Ensure all HTML fields are not null before PDF creation
            generalUs.findings = Objects.requireNonNullElse(generalUs.findings, "");
            generalUs.impression = Objects.requireNonNullElse(generalUs.impression, "");
            generalUs.indication = Objects.requireNonNullElse(generalUs.indication, "");
            generalUs.recommendation = Objects.requireNonNullElse(generalUs.recommendation, "");
    
            // Create PDF
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter pdfWriter = new PdfWriter(baos);
            PdfDocument pdfDocument = new PdfDocument(pdfWriter);
    
            Document document = new Document(pdfDocument);
            document.setMargins(10, 10, 10, 10);
    
            // Title
            Table invoiceTitle = new Table(new float[]{1});
            invoiceTitle.setWidth(UnitValue.createPercentValue(100));
            invoiceTitle.addCell(new Cell()
                    .add(new Div()
                            .setBorderBottom(new SolidBorder(1))
                            .setPaddingBottom(2)
                            .add(new Paragraph("GENERAL ULTRASOUND SCAN REPORT")
                                    .setBold()
                                    .setFontSize(11)
                                    .setTextAlignment(TextAlignment.CENTER)
                            )
                    )
                    .setBorder(Border.NO_BORDER)
                    .setPaddingBottom(15)
                    .add(new Paragraph(facilityBranding().diagnosticsHeaderLine())
                            .setFontSize(9)
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
                    .add(getLogo().setWidth(79).setHeight(68))
                    .setBorder(Border.NO_BORDER)
                    .setHorizontalAlignment(HorizontalAlignment.LEFT)
                    .setVerticalAlignment(VerticalAlignment.TOP)
                    .setPaddingTop(-7)
                    .setPaddingLeft(-27)
            );
            headerTable.addCell(new Cell()
                    .add(new Paragraph("CLIENT NAME: ").setFontSize(8).setTextAlignment(TextAlignment.LEFT))
                    .add(new Paragraph("NEXT OF KEEN: ").setFontSize(8).setTextAlignment(TextAlignment.LEFT))
                    .add(new Paragraph("EXAM: ").setFontSize(8).setTextAlignment(TextAlignment.LEFT))
                    .add(new Paragraph("ADDRESS: ").setFontSize(8).setTextAlignment(TextAlignment.LEFT))
                    .setBorder(Border.NO_BORDER)
            );
            String patientName = generalUs.patientName != null ? generalUs.patientName.toUpperCase() : "N/A";
            String nextOfKeen = patient.nextOfKinName != null ? patient.nextOfKinName.toUpperCase() : "N/A";

            String exam = procedureRequestedScan.procedureRequestedName != null ? procedureRequestedScan.procedureRequestedName.toUpperCase() : "N/A";
            String gender = patient.patientGender != null ? patient.patientGender.toUpperCase() : "N/A";
            
            headerTable.addCell(new Cell()
                    .add(new Paragraph(patientName).setFontSize(8).setTextAlignment(TextAlignment.LEFT))
                    .add(new Paragraph(nextOfKeen).setFontSize(8).setTextAlignment(TextAlignment.LEFT))
                    .add(new Paragraph(exam).setFontSize(8))
                    .add(new Paragraph(Optional.ofNullable(invoice.visit.patient)
                            .map(p -> p.patientAddress +
                                    (p.getPatientGroup() != null
                                            ? " (" + p.patientGroup.groupNameShortForm + ")"
                                            : ""))
                            .map(String::toUpperCase)
                            .orElse("")).setFontSize(8))
                    .setBorder(Border.NO_BORDER)
            );
            headerTable.addCell(new Cell()
                    .add(new Paragraph("GENDER: " + gender).setFontSize(8))
                    .add(new Paragraph("CONTACT: ").setFontSize(8))
                    .add(new Paragraph("REQUEST DATE: ").setFontSize(8))
                    .add(new Paragraph("PROCEDURE DATE: ").setFontSize(8))
                    .setBorder(Border.NO_BORDER)
            );
            String ageText = patient.patientAge != null 
                    ? patient.patientAge.toPlainString() + " YRS"
                    : "N/A";
            
            headerTable.addCell(new Cell()
                    .add(new Paragraph("AGE: " + ageText).setFontSize(8).setTextAlignment(TextAlignment.RIGHT))
                    .add(new Paragraph(String.valueOf(patient.patientContact)).setFontSize(8).setTextAlignment(TextAlignment.RIGHT))
                    .add(new Paragraph(String.valueOf(generalUs.scanRequestDate)).setFontSize(8).setTextAlignment(TextAlignment.RIGHT))
                    .add(new Paragraph(String.valueOf(generalUs.scanPerformingDate)).setFontSize(8).setTextAlignment(TextAlignment.RIGHT))
                    .setBorder(Border.NO_BORDER)
            );
    
            headerTable.setBorderBottom(new SolidBorder(1));
            document.add(headerTable);
    
            // Totals table
            Table totalsTable = new Table(new float[]{4, 2, 2});
            totalsTable.setWidth(UnitValue.createPercentValue(100));
    
            Cell notesCell1 = new Cell(6, 1)
                    .add(new Div()
                            .setBorderBottom(new SolidBorder(1))
                            .setPaddingBottom(2)
                    .add(new Paragraph()
                            .add(new Text("INDICATION: ").setBold())
                            .add(generalUs.indication != null ? generalUs.indication.toUpperCase() : "")
                            .setFontSize(9)
                            .setTextAlignment(TextAlignment.CENTER)
                    )
                    )
    
                    // FINDINGS
                    .add(new Paragraph()
                            .setMarginTop(5)
                            .add(new Text("FINDINGS:")
                                    .setUnderline()
                                    .setBold())
                            .setFontSize(10)
                            .setTextAlignment(TextAlignment.LEFT)
                    )
    
                    // HTML → PDF conversion of findings
                    .add(convertHtmlFindings(generalUs.findings))
    
                    .setTextAlignment(TextAlignment.LEFT)
                    .setFontSize(20)
                    .setBorder(Border.NO_BORDER)
                    .setVerticalAlignment(VerticalAlignment.TOP);
    
            totalsTable.addCell(notesCell1);
            document.add(totalsTable);
    
            // Impression table (unchanged)
            Table impressionTable = new Table(new float[]{1});
            impressionTable.setWidth(UnitValue.createPercentValue(100));
            impressionTable.setMarginBottom(1);
            impressionTable.addCell(new Cell()
                    .setBorder(Border.NO_BORDER)
                    .setPadding(5)
                    .add(new Paragraph("IMPRESSION/CONCLUSION:")
                            .setUnderline()
                            .setBold()
                            .setFontSize(10))
                    .add(convertHtmlImpression(generalUs.impression))
            );
    
            if (generalUs.recommendation != null && !generalUs.recommendation.trim().isEmpty()) {
                impressionTable.addCell(new Cell()
                        .setBorder(Border.NO_BORDER)
                        .setPadding(5)
                        .add(new Paragraph()
                                .add(new Text("RECOMMENDATIONS: ")
                                        .setUnderline()
                                        .setBold())
                                .setFontSize(10))
                        .add(convertHtmlImpression(generalUs.recommendation))
                );
            }
    
            document.add(impressionTable);
    
            // Signature section (unchanged)
            Table initialsTable = new Table(new float[]{3, 2});
            initialsTable.setWidth(UnitValue.createPercentValue(100));
            initialsTable.setMarginBottom(1);
    
            initialsTable.addCell(new Cell()
                    .setBorder(Border.NO_BORDER)
                    .setPadding(5)
                    .add(new Paragraph("Imaging Technologist/Sonographer / Radiographer:")
                            .setUnderline()
                            .setBold()
                            .setFontSize(10))
                    .add(new Paragraph(performedBy.toUpperCase() + " [" + qualification.toUpperCase() + "]")
                            .setFontSize(8)
                            .setMarginTop(1))
                    
            );
    
            initialsTable.addCell(new Cell()
                    .setBorder(Border.NO_BORDER)
                    .setPadding(5)
                    .add(new Paragraph("Signature / Stamp")
                            .setUnderline()
                            .setBold()
                            .setFontSize(10))
            );
    
            document.add(initialsTable);
    
            // Close
            document.close();
    
            byte[] pdfBytes = baos.toByteArray();
            return Response.ok(new ByteArrayInputStream(pdfBytes))
                    .header("Content-Disposition", "attachment; filename=invoice.pdf")
                    .type("application/pdf")
                    .build();
    
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ResponseMessage("PDF generation failed", e.getMessage()))
                    .build();
        }
    }
    






    private Div convertHtmlFindings(String html) {
        Div container = new Div();
    
        String wrappedHtml = """
            <html>
            <head>
            <style>
                body, p, div, span {
                    line-height: 1.3 !important;
                    margin: 0 !important;
                    font-size: 17px;
                    padding: 0 !important;
                }
            </style>
            </head>
            <body>
                <div>
            """ 
            + html +
            """
                </div>
            </body>
            </html>
            """;
    
        for (IElement element : HtmlConverter.convertToElements(wrappedHtml)) {
            if (element instanceof IBlockElement) {
                container.add((IBlockElement) element);
            }
        }
    
        return container;
    }

    private Div convertHtmlImpression(String html) {
        Div container = new Div();
    
        String wrappedHtml = """
            <html>
            <head>
            <style>
                body, p, div, span {
                    line-height: 1.3 !important;
                    margin: 0 !important;
                    font-size: 17px;
                    padding: 0 !important;
                }
            </style>
            </head>
            <body>
                <div>
            """ 
            + html +
            """
                </div>
            </body>
            </html>
            """;
    
        for (IElement element : HtmlConverter.convertToElements(wrappedHtml)) {
            if (element instanceof IBlockElement) {
                container.add((IBlockElement) element);
            }
        }
    
        return container;
    }
    
    





    // Utility method to create cells with alignment
    private Cell createCell(String content, int i, TextAlignment alignment) {
        Cell cell = new Cell().add(new Paragraph(content));
        cell.setTextAlignment(alignment);
        return cell;
    }

    // Helper methods (unchanged)
    private Paragraph createSectionHeader(String text) {
        return new Paragraph()
                .add(new Text(text)
                        .setUnderline()
                        .setBold()
                        .setFontSize(12)
                )
                .setTextAlignment(TextAlignment.LEFT);
    }

    private Paragraph createSectionContent(String content) {
        return new Paragraph(content)
                .setFontSize(12)
                .setTextAlignment(TextAlignment.LEFT)
                .setMarginBottom(5);
    }




    private com.itextpdf.layout.element.Image getLogo() {
        return facilityPdfLogoService.createLogoImage();
    }























}






