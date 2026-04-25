package org.example.diagnostics.xray.head.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.diagnostics.xray.head.domains.HeadXray;

@ApplicationScoped
public class HeadXrayRepository implements PanacheRepository<HeadXray> {
}
