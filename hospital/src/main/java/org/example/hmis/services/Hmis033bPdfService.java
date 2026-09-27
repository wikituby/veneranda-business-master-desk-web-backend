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
import java.util.HashMap;
import java.util.Map;
import org.example.hmis.services.payloads.Hmis033bAggregateResponse;

@ApplicationScoped
public class Hmis033bPdfService {

    private static final String TEMPLATE = "/hmis/033b-template.pdf";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final float FONT = 8.5f;

    private static final Map<String, Float> DISEASE_ROW_Y = Map.ofEntries(
            Map.entry("MA.", 524.4f), Map.entry("DY.", 508.8f), Map.entry("SA.", 492.0f),
            Map.entry("AF.", 466.7f), Map.entry("AE.", 449.9f), Map.entry("AB.", 424.6f),
            Map.entry("MG.", 407.9f), Map.entry("CH.", 391.1f), Map.entry("GW.", 374.4f),
            Map.entry("ME.", 357.7f), Map.entry("NT.", 341.0f), Map.entry("PL.", 324.3f),
            Map.entry("TF.", 307.5f), Map.entry("HB.", 290.8f), Map.entry("DR.", 274.1f),
            Map.entry("YF.", 257.4f), Map.entry("VF.", 240.6f), Map.entry("LP.", 213.4f),
            Map.entry("AX.", 191.2f), Map.entry("CV.", 84.3f));

    @Inject Hmis033bAggregationService aggregationService;

    public byte[] generate(LocalDate from, LocalDate to) {
        Hmis033bAggregateResponse data = aggregationService.aggregate(from, to);
        try (InputStream template = openTemplate();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfReader reader = new PdfReader(template);
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(reader, writer);
            PdfFont font = HmisPdfOverlay.helvetica(FONT);

            overlayHeader(pdf, font, data);
            overlayDiseases(pdf, font, data);
            overlayOpd(pdf, font, data);
            overlayMalaria(pdf, font, data);
            overlayTracerStock(pdf, font, data);

            pdf.close();
            return baos.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to generate HMIS 033b PDF", ex);
        }
    }

    private InputStream openTemplate() {
        InputStream stream = Hmis033bPdfService.class.getResourceAsStream(TEMPLATE);
        if (stream == null) {
            throw new IllegalStateException("Missing HMIS template: " + TEMPLATE);
        }
        return stream;
    }

    private void overlayHeader(PdfDocument pdf, PdfFont font, Hmis033bAggregateResponse data) {
        PdfCanvas canvas = HmisPdfOverlay.canvasOnTop(pdf, 3);
        Hmis033bAggregateResponse.Period period = data.period;
        Hmis033bAggregateResponse.FacilityHeader facility = data.facility;

        if (period.from != null) {
            HmisPdfOverlay.drawText(canvas, font, FONT, 118f, 659.4f, period.from.format(DATE_FMT));
        }
        if (period.to != null) {
            HmisPdfOverlay.drawText(canvas, font, FONT, 228f, 659.4f, period.to.format(DATE_FMT));
        }
        if (period.reportDate != null) {
            HmisPdfOverlay.drawText(canvas, font, FONT, 418f, 659.4f, period.reportDate.format(DATE_FMT));
        }
        if (period.weekNumber != null) {
            HmisPdfOverlay.drawText(canvas, font, FONT, 92f, 641.4f, String.valueOf(period.weekNumber));
        }
        HmisPdfOverlay.drawText(canvas, font, FONT, 168f, 641.4f, safe(facility.name));
        HmisPdfOverlay.drawText(canvas, font, FONT, 398f, 641.4f, safe(facility.code));
        HmisPdfOverlay.drawText(canvas, font, FONT, 92f, 615.4f, safe(facility.parish));
        HmisPdfOverlay.drawText(canvas, font, FONT, 168f, 615.4f, safe(facility.subCounty));
        HmisPdfOverlay.drawText(canvas, font, FONT, 318f, 615.4f, safe(facility.hsd));
        HmisPdfOverlay.drawText(canvas, font, FONT, 458f, 615.4f, safe(facility.district));
        if (facility.level != null && !facility.level.isBlank()) {
            HmisPdfOverlay.drawText(canvas, font, FONT, 398f, 628f, safe(facility.level));
        }
    }

