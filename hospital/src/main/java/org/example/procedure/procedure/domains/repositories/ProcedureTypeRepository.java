package org.example.procedure.procedure.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.procedure.procedure.domains.ProcedureCategory;
import org.example.procedure.procedure.domains.ProcedureType;

@ApplicationScoped
public class ProcedureTypeRepository implements PanacheRepository<ProcedureType> {
}
