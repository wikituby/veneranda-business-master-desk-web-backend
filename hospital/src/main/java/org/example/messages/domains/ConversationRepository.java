package org.example.messages.domains;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.user.domains.User;

import java.util.List;

@ApplicationScoped
public class ConversationRepository implements PanacheRepository<Conversation> {
    
    public List<Conversation> findByUser(User user) {
        return find("user1 = ?1 OR user2 = ?1 ORDER BY lastMessageTime DESC, updatedAt DESC", user)
                .list();
    }
    
    public Conversation findByUsers(User user1, User user2) {
        return find("(user1 = ?1 AND user2 = ?2) OR (user1 = ?2 AND user2 = ?1)", user1, user2)
                .firstResult();
    }
}









