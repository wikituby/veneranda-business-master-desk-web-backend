package org.example.lab.cbc.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.json.JsonWriter;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.client.domains.Patient;
import org.example.configuration.handler.ResponseMessage;
import org.example.lab.cbc.domains.Cbc;
import org.example.lab.cbc.domains.repositories.CbcRepository;
import org.example.lab.cbc.services.Payloads.requests.CbcUpdateRequest;
import org.example.lab.cbc.services.Payloads.responses.CbcDTO;
import org.example.procedure.procedureRequested.domains.ProcedureRequested;
import org.example.procedure.procedureRequested.domains.repositories.ProcedureRequestedRepository;
import org.example.visit.domains.PatientVisit;

import java.io.StringWriter;
import java.time.LocalDateTime;

@ApplicationScoped
public class CbcService {

    @Inject
    CbcRepository cbcRepository;

    @Inject
    ProcedureRequestedRepository procedureRequestedRepository;

    @Transactional
    public void createCbcReport(ProcedureRequested procedureRequested) {
        PatientVisit patientVisit = procedureRequested.visit;
        Patient patient = patientVisit.patient;

        Cbc cbc = new Cbc();
        cbc.patientName = patient.patientFirstName + " " + patient.patientSecondName;
        cbc.gender = patient.patientGender;
        cbc.patientAge = patient.patientAge;
        cbc.visit = procedureRequested.visit;
        cbc.procedureRequested = procedureRequested;
        cbc.doneBy = "";
        cbc.labReportTitle = "";
        cbc.test = procedureRequested.procedureRequestedName;

        cbc.wbc = "";
        cbc.lymph = "";
        cbc.mid = "";
        cbc.gran = "";
        cbc.lymphPercent = "";
        cbc.midPercent = "";
        cbc.granPercent = "";
        cbc.hgb = "";
        cbc.rbc = "";
        cbc.hct = "";
        cbc.mcv = "";
        cbc.mch = "";
        cbc.mchc = "";
        cbc.rdwCv = "";
        cbc.rdwSd = "";
        cbc.plt = "";
        cbc.mpv = "";
        cbc.pdw = "";
        cbc.pct = "";
        cbc.interpretations = "";

        cbc.reportCreationDateAndTime = LocalDateTime.now();
        cbc.sampleCollectionDateAndTime = LocalDateTime.now();
        cbc.labRequestDate = procedureRequested.dateOfProcedure;

        cbcRepository.persist(cbc);

        procedureRequested.doneBy = cbc.doneBy;
        procedureRequestedRepository.persist(procedureRequested);
    }

    @Transactional
    public Response updateCbcReportById(Long id, CbcUpdateRequest request) {
        Cbc cbc = Cbc.findById(id);
        if (cbc == null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ResponseMessage("CBC report not found for ID: " + id))
                    .build();
        }

        ProcedureRequested procedureRequested = ProcedureRequested.findById(cbc.procedureRequested.id);

        cbc.procedureRequested = procedureRequested;
        cbc.test = procedureRequested.procedureRequestedName;

        if (hasText(request.doneBy)) {
            cbc.doneBy = request.doneBy;
        }
        if (hasText(request.labReportTitle)) {
            cbc.labReportTitle = request.labReportTitle;
        }
        if (hasText(request.wbc)) {
            cbc.wbc = request.wbc;
        }
        if (hasText(request.lymph)) {
            cbc.lymph = request.lymph;
        }
        if (hasText(request.mid)) {
            cbc.mid = request.mid;
        }
        if (hasText(request.gran)) {
            cbc.gran = request.gran;
        }
        if (hasText(request.lymphPercent)) {
            cbc.lymphPercent = request.lymphPercent;
        }
        if (hasText(request.midPercent)) {
            cbc.midPercent = request.midPercent;
        }
        if (hasText(request.granPercent)) {
            cbc.granPercent = request.granPercent;
        }
        if (hasText(request.hgb)) {
            cbc.hgb = request.hgb;
        }
        if (hasText(request.rbc)) {
            cbc.rbc = request.rbc;
        }
        if (hasText(request.hct)) {
            cbc.hct = request.hct;
        }
        if (hasText(request.mcv)) {
            cbc.mcv = request.mcv;
        }
        if (hasText(request.mch)) {
            cbc.mch = request.mch;
        }
        if (hasText(request.mchc)) {
            cbc.mchc = request.mchc;
        }
        if (hasText(request.rdwCv)) {
            cbc.rdwCv = request.rdwCv;
        }
        if (hasText(request.rdwSd)) {
            cbc.rdwSd = request.rdwSd;
        }
        if (hasText(request.plt)) {
            cbc.plt = request.plt;
        }
        if (hasText(request.mpv)) {
            cbc.mpv = request.mpv;
        }
        if (hasText(request.pdw)) {
            cbc.pdw = request.pdw;
        }
        if (hasText(request.pct)) {
            cbc.pct = request.pct;
        }
        if (request.interpretations != null) {
            // UI often sends every parameter key with "" values — that JSON is huge and blows VARCHAR limits.
            // Treat "all blank" as no interpretations: persist empty string.
            JsonObject stripped = stripBlankStringInterpretationEntries(request.interpretations);
            if (stripped.isEmpty()) {
                cbc.interpretations = "";
            } else {
                cbc.interpretations = jsonObjectToCompactString(stripped);
            }
        }

