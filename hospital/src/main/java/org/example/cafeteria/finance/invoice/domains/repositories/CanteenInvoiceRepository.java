package org.example.cafeteria.finance.invoice.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import org.example.cafeteria.finance.invoice.domains.CanteenInvoice;

@ApplicationScoped
public class CanteenInvoiceRepository implements PanacheRepository<CanteenInvoice> {

    @Transactional
    public int deleteInvoiceById(Long id) {
        // Define the native SQL query
        Query query = getEntityManager().createNativeQuery("DELETE FROM canteenInvoice WHERE id = :id");
        query.setParameter("id", id);

        // Execute the query and return the number of rows affected
        return query.executeUpdate();
    }
}






