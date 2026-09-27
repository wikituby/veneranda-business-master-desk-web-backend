package org.example.subscription.endpoints;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.configuration.security.JwtUtils;
import org.example.statics.StatusTypes;
import org.example.subscription.services.SubscriptionService;
import org.example.subscription.services.payloads.ActivateSubscriptionRequest;
import org.example.subscription.services.payloads.SubscriptionStatusDTO;
import org.example.user.domains.User;
import org.example.user.domains.repositories.UserRepository;

@Path("subscription")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Subscription Module", description = "Facility subscription activation")
public class SubscriptionController {

    @Inject
    SubscriptionService subscriptionService;

    @Inject
    JwtUtils jwtUtils;

    @Inject
    UserRepository userRepository;

    @GET
    @Path("status")
    @Operation(summary = "Get subscription status for logged-in user")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = SubscriptionStatusDTO.class)))
    public Response getStatus(@Context HttpHeaders headers) {
        Long userId = resolveUserId(headers);
        SubscriptionStatusDTO status = subscriptionService.getStatusForUser(userId);
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, status)).build();
    }

    @POST
    @Path("activate")
    @Transactional
    @Operation(summary = "Activate subscription with a token")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = SubscriptionStatusDTO.class)))
    public Response activate(ActivateSubscriptionRequest request, @Context HttpHeaders headers) {
        Long userId = resolveUserId(headers);
        SubscriptionStatusDTO status = subscriptionService.activate(userId, request != null ? request.token : null);
        return Response.ok(new ResponseMessage(StatusTypes.CREATED.label, status)).build();
    }

    @POST
    @Path("cancel")
    @Transactional
    @Operation(summary = "Cancel the active facility subscription")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = SubscriptionStatusDTO.class)))
    public Response cancel(@Context HttpHeaders headers) {
        Long userId = resolveUserId(headers);
        SubscriptionStatusDTO status = subscriptionService.cancel(userId);
        return Response.ok(new ResponseMessage(StatusTypes.UPDATED_SUCCESSFULLY.label, status)).build();
    }

    private Long resolveUserId(HttpHeaders headers) {
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
        return user.id;
    }
}
