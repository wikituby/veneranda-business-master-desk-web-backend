package org.example.hmis.services;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.io.font.constants.StandardFonts;
import java.io.IOException;

/** Draws text on top of an existing PDF page (template overlay). */
public final class HmisPdfOverlay {

    private HmisPdfOverlay() {
    }

    public static PdfFont helvetica(float size) throws IOException {
        return PdfFontFactory.createFont(StandardFonts.HELVETICA);
    }

    public static PdfCanvas canvasOnTop(PdfDocument pdf, int pageNumber) {
        PdfPage page = pdf.getPage(pageNumber);
        return new PdfCanvas(page.newContentStreamAfter(), page.getResources(), pdf);
    }

    public static void drawText(PdfCanvas canvas, PdfFont font, float fontSize, float x, float y, String text) {
        if (text == null || text.isBlank()) {
            return;
        }
        String value = sanitize(text);
        if (value.isBlank()) {
            return;
        }
        canvas.saveState();
        canvas.setFillColor(ColorConstants.BLACK);
        canvas.beginText()
                .setFontAndSize(font, fontSize)
                .moveText(x, y)
                .showText(value)
                .endText();
        canvas.restoreState();
    }

    public static void drawInt(PdfCanvas canvas, PdfFont font, float fontSize, float x, float y, int value) {
        drawText(canvas, font, fontSize, x, y, String.valueOf(value));
    }

    public static void drawInBox(
            PdfCanvas canvas, PdfFont font, float fontSize, Rectangle box, String text, boolean rightAlign) {
        if (text == null || text.isBlank()) {
            return;
        }
        String value = sanitize(text);
        if (value.isBlank()) {
            return;
        }
        float textWidth = font.getWidth(value, fontSize);
        float x = box.getX() + 2f;
        if (rightAlign && textWidth < box.getWidth()) {
            x = box.getX() + box.getWidth() - textWidth - 2f;
        }
        float y = box.getY() + (box.getHeight() / 2f) - (fontSize / 3f);
        drawText(canvas, font, fontSize, x, y, value);
    }

    private static String sanitize(String text) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c >= 32 && c <= 126) {
                out.append(c);
            } else if (Character.isWhitespace(c)) {
                out.append(' ');
            }
        }
        return out.toString().trim();
    }
}