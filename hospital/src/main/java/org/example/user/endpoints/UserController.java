package org.example.user.endpoints;

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
import org.example.auth.services.UserAuthService;
import org.example.auth.services.payloads.UpdatePasswordRequest;
import org.example.auth.services.payloads.RoleResponse;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.statics.StatusTypes;
import org.example.user.domains.User;
import org.example.user.domains.repositories.UserRepository;
import org.example.user.services.UserService;
import org.example.user.services.payLoads.requests.AgentUserRequest;
import org.example.user.services.payLoads.requests.UpdateAgentRole;
import org.example.user.services.payLoads.requests.UpdateProfilePicRequest;
import org.example.user.services.payLoads.requests.CreateRoleRequest;
import org.example.user.services.payLoads.requests.UpdateRequest;
import org.example.user.services.payLoads.requests.UpdateRoleNameRequest;
import org.example.user.services.payLoads.requests.UpdateUiPreferencesRequest;
import org.example.user.services.payLoads.requests.UserRequest;
import org.example.user.services.RoleService;

@Path("user-management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "User Management Module", description = "User Management")

public class UserController {

        @Inject
        UserService userService;

        @Inject
        UserRepository userRepository;

        @Inject
        UserAuthService userAuthService;

        @Inject
        RoleService roleService;

        @POST
        @Path("signup")
        @Transactional
        @Operation(summary = "Customer Signup", description = "Customer Signup")
        @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = User.class)))
        public Response createUser(UserRequest request){
            return Response.ok(new ResponseMessage(StatusTypes.CREATED.label,userService.createNewCustomerUser(request) )).build();
        }

        @POST
        @Path("create-user")
        @Transactional
        @Operation(summary = "Create user and return all users", description = "Create user and return all users")
        @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = User.class,type = SchemaType.ARRAY)))
        public Response createUserAndListAll(UserRequest request){
            return Response.ok(new ResponseMessage(StatusTypes.CREATED.label, userService.createNewCustomerUserAndListAll(request))).build();
        }

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

        @PUT
        @Path("update-profile-pic/{id}")
        @Transactional
        @Operation(summary = "Update user profile picture by Id", description = "Update user profile picture by Id")
        @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = User.class)))
        public Response updateProfilePic(@PathParam("id") Long id, UpdateProfilePicRequest request){
                return Response.ok(new ResponseMessage(ActionMessages.UPDATED.label,userService.updateProfilePicById(request, id))).build();
        }

        @PUT
        @Path("update-ui-preferences/{id}")
        @Transactional
        @Operation(
                summary = "Update user UI preferences",
                description = "Save locked UI defaults such as registry items-per-page")
        @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = User.class)))
        public Response updateUiPreferences(@PathParam("id") Long id, UpdateUiPreferencesRequest request) {
                Integer size = request == null ? null : request.registryItemsPerPage;
                return Response.ok(new ResponseMessage(
                        ActionMessages.UPDATED.label,
                        userService.updateRegistryItemsPerPage(id, size))).build();
        }

        @PUT
        @Path("update-password/{id}")
        @Transactional
        //@RolesAllowed({"ADMIN","CUSTOMER","AGENT"})
        @Operation(summary = "Update user password by Id", description = "Update user password by Id")
        @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = User.class)))
        public Response updatePassword(@PathParam("id") Long id, UpdatePasswordRequest request){
                return Response.ok(new ResponseMessage(ActionMessages.UPDATED.label,userAuthService.updatePassword(id, request))).build();
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
        //@RolesAllowed({"ADMIN","AGENT"})
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
        @Transactional
        @Operation(summary = "get all roles", description = "get all roles")
        @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = RoleResponse.class)))
        public Response role() {
                return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, userService.getAllRoles())).build();
        }

        @POST
        @Path("create-role")
        @Transactional
        @Operation(summary = "Create role", description = "Create a new role")
        @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = RoleResponse.class)))
        public Response createRole(CreateRoleRequest request) {
                return Response.ok(new ResponseMessage(
                        StatusTypes.CREATED.label,
                        roleService.createRole(request != null ? request.name : null)
                )).build();
        }

        @PUT
        @Path("update-role")
        @Transactional
        @Operation(summary = "Update role", description = "Rename a role and update users assigned to it")
        @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = RoleResponse.class)))
        public Response updateRole(UpdateRoleNameRequest request) {
                String oldName = request != null ? request.oldName : null;
                String newName = request != null ? request.newName : null;
                return Response.ok(new ResponseMessage(
                        ActionMessages.UPDATED.label,
                        roleService.updateRole(oldName, newName)
                )).build();
        }

        @DELETE
        @Path("delete-role/{role}")
        @Transactional
        @Operation(summary = "Delete role", description = "Delete a role when no users are assigned to it")
        @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = RoleResponse.class)))
        public Response deleteRole(@PathParam("role") String role) {
                return Response.ok(new ResponseMessage(
                        ActionMessages.DELETED.label,
                        roleService.deleteRole(role)
                )).build();
        }
    }








