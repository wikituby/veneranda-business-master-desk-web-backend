package org.example.subscription.mobilemoney;

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
import org.example.subscription.mobilemoney.payloads.MobileMoneyPayRequest;
import org.example.subscription.mobilemoney.payloads.MobileMoneyPaymentStatusDTO;
import org.example.subscription.mobilemoney.payloads.MobileMoneySettingsRequest;
import org.example.user.domains.User;
import org.example.user.domains.repositories.UserRepository;

@Path("subscription/mobile-money")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Mobile Money", description = "MTN MoMo and Airtel Money collection (request-to-pay)")
public class MobileMoneyController {

    @Inject
    MobileMoneyService mobileMoneyService;

    @Inject
    MobileMoneySettingsService settingsService;

    @Inject
    JwtUtils jwtUtils;

    @Inject
    UserRepository userRepository;

    @GET
    @Path("providers")
    @Operation(summary = "Which mobile money providers are configured")
    public Response providers() {
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, mobileMoneyService.providersInfo())).build();
    }

    @GET
    @Path("settings")
    @Operation(summary = "Get mobile money API keys (MD/admin)")
    public Response getSettings(@Context HttpHeaders headers) {
        User user = resolveUser(headers);
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, settingsService.getSettings(user))).build();
    }

    @PUT
    @Path("settings")
    @Operation(summary = "Save mobile money API keys to database (MD/admin)")
    public Response saveSettings(MobileMoneySettingsRequest request, @Context HttpHeaders headers) {
        User user = resolveUser(headers);
        return Response.ok(new ResponseMessage(StatusTypes.UPDATED_SUCCESSFULLY.label,
                settingsService.saveSettings(user, request))).build();
    }

    @POST
    @Path("pay")
    @Operation(summary = "Start MTN or Airtel request-to-pay (PIN prompt on phone)")
    public Response pay(MobileMoneyPayRequest request) {
        try {
            MobileMoneyPaymentStatusDTO status = mobileMoneyService.initiate(request);
            return Response.ok(new ResponseMessage(StatusTypes.CREATED.label, status)).build();
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity(new ResponseMessage(e.getMessage(), null))
                            .build());
        } catch (IllegalStateException e) {
            throw new WebApplicationException(
                    Response.status(Response.Status.BAD_GATEWAY)
                            .entity(new ResponseMessage(e.getMessage(), null))
                            .build());
        }
    }

    @GET
    @Path("status/{referenceId}")
    @Operation(summary = "Poll payment status after customer enters PIN")
    public Response status(@PathParam("referenceId") String referenceId) {
        MobileMoneyPaymentStatusDTO status = mobileMoneyService.getStatus(referenceId);
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, status)).build();
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
