package org.example.lab.generalReport.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.client.domains.Patient;
import org.example.configuration.handler.ResponseMessage;
import org.example.lab.generalReport.domains.GeneralLabReport;
import org.example.lab.generalReport.domains.repositories.GeneralLabReportRepository;
import org.example.lab.generalReport.services.Payloads.requests.GeneralLabReportUpdateRequest;
import org.example.lab.generalReport.services.Payloads.responses.GeneralLabReportDTO;
import org.example.procedure.procedureRequested.domains.ProcedureRequested;
import org.example.procedure.procedureRequested.domains.repositories.ProcedureRequestedRepository;
import org.example.visit.domains.PatientVisit;

import java.time.LocalDateTime;

@ApplicationScoped
public class GeneralLabReportService {

    @Inject
    GeneralLabReportRepository generalLabReportRepository;

    @Inject
    ProcedureRequestedRepository procedureRequestedRepository;


    @Transactional
    public void createGeneralLabReport(ProcedureRequested procedureRequested) {
        PatientVisit patientVisit = procedureRequested.visit;
        Patient patient = patientVisit.patient;

        GeneralLabReport report = new GeneralLabReport();
        report.patientName = patient.patientFirstName + " " + patient.patientSecondName;
        report.gender = patient.patientGender;
        report.patientAge = patient.patientAge;
        report.visit = procedureRequested.visit;
        report.procedureRequested = procedureRequested;
        report.doneBy = procedureRequested.doneBy != null ? procedureRequested.doneBy : "";
        report.recommendation = "";
        report.labReportTitle = "";
        report.test = procedureRequested.procedureRequestedName;
        report.result = "";
        report.notes = "";
        report.reportCreationDateAndTime = LocalDateTime.now();
        report.sampleCollectionDateAndTime = LocalDateTime.now();
        report.labRequestDate = procedureRequested.dateOfProcedure;

        generalLabReportRepository.persist(report);

        procedureRequested.doneBy = report.doneBy;
        procedureRequestedRepository.persist(procedureRequested);
    }

    @Transactional
    public Response updateGeneralLabReportById(Long id, GeneralLabReportUpdateRequest request) {
        GeneralLabReport report = GeneralLabReport.findById(id);
        if (report == null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ResponseMessage("General lab report not found for ID: " + id))
                    .build();
        }

        ProcedureRequested procedureRequested = ProcedureRequested.findById(report.procedureRequested.id);

        report.procedureRequested = procedureRequested;
        report.test = procedureRequested.procedureRequestedName;

        if (request.doneBy != null) {
            report.doneBy = request.doneBy;
        }
        if (request.recommendation != null) {
            report.recommendation = request.recommendation;
        }
        if (request.labReportTitle != null) {
            report.labReportTitle = request.labReportTitle;
        }
        if (request.result != null) {
            report.result = request.result;
        }
        if (request.notes != null) {
            report.notes = request.notes;
        }

        report.reportUpDatedDateAndTime = LocalDateTime.now();

        generalLabReportRepository.persist(report);
        generalLabReportRepository.flush();

        String plainResult = htmlToPlainText(report.result);
        if (request.result != null) {
            procedureRequested.report = plainResult;
        } else if (request.labReportTitle != null && !request.labReportTitle.isBlank()) {
            procedureRequested.report = request.labReportTitle.trim();
        }
        if (request.doneBy != null) {
            procedureRequested.doneBy = report.doneBy;
        }

        if (plainResult.isEmpty()) {
            procedureRequested.status = "Pending";
            procedureRequested.bgColor = "rgb(6, 113, 212)";
        } else {
            procedureRequested.status = "Done";
            procedureRequested.bgColor = "rgb(5, 182, 58)";
        }
        

        procedureRequestedRepository.persist(procedureRequested);

        return Response.status(Response.Status.CREATED)
                .entity(new ResponseMessage("General lab report updated successfully", new GeneralLabReportDTO(report)))
                .build();
    }

    @Transactional
    public Response getLabReportByRequestId(Long procedureRequestedId) {
        GeneralLabReport report = GeneralLabReport.find("procedureRequested.id", procedureRequestedId).firstResult();
        if (report == null) {
            ProcedureRequested procedureRequested = ProcedureRequested.findById(procedureRequestedId);
            if (procedureRequested == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ResponseMessage("Procedure request not found for ID: " + procedureRequestedId))
                        .build();
            }
            createGeneralLabReport(procedureRequested);
            report = GeneralLabReport.find("procedureRequested.id", procedureRequestedId).firstResult();
        }
        return Response.ok(new ResponseMessage("lab report fetched successfully", new GeneralLabReportDTO(report))).build();
    }

    /** Plain-text summary for procedure request list cards (Quill stores HTML in result). */
    private static String htmlToPlainText(String html) {
        if (html == null || html.isBlank()) {
            return "";
        }
        String text = html
                .replaceAll("(?i)<\\s*br\\s*/?>", "\n")
                .replaceAll("(?i)</\\s*p\\s*>", "\n")
                .replaceAll("(?i)</\\s*div\\s*>", "\n")
                .replaceAll("(?i)</\\s*li\\s*>", "\n")
                .replaceAll("<[^>]+>", " ")
                .replace('\u00a0', ' ')
                .replaceAll("[ \\t]+\\n", "\n")
                .replaceAll("\\n{3,}", "\n\n")
                .replaceAll("\\s+", " ")
                .trim();
        return text;
    }
}








