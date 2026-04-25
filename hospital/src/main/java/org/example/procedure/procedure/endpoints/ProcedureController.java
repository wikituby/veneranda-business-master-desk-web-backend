package org.example.procedure.procedure.endpoints;

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
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.procedure.procedure.services.payloads.requests.*;
import org.example.procedure.procedure.services.ProcedureService;
import org.example.procedure.procedure.services.payloads.responses.dtos.ProcedureDTO;

import java.util.List;

@Path("Patient-management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Patient Management Module", description = "Patient Management")

public class ProcedureController {

    @Inject
    ProcedureService procedureService;

    @POST
    @Path("create-new-procedure")
    @Transactional
    @Operation(summary = "new-procedure", description = "new-procedure")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ProcedureDTO.class)))
    public Response createNewProcedure(ProcedureRequest request){
        return procedureService.createNewProcedure(request);
    }

    /*@POST
    @Path("create-new-service-category")
    @Transactional
    @Operation(summary = "new-category", description = "new-category")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ProcedureCategoryDTO.class)))
    public Response createNewProcedureCategory(ProcedureCategoryRequest request){
        return procedureService.createNewProcedureCategory(request);
    }*/




   /*  @GET
    @Transactional
    @Path("/get-all-service-categories")
    // @RolesAllowed({"ADMIN"})
    @Operation(summary = "Get all categories", description = "Retrieve a list of all categories")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ProcedureCategoryDTO.class, type = SchemaType.ARRAY)))
    public Response getAllProceduresCategories() {
        List<ProcedureCategoryDTO> procedureCategoryList = procedureService.getAllProceduresCategories();
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, procedureCategoryList)).build();
    }*/

 

    /*@PUT
    @Path("update-service-category/{id}")
    //@RolesAllowed({"ADMIN","CUSTOMER"})
    @Transactional
    @Operation(summary = "Update category", description = "Update category")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ProcedureCategoryDTO.class)))
    public Response updateServiceCategory(@PathParam("id") Long id, ProcedureCategoryUpdateRequest request){
        return Response.ok(new ResponseMessage(ActionMessages.UPDATED.label,procedureService.updateServiceCategoryById(id, request) )).build();
    }*/


 

    @POST
    @Path("create-bulk-procedures")
    @Transactional
    @Operation(summary = "Create Bulk Procedures",description = "Creates multiple new procedures in bulk. Skips duplicates based on category and procedureType.")
    @APIResponse(description = "Bulk procedure creation result",responseCode = "200",content = @Content(schema = @Schema(implementation = ResponseMessage.class)))
    @APIResponse(description = "Invalid request",responseCode = "400")
    public Response createBulkProcedures(List<ProcedureRequest> requests) {
        return procedureService.createBulkProcedures(requests);
    }


    @GET
    @Transactional
    @Path("/get-all-procedures")
    // @RolesAllowed({"ADMIN"})
    @Operation(summary = "Get all procedures", description = "Retrieve a list of all procedures")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ProcedureDTO.class, type = SchemaType.ARRAY)))
    public Response getAllProcedures() {
        List<ProcedureDTO> procedureList = procedureService.getAllProcedures();
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, procedureList)).build();
    }

    @GET
    @Path("get-all-labTest-procedures")
    //@RolesAllowed({"ADMIN","CUSTOMER"})
    @Transactional
    @Operation(summary = "get all labTest procedures", description = "get all labTest procedures")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ProcedureDTO.class)))
    public Response getAllLabTestProcedures(){
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label,procedureService.getLabTestProcedures() )).build();
    }

    @GET
    @Path("get-all-scan-procedures")
    //@RolesAllowed({"ADMIN","CUSTOMER"})
    @Transactional
    @Operation(summary = "get all scan procedures", description = "get all scan procedures")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ProcedureDTO.class)))
    public Response getAllScanProcedures(){
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label,procedureService.getScanProcedures() )).build();
    }


    @GET
    @Path("get-Other-Procedures")
    //@RolesAllowed({"ADMIN","CUSTOMER"})
    @Transactional
    @Operation(summary = "get Other Procedures", description = "get Other Procedures")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ProcedureDTO.class)))
    public Response getOtherProcedures(){
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label,procedureService.getOtherProcedures() )).build();
    }

    @DELETE
    @Path("delete-service/{id}")
    //@RolesAllowed({"ADMIN"})
    @Transactional
    @Operation(summary = "delete service by id ", description = "delete service by id.")
    @APIResponse(description = "Successful", responseCode = "200")
    public Response deleteServiceById(@PathParam("id") Long id){
        return procedureService.deleteServiceById(id);

    }

    @PUT
    @Path("update-service/{id}")
    //@RolesAllowed({"ADMIN","CUSTOMER"})
    @Transactional
    @Operation(summary = "Update service", description = "Update service")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ProcedureDTO.class)))
    public Response updateService(@PathParam("id") Long id, ProcedureUpdateRequest request){
        return Response.ok(new ResponseMessage(ActionMessages.UPDATED.label,procedureService.updateProcedureById(id, request) )).build();
    }



}






