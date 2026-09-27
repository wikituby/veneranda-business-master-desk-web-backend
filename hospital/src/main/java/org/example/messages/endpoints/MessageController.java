package org.example.messages.endpoints;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.example.messages.services.MessageService;
import org.example.messages.services.payloads.requests.CreateConversationRequest;
import org.example.messages.services.payloads.requests.SendMessageRequest;
import org.example.messages.services.payloads.responses.ConversationDTO;
import org.example.messages.services.payloads.responses.MessageDTO;
import org.example.messages.services.payloads.responses.UnreadCountDTO;
import io.quarkus.security.Authenticated;


@Path("messages")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Messages Module", description = "User-to-User Messaging")
public class MessageController {

    @Inject
    MessageService messageService;

    @POST

    @Authenticated
    @Path("/conversations")
    @Operation(summary = "Create a new conversation", description = "Create a new conversation with another user")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ConversationDTO.class)))
    public Response createConversation(CreateConversationRequest request) {
        return messageService.createConversation(request);
    }

    @GET
    @Authenticated
    @Path("/conversations")
    @Operation(summary = "Get all conversations", description = "Get all conversations for the current user")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ConversationDTO.class, type = org.eclipse.microprofile.openapi.annotations.enums.SchemaType.ARRAY)))
    public Response getAllConversations() {
        return messageService.getAllConversations();
    }

    @GET
    @Authenticated
    @Path("/unread-count")
    @Operation(summary = "Get unread message count", description = "Lightweight total unread count for the header badge")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = UnreadCountDTO.class)))
    public Response getUnreadCount() {
        return messageService.getUnreadCount();
    }

    @GET
    @Authenticated
    @Path("/conversations/{id}/messages")
    @Operation(summary = "Get messages for a conversation", description = "Get all messages in a conversation")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = MessageDTO.class, type = org.eclipse.microprofile.openapi.annotations.enums.SchemaType.ARRAY)))
    public Response getConversationMessages(@PathParam("id") Long conversationId) {
        return messageService.getConversationMessages(conversationId);
    }

    @POST
    @Authenticated
    @Path("/conversations/{id}/messages")
    @Operation(summary = "Send a message", description = "Send a message in a conversation")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = MessageDTO.class)))
    public Response sendMessage(@PathParam("id") Long conversationId, SendMessageRequest request) {
        return messageService.sendMessage(conversationId, request);
    }

    @GET
    @Authenticated
    @Path("/users")
    @Operation(summary = "Get available users", description = "Get all users available for messaging")
    @APIResponse(description = "Successful", responseCode = "200")
    public Response getAvailableUsers() {
        return messageService.getAvailableUsers();
    }

    @PUT
    @Authenticated
    @Path("/conversations/{id}/read")
    @Operation(summary = "Mark conversation as read", description = "Mark all messages in a conversation as read for the current user")
    public Response markConversationAsRead(@PathParam("id") Long conversationId) {
        return messageService.markConversationAsRead(conversationId);
    }
}







