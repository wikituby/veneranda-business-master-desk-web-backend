package org.example.inventory.stock.services;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ResponseMessage;
import org.example.inventory.item.domain.Item;
import org.example.inventory.item.domain.repositories.ItemRepository;
import org.example.inventory.stock.domains.*;
import org.example.inventory.stock.domains.repositories.*;
import org.example.inventory.stock.services.payloads.requests.StockVerificationApprovalRequest;
import org.example.inventory.stock.services.payloads.requests.StockVerificationRequest;
import org.example.inventory.stock.services.payloads.responses.dtos.*;
import org.example.subscription.domains.repositories.FacilityBusinessSettingsRepository;
import org.example.subscription.domains.repositories.FacilitySubscriptionRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class StockVerificationService {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_TALLYING = "TALLYING";
    public static final String STATUS_NOT_TALLYING_GENUINE = "NOT_TALLYING_WITH_GENUINE_REASON";
    public static final String STATUS_NOT_TALLYING_NO_REASON = "NOT_TALLYING_WITHOUT_GENUINE_REASON";

    @Inject StockVerificationRepository stockVerificationRepository;
    @Inject VerificationStatusRepository verificationStatusRepository;
    @Inject StockVerificationReasonRepository stockVerificationReasonRepository;
    @Inject StockConfidenceLevelRepository stockConfidenceLevelRepository;
    @Inject ItemRepository itemRepository;
    @Inject FacilityBusinessSettingsRepository businessSettingsRepository;
    @Inject FacilitySubscriptionRepository facilitySubscriptionRepository;

    public boolean resolveUseStockBatch() {
        Long facilityId = facilitySubscriptionRepository
                .find("order by id desc")
                .firstResultOptional()
                .map(s -> s.facilityId)
                .orElse(null);
        if (facilityId == null) {
            return true;
        }
        return businessSettingsRepository.findByFacilityId(facilityId)
                .map(s -> s.pharmacyUseStockBatch == null || Boolean.TRUE.equals(s.pharmacyUseStockBatch))
                .orElse(true);
    }

    public Map<String, Object> getCatalog() {
        Map<String, Object> catalog = new HashMap<>();
        catalog.put("statuses", verificationStatusRepository.list("active = true or active is null",
                Sort.ascending("displayOrder", "id")).stream().map(VerificationCatalogDto::fromStatus).toList());
        catalog.put("reasons", stockVerificationReasonRepository.list("active = true or active is null",
                Sort.ascending("displayOrder", "id")).stream().map(VerificationCatalogDto::fromReason).toList());
        catalog.put("confidenceLevels", stockConfidenceLevelRepository.list("active = true or active is null",
                Sort.ascending("displayOrder", "id")).stream().map(VerificationCatalogDto::fromConfidence).toList());
        catalog.put("useStockBatch", resolveUseStockBatch());
        return catalog;
    }

    @Transactional
    public StockVerificationPreviewDTO preview(Long stockBatchId) {
        StockBatch batch = StockBatch.findById(stockBatchId);
        if (batch == null) {
            return null;
        }
        MovementSummary movement = summarizeMovementsSinceLastVerification(batch);
        return buildBatchPreview(batch, movement, BigDecimal.ZERO);
    }

    @Transactional
    public StockVerificationPreviewDTO previewItem(Long itemId) {
        Item item = itemRepository.findById(itemId);
        if (item == null) {
            return null;
        }
        MovementSummary movement = summarizeMovementsSinceLastItemVerification(item);
        return buildItemPreview(item, movement, BigDecimal.ZERO);
    }

    @Transactional
    public Response create(StockVerificationRequest request) {
        if (request == null || request.physicalQuantity == null) {
            return badRequest("Physical quantity is required.");
        }
        boolean useStockBatch = resolveUseStockBatch();
        if (useStockBatch) {
            if (request.stockBatchId == null) {
                return badRequest("Stock batch and physical quantity are required.");
            }
            return createForBatch(request);
        }
        if (request.itemId == null) {
            return badRequest("Item and physical quantity are required.");
        }
        return createForItem(request);
    }

    private Response createForBatch(StockVerificationRequest request) {
        StockBatch batch = StockBatch.findById(request.stockBatchId);
        if (batch == null) {
            return notFound("Stock batch not found.");
        }
        MovementSummary movement = summarizeMovementsSinceLastVerification(batch);
        BigDecimal physical = request.physicalQuantity;
        BigDecimal variance = physical.subtract(movement.expectedQuantity);

        VerificationStatus status = resolveStatus(request.verificationStatusId, variance, request.verificationReasonId);
        if (status == null) {
            return badRequest("Verification status not found.");
        }
        StockVerificationReason reason = null;
        if (request.verificationReasonId != null) {
            reason = stockVerificationReasonRepository.findById(request.verificationReasonId);
        }
        if (reason != null && Boolean.TRUE.equals(reason.requiresDescription)
                && (request.description == null || request.description.isBlank())) {
            return badRequest("Description is required for the selected reason.");
        }

        LocalDateTime now = LocalDateTime.now();
        StockVerification verification = new StockVerification();
        verification.stockBatch = batch;
        verification.itemId = null;
        verification.stockItemId = batch.stockItemId;
        verification.stockItemName = batch.stockItemName;
        verification.storeId = batch.storeId;
        verification.storeName = batch.storeName;
        applyCommonFields(verification, request, movement, physical, variance, status, reason, now);
        int score = calculateConfidenceScore(movement, variance,
                countHistoricalVariancesForBatch(batch.id));
        verification.confidenceScore = score;
        verification.confidenceLevel = resolveConfidenceLevel(score);

        stockVerificationRepository.persist(verification);
        return Response.ok(new ResponseMessage("Stock verification recorded", new StockVerificationDTO(verification))).build();
    }

    private Response createForItem(StockVerificationRequest request) {
        Item item = itemRepository.findById(request.itemId);
        if (item == null) {
            return notFound("Item not found.");
        }
        MovementSummary movement = summarizeMovementsSinceLastItemVerification(item);
        BigDecimal physical = request.physicalQuantity;
        BigDecimal variance = physical.subtract(movement.expectedQuantity);

        VerificationStatus status = resolveStatus(request.verificationStatusId, variance, request.verificationReasonId);
        if (status == null) {
            return badRequest("Verification status not found.");
        }
        StockVerificationReason reason = null;
        if (request.verificationReasonId != null) {
            reason = stockVerificationReasonRepository.findById(request.verificationReasonId);
        }
        if (reason != null && Boolean.TRUE.equals(reason.requiresDescription)
                && (request.description == null || request.description.isBlank())) {
            return badRequest("Description is required for the selected reason.");
        }

        LocalDateTime now = LocalDateTime.now();
        StockVerification verification = new StockVerification();
        verification.stockBatch = null;
        verification.itemId = item.id;
        verification.stockItemId = item.id;
        verification.stockItemName = item.title;
        verification.storeId = null;
        verification.storeName = null;
        applyCommonFields(verification, request, movement, physical, variance, status, reason, now);
        int score = calculateConfidenceScore(movement, variance,
                countHistoricalVariancesForItem(item.id));
        verification.confidenceScore = score;
        verification.confidenceLevel = resolveConfidenceLevel(score);

        stockVerificationRepository.persist(verification);
        return Response.ok(new ResponseMessage("Stock verification recorded", new StockVerificationDTO(verification))).build();
    }

    private void applyCommonFields(
            StockVerification verification,
            StockVerificationRequest request,
            MovementSummary movement,
            BigDecimal physical,
            BigDecimal variance,
            VerificationStatus status,
            StockVerificationReason reason,
            LocalDateTime now) {
        verification.verificationDateTime = now;
        verification.systemQuantityAtVerification = movement.currentSystemQuantity;
        verification.physicalQuantity = physical;
        verification.addingQuantity = movement.addingQuantity;
        verification.deductingQuantity = movement.deductingQuantity;
        verification.addingTransactionCount = movement.addingTransactionCount;
        verification.deductingTransactionCount = movement.deductingTransactionCount;
        verification.expectedQuantity = movement.expectedQuantity;
        verification.varianceQuantity = variance;
        verification.verificationStatus = status;
        verification.verificationReason = reason;
        verification.description = trim(request.description);
        verification.performedByUserId = request.performedByUserId;
        verification.performedByUserName = trim(request.performedByUserName);
        verification.createdAt = now;
        verification.updatedAt = now;
    }

    @Transactional
    public Response approve(Long id, StockVerificationApprovalRequest request) {
        StockVerification verification = stockVerificationRepository.findById(id);
        if (verification == null) {
            return notFound("Verification not found.");
        }
        if (request == null || request.approvedByUserId == null) {
            return badRequest("Approver is required.");
        }
        verification.approvedByUserId = request.approvedByUserId;
        verification.approvedByUserName = trim(request.approvedByUserName);
        verification.approvedAt = LocalDateTime.now();
        verification.updatedAt = verification.approvedAt;
        stockVerificationRepository.persist(verification);
        return Response.ok(new ResponseMessage("Verification approved", new StockVerificationDTO(verification))).build();
    }

    @Transactional
    public List<StockVerificationDTO> getAll(Long storeId) {
        List<StockVerification> list = storeId == null
                ? stockVerificationRepository.listAll(Sort.descending("verificationDateTime", "id"))
                : stockVerificationRepository.list("storeId = ?1", Sort.descending("verificationDateTime", "id"), storeId);
        return list.stream().map(StockVerificationDTO::new).toList();
    }

    @Transactional
    public Response getById(Long id) {
        StockVerification verification = stockVerificationRepository.findById(id);
        if (verification == null) {
            return notFound("Verification not found.");
        }
        return Response.ok(new ResponseMessage("Fetched", new StockVerificationDTO(verification))).build();
    }

    @Transactional
    public List<StockVerificationDTO> getByBatch(Long stockBatchId) {
        return stockVerificationRepository.list("stockBatch.id = ?1",
                Sort.descending("verificationDateTime", "id"), stockBatchId)
                .stream().map(StockVerificationDTO::new).toList();
    }

    @Transactional
    public List<StockVerificationDTO> getByItem(Long itemId) {
        return stockVerificationRepository.list("itemId = ?1",
                Sort.descending("verificationDateTime", "id"), itemId)
                .stream().map(StockVerificationDTO::new).toList();
    }

    @Transactional
    public StockVerificationDashboardDTO getDashboard(Long storeId) {
        boolean useStockBatch = resolveUseStockBatch();
        StockVerificationDashboardDTO dashboard = new StockVerificationDashboardDTO();
        dashboard.useStockBatch = useStockBatch;

        if (useStockBatch) {
            List<StockBatch> batches = storeId == null
                    ? StockBatch.listAll(Sort.ascending("stockItemName", "id"))
                    : StockBatch.list("storeId = ?1", Sort.ascending("stockItemName", "id"), storeId);
            for (StockBatch batch : batches) {
                StockVerificationDashboardDTO.StockVerificationDashboardRowDTO row =
                        new StockVerificationDashboardDTO.StockVerificationDashboardRowDTO();
                row.stockBatchId = batch.id;
                row.itemId = null;
                row.stockItemId = batch.stockItemId;
                row.stockItemName = batch.stockItemName;
                row.storeName = batch.storeName;
                row.batchNumber = batch.batchNumber;
                row.stockAtHand = nz(batch.stockAtHand);
                StockVerification last = findLastVerificationForBatch(batch.id);
                if (last != null) {
                    row.lastVerification = new StockVerificationDTO(last);
                }
                row.currentPreview = preview(batch.id);
                dashboard.rows.add(row);
            }
        } else {
            List<Item> items = itemRepository.listAll(Sort.ascending("title", "id"));
            for (Item item : items) {
                StockVerificationDashboardDTO.StockVerificationDashboardRowDTO row =
                        new StockVerificationDashboardDTO.StockVerificationDashboardRowDTO();
                row.stockBatchId = null;
                row.itemId = item.id;
                row.stockItemId = item.id;
                row.stockItemName = item.title;
                row.storeName = null;
                row.batchNumber = item.batchNumber;
                row.stockAtHand = nz(item.stockAtHand);
                StockVerification last = findLastVerificationForItem(item.id);
                if (last != null) {
                    row.lastVerification = new StockVerificationDTO(last);
                }
                row.currentPreview = previewItem(item.id);
                dashboard.rows.add(row);
            }
        }
        return dashboard;
    }

    private StockVerificationPreviewDTO buildBatchPreview(
            StockBatch batch, MovementSummary movement, BigDecimal variance) {
        StockVerificationPreviewDTO preview = new StockVerificationPreviewDTO();
        preview.stockBatchId = batch.id;
        preview.itemId = null;
        preview.stockItemId = batch.stockItemId;
        preview.stockItemName = batch.stockItemName;
        preview.storeId = batch.storeId;
        preview.storeName = batch.storeName;
        preview.batchNumber = batch.batchNumber;
        preview.currentSystemQuantity = nz(batch.stockAtHand);
        fillPreviewMovement(preview, movement, variance,
                countHistoricalVariancesForBatch(batch.id));
        return preview;
    }

    private StockVerificationPreviewDTO buildItemPreview(
            Item item, MovementSummary movement, BigDecimal variance) {
        StockVerificationPreviewDTO preview = new StockVerificationPreviewDTO();
        preview.stockBatchId = null;
        preview.itemId = item.id;
        preview.stockItemId = item.id;
        preview.stockItemName = item.title;
        preview.storeId = null;
        preview.storeName = null;
        preview.batchNumber = item.batchNumber;
        preview.currentSystemQuantity = nz(item.stockAtHand);
        fillPreviewMovement(preview, movement, variance,
                countHistoricalVariancesForItem(item.id));
        return preview;
    }

    private void fillPreviewMovement(
            StockVerificationPreviewDTO preview,
            MovementSummary movement,
            BigDecimal variance,
            long historicalVariances) {
        preview.lastVerificationDateTime = movement.lastVerificationDateTime;
        preview.lastPhysicalQuantity = movement.baselinePhysicalQuantity;
        preview.addingQuantity = movement.addingQuantity;
        preview.deductingQuantity = movement.deductingQuantity;
        preview.addingTransactionCount = movement.addingTransactionCount;
        preview.deductingTransactionCount = movement.deductingTransactionCount;
        preview.expectedQuantity = movement.expectedQuantity;
        int score = calculateConfidenceScore(movement, variance, historicalVariances);
        preview.confidenceScore = score;
        StockConfidenceLevel level = resolveConfidenceLevel(score);
        if (level != null) {
            preview.confidenceLevelId = level.id;
            preview.confidenceLevelCode = level.code;
            preview.confidenceLevelName = level.name;
        }
        VerificationStatus suggested = suggestStatus(variance, null);
        if (suggested != null) {
            preview.suggestedStatusId = suggested.id;
            preview.suggestedStatusCode = suggested.code;
            preview.suggestedStatusName = suggested.name;
        }
    }

    private MovementSummary summarizeMovementsSinceLastVerification(StockBatch batch) {
        MovementSummary summary = new MovementSummary();
        summary.currentSystemQuantity = nz(batch.stockAtHand);
        StockVerification last = findLastVerificationForBatch(batch.id);
        summary.lastVerificationDateTime = last != null ? last.verificationDateTime : null;
        summary.baselinePhysicalQuantity = last != null ? nz(last.physicalQuantity) : null;

        LocalDateTime since = summary.lastVerificationDateTime != null
                ? summary.lastVerificationDateTime
                : LocalDateTime.of(1970, 1, 1, 0, 0);

        List<StockTracking> movements = StockTracking.list(
                "stockBatchId = ?1 and recordedAt > ?2",
                Sort.ascending("recordedAt", "id"),
                batch.id,
                since);

        applyMovementTotals(summary, movements, last, nz(batch.stockAtHand));
        return summary;
    }

    private MovementSummary summarizeMovementsSinceLastItemVerification(Item item) {
        MovementSummary summary = new MovementSummary();
        summary.currentSystemQuantity = nz(item.stockAtHand);
        StockVerification last = findLastVerificationForItem(item.id);
        summary.lastVerificationDateTime = last != null ? last.verificationDateTime : null;
        summary.baselinePhysicalQuantity = last != null ? nz(last.physicalQuantity) : null;

        // Shop-item sales often do not write StockTracking rows; fall back to current stock.
        LocalDateTime since = summary.lastVerificationDateTime != null
                ? summary.lastVerificationDateTime
                : LocalDateTime.of(1970, 1, 1, 0, 0);
        List<StockTracking> movements = StockTracking.list(
                "stockBatchId is null and stockItemId = ?1 and recordedAt > ?2",
                Sort.ascending("recordedAt", "id"),
                item.id,
                since);
        applyMovementTotals(summary, movements, last, nz(item.stockAtHand));
        return summary;
    }

    private void applyMovementTotals(
            MovementSummary summary,
            List<StockTracking> movements,
            StockVerification last,
            BigDecimal currentStock) {
        BigDecimal adding = BigDecimal.ZERO;
        BigDecimal deducting = BigDecimal.ZERO;
        int addingCount = 0;
        int deductingCount = 0;
        for (StockTracking movement : movements) {
            BigDecimal qty = nz(movement.quantityChanged);
            if (StockTrackingService.TX_IN.equalsIgnoreCase(movement.transactionType)) {
                adding = adding.add(qty);
                addingCount++;
            } else if (StockTrackingService.TX_OUT.equalsIgnoreCase(movement.transactionType)) {
                deducting = deducting.add(qty);
                deductingCount++;
            }
        }
        summary.addingQuantity = adding;
        summary.deductingQuantity = deducting;
        summary.addingTransactionCount = addingCount;
        summary.deductingTransactionCount = deductingCount;

        BigDecimal baseline;
        if (last != null) {
            baseline = nz(last.physicalQuantity);
        } else {
            baseline = currentStock.subtract(adding).add(deducting);
        }
        summary.expectedQuantity = baseline.add(adding).subtract(deducting);
    }

    private StockVerification findLastVerificationForBatch(Long stockBatchId) {
        return stockVerificationRepository.find(
                "stockBatch.id = ?1 order by verificationDateTime desc, id desc",
                stockBatchId).firstResult();
    }

    private StockVerification findLastVerificationForItem(Long itemId) {
        return stockVerificationRepository.find(
                "itemId = ?1 order by verificationDateTime desc, id desc",
                itemId).firstResult();
    }

    private long countHistoricalVariancesForBatch(Long stockBatchId) {
        return stockVerificationRepository.count(
                "stockBatch.id = ?1 and varianceQuantity <> 0", stockBatchId);
    }

    private long countHistoricalVariancesForItem(Long itemId) {
        return stockVerificationRepository.count(
                "itemId = ?1 and varianceQuantity <> 0", itemId);
    }

    private VerificationStatus resolveStatus(Long statusId, BigDecimal variance, Long reasonId) {
        if (statusId != null) {
            VerificationStatus explicit = verificationStatusRepository.findById(statusId);
            if (explicit != null) {
                return explicit;
            }
        }
        return suggestStatus(variance, reasonId);
    }

    private VerificationStatus suggestStatus(BigDecimal variance, Long reasonId) {
        if (variance.compareTo(BigDecimal.ZERO) == 0) {
            return findStatusByCode(STATUS_TALLYING);
        }
        if (reasonId != null) {
            return findStatusByCode(STATUS_NOT_TALLYING_GENUINE);
        }
        return findStatusByCode(STATUS_NOT_TALLYING_NO_REASON);
    }

    private VerificationStatus findStatusByCode(String code) {
        return verificationStatusRepository.find("code", code).firstResult();
    }

    private StockConfidenceLevel resolveConfidenceLevel(int score) {
        List<StockConfidenceLevel> levels = stockConfidenceLevelRepository.list(
                "active = true or active is null", Sort.ascending("displayOrder", "id"));
        for (StockConfidenceLevel level : levels) {
            int min = level.minimumScore != null ? level.minimumScore : 0;
            int max = level.maximumScore != null ? level.maximumScore : 100;
            if (score >= min && score <= max) {
                return level;
            }
        }
        return levels.isEmpty() ? null : levels.get(levels.size() - 1);
    }

    private int calculateConfidenceScore(
            MovementSummary movement, BigDecimal variance, long historicalVariances) {
        int score = 100;
        if (movement.lastVerificationDateTime != null) {
            long days = ChronoUnit.DAYS.between(movement.lastVerificationDateTime, LocalDateTime.now());
            if (days > 30) score -= 15;
            if (days > 60) score -= 20;
            if (days > 90) score -= 15;
        } else {
            score -= 10;
        }
        int txCount = movement.addingTransactionCount + movement.deductingTransactionCount;
        if (txCount > 25) score -= 10;
        if (txCount > 75) score -= 15;
        if (variance.compareTo(BigDecimal.ZERO) != 0) {
            score -= 25;
        }
        if (historicalVariances > 1) score -= 10;
        if (historicalVariances > 3) score -= 10;
        return Math.max(0, Math.min(100, score));
    }

    private static BigDecimal nz(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private Response badRequest(String message) {
        return Response.status(Response.Status.BAD_REQUEST).entity(new ResponseMessage(message, null)).build();
    }

    private Response notFound(String message) {
        return Response.status(Response.Status.NOT_FOUND).entity(new ResponseMessage(message, null)).build();
    }

    private static class MovementSummary {
        LocalDateTime lastVerificationDateTime;
        BigDecimal baselinePhysicalQuantity;
        BigDecimal currentSystemQuantity = BigDecimal.ZERO;
        BigDecimal addingQuantity = BigDecimal.ZERO;
        BigDecimal deductingQuantity = BigDecimal.ZERO;
        int addingTransactionCount;
        int deductingTransactionCount;
        BigDecimal expectedQuantity = BigDecimal.ZERO;
    }
}
