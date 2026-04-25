package org.example.procedure.procedure.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.procedure.procedure.domains.ProcedureCategory;

@ApplicationScoped
public class ProcedureCategoryRepository implements PanacheRepository<ProcedureCategory> {
}






