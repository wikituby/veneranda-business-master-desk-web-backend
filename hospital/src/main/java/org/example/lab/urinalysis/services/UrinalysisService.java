package org.example.lab.urinalysis.services;

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
import org.example.lab.urinalysis.domains.Urinalysis;
import org.example.lab.urinalysis.domains.repositories.UrinalysisRepository;
import org.example.lab.urinalysis.services.Payloads.requests.UrinalysisUpdateRequest;
import org.example.lab.urinalysis.services.Payloads.responses.UrinalysisDTO;
import org.example.procedure.procedureRequested.domains.ProcedureRequested;
import org.example.procedure.procedureRequested.domains.repositories.ProcedureRequestedRepository;
import org.example.visit.domains.PatientVisit;

import java.io.StringWriter;
import java.time.LocalDateTime;

@ApplicationScoped
public class UrinalysisService {

    @Inject
    UrinalysisRepository urinalysisRepository;

    @Inject
    ProcedureRequestedRepository procedureRequestedRepository;

    @Transactional
    public void createUrinalysisReport(ProcedureRequested procedureRequested) {
        PatientVisit patientVisit = procedureRequested.visit;
        Patient patient = patientVisit.patient;

        Urinalysis urinalysis = new Urinalysis();
        urinalysis.patientName = patient.patientFirstName + " " + patient.patientSecondName;
        urinalysis.gender = patient.patientGender;
        urinalysis.patientAge = patient.patientAge;
        urinalysis.visit = procedureRequested.visit;
        urinalysis.procedureRequested = procedureRequested;
        urinalysis.doneBy = "";
        urinalysis.labReportTitle = "";
        urinalysis.test = procedureRequested.procedureRequestedName;

        urinalysis.ph = "";
        urinalysis.sg = "";
        urinalysis.protein = "";
        urinalysis.glucose = "";
        urinalysis.ketones = "";
        urinalysis.blood = "";
        urinalysis.bilirubin = "";
        urinalysis.urobilinogen = "";
        urinalysis.nitrite = "";
        urinalysis.leukocyteE = "";
        urinalysis.epithelialCells = "";
        urinalysis.pusCellsWbcs = "";
        urinalysis.casts = "";
        urinalysis.redCells = "";
        urinalysis.crystals = "";
        urinalysis.color = "";
        urinalysis.appearance = "";
        urinalysis.volume = "";
        urinalysis.interpretations = "";
        urinalysis.others = "";

        urinalysis.reportCreationDateAndTime = LocalDateTime.now();
        urinalysis.sampleCollectionDateAndTime = LocalDateTime.now();
        urinalysis.labRequestDate = procedureRequested.dateOfProcedure;

        urinalysisRepository.persist(urinalysis);

        procedureRequested.doneBy = urinalysis.doneBy;
        procedureRequestedRepository.persist(procedureRequested);
    }

    @Transactional
    public Response updateUrinalysisReportById(Long id, UrinalysisUpdateRequest request) {
        Urinalysis urinalysis = Urinalysis.findById(id);
        if (urinalysis == null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ResponseMessage("Urinalysis report not found for ID: " + id))
                    .build();
        }

        ProcedureRequested procedureRequested = ProcedureRequested.findById(urinalysis.procedureRequested.id);

        urinalysis.procedureRequested = procedureRequested;
        urinalysis.test = procedureRequested.procedureRequestedName;

