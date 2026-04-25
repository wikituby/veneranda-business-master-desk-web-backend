package org.example.lab.multipleParameterReport.urinalysis.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.lab.multipleParameterReport.urinalysis.domains.Urinalysis;

@ApplicationScoped
public class UrinalysisRepository implements PanacheRepository<Urinalysis> {
}
