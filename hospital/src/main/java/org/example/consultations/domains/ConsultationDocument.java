package org.example.consultations.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import java.time.LocalDateTime;

@Entity
public class ConsultationDocument extends PanacheEntity {

    @Column(name = "visit_id", nullable = false)
    public Long visitId;

    @Column(nullable = false)
    public String fileName;

    @Column(nullable = false, columnDefinition = "TEXT")
    public String fileUrl;

    @Column
    public String contentType;

    @Column
    public Long fileSizeBytes;

    @Column(columnDefinition = "TEXT")
    public String description;

    @Column
    public String uploadedBy;

    @JsonbDateFormat(value = "yyyy-MM-dd'T'HH:mm:ss")
    public LocalDateTime uploadedAt;
}
