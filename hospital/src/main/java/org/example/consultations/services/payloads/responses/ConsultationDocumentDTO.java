package org.example.consultations.services.payloads.responses;

import org.example.consultations.domains.ConsultationDocument;

import java.time.LocalDateTime;

public class ConsultationDocumentDTO {
    public Long id;
    public Long visitId;
    public String fileName;
    public String fileUrl;
    public String contentType;
    public Long fileSizeBytes;
    public String description;
    public String uploadedBy;
    public LocalDateTime uploadedAt;

    public static ConsultationDocumentDTO from(ConsultationDocument doc) {
        ConsultationDocumentDTO dto = new ConsultationDocumentDTO();
        dto.id = doc.id;
        dto.visitId = doc.visitId;
        dto.fileName = doc.fileName;
        dto.fileUrl = doc.fileUrl;
        dto.contentType = doc.contentType;
        dto.fileSizeBytes = doc.fileSizeBytes;
        dto.description = doc.description;
        dto.uploadedBy = doc.uploadedBy;
        dto.uploadedAt = doc.uploadedAt;
        return dto;
    }
}
