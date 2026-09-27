package org.example.subscription.dgateway;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("webhooks/dgateway")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "DGateway Webhooks", description = "Inbound DGateway payment webhooks")
public class DGatewayWebhookController {

    @Inject
    DGatewayService dGatewayService;

    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN, MediaType.WILDCARD})
    @Operation(summary = "Receive DGateway webhook (HMAC verified)")
    public Response receive(
            InputStream bodyStream,
            @HeaderParam("X-DGateway-Signature") String signature) {
        try {
            byte[] bytes = bodyStream == null ? new byte[0] : bodyStream.readAllBytes();
            String rawBody = new String(bytes, StandardCharsets.UTF_8);
            dGatewayService.applyWebhook(rawBody, signature);
            return Response.ok(Map.of("ok", true)).build();
        } catch (SecurityException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("ok", false, "message", e.getMessage()))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("ok", false, "message", e.getMessage() != null ? e.getMessage() : "bad webhook"))
                    .build();
        }
    }
}