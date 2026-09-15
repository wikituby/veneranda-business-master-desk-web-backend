package org.example.support.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "website_contact_messages")
public class WebsiteContactMessage extends PanacheEntity {

    @Column(name = "full_name", nullable = false)
    public String fullName;

    @Column(name = "phone", nullable = false)
    public String phone;

    @Column(name = "email")
    public String email;

    @Column(nullable = false)
    public String subject;

    @Column(columnDefinition = "TEXT", nullable = false)
    public String message;

    @Column(name = "status")
    public String status = "NEW";

    @Column(name = "source")
    public String source = "website";

    @Column(name = "created_at")
    @JsonbDateFormat(value = "yyyy-MM-dd'T'HH:mm:ss")
    public LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null || status.isBlank()) {
            status = "NEW";
        }
        if (source == null || source.isBlank()) {
            source = "website";
        }
    }
}
