package org.example.procedure.procedure.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.procedure.procedure.domains.ProcedureUnitSellingModel;

@ApplicationScoped
public class ProcedureUnitSellingModelRepository implements PanacheRepository<ProcedureUnitSellingModel> {
}
