package org.example.client.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.client.domains.DeletedPatient;

@ApplicationScoped
public class DeletePatientNosRepository implements PanacheRepository<DeletedPatient> {
}






