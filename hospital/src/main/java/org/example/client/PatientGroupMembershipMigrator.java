package org.example.client;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Ensures {@code patient_patient_group} exists and is backfilled from {@code Patient.group_id}.
 */
@ApplicationScoped
public class PatientGroupMembershipMigrator {

    private static final Logger LOG = Logger.getLogger(PatientGroupMembershipMigrator.class);

    @Inject
    DataSource dataSource;

    void onStart(@Observes StartupEvent event) {
        migrate();
    }

    @Transactional
    void migrate() {
        try (Connection c = dataSource.getConnection(); Statement st = c.createStatement()) {
            st.execute("""
                    CREATE TABLE IF NOT EXISTS patient_patient_group (
                      patient_id BIGINT NOT NULL,
                      group_id BIGINT NOT NULL,
                      PRIMARY KEY (patient_id, group_id)
                    )
                    """);
            // Backfill primary group into membership (idempotent).
            int inserted = 0;
            try (PreparedStatement ps = c.prepareStatement("""
                    INSERT IGNORE INTO patient_patient_group (patient_id, group_id)
                    SELECT id, group_id FROM Patient WHERE group_id IS NOT NULL
                    """)) {
                inserted = ps.executeUpdate();
            } catch (Exception mysqlIgnore) {
                // PostgreSQL / other dialects: INSERT ... ON CONFLICT
                try (PreparedStatement ps = c.prepareStatement("""
                        INSERT INTO patient_patient_group (patient_id, group_id)
                        SELECT id, group_id FROM Patient WHERE group_id IS NOT NULL
                        ON CONFLICT DO NOTHING
                        """)) {
                    inserted = ps.executeUpdate();
                } catch (Exception pgIgnore) {
                    // Fallback: row-by-row for dialects without IGNORE/ON CONFLICT.
                    try (ResultSet rs = st.executeQuery(
                            "SELECT id, group_id FROM Patient WHERE group_id IS NOT NULL");
                         PreparedStatement check = c.prepareStatement(
                                 "SELECT 1 FROM patient_patient_group WHERE patient_id=? AND group_id=?");
                         PreparedStatement ins = c.prepareStatement(
                                 "INSERT INTO patient_patient_group (patient_id, group_id) VALUES (?,?)")) {
                        while (rs.next()) {
                            long pid = rs.getLong(1);
                            long gid = rs.getLong(2);
                            check.setLong(1, pid);
                            check.setLong(2, gid);
                            try (ResultSet exists = check.executeQuery()) {
                                if (!exists.next()) {
                                    ins.setLong(1, pid);
                                    ins.setLong(2, gid);
                                    ins.executeUpdate();
                                    inserted++;
                                }
                            }
                        }
                    }
                }
            }
            if (inserted > 0) {
                LOG.infof("patient_patient_group backfill inserted %d row(s)", inserted);
            } else {
                LOG.info("patient_patient_group ready");
            }
        } catch (Exception e) {
            LOG.warnf(e, "patient_patient_group migrate skipped: %s", e.getMessage());
        }
    }
}
