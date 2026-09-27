package org.example.messages.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.example.inventory.item.domain.Item;
import org.example.messages.services.payloads.responses.MessagePushEvent;
import org.example.queue.domains.HospitalModule;
import org.example.queue.domains.PatientQueueEntry;
import org.example.queue.domains.repositories.HospitalModuleRepository;
import org.example.queue.services.payloads.responses.PatientQueueEntryDTO;
import org.example.treatment.treatmentChart.domains.TreatmentChart;
import org.example.user.domains.User;
import org.example.user.domains.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class AppNotificationPushService {

    private static final Logger LOG = LoggerFactory.getLogger(AppNotificationPushService.class);

    @Inject
    MessagingWebSocketRegistry messagingWebSocketRegistry;

    @Inject
    UserRepository userRepository;

    @Inject
    HospitalModuleRepository hospitalModuleRepository;

    public void pushQueuePatient(PatientQueueEntry entry) {
        if (entry == null) {
            return;
        }
        PatientQueueEntryDTO dto = new PatientQueueEntryDTO(entry);
        MessagePushEvent event = MessagePushEvent.queuePatient(dto);
        Set<Long> recipients = resolveQueueRecipients(entry);
        for (Long userId : recipients) {
            messagingWebSocketRegistry.pushToUser(userId, event);
        }
        LOG.debug("Pushed QUEUE_PATIENT to {} user(s) for entry {}", recipients.size(), entry.id);
    }

    public void maybePushStockAlert(Item item, BigDecimal stockBefore) {
        if (item == null || item.reOrderLevel == null) {
            return;
        }
        BigDecimal before = nz(stockBefore);
        BigDecimal after = nz(item.stockAtHand);
        BigDecimal reorder = BigDecimal.valueOf(item.reOrderLevel);

        boolean wasAbove = before.compareTo(reorder) > 0;
        boolean nowLow = after.compareTo(reorder) <= 0;
        if (!nowLow || !wasAbove) {
            return;
        }

        MessagePushEvent event = MessagePushEvent.stockAlert(item);
        Set<Long> recipients = resolveStockAlertRecipients();
        for (Long userId : recipients) {
            messagingWebSocketRegistry.pushToUser(userId, event);
        }
        LOG.debug("Pushed STOCK_ALERT to {} user(s) for item {}", recipients.size(), item.id);
    }

    public void pushTreatmentDoseDue(TreatmentChart chart) {
        if (chart == null) {
            return;
        }
        MessagePushEvent event = MessagePushEvent.treatmentDoseDue(chart);
        Set<Long> recipients = resolveTreatmentDoseAlertRecipients();
        for (Long userId : recipients) {
            messagingWebSocketRegistry.pushToUser(userId, event);
        }
        LOG.debug("Pushed TREATMENT_DOSE_DUE to {} user(s) for chart {}", recipients.size(), chart.id);
    }

    private Set<Long> resolveTreatmentDoseAlertRecipients() {
        return userRepository.listAll().stream()
                .filter(Objects::nonNull)
                .filter(this::canReceiveTreatmentDoseAlerts)
                .map(user -> user.id)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private boolean canReceiveTreatmentDoseAlerts(User user) {
        if (user.role == null || user.role.isBlank()) {
            return false;
        }
        String role = user.role.trim().toLowerCase();
        return "admin".equals(role)
                || "md".equals(role)
                || "doctor".equals(role)
                || "clinician".equals(role)
                || "nurse".equals(role)
                || "pharmacist".equals(role)
                || "pharmacy".equals(role);
    }

    private Set<Long> resolveQueueRecipients(PatientQueueEntry entry) {
        List<User> users = userRepository.listAll();
        Set<Long> recipients = new HashSet<>();
        for (User user : users) {
            if (user == null || user.id == null) {
                continue;
            }
            if (isQueueVisibleForUser(entry, user)) {
                recipients.add(user.id);
            }
        }
        return recipients;
    }

    private Set<Long> resolveStockAlertRecipients() {
        return userRepository.listAll().stream()
                .filter(Objects::nonNull)
                .filter(this::canReceiveStockAlerts)
                .map(user -> user.id)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private boolean canReceiveStockAlerts(User user) {
        if (user.role == null || user.role.isBlank()) {
            return false;
        }
        String role = user.role.trim().toLowerCase();
        return "admin".equals(role)
                || "md".equals(role)
                || "doctor".equals(role)
                || "clinician".equals(role)
                || "pharmacist".equals(role)
                || "pharmacy".equals(role)
                || role.contains("inventory")
                || role.contains("store");
    }

    private boolean isQueueVisibleForUser(PatientQueueEntry entry, User user) {
        if (isPrivilegedRole(user.role)) {
            return true;
        }

        Set<Long> moduleIds = parseIdCsv(user.assignedModuleIds);
        Set<Long> clinicIds = parseIdCsv(user.assignedClinicIds);

        if (!moduleIds.isEmpty() || !clinicIds.isEmpty()) {
            if (entry.toModule != null && moduleIds.contains(entry.toModule.id)) {
                return true;
            }
            if (entry.clinic != null && clinicIds.contains(entry.clinic.id)) {
                return true;
            }
            return false;
        }

        if (isPharmacyRole(user.role)) {
            Long pharmacyId = resolvePharmacyModuleId();
            return entry.toModule != null && pharmacyId != null && pharmacyId.equals(entry.toModule.id);
        }

        if (isLabRole(user.role)) {
            Long labId = resolveModuleIdByCode("LAB");
            return entry.toModule != null && labId != null && labId.equals(entry.toModule.id);
        }

        return isModuleAllowedForRole(entry.toModule, user.role);
    }

    private boolean isPrivilegedRole(String role) {
        if (role == null || role.isBlank()) {
            return false;
        }
        String normalized = role.trim().toLowerCase();
        return "admin".equals(normalized) || "md".equals(normalized);
    }

    private boolean isPharmacyRole(String role) {
        if (role == null || role.isBlank()) {
            return false;
        }
        String normalized = role.trim().toLowerCase();
        return "pharmacist".equals(normalized) || "pharmacy".equals(normalized);
    }

    private boolean isLabRole(String role) {
        if (role == null || role.isBlank()) {
            return false;
        }
        String normalized = role.trim().toLowerCase();
        return "lab".equals(normalized)
                || "lab technician".equals(normalized)
                || "lab assistant".equals(normalized);
    }

    private boolean isModuleAllowedForRole(HospitalModule module, String role) {
        if (module == null) {
            return false;
        }
        if (role == null || role.isBlank() || isPrivilegedRole(role)) {
            return true;
        }
        if (module.allowedRoles == null || module.allowedRoles.isBlank()) {
            return true;
        }
        String normalized = role.trim().toLowerCase();
        for (String allowed : module.allowedRoles.split(",")) {
            if (normalized.equals(allowed.trim().toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private Long resolvePharmacyModuleId() {
        return resolveModuleIdByCode("PHARMACY");
    }

    private Long resolveModuleIdByCode(String code) {
        HospitalModule module = hospitalModuleRepository.find("code", code).firstResult();
        return module != null ? module.id : null;
    }

    private Set<Long> parseIdCsv(String raw) {
        if (raw == null || raw.isBlank()) {
            return Set.of();
        }
        Set<Long> ids = new HashSet<>();
        for (String part : raw.split(",")) {
            if (part == null || part.isBlank()) {
                continue;
            }
            try {
                ids.add(Long.parseLong(part.trim()));
            } catch (NumberFormatException ignored) {
                /* skip invalid ids */
            }
        }
        return ids;
    }

    private static BigDecimal nz(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
