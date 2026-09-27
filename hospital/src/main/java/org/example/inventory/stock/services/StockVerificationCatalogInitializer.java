package org.example.inventory.stock.services;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.example.inventory.stock.domains.StockConfidenceLevel;
import org.example.inventory.stock.domains.StockVerificationReason;
import org.example.inventory.stock.domains.VerificationStatus;
import org.example.inventory.stock.domains.repositories.StockConfidenceLevelRepository;
import org.example.inventory.stock.domains.repositories.StockVerificationReasonRepository;
import org.example.inventory.stock.domains.repositories.VerificationStatusRepository;

import java.time.LocalDateTime;

@ApplicationScoped
public class StockVerificationCatalogInitializer {

    @Inject VerificationStatusRepository verificationStatusRepository;
    @Inject StockVerificationReasonRepository stockVerificationReasonRepository;
    @Inject StockConfidenceLevelRepository stockConfidenceLevelRepository;

    @Transactional
    void onStart(@Observes StartupEvent event) {
        seedStatuses();
        seedReasons();
        seedConfidenceLevels();
    }

    private void seedStatuses() {
        ensureStatus("PENDING", "Pending", "Verification started but not yet completed.", 1);
        ensureStatus("TALLYING", "Tallying", "Physical count matches expected stock.", 2);
        ensureStatus("NOT_TALLYING_WITH_GENUINE_REASON", "Not tallying with genuine reason",
                "Variance exists and a configured reason was provided.", 3);
        ensureStatus("NOT_TALLYING_WITHOUT_GENUINE_REASON", "Not tallying without genuine reason",
                "Variance exists but no acceptable reason was provided.", 4);
    }

    private void seedReasons() {
        ensureReason("DAMAGED", "Damaged", "Stock damaged and unusable.", false, true, 1);
        ensureReason("EXPIRED", "Expired", "Stock expired.", false, true, 2);
        ensureReason("SPOILED", "Spoiled", "Stock spoiled.", false, true, 3);
        ensureReason("SPILLAGE", "Spillage", "Stock lost through spillage.", true, false, 4);
        ensureReason("COUNTING_ERROR", "Counting error", "Previous count was incorrect.", true, false, 5);
        ensureReason("DATA_ENTRY_ERROR", "Data entry error", "System entry did not match physical reality.", true, false, 6);
        ensureReason("UNRECORDED_DISPENSING", "Unrecorded dispensing", "Items dispensed but not recorded.", true, true, 7);
        ensureReason("UNRECORDED_TRANSFER", "Unrecorded transfer", "Transfer happened without ledger update.", true, true, 8);
        ensureReason("RETURN_NOT_RECORDED", "Return not recorded", "Returned stock not captured in the system.", true, false, 9);
        ensureReason("THEFT", "Theft", "Stock lost through theft.", true, true, 10);
        ensureReason("OTHER", "Other", "Other configured reason.", true, false, 99);
    }

    private void seedConfidenceLevels() {
        ensureConfidence("HIGH", "High", "Stock ledger is highly trustworthy.", 80, 100, 1);
        ensureConfidence("MEDIUM", "Medium", "Stock ledger has moderate trust.", 50, 79, 2);
        ensureConfidence("LOW", "Low", "Stock ledger trust is low; recount recommended.", 0, 49, 3);
    }

    private void ensureStatus(String code, String name, String description, int order) {
        if (verificationStatusRepository.find("code", code).firstResult() != null) {
            return;
        }
        VerificationStatus row = new VerificationStatus();
        row.code = code;
        row.name = name;
        row.description = description;
        row.active = Boolean.TRUE;
        row.displayOrder = order;
        row.createdAt = LocalDateTime.now();
        row.updatedAt = row.createdAt;
        verificationStatusRepository.persist(row);
    }

    private void ensureReason(String code, String name, String description, boolean requiresDescription,
                              boolean requiresApproval, int order) {
        if (stockVerificationReasonRepository.find("code", code).firstResult() != null) {
            return;
        }
        StockVerificationReason row = new StockVerificationReason();
        row.code = code;
        row.name = name;
        row.description = description;
        row.requiresDescription = requiresDescription;
        row.requiresApproval = requiresApproval;
        row.active = Boolean.TRUE;
        row.displayOrder = order;
        row.createdAt = LocalDateTime.now();
        row.updatedAt = row.createdAt;
        stockVerificationReasonRepository.persist(row);
    }

    private void ensureConfidence(String code, String name, String description, int min, int max, int order) {
        if (stockConfidenceLevelRepository.find("code", code).firstResult() != null) {
            return;
        }
        StockConfidenceLevel row = new StockConfidenceLevel();
        row.code = code;
        row.name = name;
        row.description = description;
        row.minimumScore = min;
        row.maximumScore = max;
        row.active = Boolean.TRUE;
        row.displayOrder = order;
        row.createdAt = LocalDateTime.now();
        row.updatedAt = row.createdAt;
        stockConfidenceLevelRepository.persist(row);
    }
}