    private void overlayDiseases(PdfDocument pdf, PdfFont font, Hmis033bAggregateResponse data) {
        PdfCanvas canvas = HmisPdfOverlay.canvasOnTop(pdf, 3);
        Map<String, Hmis033bAggregateResponse.DiseaseLine> byCode = new HashMap<>();
        if (data.diseases != null) {
            for (Hmis033bAggregateResponse.DiseaseLine line : data.diseases) {
                if (line != null && line.code != null) {
                    byCode.put(line.code, line);
                }
            }
        }
        for (Map.Entry<String, Float> entry : DISEASE_ROW_Y.entrySet()) {
            Hmis033bAggregateResponse.DiseaseLine line = byCode.get(entry.getKey());
            if (line == null) {
                continue;
            }
            float y = entry.getValue();
            HmisPdfOverlay.drawInt(canvas, font, FONT, 282f, y, line.casesThisWeek);
            HmisPdfOverlay.drawInt(canvas, font, FONT, 358f, y, line.deathsThisWeek);
            HmisPdfOverlay.drawInt(canvas, font, FONT, 422f, y, line.testedCases);
            HmisPdfOverlay.drawInt(canvas, font, FONT, 498f, y, line.positiveCases);
        }
    }

    private void overlayOpd(PdfDocument pdf, PdfFont font, Hmis033bAggregateResponse data) {
        PdfCanvas canvas = HmisPdfOverlay.canvasOnTop(pdf, 4);
        HmisPdfOverlay.drawInt(canvas, font, FONT, 200f, 162f, data.opd.newAttendance);
        HmisPdfOverlay.drawInt(canvas, font, FONT, 290f, 162f, data.opd.totalAttendance);
        HmisPdfOverlay.drawInt(canvas, font, FONT, 200f, 145f, data.referrals.referralsOut);
        HmisPdfOverlay.drawInt(canvas, font, FONT, 290f, 145f, data.referrals.referralsIn);
    }

    private void overlayMalaria(PdfDocument pdf, PdfFont font, Hmis033bAggregateResponse data) {
        PdfCanvas canvas = HmisPdfOverlay.canvasOnTop(pdf, 5);
        Hmis033bAggregateResponse.MalariaSummary m = data.malaria;
        float y = 488f;
        HmisPdfOverlay.drawInt(canvas, font, FONT, 108f, y, m.suspectedFever);
        HmisPdfOverlay.drawInt(canvas, font, FONT, 168f, y, m.testedRdt);
        HmisPdfOverlay.drawInt(canvas, font, FONT, 198f, y, m.rdtPositive);
        HmisPdfOverlay.drawInt(canvas, font, FONT, 258f, y, m.testedMicroscopy);
        HmisPdfOverlay.drawInt(canvas, font, FONT, 308f, y, m.microscopyPositive);
        HmisPdfOverlay.drawInt(canvas, font, FONT, 368f, y, m.notTestedTreated);
        HmisPdfOverlay.drawInt(canvas, font, FONT, 428f, y, m.rdtPositiveTreated);
        HmisPdfOverlay.drawInt(canvas, font, FONT, 488f, y, m.microscopyPositiveTreated);
    }

    private void overlayTracerStock(PdfDocument pdf, PdfFont font, Hmis033bAggregateResponse data) {
        PdfCanvas canvas = HmisPdfOverlay.canvasOnTop(pdf, 6);
        if (data.tracerStock == null) {
            return;
        }
        float[] xs = {118f, 198f, 278f, 358f, 438f, 518f};
        int index = 0;
        for (Hmis033bAggregateResponse.TracerStockLine line : data.tracerStock) {
            if (line == null || index >= xs.length) {
                continue;
            }
            HmisPdfOverlay.drawText(canvas, font, FONT, xs[index], 468f, formatBalance(line.balance));
            index++;
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String formatBalance(double value) {
        if (Math.rint(value) == value) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }
}