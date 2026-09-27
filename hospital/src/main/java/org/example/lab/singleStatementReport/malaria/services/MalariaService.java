package org.example.lab.singleStatementReport.malaria.services;

import io.vertx.mutiny.sqlclient.Pool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.client.domains.Patient;
import org.example.configuration.handler.ResponseMessage;
import org.example.lab.singleStatementReport.malaria.domains.Malaria;
import org.example.lab.singleStatementReport.malaria.domains.repositories.MalariaRepository;
import org.example.lab.singleStatementReport.malaria.services.Payloads.requests.MalariaUpdateRequest;
import org.example.lab.singleStatementReport.malaria.services.Payloads.responses.MalariaDTO;
import org.example.procedure.procedureRequested.domains.ProcedureRequested;
import org.example.procedure.procedureRequested.domains.repositories.ProcedureRequestedRepository;
import org.example.visit.domains.PatientVisit;

import java.time.LocalDateTime;

@ApplicationScoped
public class MalariaService {

    @Inject
    MalariaRepository malariaRepository;

    @Inject
    ProcedureRequestedRepository procedureRequestedRepository;

    @Inject
    Pool client;

    @Transactional
    public void createMrdtReport(ProcedureRequested procedureRequested){

        PatientVisit patientVisit = procedureRequested.visit; // ✅ correct

        Patient patient = patientVisit.patient;

        Malaria malaria = new Malaria();
        malaria.patientName = patient.patientFirstName +" "+ patient.patientSecondName;
        malaria.gender = patient.patientGender;
        malaria.patientAge = patient.patientAge;
        malaria.visit = procedureRequested.visit;
        malaria.procedureRequested = procedureRequested;
        malaria.doneBy = "";
        malaria.recommendation = "";
        malaria.labReportTitle = "";
        malaria.test = procedureRequested.procedureRequestedName;
        malaria.bs = "";
        malaria.mrdt = "";

        malaria.reportCreationDateAndTime = LocalDateTime.now();
        malaria.sampleCollectionDateAndTime = LocalDateTime.now();

        malaria.labRequestDate = procedureRequested.dateOfProcedure;

        malariaRepository.persist(malaria);

        procedureRequested.doneBy = malaria.doneBy;
        procedureRequestedRepository.persist(procedureRequested);

    }

    @Transactional
    public Response updateMalariaReportById(Long id, MalariaUpdateRequest request) {
        Malaria malaria = Malaria.findById(id); // ✅ correct
        if (malaria == null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ResponseMessage("malaria Report not found for ID: " + id))
                    .build();
        }

        ProcedureRequested procedureRequested = ProcedureRequested.findById(malaria.procedureRequested.id);

        malaria.procedureRequested = procedureRequested;
        malaria.test = procedureRequested.procedureRequestedName;

        if (request.doneBy != null) {
            malaria.doneBy = request.doneBy;
        }
        if (request.recommendation != null) {
            malaria.recommendation = request.recommendation;
        }
        if (request.bs != null) {
            malaria.bs = request.bs;
            malaria.labReportTitle = request.bs;
        }
        if (request.notes != null) {
            malaria.notes = request.notes;
        }
        if (request.mrdt != null) {
            malaria.mrdt = request.mrdt;
        }

        malaria.reportUpDatedDateAndTime = LocalDateTime.now();

        malariaRepository.persist(malaria);
        malariaRepository.flush();

        if (request.bs != null) {
            procedureRequested.report = malaria.bs;
        }
        if (request.doneBy != null) {
            procedureRequested.doneBy = malaria.doneBy;
        }

        if (malaria.bs == null || malaria.bs.isEmpty() ||
                malaria.mrdt == null || malaria.mrdt.isEmpty()
)
        {
            procedureRequested.status = "Pending";
            procedureRequested.bgColor = "rgb(6, 113, 212)";
        }else{
            procedureRequested.status = "Done";
            procedureRequested.bgColor = "rgb(5, 182, 58)";
        }

        procedureRequestedRepository.persist(procedureRequested);

        return Response.status(Response.Status.CREATED)
                .entity(new ResponseMessage("malaria report updated successfully", new MalariaDTO(malaria)))
                .build();


    }


    @Transactional
    public Response getLabReportByRequestId(Long procedureRequestedId){
        Malaria mrdt = Malaria.find("procedureRequested.id", procedureRequestedId).firstResult();
        if (mrdt == null) {
            ProcedureRequested procedureRequested = ProcedureRequested.findById(procedureRequestedId);
            if (procedureRequested == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ResponseMessage("Procedure request not found for ID: " + procedureRequestedId))
                        .build();
            }
            createMrdtReport(procedureRequested);
            mrdt = Malaria.find("procedureRequested.id", procedureRequestedId).firstResult();
        }
       /*return Response.status(Response.Status.FOUND)
               .entity(new ResponseMessage("scan report fetched successfully: ", new GeneralUsDTO(generalUs)))
               .build();*/
        return Response.ok(new ResponseMessage("lab report fetched successfully", new MalariaDTO(mrdt))).build();
    }









}






