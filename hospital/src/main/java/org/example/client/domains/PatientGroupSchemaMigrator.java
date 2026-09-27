package org.example.client.domains;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

/**
 * Ensures {@code PatientGroup.allowsCreditDespiteUnpaidBalance} exists.
 * Hibernate {@code update} can fail to add NOT NULL columns on tables with rows
 * (no DEFAULT), leaving the column missing and breaking dashboard joins.
 */
@ApplicationScoped
public class PatientGroupSchemaMigrator {

    private static final Logger LOG = Logger.getLogger(PatientGroupSchemaMigrator.class);

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
                        "ALTER TABLE patientgroup ADD COLUMN IF NOT EXISTS allowscreditdespiteunpaidbalance boolean NOT NULL DEFAULT false"
                ).executeUpdate();
                entityManager.createNativeQuery(
                        "UPDATE patientgroup SET allowscreditdespiteunpaidbalance = false WHERE allowscreditdespiteunpaidbalance IS NULL"
                ).executeUpdate();
            } else {
                try {
                    entityManager.createNativeQuery(
                            "ALTER TABLE PatientGroup ADD COLUMN allowsCreditDespiteUnpaidBalance BIT(1) NOT NULL DEFAULT 0"
                    ).executeUpdate();
                } catch (Exception ignored) {
                    // Column already present
                }
            }
            LOG.info("PatientGroup.allowsCreditDespiteUnpaidBalance ensured");
        } catch (Exception e) {
            LOG.warnf(e, "PatientGroup credit column migrate skipped: %s", e.getMessage());
        }
    }
}
