package org.example.pharmacy.otc;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

/**
 * Ensures OTC sale lines can be item-only (no stock batch).
 * Hibernate {@code update} does not relax an existing NOT NULL column.
 */
@ApplicationScoped
public class OtcPharmacySchemaMigrator {

    private static final Logger LOG = Logger.getLogger(OtcPharmacySchemaMigrator.class);

    @Inject
    EntityManager entityManager;

    @ConfigProperty(name = "quarkus.datasource.db-kind", defaultValue = "mysql")
    String dbKind;

    @Transactional
    void onStart(@Observes StartupEvent ev) {
        boolean postgres = dbKind != null && dbKind.toLowerCase().contains("postgres");
        try {
            if (postgres) {
                entityManager.createNativeQuery(
                        "ALTER TABLE otc_pharmacy_sale_line ALTER COLUMN stock_batch_id DROP NOT NULL"
                ).executeUpdate();
            } else {
                entityManager.createNativeQuery(
                        "ALTER TABLE otc_pharmacy_sale_line MODIFY COLUMN stock_batch_id BIGINT NULL"
                ).executeUpdate();
            }
            LOG.info("otc_pharmacy_sale_line.stock_batch_id is nullable");
        } catch (Exception e) {
            // Already nullable, wrong dialect, or table missing — non-fatal.
            LOG.debugf(e, "OTC schema migrate skipped: %s", e.getMessage());
        }
        try {
            if (postgres) {
                entityManager.createNativeQuery(
                        "ALTER TABLE otc_pharmacy_sale_line ADD COLUMN IF NOT EXISTS reversed BOOLEAN NOT NULL DEFAULT FALSE"
                ).executeUpdate();
            } else {
                entityManager.createNativeQuery(
                        "ALTER TABLE otc_pharmacy_sale_line ADD COLUMN reversed BIT(1) NOT NULL DEFAULT 0"
                ).executeUpdate();
            }
            LOG.info("otc_pharmacy_sale_line.reversed added");
        } catch (Exception e) {
            LOG.debugf(e, "OTC reversed column migrate skipped: %s", e.getMessage());
        }
        try {
            if (postgres) {
                entityManager.createNativeQuery(
                        "ALTER TABLE otc_pharmacy_sale_line ADD COLUMN IF NOT EXISTS treatment_requested_id BIGINT NULL"
                ).executeUpdate();
            } else {
                entityManager.createNativeQuery(
                        "ALTER TABLE otc_pharmacy_sale_line ADD COLUMN treatment_requested_id BIGINT NULL"
                ).executeUpdate();
            }
            LOG.info("otc_pharmacy_sale_line.treatment_requested_id added");
        } catch (Exception e) {
            LOG.debugf(e, "OTC treatment_requested_id migrate skipped: %s", e.getMessage());
        }
        try {
            if (postgres) {
                entityManager.createNativeQuery(
                        "ALTER TABLE otc_pharmacy_sale ADD COLUMN IF NOT EXISTS document_type VARCHAR(40)"
                ).executeUpdate();
            } else {
                entityManager.createNativeQuery(
                        "ALTER TABLE otc_pharmacy_sale ADD COLUMN document_type VARCHAR(40) NULL"
                ).executeUpdate();
            }
            LOG.info("otc_pharmacy_sale.document_type added");
        } catch (Exception e) {
            LOG.debugf(e, "OTC document_type migrate skipped: %s", e.getMessage());
        }
        try {
            if (postgres) {
                entityManager.createNativeQuery(
                        "ALTER TABLE otc_pharmacy_sale_line ADD COLUMN IF NOT EXISTS stockdeferred BOOLEAN NOT NULL DEFAULT FALSE"
                ).executeUpdate();
            } else {
                entityManager.createNativeQuery(
                        "ALTER TABLE otc_pharmacy_sale_line ADD COLUMN stockdeferred BIT(1) NOT NULL DEFAULT 0"
                ).executeUpdate();
            }
            LOG.info("otc_pharmacy_sale_line.stockdeferred added");
        } catch (Exception e) {
            LOG.debugf(e, "OTC stockdeferred column migrate skipped: %s", e.getMessage());
        }
        try {
            if (postgres) {
                entityManager.createNativeQuery(
                        "ALTER TABLE otc_pharmacy_sale_line ADD COLUMN IF NOT EXISTS visit_sundry_id BIGINT NULL"
                ).executeUpdate();
            } else {
                entityManager.createNativeQuery(
                        "ALTER TABLE otc_pharmacy_sale_line ADD COLUMN visit_sundry_id BIGINT NULL"
                ).executeUpdate();
            }
            LOG.info("otc_pharmacy_sale_line.visit_sundry_id added");
        } catch (Exception e) {
            LOG.debugf(e, "OTC visit_sundry_id migrate skipped: %s", e.getMessage());
        }
        try {
            if (postgres) {
                entityManager.createNativeQuery(
                        "ALTER TABLE otc_pharmacy_sale ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT TRUE"
                ).executeUpdate();
            } else {
                entityManager.createNativeQuery(
                        "ALTER TABLE otc_pharmacy_sale ADD COLUMN active BIT(1) NOT NULL DEFAULT 1"
                ).executeUpdate();
            }
            LOG.info("otc_pharmacy_sale.active added");
        } catch (Exception e) {
            LOG.debugf(e, "OTC active column migrate skipped: %s", e.getMessage());
        }
    }
}
