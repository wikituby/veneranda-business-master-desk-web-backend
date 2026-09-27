package org.example.finance.invoice.services;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.property.TextAlignment;
import org.example.subscription.services.FacilityBranding;

public class FooterHelperInvoice implements IEventHandler {

    private final FacilityBranding branding;

    public FooterHelperInvoice(FacilityBranding branding) {
        this.branding = branding != null ? branding : emptyBranding();
    }

    private static FacilityBranding emptyBranding() {
        return new FacilityBranding(
                "Health Facility", null, "", "", "", "", "",
                null, null, null,
                "", "", "",
                "", ""
        );
    }

    @Override
    public void handleEvent(Event event) {
        PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
        PdfDocument pdfDoc = docEvent.getDocument();
        PdfPage page = docEvent.getPage();
        Rectangle pageSize = page.getPageSize();

        PdfCanvas pdfCanvas = new PdfCanvas(page.newContentStreamAfter(), page.getResources(), pdfDoc);

        try (Canvas canvas = new Canvas(pdfCanvas, pdfDoc, pageSize)) {
            Div footerContainer = new Div()
                    .setWidth(pageSize.getWidth() - 72)
                    .setFixedPosition(
                            pageSize.getLeft() + 36,
                            54,
                            pageSize.getWidth() - 72
                    );

            Paragraph mainFooter = new Paragraph()
                    .add(branding.invoiceFooterMainLine())
                    .setFontSize(8)
                    .setMarginBottom(3)
                    .setTextAlignment(TextAlignment.CENTER);

            String contact = branding.invoiceFooterContactResolved();
            Paragraph contactInfo = null;
            if (contact != null && !contact.isBlank()) {
                contactInfo = new Paragraph()
                        .add(contact)
                        .setFontSize(7)
                        .setMarginBottom(4)
                        .setTextAlignment(TextAlignment.CENTER);
            }

            Paragraph thankYou = new Paragraph()
                    .add(branding.thankYouLine())
                    .setFontSize(6)
                    .setItalic()
                    .setTextAlignment(TextAlignment.CENTER);

            footerContainer.add(mainFooter);
            if (contactInfo != null) {
                footerContainer.add(contactInfo);
            }
            footerContainer.add(thankYou);

            canvas.add(footerContainer);

            pdfCanvas.setStrokeColor(ColorConstants.BLACK)
                    .setLineWidth(0.5f)
                    .moveTo(pageSize.getLeft() + 36, 72)
                    .lineTo(pageSize.getRight() - 36, 72)
                    .stroke();
        }
        pdfCanvas.release();
    }
}
