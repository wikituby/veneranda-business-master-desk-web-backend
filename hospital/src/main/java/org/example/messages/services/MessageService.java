package org.example.messages.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.example.configuration.handler.ResponseMessage;
import org.example.messages.domains.Conversation;
import org.example.messages.domains.ConversationRepository;
import org.example.messages.domains.Message;
import org.example.messages.domains.MessageRepository;
import org.example.messages.services.payloads.requests.CreateConversationRequest;
import org.example.messages.services.payloads.requests.SendMessageRequest;
import org.example.messages.services.payloads.responses.ConversationDTO;
import org.example.messages.services.payloads.responses.MessageDTO;
import org.example.user.domains.User;
import org.example.user.domains.repositories.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class MessageService {

    @Inject
    ConversationRepository conversationRepository;

    @Inject
    MessageRepository messageRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    JsonWebToken jwt;

    private User getCurrentUser() {
        String username = jwt.getClaim("upn");
        if (username == null) {
            throw new WebApplicationException("User not authenticated", Response.Status.UNAUTHORIZED);
        }
        return userRepository.findByUsernameOptional(username)
                .orElseThrow(() -> new WebApplicationException("User not found", Response.Status.NOT_FOUND));
    }

    @Transactional
    public Response createConversation(CreateConversationRequest request) {
        User currentUser = getCurrentUser();
        User recipient = userRepository.findByIdOptional(request.recipientId)
                .orElseThrow(() -> new WebApplicationException("Recipient not found", Response.Status.NOT_FOUND));

        if (currentUser.id.equals(recipient.id)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Cannot create conversation with yourself", null))
                    .build();
        }

        // Check if conversation already exists
        Conversation existing = conversationRepository.findByUsers(currentUser, recipient);
        if (existing != null) {
            return Response.ok(new ResponseMessage("Conversation already exists", toConversationDTO(existing, currentUser))).build();
        }

        Conversation conversation = new Conversation();
        conversation.user1 = currentUser;
        conversation.user2 = recipient;
        conversation.lastMessageTime = LocalDateTime.now();
        conversation.lastMessageText = "";
        
        // Persist to database
        conversationRepository.persist(conversation);

        return Response.ok(new ResponseMessage("Conversation created", toConversationDTO(conversation, currentUser))).build();
    }

    public Response getAllConversations() {
        User currentUser = getCurrentUser();
        List<Conversation> conversations = conversationRepository.findByUser(currentUser);
        
        List<ConversationDTO> dtos = conversations.stream()
                .map(conv -> toConversationDTO(conv, currentUser))
                .collect(Collectors.toList());

        return Response.ok(new ResponseMessage("Conversations fetched", dtos)).build();
    }

    public Response getConversationMessages(Long conversationId) {
        User currentUser = getCurrentUser();
        Conversation conversation = conversationRepository.findByIdOptional(conversationId)
                .orElseThrow(() -> new WebApplicationException("Conversation not found", Response.Status.NOT_FOUND));

        // Verify user is part of conversation
        if (!conversation.user1.id.equals(currentUser.id) && !conversation.user2.id.equals(currentUser.id)) {
            throw new WebApplicationException("Unauthorized access to conversation", Response.Status.FORBIDDEN);
        }

        List<Message> messages = messageRepository.findByConversation(conversation);
        List<MessageDTO> dtos = messages.stream()
                .map(this::toMessageDTO)
                .collect(Collectors.toList());

        return Response.ok(new ResponseMessage("Messages fetched", dtos)).build();
    }

    @Transactional
    public Response sendMessage(Long conversationId, SendMessageRequest request) {
        User currentUser = getCurrentUser();
        Conversation conversation = conversationRepository.findByIdOptional(conversationId)
                .orElseThrow(() -> new WebApplicationException("Conversation not found", Response.Status.NOT_FOUND));

        // Verify user is part of conversation
        if (!conversation.user1.id.equals(currentUser.id) && !conversation.user2.id.equals(currentUser.id)) {
            throw new WebApplicationException("Unauthorized access to conversation", Response.Status.FORBIDDEN);
        }

        Message message = new Message();
        message.conversation = conversation;
        message.sender = currentUser;
        message.text = request.text;
        
        // Persist message to database
        messageRepository.persist(message);

        // Update conversation last message
        conversation.lastMessageTime = LocalDateTime.now();
        conversation.lastMessageText = request.text;
        conversationRepository.persist(conversation);

        return Response.ok(new ResponseMessage("Message sent", toMessageDTO(message))).build();
    }

    public Response getAvailableUsers() {
        User currentUser = getCurrentUser();
        List<User> users = userRepository.listAll();
        
        List<User> availableUsers = users.stream()
                .filter(user -> !user.id.equals(currentUser.id))
                .collect(Collectors.toList());

        return Response.ok(new ResponseMessage("Users fetched", availableUsers)).build();
    }

    private ConversationDTO toConversationDTO(Conversation conv, User currentUser) {
        ConversationDTO dto = new ConversationDTO();
        dto.id = conv.id;
        
        // Determine the other participant
        User otherUser = conv.user1.id.equals(currentUser.id) ? conv.user2 : conv.user1;
        dto.participantId = otherUser.id;
        dto.participantName = otherUser.username != null ? otherUser.username : "Unknown User";
        dto.participantUsername = otherUser.username;
        dto.lastMessage = conv.lastMessageText;
        dto.lastMessageTime = conv.lastMessageTime;
        dto.createdAt = conv.createdAt;
        dto.updatedAt = conv.updatedAt;
        
        // Count unread messages
        dto.unreadCount = messageRepository.countUnreadMessages(conv, currentUser.id);
        
        return dto;
    }

    private MessageDTO toMessageDTO(Message msg) {
        MessageDTO dto = new MessageDTO();
        dto.id = msg.id;
        dto.conversationId = msg.conversation.id;
        dto.senderId = msg.sender.id;
        dto.text = msg.text;
        dto.createdAt = msg.createdAt;
        dto.isRead = msg.isRead;
        return dto;
    }
}







