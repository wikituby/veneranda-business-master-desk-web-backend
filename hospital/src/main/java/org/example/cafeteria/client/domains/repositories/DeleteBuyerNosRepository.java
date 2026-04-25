package org.example.cafeteria.client.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.cafeteria.client.domains.DeletedBuyerNos;

@ApplicationScoped
public class DeleteBuyerNosRepository implements PanacheRepository<DeletedBuyerNos> {
}