        if (request.doneBy != null) {
            urinalysis.doneBy = request.doneBy;
        }
        if (request.labReportTitle != null) {
            urinalysis.labReportTitle = request.labReportTitle;
        }
        if (request.ph != null) {
            urinalysis.ph = request.ph;
        }
        if (request.sg != null) {
            urinalysis.sg = request.sg;
        }
        if (request.protein != null) {
            urinalysis.protein = request.protein;
        }
        if (request.glucose != null) {
            urinalysis.glucose = request.glucose;
        }
        if (request.ketones != null) {
            urinalysis.ketones = request.ketones;
        }
        if (request.blood != null) {
            urinalysis.blood = request.blood;
        }
        if (request.bilirubin != null) {
            urinalysis.bilirubin = request.bilirubin;
        }
        if (request.urobilinogen != null) {
            urinalysis.urobilinogen = request.urobilinogen;
        }
        if (request.nitrite != null) {
            urinalysis.nitrite = request.nitrite;
        }
        if (request.leukocyteE != null) {
            urinalysis.leukocyteE = request.leukocyteE;
        }
        if (request.epithelialCells != null) {
            urinalysis.epithelialCells = request.epithelialCells;
        }
        if (request.pusCellsWbcs != null) {
            urinalysis.pusCellsWbcs = request.pusCellsWbcs;
        }
        if (request.casts != null) {
            urinalysis.casts = request.casts;
        }
        if (request.redCells != null) {
            urinalysis.redCells = request.redCells;
        }
        if (request.crystals != null) {
            urinalysis.crystals = request.crystals;
        }
        if (request.color != null) {
            urinalysis.color = request.color;
        }
        if (request.appearance != null) {
            urinalysis.appearance = request.appearance;
        }
        if (request.volume != null) {
            urinalysis.volume = request.volume;
        }
        if (request.interpretations != null) {
            JsonObject stripped = stripBlankStringInterpretationEntries(request.interpretations);
            if (stripped.isEmpty()) {
                urinalysis.interpretations = "";
            } else {
                urinalysis.interpretations = jsonObjectToCompactString(stripped);
            }
        }
        if (request.others != null) {
            urinalysis.others = request.others;
        }

        urinalysis.reportUpDatedDateAndTime = LocalDateTime.now();

        urinalysisRepository.persist(urinalysis);
        urinalysisRepository.flush();

        if (request.labReportTitle != null) {
            procedureRequested.report = urinalysis.labReportTitle;
        }
        if (request.doneBy != null) {
            procedureRequested.doneBy = urinalysis.doneBy;
        }

        if (areAllResultsEmpty(urinalysis)) {
            procedureRequested.status = "Pending";
            procedureRequested.bgColor = "rgb(6, 113, 212)";
        } else {
            procedureRequested.status = "Done";
            procedureRequested.bgColor = "rgb(5, 182, 58)";
        }

        procedureRequestedRepository.persist(procedureRequested);

        return Response.status(Response.Status.CREATED)
                .entity(new ResponseMessage("Urinalysis report updated successfully", new UrinalysisDTO(urinalysis)))
                .build();
    }

    @Transactional
    public Response getLabReportByRequestId(Long procedureRequestedId) {
        Urinalysis urinalysis = Urinalysis.find("procedureRequested.id", procedureRequestedId).firstResult();
        if (urinalysis == null) {
            ProcedureRequested procedureRequested = ProcedureRequested.findById(procedureRequestedId);
            if (procedureRequested == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ResponseMessage("Procedure request not found for ID: " + procedureRequestedId))
                        .build();
            }
            createUrinalysisReport(procedureRequested);
            urinalysis = Urinalysis.find("procedureRequested.id", procedureRequestedId).firstResult();
        }
        return Response.ok(new ResponseMessage("lab report fetched successfully", new UrinalysisDTO(urinalysis))).build();
    }

    private boolean areAllResultsEmpty(Urinalysis urinalysis) {
        return isBlank(urinalysis.ph) &&
                isBlank(urinalysis.sg) &&
                isBlank(urinalysis.protein) &&
                isBlank(urinalysis.glucose) &&
                isBlank(urinalysis.ketones) &&
                isBlank(urinalysis.blood) &&
                isBlank(urinalysis.bilirubin) &&
                isBlank(urinalysis.urobilinogen) &&
                isBlank(urinalysis.nitrite) &&
                isBlank(urinalysis.leukocyteE) &&
                isBlank(urinalysis.epithelialCells) &&
                isBlank(urinalysis.pusCellsWbcs) &&
                isBlank(urinalysis.casts) &&
                isBlank(urinalysis.redCells) &&
                isBlank(urinalysis.crystals) &&
                isBlank(urinalysis.color) &&
                isBlank(urinalysis.appearance) &&
                isBlank(urinalysis.volume) &&
                isBlank(urinalysis.others);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
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







