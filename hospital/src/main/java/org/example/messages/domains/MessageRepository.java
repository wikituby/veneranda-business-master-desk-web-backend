package org.example.messages.domains;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class MessageRepository implements PanacheRepository<Message> {
    
    public List<Message> findByConversation(Conversation conversation) {
        return find("conversation = ?1 ORDER BY createdAt ASC", conversation).list();
    }
    
    public Long countUnreadMessages(Conversation conversation, Long userId) {
        return count("conversation = ?1 AND sender.id != ?2 AND isRead = false", conversation, userId);
    }

    public long markConversationReadForRecipient(Conversation conversation, Long recipientUserId) {
        return update(
                "isRead = true where conversation = ?1 and sender.id != ?2 and isRead = false",
                conversation,
                recipientUserId);
    }

    public long countTotalUnreadForUser(Long userId) {
        return count(
                "(conversation.user1.id = ?1 OR conversation.user2.id = ?1) AND sender.id != ?1 AND isRead = false",
                userId);
    }
}









