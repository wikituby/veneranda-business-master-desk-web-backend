package org.example.cafeteria.sales.saleDay.endpoints;


import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.example.cafeteria.sales.saleDay.services.SaleDayService;
import org.example.cafeteria.sales.saleDay.services.payloads.requests.SaleDayRequest;
import org.example.cafeteria.sales.saleDay.services.payloads.requests.SaleDayStatusUpdateRequest;
import org.example.cafeteria.sales.saleDay.services.payloads.requests.SaleDayUpdateRequest;
import org.example.cafeteria.sales.saleDay.services.payloads.responses.SaleDayDTO;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.statics.StatusTypes;

import java.util.List;

@Path("Cafeteria-sale-day-management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Cafeteria sale day Management Module", description = "Cafeteria sale day Management")

public class SaleDayController {

    @Inject
    SaleDayService saleDayService;


    @POST
    @Path("create-new-sale-day/{id}")
    @Transactional
    @Operation(summary = "new-sale-day", description = "new-sale-day")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = SaleDayDTO.class)))
    public Response createPatientVisit(@PathParam("id") Long id, SaleDayRequest request){
        return Response.ok(new ResponseMessage(StatusTypes.VISIT_CREATED_SUCCESSFULLY.label, saleDayService.createNewPatientVisit(id, request) )).build();
    }

    @GET
    @Transactional
    @Path("/get-all-sale-day")
    @Operation(summary = "Get all sale-day", description = "Retrieve a list of all sale-day")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = SaleDayDTO.class, type = SchemaType.ARRAY)))
    public Response getAllPatients() {
        List<SaleDayDTO> patientVisitList = saleDayService.getAllPatients();
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, patientVisitList)).build();
    }

    @PUT
    @Path("update-sale-day/{id}")
    //@RolesAllowed({"ADMIN","CUSTOMER"})
    @Transactional
    @Operation(summary = "Update sale-day", description = "Update sale-day")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = SaleDayDTO.class)))
    public Response updatePatientVisit(@PathParam("id") Long id, SaleDayUpdateRequest request){
        return Response.ok(new ResponseMessage(ActionMessages.UPDATED.label, saleDayService.updatePatientVisitById(id, request) )).build();
    }

    @PUT
    @Path("update-sale-day-status/{id}")
    //@RolesAllowed({"ADMIN","CUSTOMER"})
    @Transactional
    @Operation(summary = "Update sale-day status", description = "Update sale-day status")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = SaleDayDTO.class)))
    public Response updatePatientVisitStatus(@PathParam("id") Long id, SaleDayStatusUpdateRequest request){
        return Response.ok(new ResponseMessage(ActionMessages.UPDATED.label, saleDayService.updatePatientVisitStatusById(id, request) )).build();
    }

    @GET
    @Path("get-sale-day-List-by-id/{id}")
    @Operation(summary = "Get sale-day where patient id", description = "Get sale-day where patient id")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = SaleDayDTO.class)))
    public Response getPatientVisitListById(@PathParam("id") Long patientId) {
        // Call the service method to get a list of InitialTriageVitalsDTO for the given visitId
        List<SaleDayDTO> patientVisitList = saleDayService.getVisitByPatientId(patientId);

        // Return a successful response with the list of DTOs
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, patientVisitList)).build();
    }


    @GET
    @Path("get-latest-sale-day-by-patient-id/{id}")
    @Operation(summary = "Get the latest sale-day by patient id", description = "Retrieve the most recent sale-day by patient id")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = SaleDayDTO.class)))
    public Response getLatestPatientVisitByPatientId(@PathParam("id") Long patientId) {

        return saleDayService.getLatestVisitByPatientId(patientId);
    }


}






