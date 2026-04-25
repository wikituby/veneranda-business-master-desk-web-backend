package org.example.messages.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.persistence.*;
import org.example.user.domains.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversations")
public class Conversation extends PanacheEntity {

    @ManyToOne
    @JoinColumn(name = "user1_id", nullable = false)
    public User user1;

    @ManyToOne
    @JoinColumn(name = "user2_id", nullable = false)
    public User user2;

    @Column(name = "last_message_time")
    @JsonbDateFormat(value = "yyyy-MM-dd'T'HH:mm:ss")
    public LocalDateTime lastMessageTime;

    @Column(name = "last_message_text", columnDefinition = "TEXT")
    public String lastMessageText;

    @Column(name = "created_at")
    @JsonbDateFormat(value = "yyyy-MM-dd'T'HH:mm:ss")
    public LocalDateTime createdAt;

    @Column(name = "updated_at")
    @JsonbDateFormat(value = "yyyy-MM-dd'T'HH:mm:ss")
    public LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}









