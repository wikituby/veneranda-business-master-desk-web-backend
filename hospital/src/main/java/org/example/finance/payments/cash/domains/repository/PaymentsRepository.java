package org.example.finance.payments.cash.domains.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import org.example.finance.payments.cash.domains.Payments;

@ApplicationScoped
public class PaymentsRepository implements PanacheRepository<Payments> {

    @Transactional
    public void hardDeleteById(Long id) {
        deleteById(id);
    }

    @Transactional
    public int deletePaymentById(Long id) {
        // Define the native SQL query
        Query query = getEntityManager().createNativeQuery("DELETE FROM Payments WHERE id = :id");
        query.setParameter("id", id);

        // Execute the query and return the number of rows affected
        return query.executeUpdate();
    }
}







