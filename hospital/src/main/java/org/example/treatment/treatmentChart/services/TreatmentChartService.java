package org.example.treatment.treatmentChart.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ResponseMessage;
import org.example.inventory.item.domain.Item;
import org.example.inventory.item.domain.repositories.ItemRepository;
import org.example.inventory.item.services.ShopItemService;
import org.example.inventory.stock.domains.StockBatch;
import org.example.treatment.treatmentChart.domains.TreatmentChart;
import org.example.treatment.treatmentChart.domains.repositories.TreatmentChartRepository;
import org.example.treatment.treatmentChart.services.payloads.requests.TreatmentChartRequest;
import org.example.treatment.treatmentChart.services.payloads.responses.TreatmentChartDTO;
import org.example.treatment.domains.TreatmentRequested;
import org.example.treatment.domains.repositories.TreatmentRequestedRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class TreatmentChartService {

    @Inject
    TreatmentChartRepository treatmentChartRepository;

    @Inject
    TreatmentRequestedRepository treatmentRequestedRepository;

    @Inject
    ItemRepository itemRepository;

    @Inject
    ShopItemService itemService;

    @Transactional
    public Response createTreatmentChart(Long treatmentRequestedId, TreatmentChartRequest request) {
        TreatmentRequested treatmentRequested = treatmentRequestedRepository.findById(treatmentRequestedId);
        if (treatmentRequested == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Treatment request not found for ID: " + treatmentRequestedId))
                    .build();
        }
        if (!treatmentRequested.isDispensedOrGiven()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage(
                            "Only dispensed (DSP) treatments can be added to the treatment chart.",
                            "NOT_DISPENSED"))
                    .build();
        }

        String administrationPlace = normalizeAdministrationPlace(request.administrationPlace);
        boolean takenAway = administrationPlace != null;

        LocalDate dateGiven = request.dateGiven != null ? request.dateGiven : LocalDate.now();
        LocalTime timeGiven = request.timeGiven != null ? request.timeGiven : LocalTime.now();

        if (!takenAway) {
            Response remainingError = validateCanAddChartEntry(treatmentRequested, request);
            if (remainingError != null) {
                return remainingError;
            }
        } else {
            Response alreadyAway = validateNotAlreadyTakenAway(treatmentRequested);
            if (alreadyAway != null) {
                return alreadyAway;
            }
        }

        TreatmentChart chart = new TreatmentChart();
        chart.treatmentRequested = treatmentRequested;
        chart.dateGiven = dateGiven;
        chart.timeGiven = timeGiven;
        chart.givenBy = request.givenBy;
        chart.route = request.route != null ? request.route : treatmentRequested.route;
        chart.instructions = request.instructions != null ? request.instructions : treatmentRequested.instructions;
        chart.dosageValue = request.dosageValue != null ? request.dosageValue : treatmentRequested.amountPerFrequencyValue;
        chart.dosageUnit = request.dosageUnit != null ? request.dosageUnit : treatmentRequested.amountPerFrequencyUnit;
        chart.frequencyValue = request.frequencyValue != null ? request.frequencyValue : treatmentRequested.frequencyValue;
        chart.frequencyUnit = request.frequencyUnit != null ? request.frequencyUnit : treatmentRequested.frequencyUnit;
        chart.timeBetweenGivenToNextDosage = takenAway ? null : request.timeBetweenGivenToNextDosage;
        chart.dateForNextDosage = takenAway ? null : request.dateForNextDosage;
        chart.timeForNextDosage = takenAway ? null : request.timeForNextDosage;
        chart.overallTotalDosages = request.overallTotalDosages != null
                ? request.overallTotalDosages
                : getTotalDosages(treatmentRequested.totalUnits);
        chart.administrationPlace = administrationPlace;

        if (takenAway) {
            int remaining = resolveRemainingDosesForTakeaway(treatmentRequested, request);
            int unitsRemaining = resolveUnitsRemainingForTakeaway(treatmentRequested, remaining);
            chart.totalDosagesGiven = chart.overallTotalDosages != null ? chart.overallTotalDosages : remaining;
            chart.totalDosagesRemaining = remaining;
            chart.unitsUsed = unitsRemaining;
            chart.status = request.status != null && !request.status.isBlank() ? request.status : "Dispensed";
        } else {
            chart.totalDosagesGiven = request.totalDosagesGiven != null ? request.totalDosagesGiven : 0;
            chart.unitsUsed = request.unitsUsed != null ? request.unitsUsed : 0;
            chart.totalDosagesRemaining = request.totalDosagesRemaining != null
                    ? request.totalDosagesRemaining
                    : calculateRemaining(chart.overallTotalDosages, chart.totalDosagesGiven);
            chart.status = request.status != null ? request.status : "Pending";
        }

        if (!takenAway && isGivenStatus(chart.status)) {
            Response stockError = deductStockForChartEntry(treatmentRequested, chart.unitsUsed);
            if (stockError != null) {
                return stockError;
            }
        }

        treatmentChartRepository.persist(chart);
        if (takenAway) {
            treatmentRequested.administered = true;
            treatmentRequested.persist();
        } else {
            markAdministeredIfAllDosesGiven(treatmentRequested, chart);
        }

        return Response.status(Response.Status.CREATED)
                .entity(new ResponseMessage("Treatment chart created successfully", new TreatmentChartDTO(chart)))
                .build();
    }

    @Transactional
    public Response updateTreatmentChart(Long id, TreatmentChartRequest request) {
        TreatmentChart chart = treatmentChartRepository.findById(id);
        if (chart == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Treatment chart not found for ID: " + id))
                    .build();
        }

        TreatmentRequested treatmentRequested = chart.treatmentRequested;
        boolean wasGiven = isGivenStatus(chart.status);
        Integer previousUnitsUsed = chart.unitsUsed;

        chart.dateGiven = request.dateGiven != null ? request.dateGiven : LocalDate.now();
        chart.timeGiven = request.timeGiven != null ? request.timeGiven : LocalTime.now();
        chart.givenBy = request.givenBy != null ? request.givenBy : chart.givenBy;
        chart.route = request.route != null ? request.route : chart.route;
        chart.instructions = request.instructions != null ? request.instructions : chart.instructions;
        chart.dosageValue = request.dosageValue != null ? request.dosageValue : chart.dosageValue;
        chart.dosageUnit = request.dosageUnit != null ? request.dosageUnit : chart.dosageUnit;
        chart.frequencyValue = request.frequencyValue != null ? request.frequencyValue : chart.frequencyValue;
        chart.frequencyUnit = request.frequencyUnit != null ? request.frequencyUnit : chart.frequencyUnit;
        chart.timeBetweenGivenToNextDosage = request.timeBetweenGivenToNextDosage != null ? request.timeBetweenGivenToNextDosage : chart.timeBetweenGivenToNextDosage;
        if (request.dateForNextDosage != null || request.timeForNextDosage != null) {
            chart.dateForNextDosage = request.dateForNextDosage != null ? request.dateForNextDosage : chart.dateForNextDosage;
            chart.timeForNextDosage = request.timeForNextDosage != null ? request.timeForNextDosage : chart.timeForNextDosage;
            chart.nextDoseAlertSentAt = null;
        }
        chart.status = request.status != null ? request.status : chart.status;

        if (request.totalDosagesGiven != null) {
            chart.totalDosagesGiven = request.totalDosagesGiven;
        }
        if (request.unitsUsed != null) {
            chart.unitsUsed = request.unitsUsed;
        }
        if (request.overallTotalDosages != null) {
            chart.overallTotalDosages = request.overallTotalDosages;
        }
        chart.totalDosagesRemaining = request.totalDosagesRemaining != null
                ? request.totalDosagesRemaining
                : calculateRemaining(chart.overallTotalDosages, chart.totalDosagesGiven);

        boolean nowGiven = isGivenStatus(chart.status);
        if (wasGiven && !nowGiven) {
            restoreStockForChartEntry(treatmentRequested, previousUnitsUsed);
        } else if (!wasGiven && nowGiven) {
            Response stockError = deductStockForChartEntry(treatmentRequested, chart.unitsUsed);
            if (stockError != null) {
                return stockError;
            }
        } else if (wasGiven && nowGiven) {
            int previous = previousUnitsUsed != null ? previousUnitsUsed : 0;
            int current = chart.unitsUsed != null ? chart.unitsUsed : 0;
            if (current != previous) {
                restoreStockForChartEntry(treatmentRequested, previous);
                Response stockError = deductStockForChartEntry(treatmentRequested, current);
                if (stockError != null) {
                    return stockError;
                }
            }
        }

        treatmentChartRepository.persist(chart);
        markAdministeredIfAllDosesGiven(treatmentRequested, chart);

        return Response.ok(new ResponseMessage("Treatment chart updated successfully", new TreatmentChartDTO(chart))).build();
    }

    @Transactional
    public Response getTreatmentChartByRequestId(Long treatmentRequestedId) {
        TreatmentChart chart = TreatmentChart.find("treatmentRequested.id", treatmentRequestedId).firstResult();
        if (chart == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Treatment chart not found for treatment request ID: " + treatmentRequestedId))
                    .build();
        }
        return Response.ok(new ResponseMessage("treatment chart fetched successfully", new TreatmentChartDTO(chart))).build();
    }

    public Response getTreatmentChartsByRequestId(Long treatmentRequestedId) {
        List<TreatmentChart> charts = TreatmentChart
                .find("treatmentRequested.id = ?1 order by dateGiven asc, timeGiven asc, id asc", treatmentRequestedId)
                .list();
        List<TreatmentChartDTO> dtos = charts.stream().map(TreatmentChartDTO::new).collect(Collectors.toList());
        return Response.ok(new ResponseMessage("Treatment charts fetched successfully", dtos)).build();
    }

    public Response getTreatmentChartsByVisitId(Long visitId) {
        List<TreatmentChart> charts = TreatmentChart.find("treatmentRequested.visit.id", visitId).list();
        List<TreatmentChartDTO> dtos = charts.stream().map(TreatmentChartDTO::new).collect(Collectors.toList());
        return Response.ok(new ResponseMessage("Treatment charts fetched successfully", dtos)).build();
    }

    @Transactional
    public Response deleteTreatmentChart(Long id) {
        TreatmentChart chart = treatmentChartRepository.findById(id);
        if (chart == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Treatment chart not found for ID: " + id))
                    .build();
        }
        TreatmentRequested treatment = chart.treatmentRequested;
        if (isGivenStatus(chart.status)) {
            restoreStockForChartEntry(treatment, chart.unitsUsed);
        }
        treatmentChartRepository.delete(chart);
        treatmentChartRepository.flush();
        unmarkAdministeredIfDosesRemain(treatment);
        return Response.ok(new ResponseMessage("Treatment chart deleted successfully", null)).build();
    }

    private boolean isGivenStatus(String status) {
        if (status == null) {
            return false;
        }
        String normalized = status.trim().toLowerCase();
        return "given".equals(normalized) || "dispensed".equals(normalized);
    }

    private Response deductStockForChartEntry(TreatmentRequested treatment, Integer unitsUsed) {
        BigDecimal qty = chartUnitsToQuantity(unitsUsed);
        if (qty.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        if (treatment.stockBatch != null) {
            StockBatch batch = treatment.stockBatch;
            BigDecimal stock = nz(batch.stockAtHand);
            if (qty.compareTo(stock) > 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ResponseMessage(
                                "Insufficient stock. Available: " + stock + ", Required: " + qty,
                                "INSUFFICIENT_STOCK"))
                        .build();
            }
            batch.stockAtHand = stock.subtract(qty);
            batch.persist();
            treatment.lastStockAtHand = batch.stockAtHand;
            treatment.persist();
        } else if (treatment.itemId != null) {
            Item item = itemRepository.findById(treatment.itemId);
            if (item != null) {
                BigDecimal stock = nz(item.stockAtHand);
                if (qty.compareTo(stock) > 0) {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity(new ResponseMessage(
                                    "Insufficient stock. Available: " + stock + ", Required: " + qty,
                                    "INSUFFICIENT_STOCK"))
                            .build();
                }
                itemService.updateItemStockAtHandAfterSelling(qty, item);
                treatment.lastStockAtHand = nz(item.stockAtHand);
                treatment.persist();
            }
        }
        return null;
    }

    private void restoreStockForChartEntry(TreatmentRequested treatment, Integer unitsUsed) {
        BigDecimal qty = chartUnitsToQuantity(unitsUsed);
        if (qty.compareTo(BigDecimal.ZERO) <= 0 || treatment == null) {
            return;
        }
        if (treatment.stockBatch != null) {
            StockBatch batch = treatment.stockBatch;
            batch.stockAtHand = nz(batch.stockAtHand).add(qty);
            batch.persist();
            treatment.lastStockAtHand = batch.stockAtHand;
            treatment.persist();
        } else if (treatment.itemId != null) {
            Item item = itemRepository.findById(treatment.itemId);
            if (item != null) {
                item.stockAtHand = nz(item.stockAtHand).add(qty);
                item.persist();
                treatment.lastStockAtHand = item.stockAtHand;
                treatment.persist();
            }
        }
    }

    private static BigDecimal chartUnitsToQuantity(Integer unitsUsed) {
        if (unitsUsed == null || unitsUsed <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(unitsUsed);
    }

    private static BigDecimal nz(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private Integer getTotalDosages(BigDecimal totalUnits) {
        if (totalUnits == null) {
            return 0;
        }
        return totalUnits.setScale(0, RoundingMode.CEILING).intValue();
    }

    private Integer calculateRemaining(Integer overallTotalDosages, Integer totalDosagesGiven) {
        int total = overallTotalDosages != null ? overallTotalDosages : 0;
        int given = totalDosagesGiven != null ? totalDosagesGiven : 0;
        return Math.max(total - given, 0);
    }

    private void markAdministeredIfAllDosesGiven(TreatmentRequested treatment, TreatmentChart chart) {
        if (treatment == null || chart == null) {
            return;
        }
        int remaining = chart.totalDosagesRemaining != null ? chart.totalDosagesRemaining : 0;
        int given = chart.totalDosagesGiven != null ? chart.totalDosagesGiven : 0;
        int overall = chart.overallTotalDosages != null ? chart.overallTotalDosages : 0;
        if ((remaining <= 0 && given > 0) || (overall > 0 && given >= overall)) {
            treatment.administered = true;
            treatment.persist();
        }
    }

    private void unmarkAdministeredIfDosesRemain(TreatmentRequested treatment) {
        if (treatment == null || treatment.id == null) {
            return;
        }
        List<TreatmentChart> remainingCharts = TreatmentChart
                .find("treatmentRequested.id = ?1 order by dateGiven desc, timeGiven desc, id desc", treatment.id)
                .list();
        boolean complete = remainingCharts.stream().anyMatch(c -> {
            if (c.administrationPlace != null && !c.administrationPlace.isBlank()) {
                return true;
            }
            int remaining = c.totalDosagesRemaining != null ? c.totalDosagesRemaining : 0;
            int given = c.totalDosagesGiven != null ? c.totalDosagesGiven : 0;
            int overall = c.overallTotalDosages != null ? c.overallTotalDosages : 0;
            return (remaining <= 0 && given > 0) || (overall > 0 && given >= overall);
        });
        if (!complete && Boolean.TRUE.equals(treatment.administered)) {
            treatment.administered = false;
            treatment.persist();
        }
    }

    private static String normalizeAdministrationPlace(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String normalized = raw.trim().toUpperCase().replace(' ', '_');
        if ("HOME".equals(normalized) || "DRUGS_TAKEN_FROM_HOME".equals(normalized)) {
            return "HOME";
        }
        if ("PERIPHERAL_CLINIC".equals(normalized)
                || "PERIPHERAL".equals(normalized)
                || "DRUGS_TAKEN_FOR_A_PERIPHERAL_CLINIC".equals(normalized)) {
            return "PERIPHERAL_CLINIC";
        }
        return null;
    }

    private Response validateNotAlreadyTakenAway(TreatmentRequested treatmentRequested) {
        long takenAway = TreatmentChart.count(
                "treatmentRequested.id = ?1 and administrationPlace is not null",
                treatmentRequested.id);
        if (takenAway > 0 || Boolean.TRUE.equals(treatmentRequested.administered)) {
            String treatmentName = treatmentRequested.itemName != null ? treatmentRequested.itemName : "This treatment";
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage(
                            treatmentName + " has already been marked as taken away or fully administered.",
                            "ALREADY_TAKEN_AWAY"))
                    .build();
        }
        return null;
    }

    private int resolveRemainingDosesForTakeaway(TreatmentRequested treatmentRequested, TreatmentChartRequest request) {
        if (request.totalDosagesRemaining != null && request.totalDosagesRemaining >= 0) {
            return request.totalDosagesRemaining;
        }
        List<TreatmentChart> existing = TreatmentChart
                .find("treatmentRequested.id = ?1 order by dateGiven desc, timeGiven desc, id desc",
                        treatmentRequested.id)
                .list();
        if (!existing.isEmpty() && existing.get(0).totalDosagesRemaining != null) {
            return Math.max(0, existing.get(0).totalDosagesRemaining);
        }
        int overall = resolveOverallTotalDosages(treatmentRequested, request);
        return Math.max(0, overall - existing.size());
    }

    private int resolveUnitsRemainingForTakeaway(TreatmentRequested treatmentRequested, int remainingDoses) {
        if (treatmentRequested.totalUnits != null) {
            return Math.max(0, treatmentRequested.totalUnits.setScale(0, RoundingMode.HALF_UP).intValue());
        }
        if (treatmentRequested.amountPerFrequencyValue != null && remainingDoses > 0) {
            return treatmentRequested.amountPerFrequencyValue
                    .multiply(BigDecimal.valueOf(remainingDoses))
                    .setScale(0, RoundingMode.HALF_UP)
                    .intValue();
        }
        return Math.max(0, remainingDoses);
    }

    private Response validateCanAddChartEntry(TreatmentRequested treatmentRequested, TreatmentChartRequest request) {
        List<TreatmentChart> existing = TreatmentChart
                .find("treatmentRequested.id = ?1 order by dateGiven desc, timeGiven desc, id desc",
                        treatmentRequested.id)
                .list();

        boolean takenAway = existing.stream().anyMatch(c ->
                c.administrationPlace != null && !c.administrationPlace.isBlank());
        if (takenAway) {
            String treatmentName = treatmentRequested.itemName != null ? treatmentRequested.itemName : "This treatment";
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage(
                            treatmentName + " was taken away (home / peripheral clinic) and cannot receive more facility doses.",
                            "TAKEN_AWAY"))
                    .build();
        }

        int remaining;
        if (!existing.isEmpty()) {
            TreatmentChart latest = existing.get(0);
            if (latest.totalDosagesRemaining != null) {
                remaining = latest.totalDosagesRemaining;
            } else {
                int overall = resolveOverallTotalDosages(treatmentRequested, request);
                remaining = Math.max(0, overall - existing.size());
            }
        } else {
            remaining = resolveOverallTotalDosages(treatmentRequested, request);
        }

        if (remaining <= 0) {
            String treatmentName = treatmentRequested.itemName != null ? treatmentRequested.itemName : "This treatment";
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage(
                            treatmentName + " has no remaining doses. All scheduled doses have already been recorded.",
                            "NO_REMAINING_DOSES"))
                    .build();
        }
        return null;
    }

    private int resolveOverallTotalDosages(TreatmentRequested treatmentRequested, TreatmentChartRequest request) {
        if (request.overallTotalDosages != null && request.overallTotalDosages > 0) {
            return request.overallTotalDosages;
        }
        if (treatmentRequested.frequencyValue != null && treatmentRequested.durationValue != null) {
            int overall = treatmentRequested.frequencyValue
                    .multiply(treatmentRequested.durationValue)
                    .setScale(0, RoundingMode.HALF_UP)
                    .intValue();
            if (overall > 0) {
                return overall;
            }
        }
        return Math.max(1, getTotalDosages(treatmentRequested.totalUnits));
    }
}
