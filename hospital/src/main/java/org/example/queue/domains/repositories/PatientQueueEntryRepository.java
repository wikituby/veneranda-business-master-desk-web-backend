package org.example.queue.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.queue.domains.PatientQueueEntry;

@ApplicationScoped
public class PatientQueueEntryRepository implements PanacheRepository<PatientQueueEntry> {
}
