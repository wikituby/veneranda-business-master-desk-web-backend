package org.example.assistant.endpoints;

import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.example.assistant.services.DoctorAssistantLlmService;
import org.example.configuration.handler.ResponseMessage;

@Path("Client-management/doctor-assistant")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Doctor Assistant", description = "Clinical copilot LLM proxy and tools")
public class DoctorAssistantController {

    @Inject
    DoctorAssistantLlmService llmService;

    @GET
    @Path("llm-config")
    @Operation(summary = "Doctor Assistant LLM availability (no secrets exposed)")
    public Response llmConfig() {
        return Response.ok(new ResponseMessage("OK", llmService.getPublicConfig())).build();
    }

    @POST
    @Path("llm-chat")
    @Operation(summary = "Proxy chat/completions for Doctor Assistant agent (OpenAI-compatible)")
    public Response llmChat(JsonObject body) {
        try {
            String apiKey = body.containsKey("apiKey") && !body.isNull("apiKey") ? body.getString("apiKey") : null;
            JsonObject result = llmService.chat(body, apiKey);
            return Response.ok(new ResponseMessage("OK", result)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage(e.getMessage(), null))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_GATEWAY)
                    .entity(new ResponseMessage("LLM proxy failed: " + e.getMessage(), null))
                    .build();
        }
    }
}
