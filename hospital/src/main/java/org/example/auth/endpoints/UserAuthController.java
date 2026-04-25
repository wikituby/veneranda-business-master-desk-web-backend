package org.example.auth.endpoints;

import io.vertx.core.http.HttpServerRequest;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.example.auth.services.UserAuthService;
import org.example.auth.services.payloads.*;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.user.domains.User;

@Path("auth")

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Auth Management Module", description = "Auth process")
public class UserAuthController {

    @HeaderParam("AuthToken")
    String remoteToken;

    @Inject
    UserAuthService userAuthService;


    @POST
    @Path("user-login")
    @Operation(summary = "Login", description = "Agent (shop) login")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = UserAuthResponse.class)))
    public Response login(UserAuthRequest request, @Context HttpServerRequest httpRequest) {
        UserAuthResponse response = userAuthService.login(request);
        return Response.ok(response).build();
    }

    @POST
    @Path("reset-link")
    @Operation(summary = "Send password reset link", description = "Send password reset link")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    public Response resend(ResetPasswordRequest request) {
        return userAuthService.sendResetPassword(request);
    }

    @PUT
    @Path("update-password/{id}")
    @Transactional
    //@RolesAllowed({"ADMIN","CUSTOMER","AGENT"})
    @Operation(summary = "Update user password", description = "Update user password")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = User.class)))
    public Response updatePassword(@PathParam("id") Long id, UpdatePasswordRequest request){
        return Response.ok(new ResponseMessage(ActionMessages.UPDATED.label,userAuthService.updatePassword(id, request) )).build();
    }

    /*@POST
    @Path("reset-password")
    @Operation(summary = "Reset password ", description = "password reset")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    public Response reset(@QueryParam("token") String token, ForcePasswordUpdateRequest request) {
        return userAuthService.updatePassword(token, request);
    }*/


}






