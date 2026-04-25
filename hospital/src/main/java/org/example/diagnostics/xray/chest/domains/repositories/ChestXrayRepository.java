package org.example.diagnostics.xray.chest.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.diagnostics.xray.chest.domains.ChestXray;

@ApplicationScoped
public class ChestXrayRepository implements PanacheRepository<ChestXray> {
}
