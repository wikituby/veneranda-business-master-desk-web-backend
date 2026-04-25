package org.example.procedure.procedure.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.procedure.procedure.domains.Procedure;

@ApplicationScoped
public class ProcedureRepository implements PanacheRepository<Procedure> {

    public Procedure findByCategory(String procedureRequestedName) {
        return find("procedureRequestedName", procedureRequestedName).firstResult();
    }
}






