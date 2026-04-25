package org.example.messages.services.payloads.responses;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.example.messages.domains.Message;

import java.time.LocalDateTime;

public class MessageDTO {
    public Long id;
    public Long conversationId;
    public Long senderId;
    public String text;
    
    @Schema(type = SchemaType.STRING, format = "date-time")
    public LocalDateTime createdAt;
    
    public Boolean isRead;

    public MessageDTO() {
    }

    public MessageDTO(Message message) {
        if (message != null) {
            this.id = message.id;
            this.conversationId = message.conversation != null ? message.conversation.id : null;
            this.senderId = message.sender != null ? message.sender.id : null;
            this.text = message.text;
            this.createdAt = message.createdAt;
            this.isRead = message.isRead;
        }
    }
}









