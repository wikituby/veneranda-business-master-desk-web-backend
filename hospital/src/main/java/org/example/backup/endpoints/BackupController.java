package org.example.backup.endpoints;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.example.backup.services.BackupOrchestratorService;
import org.example.backup.services.BackupSettingsService;
import org.example.backup.services.payloads.BackupSettingsRequest;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.configuration.security.JwtUtils;
import org.example.statics.StatusTypes;
import org.example.user.domains.User;
import org.example.user.domains.repositories.UserRepository;

@Path("backup")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Backup Module", description = "Local and Dropbox database backup & restoration")
public class BackupController {

    @Inject
    BackupSettingsService backupSettingsService;

    @Inject
    BackupOrchestratorService backupOrchestratorService;

    @Inject
    JwtUtils jwtUtils;

    @Inject
    UserRepository userRepository;

    @GET
    @Path("settings")
    @Operation(summary = "Get backup settings")
    public Response getSettings(@Context HttpHeaders headers) {
        User user = resolveUser(headers);
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label,
                backupSettingsService.getSettings(user))).build();
    }

    @PUT
    @Path("settings")
    @Operation(summary = "Save backup settings")
    public Response saveSettings(BackupSettingsRequest request, @Context HttpHeaders headers) {
        User user = resolveUser(headers);
        return Response.ok(new ResponseMessage(StatusTypes.UPDATED_SUCCESSFULLY.label,
                backupSettingsService.saveSettings(user, request))).build();
    }

    @POST
    @Path("run-now")
    @Operation(summary = "Run backup immediately")
    public Response runNow(@Context HttpHeaders headers) {
        User user = resolveUser(headers);
        backupSettingsService.assertCanManage(user);
        return Response.ok(new ResponseMessage(StatusTypes.CREATED.label,
                backupOrchestratorService.runBackupNow())).build();
    }

    @POST
    @Path("restore-now")
    @Operation(summary = "Restore database from local or Dropbox backup")
    public Response restoreNow(
            @QueryParam("source") String source,
            @QueryParam("path") String path,
            @Context HttpHeaders headers) {
        User user = resolveUser(headers);
        backupSettingsService.assertCanManage(user);
        return Response.ok(new ResponseMessage(StatusTypes.UPDATED_SUCCESSFULLY.label,
                backupOrchestratorService.restoreNow(source, path))).build();
    }

    @POST
    @Path("dropbox-logout")
    @Operation(summary = "Clear Dropbox tokens from settings")
    public Response dropboxLogout(@Context HttpHeaders headers) {
        User user = resolveUser(headers);
        return Response.ok(new ResponseMessage(StatusTypes.UPDATED_SUCCESSFULLY.label,
                backupSettingsService.logoutDropbox(user))).build();
    }

    private User resolveUser(HttpHeaders headers) {
        String auth = headers.getHeaderString("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new jakarta.ws.rs.WebApplicationException("Unauthorized", 401);
        }
        String jwt = auth.substring("Bearer ".length()).trim();
        String email = jwtUtils.getUserNameFromJwtToken(jwt);
        User user = userRepository.getUserByEmail(email);
        if (user == null) {
            throw new jakarta.ws.rs.WebApplicationException("User not found", 404);
        }
        return user;
    }
}
