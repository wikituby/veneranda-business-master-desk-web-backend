package org.example.messages.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.persistence.*;
import org.example.user.domains.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message extends PanacheEntity {

    @ManyToOne
    @JoinColumn(name = "conversation_id", nullable = false)
    public Conversation conversation;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    public User sender;

    @Column(columnDefinition = "TEXT", nullable = false)
    public String text;

    @Column(name = "created_at")
    @JsonbDateFormat(value = "yyyy-MM-dd'T'HH:mm:ss")
    public LocalDateTime createdAt;

    @Column(name = "is_read")
    public Boolean isRead = false;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}









