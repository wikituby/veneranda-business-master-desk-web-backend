package org.example.subscription.flutterwave;

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
import org.example.subscription.flutterwave.payloads.FlutterwaveCollectRequest;
import org.example.subscription.flutterwave.payloads.FlutterwavePaymentStatusDTO;
import org.example.subscription.flutterwave.payloads.FlutterwaveSettingsRequest;
import org.example.user.domains.User;
import org.example.user.domains.repositories.UserRepository;

@Path("subscription/flutterwave")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Flutterwave", description = "Flutterwave Uganda mobile money collections")
public class FlutterwaveController {

    @Inject
    FlutterwaveService flutterwaveService;

    @Inject
    FlutterwaveSettingsService settingsService;

    @Inject
    JwtUtils jwtUtils;

    @Inject
    UserRepository userRepository;

    @GET
    @Path("providers")
    public Response providers() {
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, flutterwaveService.providersInfo())).build();
    }

    @GET
    @Path("settings")
    public Response getSettings(@Context HttpHeaders headers) {
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label,
                settingsService.getSettings(resolveUser(headers)))).build();
    }

    @PUT
    @Path("settings")
    public Response saveSettings(FlutterwaveSettingsRequest request, @Context HttpHeaders headers) {
        return Response.ok(new ResponseMessage(StatusTypes.UPDATED_SUCCESSFULLY.label,
                settingsService.saveSettings(resolveUser(headers), request))).build();
    }

    @POST
    @Path("pay")
    @Operation(summary = "Start Flutterwave Uganda MoMo charge")
    public Response pay(FlutterwaveCollectRequest request) {
        try {
            FlutterwavePaymentStatusDTO status = flutterwaveService.initiate(request);
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
    public Response status(@PathParam("referenceId") String referenceId) {
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label,
                flutterwaveService.getStatus(referenceId))).build();
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