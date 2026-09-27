package org.example.treatment.treatmentChart.services;

import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.Scheduled.ConcurrentExecution;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class TreatmentDoseAlertScheduler {

    private static final Logger LOG = Logger.getLogger(TreatmentDoseAlertScheduler.class);

    @Inject
    TreatmentDoseAlertService treatmentDoseAlertService;

    @Scheduled(every = "1m", concurrentExecution = ConcurrentExecution.SKIP)
    void checkDueTreatmentDoses() {
        try {
            treatmentDoseAlertService.processDueDoses();
        } catch (Exception e) {
            LOG.error("Treatment dose alert scheduler failed", e);
        }
    }
}
