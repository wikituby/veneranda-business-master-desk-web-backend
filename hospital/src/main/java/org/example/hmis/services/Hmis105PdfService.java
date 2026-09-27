package org.example.hmis.services;

import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.example.hmis.services.payloads.Hmis033bAggregateResponse;

/** HMIS Form 105 monthly outpatient report — preserves full 25-page official template. */
@ApplicationScoped
public class Hmis105PdfService {

    private static final String TEMPLATE = "/hmis/105-template.pdf";
    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("MMMM yyyy");
    private static final float FONT = 8.5f;

    @Inject Hmis033bAggregationService aggregationService;

    public byte[] generate(LocalDate from, LocalDate to) {
        LocalDate monthStart = from.withDayOfMonth(1);
        LocalDate monthEnd = from.withDayOfMonth(from.lengthOfMonth());
        if (to != null && to.isAfter(monthEnd)) {
            monthEnd = to;
        }
        Hmis033bAggregateResponse data = aggregationService.aggregate(monthStart, monthEnd);
        try (InputStream template = openTemplate();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfReader reader = new PdfReader(template);
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(reader, writer);
            PdfFont font = HmisPdfOverlay.helvetica(FONT);

            overlayCoverHeader(pdf, font, data, monthStart);
            overlayOpdTotals(pdf, font, data);
            overlayDiagnosesSummary(pdf, font, data);

            pdf.close();
            return baos.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to generate HMIS 105 PDF", ex);
        }
    }

    private InputStream openTemplate() {
        InputStream stream = Hmis105PdfService.class.getResourceAsStream(TEMPLATE);
        if (stream == null) {
            throw new IllegalStateException("Missing HMIS template: " + TEMPLATE);
        }
        return stream;
    }

    private void overlayCoverHeader(PdfDocument pdf, PdfFont font, Hmis033bAggregateResponse data, LocalDate month) {
        PdfCanvas canvas = HmisPdfOverlay.canvasOnTop(pdf, 1);
        Hmis033bAggregateResponse.FacilityHeader facility = data.facility;
        HmisPdfOverlay.drawText(canvas, font, FONT, 145f, 266.7f, safe(facility.name));
        HmisPdfOverlay.drawText(canvas, font, FONT, 430f, 266.7f, safe(facility.level));
        HmisPdfOverlay.drawText(canvas, font, FONT, 500f, 266.7f, safe(facility.code));
        HmisPdfOverlay.drawText(canvas, font, FONT, 80f, 229f, safe(facility.hsd));
        HmisPdfOverlay.drawText(canvas, font, FONT, 80f, 191.3f, safe(facility.district));
        HmisPdfOverlay.drawText(canvas, font, FONT, 145f, 144.9f, month.format(MONTH_FMT));
        if (facility.subCounty != null && !facility.subCounty.isBlank()) {
            HmisPdfOverlay.drawText(canvas, font, FONT, 80f, 210f, safe(facility.subCounty));
        }
        if (facility.parish != null && !facility.parish.isBlank()) {
            HmisPdfOverlay.drawText(canvas, font, FONT, 80f, 248f, safe(facility.parish));
        }
    }

    /** Page 4 of template: OPD attendance grid — v1 fills month totals in the 20+ Male/Female columns. */
    private void overlayOpdTotals(PdfDocument pdf, PdfFont font, Hmis033bAggregateResponse data) {
        PdfCanvas canvas = HmisPdfOverlay.canvasOnTop(pdf, 4);
        Hmis033bAggregateResponse.FacilityHeader facility = data.facility;
        HmisPdfOverlay.drawText(canvas, font, FONT, 130f, 703.5f, safe(facility.name));
        HmisPdfOverlay.drawText(canvas, font, FONT, 130f, 683f, safe(facility.district));
        HmisPdfOverlay.drawText(canvas, font, FONT, 130f, 662.4f, safe(facility.subCounty));

        int newAtt = data.opd.newAttendance;
        int reatt = data.opd.reattendance;
        HmisPdfOverlay.drawInt(canvas, font, FONT, 285f, 553f, newAtt);
        HmisPdfOverlay.drawInt(canvas, font, FONT, 305f, 553f, 0);
        HmisPdfOverlay.drawInt(canvas, font, FONT, 285f, 529f, reatt);
        HmisPdfOverlay.drawInt(canvas, font, FONT, 305f, 529f, 0);
    }

    /** Top epidemic diagnoses on the monthly diagnoses page (MA, DY, SA). */
    private void overlayDiagnosesSummary(PdfDocument pdf, PdfFont font, Hmis033bAggregateResponse data) {
        if (data.diseases == null) {
            return;
        }
        PdfCanvas canvas = HmisPdfOverlay.canvasOnTop(pdf, 4);
        float y = 400f;
        float step = 14f;
        int row = 0;
        for (Hmis033bAggregateResponse.DiseaseLine line : data.diseases) {
            if (line == null || line.casesThisWeek <= 0) {
                continue;
            }
            if (!"MA.".equals(line.code) && !"DY.".equals(line.code) && !"SA.".equals(line.code)) {
                continue;
            }
            HmisPdfOverlay.drawText(canvas, font, FONT, 28f, y - (row * step), line.code);
            HmisPdfOverlay.drawInt(canvas, font, FONT, 285f, y - (row * step), line.casesThisWeek);
            row++;
            if (row >= 3) {
                break;
            }
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}