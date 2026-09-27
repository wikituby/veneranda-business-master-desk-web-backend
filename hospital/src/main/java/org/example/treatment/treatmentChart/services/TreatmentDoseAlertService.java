package org.example.treatment.treatmentChart.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.example.messages.services.AppNotificationPushService;
import org.example.treatment.treatmentChart.domains.TreatmentChart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class TreatmentDoseAlertService {

    private static final Logger LOG = LoggerFactory.getLogger(TreatmentDoseAlertService.class);

    @Inject
    AppNotificationPushService appNotificationPushService;

    @Transactional
    public void processDueDoses() {
        LocalDateTime now = LocalDateTime.now();
        List<TreatmentChart> pending = TreatmentChart.list(
                "dateForNextDosage is not null and timeForNextDosage is not null and nextDoseAlertSentAt is null"
        );
        if (pending.isEmpty()) {
            return;
        }

        Map<Long, TreatmentChart> latestByRequest = new HashMap<>();
        for (TreatmentChart chart : pending) {
            if (chart.treatmentRequested == null || chart.treatmentRequested.id == null) {
                continue;
            }
            if (!hasRemainingDoses(chart)) {
                continue;
            }
            if (!isGivenStatus(chart.status)) {
                continue;
            }
            Long requestId = chart.treatmentRequested.id;
            TreatmentChart existing = latestByRequest.get(requestId);
            if (existing == null || chartSortKey(chart) > chartSortKey(existing)) {
                latestByRequest.put(requestId, chart);
            }
        }

        int pushed = 0;
        for (TreatmentChart chart : latestByRequest.values()) {
            if (!isDue(chart, now)) {
                continue;
            }
            appNotificationPushService.pushTreatmentDoseDue(chart);
            chart.nextDoseAlertSentAt = now;
            chart.persist();
            pushed++;
        }

        if (pushed > 0) {
            LOG.info("Pushed {} treatment dose due alert(s)", pushed);
        }
    }

    private static boolean hasRemainingDoses(TreatmentChart chart) {
        Integer remaining = chart.totalDosagesRemaining;
        if (remaining != null && remaining <= 0) {
            return false;
        }
        Integer overall = chart.overallTotalDosages;
        Integer given = chart.totalDosagesGiven;
        if (overall != null && given != null && given >= overall) {
            return false;
        }
        return true;
    }

    private static boolean isDue(TreatmentChart chart, LocalDateTime now) {
        LocalDateTime due = LocalDateTime.of(chart.dateForNextDosage, chart.timeForNextDosage);
        return !due.isAfter(now);
    }

    private static boolean isGivenStatus(String status) {
        if (status == null) {
            return false;
        }
        String normalized = status.trim().toLowerCase();
        return "given".equals(normalized) || "dispensed".equals(normalized);
    }

    private static long chartSortKey(TreatmentChart chart) {
        LocalDateTime given = LocalDateTime.of(
                chart.dateGiven != null ? chart.dateGiven : chart.dateForNextDosage,
                chart.timeGiven != null ? chart.timeGiven : chart.timeForNextDosage
        );
        long base = given.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        return base * 1000L + (chart.id != null ? chart.id : 0L);
    }
}
