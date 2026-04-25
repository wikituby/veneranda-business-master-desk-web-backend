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

public class FooterHelperInvoice implements IEventHandler {

    @Override
    public void handleEvent(Event event) {
        PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
        PdfDocument pdfDoc = docEvent.getDocument();
        PdfPage page = docEvent.getPage();
        Rectangle pageSize = page.getPageSize();

        // Create canvas for footer (using newContentStreamAfter() to ensure it's on top)
        PdfCanvas pdfCanvas = new PdfCanvas(page.newContentStreamAfter(), page.getResources(), pdfDoc);

        try (Canvas canvas = new Canvas(pdfCanvas, pdfDoc, pageSize)) {
            // Footer container with background (optional)
            Div footerContainer = new Div()
                    .setWidth(pageSize.getWidth() - 72) // Account for margins
                    .setFixedPosition(
                            pageSize.getLeft() + 36,  // Left margin
                            54,                       // Increased bottom margin (54 points = 0.75 inch)
                            pageSize.getWidth() - 72   // Width accounting for margins
                    );

            // Main footer text
            Paragraph mainFooter = new Paragraph()
                    .add("- Department of Finance - Veneranda Medical (A subsidiary of Veneranda Hospital) - System Generated -\n EQUITY BANK UG LTD: [ UGX ACC. NO: 1044203473402] [USD ACC.NO: 1044203473403]")
                    .setFontSize(8)
                    .setMarginBottom(3)
                    .setTextAlignment(TextAlignment.CENTER);

            // Contact information
            Paragraph contactInfo = new Paragraph()
                    .add("Bugogo Village, Kyegegwa | Tel: +256 784 411 848 | Email: venerandahospital@gmail.com")
                    .setFontSize(7)
                    .setMarginBottom(4)
                    .setTextAlignment(TextAlignment.CENTER);

            // Thank you message
            Paragraph thankYou = new Paragraph()
                    .add("Thank you for choosing Veneranda Hospital!")
                    .setFontSize(6)
                    .setItalic()
                    .setTextAlignment(TextAlignment.CENTER);

            // Add all elements to footer container
            footerContainer.add(mainFooter);
            footerContainer.add(contactInfo);
            footerContainer.add(thankYou);

            // Add footer container to canvas
            canvas.add(footerContainer);

            // Optional: Add a separator line above footer (position adjusted higher)
            pdfCanvas.setStrokeColor(ColorConstants.BLACK)
                    .setLineWidth(0.5f)
                    .moveTo(pageSize.getLeft() + 36, 72)  // 72 points from bottom (1 inch)
                    .lineTo(pageSize.getRight() - 36, 72)  // 72 points from bottom (1 inch)
                    .stroke();
        }
        pdfCanvas.release();
    }
}





