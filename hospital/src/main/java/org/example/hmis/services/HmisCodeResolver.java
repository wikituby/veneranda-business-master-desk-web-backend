package org.example.hmis.services;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.example.consultations.domains.Diagnosis;
import org.example.consultations.domains.DiagnosisType;

@ApplicationScoped
public class HmisCodeResolver {

    private static final Map<String, String> BUILTIN_KEYWORDS = Map.ofEntries(
            Map.entry("malaria", "MA."),
            Map.entry("typhoid", "TF."),
            Map.entry("dysentery", "DY."),
            Map.entry("cholera", "CH."),
            Map.entry("measles", "ME."),
            Map.entry("meningitis", "MG."),
            Map.entry("hepatitis", "HB."),
            Map.entry("covid", "CV."),
            Map.entry("sari", "SA."),
            Map.entry("pneumonia", "SA."),
            Map.entry("rabies", "AB."),
            Map.entry("animal bite", "AB."),
            Map.entry("tuberculosis", "DR."),
            Map.entry("tb", "DR."),
            Map.entry("leprosy", "LP."),
            Map.entry("anthrax", "AX."),
            Map.entry("yellow fever", "YF."),
            Map.entry("guinea worm", "GW."),
            Map.entry("plague", "PL."),
            Map.entry("tetanus", "NT."));

    private volatile List<DiagnosisType> catalogCache;

    public String resolveForDiagnosis(Diagnosis diagnosis) {
        if (diagnosis == null) {
            return null;
        }
        if (diagnosis.hmisCode != null && !diagnosis.hmisCode.isBlank()) {
            return normalizeCode(diagnosis.hmisCode);
        }
        if (diagnosis.diagnosisType != null && diagnosis.diagnosisType.hmisCode != null) {
            return normalizeCode(diagnosis.diagnosisType.hmisCode);
        }
        return resolveFromText(diagnosis.name);
    }

    public String resolveFromText(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String lower = text.toLowerCase(Locale.ROOT);
        for (DiagnosisType type : loadCatalog()) {
            if (type.hmisCode == null || type.hmisCode.isBlank()) {
                continue;
            }
            if (type.title != null && lower.contains(type.title.toLowerCase(Locale.ROOT))) {
                return normalizeCode(type.hmisCode);
            }
            if (type.matchKeywords != null) {
                for (String keyword : type.matchKeywords.split(",")) {
                    String k = keyword.trim().toLowerCase(Locale.ROOT);
                    if (!k.isEmpty() && lower.contains(k)) {
                        return normalizeCode(type.hmisCode);
                    }
                }
            }
        }
        for (Map.Entry<String, String> entry : BUILTIN_KEYWORDS.entrySet()) {
            if (lower.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    public boolean isAntimalarialDrugName(String itemName) {
        if (itemName == null || itemName.isBlank()) {
            return false;
        }
        String lower = itemName.toLowerCase(Locale.ROOT);
        return lower.contains("artemether")
                || lower.contains("lumefantrine")
                || lower.contains("coartem")
                || lower.contains("artesunate")
                || lower.contains("quinine")
                || lower.contains("chloroquine")
                || lower.contains("dihydroartemisinin");
    }

    public boolean isPositiveLabResult(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        String v = value.trim().toLowerCase(Locale.ROOT);
        if (v.contains("neg") || v.contains("negative") || v.equals("-") || v.equals("0")) {
            return false;
        }
        return v.contains("pos") || v.contains("positive") || v.contains("+") || v.contains("detected") || v.equals("1");
    }

    public boolean hasLabValue(String value) {
        return value != null && !value.isBlank() && !"-".equals(value.trim());
    }

    private List<DiagnosisType> loadCatalog() {
        List<DiagnosisType> cached = catalogCache;
        if (cached == null) {
            cached = DiagnosisType.listAll();
            catalogCache = cached;
        }
        return cached;
    }

    private String normalizeCode(String code) {
        if (code == null) {
            return null;
        }
        String trimmed = code.trim().toUpperCase(Locale.ROOT);
        return trimmed.isEmpty() ? null : trimmed;
    }
}