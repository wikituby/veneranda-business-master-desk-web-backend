package org.example.visit.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.ConfigProvider;
import org.example.client.domains.Patient;
import org.example.client.domains.PatientGroup;
import org.example.client.domains.repositories.PatientGroupRepository;
import org.example.consultations.domains.Consultation;
import org.example.finance.invoice.domains.Invoice;
import org.example.procedure.procedureRequested.domains.ProcedureRequested;
import org.example.subscription.services.FacilityBrandingService;
import org.example.treatment.domains.TreatmentRequested;
import org.example.treatment.domains.repositories.TreatmentRequestedRepository;
import org.example.visit.domains.PatientVisit;
import org.example.visit.services.paloads.requests.VisitParametersRequest;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApplicationScoped
public class CompassionGroupInvoiceDocxService {

    private static final String TEMPLATE_RESOURCE = "compassion-invoice/compassion-group-invoice-template.docx";
    private static final String SCRIPT_RESOURCE = "compassion-invoice/generate_compassion_group_invoice.py";
    private static final Pattern DIGIT_SEQUENCE = Pattern.compile("\\d+");
    private static final DateTimeFormatter EXPORT_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Inject
    TreatmentRequestedRepository treatmentRequestedRepository;

    @Inject
    PatientGroupRepository patientGroupRepository;

    @Inject
    FacilityBrandingService facilityBrandingService;

    public Response generateDocx(VisitParametersRequest request, List<PatientVisit> visits) {
        PatientGroup group = resolvePatientGroup(request);
        if (group == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Patient group is required for compassion invoice export.")
                    .build();
        }
        if (request == null || request.datefrom == null || request.dateto == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Both datefrom and dateto are required for compassion invoice export.")
                    .build();
        }
        if (visits == null || visits.isEmpty()) {
            String debtHint = request.hasDebt != null
                    ? (request.hasDebt
                        ? " No visits with outstanding debt matched the selected filters."
                        : " No fully paid visits matched the selected filters.")
                    : "";
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("No visits found for the selected filters." + debtHint)
                    .build();
        }

        try {
            JsonObject payload = buildPayload(request, visits, group);
            byte[] docx = runPythonGenerator(payload);
            String filename = "medical-invoice.docx";
            return Response.ok(docx)
                    .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                    .header("Content-Type", "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to generate compassion invoice: " + e.getMessage())
                    .build();
        }
    }

    private PatientGroup resolvePatientGroup(VisitParametersRequest request) {
        if (request == null) {
            return null;
        }
        if (request.patientGroupId != null && request.patientGroupId > 0) {
            PatientGroup byId = patientGroupRepository.findById(request.patientGroupId);
            if (byId != null) {
                return byId;
            }
        }
        if (request.visitGroup != null && !request.visitGroup.isBlank()) {
            PatientGroup resolved = patientGroupRepository.findByNormalizedShortForm(request.visitGroup.trim());
            if (resolved == null) {
                resolved = patientGroupRepository.findByNormalizedGroupName(request.visitGroup.trim());
            }
            return resolved;
        }
        return null;
    }

