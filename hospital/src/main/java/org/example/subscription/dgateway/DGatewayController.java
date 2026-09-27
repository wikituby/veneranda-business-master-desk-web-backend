package org.example.subscription.dgateway;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.configuration.security.JwtUtils;
import org.example.statics.StatusTypes;
import org.example.subscription.dgateway.payloads.DGatewayCollectRequest;
import org.example.subscription.dgateway.payloads.DGatewayPaymentStatusDTO;
import org.example.subscription.dgateway.payloads.DGatewaySettingsRequest;
import org.example.user.domains.User;
import org.example.user.domains.repositories.UserRepository;

@Path("subscription/dgateway")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "DGateway", description = "Unified East Africa payments via DGateway")
public class DGatewayController {

    @Inject
    DGatewayService dGatewayService;

    @Inject
    DGatewaySettingsService settingsService;

    @Inject
    JwtUtils jwtUtils;

    @Inject
    UserRepository userRepository;

    @GET
    @Path("providers")
    @Operation(summary = "DGateway configuration status")
    public Response providers() {
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, dGatewayService.providersInfo())).build();
    }

    @GET
    @Path("settings")
    @Operation(summary = "Get DGateway settings (MD/admin)")
    public Response getSettings(@Context HttpHeaders headers) {
        User user = resolveUser(headers);
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, settingsService.getSettings(user))).build();
    }

    @PUT
    @Path("settings")
    @Operation(summary = "Save DGateway API key and webhook secret")
    public Response saveSettings(DGatewaySettingsRequest request, @Context HttpHeaders headers) {
        User user = resolveUser(headers);
        return Response.ok(new ResponseMessage(StatusTypes.UPDATED_SUCCESSFULLY.label,
                settingsService.saveSettings(user, request))).build();
    }

    @POST
    @Path("pay")
    @Operation(summary = "Start DGateway collect (mobile money or card)")
    public Response pay(DGatewayCollectRequest request) {
        try {
            DGatewayPaymentStatusDTO status = dGatewayService.initiate(request);
            return Response.ok(new ResponseMessage(StatusTypes.CREATED.label, status)).build();
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage(e.getMessage(), null)).build());
        } catch (IllegalStateException e) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_GATEWAY)
                    .entity(new ResponseMessage(e.getMessage(), null)).build());
        }
    }

    @GET
    @Path("status/{referenceId}")
    @Operation(summary = "Poll DGateway payment status")
    public Response status(@PathParam("referenceId") String referenceId) {
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label,
                dGatewayService.getStatus(referenceId))).build();
    }

    private User resolveUser(HttpHeaders headers) {
        String auth = headers.getHeaderString("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new WebApplicationException("Unauthorized", 401);
        }
        String jwt = auth.substring("Bearer ".length()).trim();
        String email = jwtUtils.getUserNameFromJwtToken(jwt);
        User user = userRepository.getUserByEmail(email);
        if (user == null) {
            throw new WebApplicationException("User not found", 404);
        }
        return user;
    }
}