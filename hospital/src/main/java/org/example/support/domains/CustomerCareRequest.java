package org.example.support.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.persistence.*;
import org.example.user.domains.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "customer_care_requests")
public class CustomerCareRequest extends PanacheEntity {

    @ManyToOne
    @JoinColumn(name = "user_id")
    public User user;

    @Column(name = "username")
    public String username;

    @Column(name = "user_email")
    public String userEmail;

    @Column(nullable = false)
    public String subject;

    @Column(nullable = false)
    public String category;

    @Column(columnDefinition = "TEXT", nullable = false)
    public String message;

    @Column(name = "facility_name")
    public String facilityName;

    @Column(name = "status")
    public String status = "OPEN";

    @Column(name = "created_at")
    @JsonbDateFormat(value = "yyyy-MM-dd'T'HH:mm:ss")
    public LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null || status.isBlank()) {
            status = "OPEN";
        }
    }
}
