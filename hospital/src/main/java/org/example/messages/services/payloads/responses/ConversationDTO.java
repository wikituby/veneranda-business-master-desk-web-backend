package org.example.messages.services.payloads.responses;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.example.messages.domains.Conversation;
import org.example.user.domains.User;

import java.time.LocalDateTime;

public class ConversationDTO {
    public Long id;
    public Long participantId;
    public String participantName;
    public String participantUsername;
    public String lastMessage;
    
    @Schema(type = SchemaType.STRING, format = "date-time")
    public LocalDateTime lastMessageTime;
    
    public Long unreadCount;
    
    @Schema(type = SchemaType.STRING, format = "date-time")
    public LocalDateTime createdAt;
    
    @Schema(type = SchemaType.STRING, format = "date-time")
    public LocalDateTime updatedAt;

    public ConversationDTO() {
    }

    public ConversationDTO(Conversation conversation, User currentUser, Long unreadCount) {
        if (conversation != null) {
            this.id = conversation.id;
            this.lastMessage = conversation.lastMessageText;
            this.lastMessageTime = conversation.lastMessageTime;
            this.createdAt = conversation.createdAt;
            this.updatedAt = conversation.updatedAt;
            this.unreadCount = unreadCount != null ? unreadCount : 0L;

            if (currentUser != null && conversation.user1 != null && conversation.user2 != null) {
                // Determine the other participant
                User otherUser = conversation.user1.id.equals(currentUser.id) ? conversation.user2 : conversation.user1;
                this.participantId = otherUser != null ? otherUser.id : null;
                this.participantName = otherUser != null && otherUser.username != null ? otherUser.username : "Unknown User";
                this.participantUsername = otherUser != null ? otherUser.username : null;
            }
        }
    }
}









