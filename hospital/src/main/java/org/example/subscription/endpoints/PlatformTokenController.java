package org.example.subscription.endpoints;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.statics.StatusTypes;
import org.example.subscription.services.ActivationTokenService;
import org.example.subscription.services.payloads.CreateActivationTokenRequest;

@Path("platform/activation-tokens")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Platform Token Generator", description = "Vendor tools for offline subscription token generation")
public class PlatformTokenController {

    @Inject
    ActivationTokenService activationTokenService;

    @GET
    @Operation(summary = "List all activation tokens")
    public Response list(@HeaderParam("X-Vendor-Key") String vendorKey) {
        activationTokenService.assertVendorKey(vendorKey);
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label,
                activationTokenService.listAll())).build();
    }

    @POST
    @Transactional
    @Operation(summary = "Generate a new activation token from days paid")
    public Response create(
            @HeaderParam("X-Vendor-Key") String vendorKey,
            CreateActivationTokenRequest request) {
        activationTokenService.assertVendorKey(vendorKey);
        return Response.ok(new ResponseMessage(StatusTypes.CREATED.label,
                activationTokenService.create(request))).build();
    }
}
