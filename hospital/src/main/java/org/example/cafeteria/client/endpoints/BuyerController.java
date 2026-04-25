package org.example.cafeteria.client.endpoints;

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
import org.example.cafeteria.client.domains.Buyer;
import org.example.cafeteria.client.services.BuyerService;
import org.example.cafeteria.client.services.payloads.requests.BuyerRequest;
import org.example.cafeteria.client.services.payloads.requests.BuyerUpdateRequest;
import org.example.cafeteria.client.services.payloads.responses.dtos.PatientDTO;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;

import java.util.List;


@Path("cafeteria-buyer-management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "cafeteria buyer Management Module", description = "cafeteria buyer Management")

public class BuyerController {

    @Inject
    BuyerService buyerService;



    @POST
    @Path("create-new-buyer")
    @Transactional
    @Operation(summary = "Create New buyer", description = "Creates a new buyer and returns the patient's details.")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = PatientDTO.class)))
    public Response createNewPatient(BuyerRequest request) {
        return buyerService.createNewPatient(request);
    }

    @POST
    @Path("create-multiple-buyers")
    @Transactional
    @Operation(summary = "Create Multiple Patients", description = "Creates multiple patients and returns the list of created patients.")
    @APIResponse(
            description = "Patients created successfully",
            responseCode = "201",
            content = @Content(schema = @Schema(implementation = PatientDTO.class))
    )
    public Response createMultiplePatients(List<BuyerRequest> requests) {
        return buyerService.createMultiplePatients(requests);
    }



    @GET
    @Transactional
    @Path("/get-buyer-with-max-number")
    @Operation(summary = "get patient with max patient number", description = "get patient with max patient number")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = Buyer.class,type = SchemaType.ARRAY)))
    public Response getAllUsers(){
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, buyerService.findMaxPatientNo())).build();
    }

    @DELETE
    @Path("/delete-buyer-by-id/{id}")
    //@RolesAllowed({"ADMIN"})
    @Transactional
    @Operation(summary = "delete buyer by id", description = "delete patient by id")
    @APIResponse(description = "Successful", responseCode = "200")
    public Response deleteUserById(@PathParam("id") Long id){
        return buyerService.deletePatientById(id);
    }

   /* @GET
    @Transactional
    @Path("/get-all-patients")
    // @RolesAllowed({"ADMIN"})
    @Operation(summary = "get all patients", description = "get all  patients")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = PatientResponse.class,type = SchemaType.ARRAY)))
    public Response getAllPatients(){
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label,patientService.getAllPatients())).build();
    }*/

    @GET
    @Transactional
    @Path("/get-all-buyers-with-debts")
    // @RolesAllowed({"ADMIN"})
    @Operation(summary = "Get all patients with debts", description = "Retrieve a list of all patients with debts")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = PatientDTO.class, type = SchemaType.ARRAY)))
    public Response getAllPatientsWithDebts() {
        List<PatientDTO> patientList = buyerService.getAllPatientsWithDebt();
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, patientList)).build();
    }


    @GET
    @Transactional
    @Path("/get-all-buyers-in-group/{id}")
    // @RolesAllowed({"ADMIN"})
    @Operation(summary = "Get all patients in a group", description = "Retrieve a list of all patients in a group")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = PatientDTO.class, type = SchemaType.ARRAY)))
    public Response getAllPatientsWithDebts(@PathParam("id") Long id) {
        List<PatientDTO> patientListInAgroup = buyerService.getAllPatientsByGroupId(id);
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, patientListInAgroup)).build();
    }

    @GET
    @Transactional
    @Path("/get-all-buyers")
    // @RolesAllowed({"ADMIN"})
    @Operation(summary = "Get all patients", description = "Retrieve a list of all patients")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = PatientDTO.class, type = SchemaType.ARRAY)))
    public Response getAllPatients() {
        List<PatientDTO> allPatientList = buyerService.getAllPatients();
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, allPatientList)).build();
    }



    @PUT
    @Path("update-buyer/{id}")
    //@RolesAllowed({"ADMIN","CUSTOMER"})
    @Transactional
    @Operation(summary = "Update patient", description = "Update patient")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = PatientDTO.class)))
    public Response updatePatient(@PathParam("id") Long id, BuyerUpdateRequest request){
        return Response.ok(new ResponseMessage(ActionMessages.PATIENT_UPDATED_SUCCESSFULLY.label, buyerService.updatePatientById(id, request) )).build();
    }

    /*@PUT
    @Path("update-patient/{id}")
    //@RolesAllowed({"ADMIN","CUSTOMER"})
    @Transactional
    @Operation(summary = "Update patient", description = "Update patient")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = Patient.class)))
    public Response updatePatient(@PathParam("id") Long id, PatientUpdateRequest request){
        return Response.ok(new ResponseMessage(ActionMessages.UPDATED.label,patientService.updatePatientById(id, request) )).build();
    }

        @GET
    @Path("get-patient/{id}")
    //@RolesAllowed({"ADMIN"})
    @Operation(summary = "Get patient", description = "Get patient")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = Patient.class)))
    public Response getPatientById(@PathParam("id") Long id){
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label,patientService.getPatientById(id) )).build();
    }
    */

    @GET
    @Path("get-buyer/{id}")
    //@RolesAllowed({"ADMIN"})
    @Operation(summary = "Get patient", description = "Get patient")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = PatientDTO.class)))
    public Response getBuyerById(@PathParam("id") Long id) {
        PatientDTO patient = buyerService.getPatientById(id);
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, patient)).build();
    }







    /*

    @GET
    @Path("get-user/{id}")
    //@RolesAllowed({"ADMIN"})
    @Operation(summary = "Get customer or agent by Id", description = "Get customer or agent by Id")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = User.class)))
    public Response getById(@PathParam("id") Long id){
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label,userService.getById(id) )).build();
    }

    @PUT
    @Path("update-user/{id}")
    //@RolesAllowed({"ADMIN","CUSTOMER"})
    @Transactional
    @Operation(summary = "Update customer or agent by Id", description = "Update customer or agent by Id")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = User.class)))
    public Response update(@PathParam("id") Long id, UpdateRequest request){
        return Response.ok(new ResponseMessage(ActionMessages.UPDATED.label,userService.updateUserById(request, id) )).build();
    }


    @GET
    @Transactional
    @Path("/get-all-users")
    // @RolesAllowed({"ADMIN"})
    @Operation(summary = "get all Users customers and agents", description = "get all Users customers and agents")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = User.class,type = SchemaType.ARRAY)))
    public Response getAllUsers(){
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label,userService.getAllUsers())).build();
    }

    @GET
    @Transactional
    @Path("/get-all-customers")
    @RolesAllowed({"ADMIN","AGENT"})
    @Operation(summary = "get all customers ", description = "get all customers")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = User.class,type = SchemaType.ARRAY)))
    public Response getAllCustomers(){
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label,userRepository.getAllCustomers())).build();
    }

    @DELETE
    @Path("{id}")
    @RolesAllowed({"ADMIN"})
    @Transactional
    @Operation(summary = "delete customer or agent by id", description = "delete customer or agent by id")
    @APIResponse(description = "Successful", responseCode = "200")
    public Response deleteUserById(@PathParam("id") Long id){
        return userService.deleteUserById(id);
    }


    @DELETE
    @Transactional
    @RolesAllowed({"ADMIN"})
    @Operation(summary = "delete all customers and agents", description = "delete all customers and agents.")
    @APIResponse(description = "Successful", responseCode = "200")
    public Response deleteAllItems(){
        userService.deleteAllUsers();
        return Response.ok(new ResponseMessage(ActionMessages.DELETED.label)).build();

    }

    ///// agent endpoints///////////////////////////////////////////////////////////


    @POST
    @Path("agent-signup")
    @RolesAllowed({"ADMIN"})
    @Transactional
    @Operation(summary = "Agent Signup", description = "Agent Signup")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = User.class)))
    public Response createAgentUser(AgentUserRequest request){
        return Response.ok(new ResponseMessage(StatusTypes.CREATED.label,userService.createNewAgentUser(request) )).build();
    }

    @GET
    @Transactional
    @Path("/get-all-agents")
    @RolesAllowed({"ADMIN"})
    @Operation(summary = "get all agents ", description = "get all agents")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = User.class,type = SchemaType.ARRAY)))
    public Response getAllAgents(){
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label,userRepository.getAllAgents())).build();
    }


    @PUT
    @Path("update-agent-role/{id}")
    @Transactional
    @RolesAllowed({"ADMIN"})
    @Operation(summary = "Update Agent Role by Id", description = "Update support Agent Role by Id")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = User.class)))
    public Response updateRole(@PathParam("id") Long id, UpdateAgentRole request){
        return Response.ok(new ResponseMessage(ActionMessages.UPDATED.label,userService.updateAgentRole(id, request) )).build();
    }

    @GET
    @Transactional
    @Path("/get-all-admins")
    @RolesAllowed({"ADMIN"})
    @Operation(summary = "get all admins ", description = "get all admins")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = User.class,type = SchemaType.ARRAY)))
    public Response getAllAdmins(){
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label,userRepository.getAllAdmins())).build();
    }

    /////end points for roles//////////////////////////////////////////////////////////////////////////


    @GET
    @Path("get-all-roles")
    @RolesAllowed({"ADMIN"})
    @Operation(summary = "get all roles", description = "get all roles")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = RoleResponse.class)))
    public Response role() {
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, userService.getAllRoles())).build();

    }*/
}









