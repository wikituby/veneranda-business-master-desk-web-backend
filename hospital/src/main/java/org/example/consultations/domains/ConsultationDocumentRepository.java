package org.example.consultations.domains;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ConsultationDocumentRepository implements PanacheRepository<ConsultationDocument> {
}
