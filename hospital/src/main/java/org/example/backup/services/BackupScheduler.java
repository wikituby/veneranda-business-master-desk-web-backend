package org.example.backup.services;

import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.Scheduled.ConcurrentExecution;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class BackupScheduler {

    private static final Logger LOG = Logger.getLogger(BackupScheduler.class);

    @Inject
    BackupOrchestratorService orchestratorService;

    @Scheduled(every = "5m", concurrentExecution = ConcurrentExecution.SKIP)
    void runEveryFiveMinutes() {
        try {
            var result = orchestratorService.runScheduledBackupIfDue();
            if (result != null) {
                LOG.infof("Scheduled backup finished: success=%s status=%s", result.success, result.lastBackupStatus);
            }
        } catch (Exception e) {
            LOG.error("Scheduled backup failed", e);
        }
    }
}
