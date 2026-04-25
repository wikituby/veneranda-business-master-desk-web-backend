package org.example.procedure.procedureRequested.endpoints;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.procedure.procedureRequested.services.payloads.responses.ProcedureRequestedDTO;
import org.example.procedure.procedureRequested.services.payloads.requests.ProcedureRequestedRequest;
import org.example.procedure.procedureRequested.services.ProcedureRequestedService;
import org.example.procedure.procedureRequested.services.payloads.requests.ProcedureRequestedUpdateRequest;

import java.math.BigDecimal;
import java.util.List;

@Path("Patient-management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Patient Management Module - ServicesRequested", description = "Patient Management")


public class ProcedureRequestedController {

    @Inject
    ProcedureRequestedService procedureRequestedService;


    @POST
    @Path("create-new-procedure-requested/{id}")
    @Transactional
    @Operation(summary = "new-procedureRequested", description = "new-procedureRequested")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ProcedureRequestedDTO.class)))
    public Response createNewProcedureRequested(@PathParam("id") Long id, ProcedureRequestedRequest request){
        return procedureRequestedService.createNewProcedureRequested(id, request);
    }

    @GET
    @Path("get-procedure-Requested-with-type-LabTest-by-visit-id/{id}")
    @Operation(summary = "Get procedureRequested where type is LabTest and visit id", description = "Get procedureRequested where type is LabTest and visit id")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ProcedureRequestedDTO.class)))
    public Response getProcedureRequestedLabTestsByVisitId(@PathParam("id") Long visitId) {
        List<ProcedureRequestedDTO> labTestProcedureRequested = procedureRequestedService.getLabTestProceduresByVisit(visitId);

        // Return a successful response with the list of DTOs
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, labTestProcedureRequested)).build();
    }

    @GET
    @Path("get-ultrasound-scan-by-visit-id/{id}")
    @Operation(summary = "get-ultrasound-scan-by-visit-id", description = "get-ultrasound-scan-by-visit-id")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ProcedureRequestedDTO.class)))
    public Response getUltrasoundScanByVisitId(@PathParam("id") Long visitId) {
        List<ProcedureRequestedDTO> ultrasoundScanProcedureRequested = procedureRequestedService.getUltrasoundScanProceduresByVisit(visitId);

        // Return a successful response with the list of DTOs
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, ultrasoundScanProcedureRequested)).build();
    }

    @GET
    @Path("get-all-ultrasound-scan-procedures")
    @Operation(summary = "get-all-ultrasound-scan", description = "get-all-ultrasound-scan")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ProcedureRequestedDTO.class)))
    public Response getAllUltrasoundScan() {
        List<ProcedureRequestedDTO> ultrasoundScanProcedureRequested = procedureRequestedService.getAllUltrasoundScanProcedures();

        // Return a successful response with the list of DTOs
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, ultrasoundScanProcedureRequested)).build();
    }

    @GET
    @Path("get-all-lab-procedures")
    @Operation(summary = "get-all-lab", description = "get-all-lab")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ProcedureRequestedDTO.class)))
    public Response getAllLabRequests() {
        List<ProcedureRequestedDTO> labProcedureRequested = procedureRequestedService.getAllLabTestProcedures();

        // Return a successful response with the list of DTOs
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, labProcedureRequested)).build();
    }

    @GET
    @Path("get-all-dental-procedures")
    @Operation(summary = "get-all-dental-procedures", description = "get-all-dental-procedures")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ProcedureRequestedDTO.class)))
    public Response getAllDentalProcedures() {
        List<ProcedureRequestedDTO> dentalProcedureRequested = procedureRequestedService.getAllDentalProcedures();

        // Return a successful response with the list of DTOs
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, dentalProcedureRequested)).build();
    }

    @PUT
    @Path("update-procedure-requested/{id}")
    //@RolesAllowed({"ADMIN","CUSTOMER"})
    @Transactional
    @Operation(summary = "Update procedure requested", description = "Update procedure requested")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ProcedureRequestedDTO.class)))
    public Response updateProcedureRequested(@PathParam("id") Long id, ProcedureRequestedUpdateRequest request){
        return Response.ok(new ResponseMessage(ActionMessages.UPDATED.label,procedureRequestedService.updateProcedureRequestedById(id, request) )).build();
    }

    @GET
    @Path("get-total-cost-of-all-lab-tests/{id}")
    @Operation(summary = "get-total-cost-of-all-lab-tests", description = "get-total-cost-of-all-lab-tests")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema()))
    public Response getProcedureRequestedLabTestsBySumVisitId(@PathParam("id") Long visitId) {
        BigDecimal totalCostLabTests;
        totalCostLabTests = procedureRequestedService.getLabTestProceduresAndSumByVisit(visitId);

        // Return a successful response with the list of DTOs
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, totalCostLabTests)).build();
    }

    @GET
    @Path("get-total-cost-of-all-Scans-by-visit-id/{id}")
    @Operation(summary = "get-total-cost-of-all-Scans", description = "get-total-cost-of-all-Scans")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema()))
    public Response getProcedureRequestedScanBySumVisitId(@PathParam("id") Long visitId) {
        BigDecimal totalCostScans;
        totalCostScans = procedureRequestedService.getScanProceduresAndSumByVisit(visitId);

        // Return a successful response with the list of DTOs
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, totalCostScans)).build();
    }


    @GET
    @Path("get-other-procedures-by-visit-id/{id}")
    @Operation(summary = "get-other-procedures-by-visit-id", description = "get-other-procedures-by-visit-id")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ProcedureRequestedDTO.class)))
    public Response getNonLabTestNonUltrasoundProceduresByVisit(@PathParam("id") Long visitId) {
        List<ProcedureRequestedDTO> otherProceduresRequested = procedureRequestedService.getNonLabTestNonUltrasoundProceduresByVisit(visitId);

        // Return a successful response with the list of DTOs
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, otherProceduresRequested)).build();
    }


    @DELETE
    @Path("/delete-requested-procedure-by-id/{id}")
    //@RolesAllowed({"ADMIN"})
    @Transactional
    @Operation(summary = "delete requested procedure by id", description = "delete requested procedure by id")
    @APIResponse(description = "Successful", responseCode = "200")
    public Response deleteRequestedProcedureById(@PathParam("id") Long id){
        return procedureRequestedService.deleteProcedureRequestById(id);
    }


    @GET
    @Path("get-procedure-requested-by-id/{id}")
    //@RolesAllowed({"ADMIN"})
    @Operation(summary = "Get procedure requested", description = "Get procedure requested")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ProcedureRequestedDTO.class)))
    public Response getProcedureRequestedById(@PathParam("id") Long id) {
        ProcedureRequestedDTO procedureRequestedById = procedureRequestedService.getOtherRequestedProcedureById(id);
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, procedureRequestedById)).build();
    }

    @GET
    @Path("get-all-procedures-requested-by-visit-id/{id}")
    @Operation(summary = "Get all procedures requested on visit", description = "Get all procedure requested")
    @APIResponse(
            description = "Successful",
            responseCode = "200",
            content = @Content(schema = @Schema(implementation = ProcedureRequestedDTO.class))
    )
    public Response getAllProceduresRequestedByVisitId(@PathParam("id") Long visitId) {
        List<ProcedureRequestedDTO> procedureRequestedByVisitId = procedureRequestedService.getRequestedProceduresByVisitId(visitId);
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, procedureRequestedByVisitId)).build();
    }

    @PUT
    @Path("update-missing-procedure-references")
    @Transactional
    @Operation(summary = "Update missing procedure references", description = "Finds ProcedureRequested records where procedure is null and updates them using procedureId")
    @APIResponse(description = "Successful", responseCode = "200")
    public Response updateMissingProcedureReferences() {
        return procedureRequestedService.updateMissingProcedureReferences();
    }









}






