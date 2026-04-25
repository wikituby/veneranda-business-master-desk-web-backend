package org.example.procedure.itemUsedInProcedure.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.procedure.itemUsedInProcedure.domains.ItemUsed;

@ApplicationScoped
public class ItemUsedRepository implements PanacheRepository<ItemUsed> {
}