    private JsonObject buildPayload(VisitParametersRequest request, List<PatientVisit> visits, PatientGroup group) {
        String facilityName = facilityBrandingService.resolveDefaultBranding().facilityName;
        if (facilityName == null || facilityName.isBlank()) {
            facilityName = "VENERANDA MEDICAL";
        }

        String groupName = group.groupName != null && !group.groupName.isBlank()
                ? group.groupName.trim()
                : (group.groupNameShortForm != null ? group.groupNameShortForm.trim() : "Group");

        String periodLabel = request.datefrom.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH))
                .toUpperCase(Locale.ENGLISH);

        List<PatientVisit> sorted = new ArrayList<>(visits);
        sorted.sort(Comparator
                .comparing((PatientVisit v) -> v.visitDate, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(v -> v.id, Comparator.nullsLast(Comparator.naturalOrder()))
                .reversed());

        JsonArrayBuilder visitsJson = Json.createArrayBuilder();
        BigDecimal grandTotal = BigDecimal.ZERO;

        for (PatientVisit visit : sorted) {
            JsonObject visitJson = buildVisitJson(visit);
            visitsJson.add(visitJson);
            grandTotal = grandTotal.add(readBigDecimal(visitJson.getJsonNumber("visitTotal")));
        }

        return Json.createObjectBuilder()
                .add("facilityName", facilityName)
                .add("groupName", groupName)
                .add("periodLabel", periodLabel)
                .add("exportDate", LocalDate.now().format(EXPORT_DATE))
                .add("grandTotal", grandTotal)
                .add("visits", visitsJson)
                .build();
    }

    private JsonObject buildVisitJson(PatientVisit visit) {
        Patient patient = visit.patient;
        String diagnosis = resolveDiagnosis(visit);
        BigDecimal discount = resolveDiscount(visit);
        JsonArrayBuilder lines = Json.createArrayBuilder();
        BigDecimal linesTotal = BigDecimal.ZERO;

        if (visit.getProceduresRequested() != null) {
            for (ProcedureRequested procedure : visit.getProceduresRequested()) {
                BigDecimal amount = nz(procedure.totalAmount);
                linesTotal = linesTotal.add(amount);
                lines.add(Json.createObjectBuilder()
                        .add("label", safeText(procedure.procedureRequestedName, "Procedure"))
                        .add("amount", amount)
                        .build());
            }
        }

        List<TreatmentRequested> treatments = treatmentRequestedRepository.list("visit.id", visit.id);
        for (TreatmentRequested treatment : treatments) {
            BigDecimal amount = nz(treatment.totalAmount);
            linesTotal = linesTotal.add(amount);
            lines.add(Json.createObjectBuilder()
                    .add("label", buildTreatmentLabel(treatment))
                    .add("amount", amount)
                    .build());
        }

        BigDecimal visitTotal = nz(visit.totalAmount);
        if (visitTotal.compareTo(BigDecimal.ZERO) <= 0) {
            visitTotal = linesTotal;
        }

        return Json.createObjectBuilder()
                .add("visitDate", visit.visitDate != null ? visit.visitDate.toString() : "")
                .add("patientId", resolvePatientInvoiceId(patient, visit))
                .add("patientName", safeText(visit.patientName, ""))
                .add("age", formatAge(visit.patientAge != null ? visit.patientAge : (patient != null ? patient.patientAge : null)))
                .add("sex", patient != null ? safeText(patient.patientGender, "").toLowerCase(Locale.ENGLISH) : "")
                .add("diagnosis", diagnosis)
                .add("discount", discount)
                .add("visitTotal", visitTotal)
                .add("lines", lines)
                .build();
    }

    private String resolvePatientInvoiceId(Patient patient, PatientVisit visit) {
        if (patient != null) {
            String fromSecondName = extractLastNumber(patient.patientSecondName);
            if (!fromSecondName.isEmpty()) {
                return fromSecondName;
            }
            if (patient.patientFileNo != null && !patient.patientFileNo.isBlank()) {
                return patient.patientFileNo.trim();
            }
            if (patient.id != null) {
                return String.valueOf(patient.id);
            }
        }

        String visitName = visit != null ? safeText(visit.patientName, "") : "";
        if (!visitName.isEmpty()) {
            String[] parts = visitName.trim().split("\\s+");
            if (parts.length > 1) {
                String fromVisitSecondName = extractLastNumber(parts[parts.length - 1]);
                if (!fromVisitSecondName.isEmpty()) {
                    return fromVisitSecondName;
                }
            }
        }
        return "";
    }

    private String extractLastNumber(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        Matcher matcher = DIGIT_SEQUENCE.matcher(value.trim());
        String lastNumber = "";
        while (matcher.find()) {
            lastNumber = matcher.group();
        }
        return lastNumber;
    }

    private String buildTreatmentLabel(TreatmentRequested treatment) {
        StringBuilder label = new StringBuilder(safeText(treatment.itemName, "Treatment"));
        appendPart(label, treatment.amountPerFrequencyValue);
        appendPart(label, treatment.amountPerFrequencyUnit);
        if (treatment.frequencyValue != null) {
            appendPart(label, treatment.frequencyValue.stripTrailingZeros().toPlainString() + " X");
        }
        appendPart(label, treatment.frequencyUnit);
        if (treatment.durationValue != null) {
            appendPart(label, "FOR " + treatment.durationValue.stripTrailingZeros().toPlainString());
        }
        appendPart(label, treatment.durationUnit);
        appendPart(label, treatment.instructions);
        appendPart(label, treatment.route);
        return label.toString().trim();
    }

    private void appendPart(StringBuilder sb, Object value) {
        if (value == null) {
            return;
        }
        String text = String.valueOf(value).trim();
        if (text.isEmpty()) {
            return;
        }
        if (!sb.isEmpty()) {
            sb.append(' ');
        }
        sb.append(text);
    }

    private String resolveDiagnosis(PatientVisit visit) {
        if (visit.getConsultation() == null || visit.getConsultation().isEmpty()) {
            return "";
        }
        Consultation first = visit.getConsultation().get(0);
        return first != null ? safeText(first.diagnosis, "") : "";
    }

    private BigDecimal resolveDiscount(PatientVisit visit) {
        if (visit.getInvoice() == null || visit.getInvoice().isEmpty()) {
            return BigDecimal.ZERO;
        }
        Invoice invoice = visit.getInvoice().get(0);
        return invoice != null ? nz(invoice.discount) : BigDecimal.ZERO;
    }

    private String formatAge(BigDecimal age) {
        if (age == null) {
            return "";
        }
        return age.stripTrailingZeros().toPlainString() + "Yrs";
    }

    private BigDecimal nz(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private BigDecimal readBigDecimal(jakarta.json.JsonNumber number) {
        return number != null ? number.bigDecimalValue() : BigDecimal.ZERO;
    }

    private String safeText(String value, String fallback) {
        return value != null && !value.isBlank() ? value.trim() : fallback;
    }

    private byte[] runPythonGenerator(JsonObject payload) throws IOException, InterruptedException {
        Path tempDir = Files.createTempDirectory("compassion-invoice-");
        Path dataPath = tempDir.resolve("payload.json");
        Path outputPath = tempDir.resolve("invoice.docx");
        Path templatePath = tempDir.resolve("template.docx");

        try {
            try (InputStream templateStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(TEMPLATE_RESOURCE)) {
                if (templateStream == null) {
                    throw new IOException("Template not found on classpath: " + TEMPLATE_RESOURCE);
                }
                Files.copy(templateStream, templatePath, StandardCopyOption.REPLACE_EXISTING);
            }

            Files.writeString(dataPath, payload.toString(), StandardCharsets.UTF_8);

            Path scriptPath = resolveScriptPath(tempDir);
            List<String> command = new ArrayList<>();
            command.addAll(resolvePythonCommandPrefix());
            command.add(scriptPath.toString());
            command.add("--template");
            command.add(templatePath.toString());
            command.add("--data");
            command.add(dataPath.toString());
            command.add("--output");
            command.add(outputPath.toString());
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true);
            Process process = builder.start();
            String processOutput = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            int exitCode = process.waitFor();
            if (exitCode != 0 || !Files.exists(outputPath)) {
                throw new IOException("Python invoice generator failed (" + exitCode + "): " + processOutput);
            }
            return Files.readAllBytes(outputPath);
        } finally {
            try {
                Files.walk(tempDir)
                        .sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (IOException ignored) {
                                // best effort cleanup
                            }
                        });
            } catch (IOException ignored) {
                // best effort cleanup
            }
        }
    }

    private Path resolveScriptPath(Path workDir) throws IOException {
        Path cwdScript = Path.of(System.getProperty("user.dir"), "scripts", "generate_compassion_group_invoice.py");
        if (Files.exists(cwdScript)) {
            return cwdScript.toAbsolutePath();
        }
        Path parentScript = Path.of(System.getProperty("user.dir"), "hospital", "scripts", "generate_compassion_group_invoice.py");
        if (Files.exists(parentScript)) {
            return parentScript.toAbsolutePath();
        }
        try (InputStream scriptStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(SCRIPT_RESOURCE)) {
            if (scriptStream != null) {
                Path extracted = workDir.resolve("generate_compassion_group_invoice.py");
                Files.copy(scriptStream, extracted, StandardCopyOption.REPLACE_EXISTING);
                return extracted;
            }
        }
        throw new IOException("Could not find generate_compassion_group_invoice.py in scripts folder or classpath.");
    }

    private String configuredPythonExecutable() {
        return trimToNull(ConfigProvider.getConfig()
                .getOptionalValue("compassion.invoice.python.executable", String.class)
                .orElse(null));
    }

    private String configuredPythonLauncherArgs() {
        return trimToNull(ConfigProvider.getConfig()
                .getOptionalValue("compassion.invoice.python.launcher-args", String.class)
                .orElse(null));
    }

    private List<String> resolvePythonCommandPrefix() throws IOException {
        String fromEnv = trimToNull(System.getenv("COMPASSION_INVOICE_PYTHON"));
        String fromEnvArgs = trimToNull(System.getenv("COMPASSION_INVOICE_PYTHON_ARGS"));
        if (fromEnv != null) {
            return buildPythonPrefix(fromEnv, fromEnvArgs);
        }
        String configured = configuredPythonExecutable();
        if (configured != null) {
            return buildPythonPrefix(configured, configuredPythonLauncherArgs());
        }

        List<List<String>> candidates = List.of(
                List.of("python"),
                List.of("python3"),
                List.of("py", "-3"),
                List.of("py")
        );
        for (List<String> prefix : candidates) {
            if (canRunPython(prefix)) {
                return prefix;
            }
        }

        if (isWindows()) {
            String onPath = findExecutableOnWindowsPath("python");
            if (onPath != null && canRunPython(List.of(onPath))) {
                return List.of(onPath);
            }
            String pyPath = findExecutableOnWindowsPath("py");
            if (pyPath != null && canRunPython(List.of(pyPath, "-3"))) {
                return List.of(pyPath, "-3");
            }
            String common = findPythonInCommonWindowsLocations();
            if (common != null && canRunPython(List.of(common))) {
                return List.of(common);
            }
        }

        throw new IOException(buildPythonMissingMessage());
    }

    private List<String> buildPythonPrefix(String executable, String launcherArgs) {
        List<String> prefix = new ArrayList<>();
        prefix.add(executable.trim());
        String args = trimToNull(launcherArgs);
        if (args != null) {
            for (String part : args.split("\\s+")) {
                if (!part.isBlank()) {
                    prefix.add(part.trim());
                }
            }
        }
        return prefix;
    }

    private boolean canRunPython(List<String> prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return false;
        }
        List<String> cmd = new ArrayList<>(prefix);
        cmd.add("--version");
        try {
            Process process = new ProcessBuilder(cmd).start();
            return process.waitFor() == 0;
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean isWindows() {
        String os = System.getProperty("os.name", "");
        return os.toLowerCase(Locale.ENGLISH).contains("win");
    }

    private String findExecutableOnWindowsPath(String name) {
        try {
            Process process = new ProcessBuilder("where.exe", name).start();
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
            if (process.waitFor() == 0 && !output.isBlank()) {
                return output.split("\\R")[0].trim();
            }
        } catch (Exception ignored) {
            // try next strategy
        }
        return null;
    }

    private String findPythonInCommonWindowsLocations() {
        List<Path> roots = new ArrayList<>();
        String localAppData = System.getenv("LOCALAPPDATA");
        if (localAppData != null && !localAppData.isBlank()) {
            roots.add(Path.of(localAppData, "Programs", "Python"));
        }
        roots.add(Path.of("C:\\Program Files", "Python"));
        roots.add(Path.of("C:\\Python"));
        for (Path root : roots) {
            if (!Files.isDirectory(root)) {
                continue;
            }
            try (var children = Files.list(root)) {
                List<Path> sorted = children
                        .filter(Files::isDirectory)
                        .sorted(Comparator.comparing((Path p) -> p.getFileName().toString()).reversed())
                        .toList();
                for (Path child : sorted) {
                    Path pythonExe = child.resolve("python.exe");
                    if (Files.isRegularFile(pythonExe)) {
                        return pythonExe.toAbsolutePath().toString();
                    }
                }
            } catch (IOException ignored) {
                // try next root
            }
        }
        return null;
    }

    private String buildPythonMissingMessage() {
        return "Python 3 is not installed or not on PATH for the hospital service. "
                + "On the API server install Python 3, then run: "
                + "pip install -r scripts/requirements-compassion-invoice.txt "
                + "(or set COMPASSION_INVOICE_PYTHON to the full path to python.exe, "
                + "e.g. C:\\Python311\\python.exe).";
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
