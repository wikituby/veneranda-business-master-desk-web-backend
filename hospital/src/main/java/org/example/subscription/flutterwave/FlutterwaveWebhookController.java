package org.example.subscription.flutterwave;

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
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("webhooks/flutterwave")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Flutterwave Webhooks")
public class FlutterwaveWebhookController {

    @Inject
    FlutterwaveService flutterwaveService;

    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN, MediaType.WILDCARD})
    public Response receive(
            InputStream bodyStream,
            @HeaderParam("flutterwave-signature") String flutterwaveSignature,
            @HeaderParam("verif-hash") String verifHash) {
        try {
            byte[] bytes = bodyStream == null ? new byte[0] : bodyStream.readAllBytes();
            String rawBody = new String(bytes, StandardCharsets.UTF_8);
            flutterwaveService.applyWebhook(rawBody, flutterwaveSignature, verifHash);
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