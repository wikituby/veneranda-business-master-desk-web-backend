package org.example.client.endpoints;

import jakarta.annotation.security.RolesAllowed;
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
import org.example.client.services.payloads.responses.dtos.PatientDTO;
import org.example.client.services.payloads.requests.PatientRequest;
import org.example.client.services.PatientService;
import org.example.client.services.payloads.requests.PatientUpdateRequest;
import org.example.client.domains.Patient;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;

import java.util.List;


@Path("Patient-management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Patient Management Module - Manage Patient Date", description = "Patient Management")

public class PatientController {

    @Inject
    PatientService patientService;



    @POST
    @Path("create-new-patient")
    //@RolesAllowed({"admin", "doctor","md"})
    @Transactional
    @Operation(summary = "Create New Patient",
            description = "Creates a new patient. Requires DOCTOR or ADMIN role.")
    @APIResponse(responseCode = "200", description = "Patient created successfully")
    @APIResponse(responseCode = "401", description = "Unauthorized")
    @APIResponse(responseCode = "403", description = "Forbidden (missing required role)")public Response createPatient(PatientRequest request) {
        return patientService.createNewPatient(request);
    }

    @POST
    @Path("create-multiple-patients")
    @Transactional
    @Operation(summary = "Create Multiple Patients", description = "Creates multiple patients and returns the list of created patients.")
    @APIResponse(
            description = "Patients created successfully",
            responseCode = "201",
            content = @Content(schema = @Schema(implementation = PatientDTO.class))
    )
    public Response createMultiplePatients(List<PatientRequest> requests) {
        return patientService.createMultiplePatients(requests);
    }



    @GET
    @Transactional
    @Path("/get-patient-with-max-number")
    @Operation(summary = "get patient with max patient number", description = "get patient with max patient number")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = Patient.class,type = SchemaType.ARRAY)))
    public Response getAllUsers(){
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label,patientService.findMaxPatientNo())).build();
    }

    @DELETE
    @Path("/delete-patient-by-id/{id}")
    //@RolesAllowed({"ADMIN"})
    @Transactional
    @Operation(summary = "delete patient by id", description = "delete patient by id")
    @APIResponse(description = "Successful", responseCode = "200")
    public Response deleteUserById(@PathParam("id") Long id){
        return patientService.deletePatientById(id);
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
    @Path("/get-all-patients-with-debts")
    // @RolesAllowed({"ADMIN"})
    @Operation(summary = "Get all patients with debts", description = "Retrieve a list of all patients with debts")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = PatientDTO.class, type = SchemaType.ARRAY)))
    public Response getAllPatientsWithDebts() {
        List<PatientDTO> patientList = patientService.getAllPatientsWithDebt();
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, patientList)).build();
    }

    @GET
    @Transactional
    @Path("/get-all-patients-with-debts-and-compassion-group")
    // @RolesAllowed({"ADMIN"})
    @Operation(summary = "Get all patients with debts and from compassion", description = "Retrieve a list of all patients with debts and from compassion")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = PatientDTO.class, type = SchemaType.ARRAY)))
    public Response getAllPatientsWithDebtsAndFromCompassion() {
        List<PatientDTO> patientList = patientService.getAllPatientsWithDebtAndCompassion().patients();
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, patientList)).build();
    }


    @GET
    //@RolesAllowed({"ADMIN"})
    @Transactional
    @Path("compassion/invoice/generate-pdf/{id}")
    @Operation(summary = "invoice pdf for compassion", description = "invoice pdf download for compassion")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    public Response generateAndReturnInvoiceForCompassionPdf() {
        return patientService.generateAndReturnInvoicePdfForListOfCompassionPatients();
    }






    @GET
    @Transactional
    @Path("/get-all-patients-in-group/{id}")
    // @RolesAllowed({"ADMIN"})
    @Operation(summary = "Get all patients in a group", description = "Retrieve a list of all patients in a group")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = PatientDTO.class, type = SchemaType.ARRAY)))
    public Response getAllPatientsWithDebts(@PathParam("id") Long id) {
        List<PatientDTO> patientListInAgroup = patientService.getAllPatientsByGroupId(id);
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, patientListInAgroup)).build();
    }

    @GET
    @Transactional
    @Path("/get-all-patients")
    // @RolesAllowed({"ADMIN"})
    @Operation(summary = "Get all patients", description = "How to Retrieve a list of all patients")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = PatientDTO.class, type = SchemaType.ARRAY)))
    public Response getAllPatients() {
        List<PatientDTO> allPatientList = patientService.getAllPatients();
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, allPatientList)).build();
    }



    @PUT
    @Path("update-patient/{id}")
    //@RolesAllowed({"ADMIN","CUSTOMER"})
    @Transactional
    @Operation(summary = "Update patient", description = "Update patient")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = PatientDTO.class)))
    public Response updatePatient(@PathParam("id") Long id, PatientUpdateRequest request){
        return patientService.updatePatientById(id, request);
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
    @Path("get-patient/{id}")
    //@RolesAllowed({"ADMIN"})
    @Operation(summary = "Get patient", description = "Get patient")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = PatientDTO.class)))
    public Response getPatientById(@PathParam("id") Long id) {
        PatientDTO patient = patientService.getPatientById(id);
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









