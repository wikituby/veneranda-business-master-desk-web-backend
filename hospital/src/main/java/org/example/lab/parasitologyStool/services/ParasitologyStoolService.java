package org.example.lab.parasitologyStool.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonWriter;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.client.domains.Patient;
import org.example.configuration.handler.ResponseMessage;
import org.example.lab.parasitologyStool.domains.ParasitologyStool;
import org.example.lab.parasitologyStool.domains.repositories.ParasitologyStoolRepository;
import org.example.lab.parasitologyStool.services.Payloads.requests.ParasitologyStoolUpdateRequest;
import org.example.lab.parasitologyStool.services.Payloads.responses.ParasitologyStoolDTO;
import org.example.procedure.procedureRequested.domains.ProcedureRequested;
import org.example.procedure.procedureRequested.domains.repositories.ProcedureRequestedRepository;
import org.example.visit.domains.PatientVisit;

import java.io.StringWriter;
import java.time.LocalDateTime;

@ApplicationScoped
public class ParasitologyStoolService {

    @Inject
    ParasitologyStoolRepository parasitologyStoolRepository;

    @Inject
    ProcedureRequestedRepository procedureRequestedRepository;


    @Transactional
    public void createParasitologyStoolReport(ProcedureRequested procedureRequested) {
        PatientVisit patientVisit = procedureRequested.visit;
        Patient patient = patientVisit.patient;

        ParasitologyStool report = new ParasitologyStool();
        report.patientName = patient.patientFirstName + " " + patient.patientSecondName;
        report.gender = patient.patientGender;
        report.patientAge = patient.patientAge;
        report.visit = procedureRequested.visit;
        report.procedureRequested = procedureRequested;
        report.doneBy = "";
        report.labReportTitle = "";
        report.test = procedureRequested.procedureRequestedName;

        report.ova = "";
        report.cysts = "";
        report.larvae = "";
        report.color = "";
        report.consistency = "";
        report.blood = "";
        report.mucus = "";
        report.visibleParasites = "";
        report.others = "";
        report.interpretations = "";

        report.reportCreationDateAndTime = LocalDateTime.now();
        report.sampleCollectionDateAndTime = LocalDateTime.now();
        report.labRequestDate = procedureRequested.dateOfProcedure;

        parasitologyStoolRepository.persist(report);

        procedureRequested.doneBy = report.doneBy;
        procedureRequestedRepository.persist(procedureRequested);
    }

    @Transactional
    public Response updateParasitologyStoolReportById(Long id, ParasitologyStoolUpdateRequest request) {
        ParasitologyStool report = ParasitologyStool.findById(id);
        if (report == null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ResponseMessage("Parasitology stool report not found for ID: " + id))
                    .build();
        }

        ProcedureRequested procedureRequested = ProcedureRequested.findById(report.procedureRequested.id);

        report.procedureRequested = procedureRequested;
        report.test = procedureRequested.procedureRequestedName;

        if (request.doneBy != null) {
            report.doneBy = request.doneBy;
        }
        if (request.labReportTitle != null) {
            report.labReportTitle = request.labReportTitle;
        }
        if (request.ova != null) {
            report.ova = request.ova;
        }
        if (request.cysts != null) {
            report.cysts = request.cysts;
        }
        if (request.larvae != null) {
            report.larvae = request.larvae;
        }
        if (request.color != null) {
            report.color = request.color;
        }
        if (request.consistency != null) {
            report.consistency = request.consistency;
        }
        if (request.blood != null) {
            report.blood = request.blood;
        }
        if (request.mucus != null) {
            report.mucus = request.mucus;
        }
        if (request.visibleParasites != null) {
            report.visibleParasites = request.visibleParasites;
        }
        if (request.others != null) {
            report.others = request.others;
        }
        if (request.interpretations != null) {
            report.interpretations = jsonObjectToCompactString(request.interpretations);
        }

        report.reportUpDatedDateAndTime = LocalDateTime.now();

        parasitologyStoolRepository.persist(report);
        parasitologyStoolRepository.flush();

        if (request.labReportTitle != null) {
            procedureRequested.report = report.labReportTitle;
        }
        if (request.doneBy != null) {
            procedureRequested.doneBy = report.doneBy;
        }

        if (areAllResultsEmpty(report)) {
            procedureRequested.status = "Pending";
            procedureRequested.bgColor = "rgb(6, 113, 212)";
        } else {
            procedureRequested.status = "Done";
            procedureRequested.bgColor = "rgb(5, 182, 58)";
        }

        procedureRequestedRepository.persist(procedureRequested);

        return Response.status(Response.Status.CREATED)
                .entity(new ResponseMessage("Parasitology stool report updated successfully", new ParasitologyStoolDTO(report)))
                .build();
    }

    @Transactional
    public Response getLabReportByRequestId(Long procedureRequestedId) {
        ParasitologyStool report = ParasitologyStool.find("procedureRequested.id", procedureRequestedId).firstResult();
        if (report == null) {
            ProcedureRequested procedureRequested = ProcedureRequested.findById(procedureRequestedId);
            if (procedureRequested == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ResponseMessage("Procedure request not found for ID: " + procedureRequestedId))
                        .build();
            }
            createParasitologyStoolReport(procedureRequested);
            report = ParasitologyStool.find("procedureRequested.id", procedureRequestedId).firstResult();
        }
        return Response.ok(new ResponseMessage("lab report fetched successfully", new ParasitologyStoolDTO(report))).build();
    }

    private boolean areAllResultsEmpty(ParasitologyStool report) {
        return isBlank(report.ova) &&
                isBlank(report.cysts) &&
                isBlank(report.larvae) &&
                isBlank(report.color) &&
                isBlank(report.consistency) &&
                isBlank(report.blood) &&
                isBlank(report.mucus) &&
                isBlank(report.visibleParasites) &&
                isBlank(report.others);
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
}