        cbc.reportUpDatedDateAndTime = LocalDateTime.now();

        cbcRepository.persist(cbc);
        cbcRepository.flush();

        if (hasText(request.labReportTitle)) {
            procedureRequested.report = cbc.labReportTitle;
        }
        if (hasText(request.doneBy)) {
            procedureRequested.doneBy = cbc.doneBy;
        }

        if (areAllResultsEmpty(cbc)) {
            procedureRequested.status = "Pending";
            procedureRequested.bgColor = "rgb(6, 113, 212)";
        } else {
            procedureRequested.status = "Done";
            procedureRequested.bgColor = "rgb(5, 182, 58)";
        }

        procedureRequestedRepository.persist(procedureRequested);

        return Response.status(Response.Status.CREATED)
                .entity(new ResponseMessage("CBC report updated successfully", new CbcDTO(cbc)))
                .build();
    }

    @Transactional
    public Response getLabReportByRequestId(Long procedureRequestedId) {
        Cbc cbc = Cbc.find("procedureRequested.id", procedureRequestedId).firstResult();
        if (cbc == null) {
            ProcedureRequested procedureRequested = ProcedureRequested.findById(procedureRequestedId);
            if (procedureRequested == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ResponseMessage("Procedure request not found for ID: " + procedureRequestedId))
                        .build();
            }
            createCbcReport(procedureRequested);
            cbc = Cbc.find("procedureRequested.id", procedureRequestedId).firstResult();
        }
        return Response.ok(new ResponseMessage("lab report fetched successfully", new CbcDTO(cbc))).build();
    }

    private boolean areAllResultsEmpty(Cbc cbc) {
        return isBlank(cbc.wbc) &&
                isBlank(cbc.lymph) &&
                isBlank(cbc.mid) &&
                isBlank(cbc.gran) &&
                isBlank(cbc.lymphPercent) &&
                isBlank(cbc.midPercent) &&
                isBlank(cbc.granPercent) &&
                isBlank(cbc.hgb) &&
                isBlank(cbc.rbc) &&
                isBlank(cbc.hct) &&
                isBlank(cbc.mcv) &&
                isBlank(cbc.mch) &&
                isBlank(cbc.mchc) &&
                isBlank(cbc.rdwCv) &&
                isBlank(cbc.rdwSd) &&
                isBlank(cbc.plt) &&
                isBlank(cbc.mpv) &&
                isBlank(cbc.pdw) &&
                isBlank(cbc.pct);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String jsonObjectToCompactString(JsonObject obj) {
        if (obj == null || obj.isEmpty()) {
            return "";
        }
        StringWriter writer = new StringWriter();
        try (JsonWriter jsonWriter = Json.createWriter(writer)) {
            jsonWriter.writeObject(obj);
        }
        return writer.toString();
    }

    /** Drops null / blank string entries so a template object with empty values does not become a long JSON blob. */
    private static JsonObject stripBlankStringInterpretationEntries(JsonObject src) {
        JsonObjectBuilder b = Json.createObjectBuilder();
        for (String key : src.keySet()) {
            JsonValue v = src.get(key);
            if (v == null || v.getValueType() == JsonValue.ValueType.NULL) {
                continue;
            }
            if (v.getValueType() == JsonValue.ValueType.STRING) {
                String s = ((JsonString) v).getString();
                if (s != null && !s.isBlank()) {
                    b.add(key, s);
                }
            } else {
                b.add(key, v);
            }
        }
        return b.build();
    }
}













