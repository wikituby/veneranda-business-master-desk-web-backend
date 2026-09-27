package org.example.treatment.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.consultations.domains.Diagnosis;
import org.example.consultations.domains.DiagnosisRepository;
import org.example.finance.invoice.services.InvoiceService;
import org.example.inventory.item.domain.Item;
import org.example.inventory.item.domain.repositories.ItemRepository;
import org.example.inventory.item.services.ItemUnitSellingModelService;
import org.example.inventory.item.services.ShopItemService;
import org.example.inventory.stock.domains.StockBatch;
import org.example.inventory.stock.services.UnitSellingModelService;
import org.example.subscription.services.SpecialPrivilegeService;
import org.example.treatment.domains.TreatmentRequested;
import org.example.treatment.domains.repositories.TreatmentRequestedRepository;
import org.example.treatment.services.payloads.requests.TreatmentRequestedRequest;
import org.example.treatment.services.payloads.requests.TreatmentStatusUpdateRequest;
import org.example.treatment.services.payloads.responses.TreatmentRequestedDTO;
import org.example.visit.domains.PatientVisit;
import org.example.visit.domains.repositories.PatientVisitRepository;

@ApplicationScoped
public class TreatmentRequestService {
    @Inject
    TreatmentRequestedRepository treatmentRequestedRepository;
    @Inject
    ShopItemService itemService;
    @Inject
    InvoiceService invoiceService;
    @Inject
    ItemRepository itemRepository;
    @Inject
    PatientVisitRepository patientVisitRepository;
    @Inject
    UnitSellingModelService unitSellingModelService;
    @Inject
    ItemUnitSellingModelService itemUnitSellingModelService;
    @Inject
    DiagnosisRepository diagnosisRepository;
    @Inject
    SpecialPrivilegeService specialPrivilegeService;
    public static final String NOT_FOUND = "Not found!";

    public Response createNewTreatmentRequested(Long id, TreatmentRequestedRequest request) {
        boolean creditAllowed;
        BigDecimal roundedQuantity = request.quantity.setScale(0, RoundingMode.CEILING);
        PatientVisit patientVisit = (PatientVisit)(this.patientVisitRepository.findById(id));
        if (patientVisit == null) {
            return Response.status(Response.Status.NOT_FOUND).entity(new ResponseMessage("Patient or item NOT FOUND", null)).build();
        }
        ResolvedMedicine medicine = this.resolveMedicine(request);
        StockBatch batch = medicine.batch;
        Item legacyItem = medicine.legacyItem;
        if (batch == null && legacyItem == null) {
            return Response.status(Response.Status.NOT_FOUND).entity(new ResponseMessage("Patient or item NOT FOUND", null)).build();
        }
        if ("closed".equals(patientVisit.visitStatus)) {
            return Response.status(Response.Status.NOT_FOUND).entity(new ResponseMessage("Visit is closed. You cannot add anything. Please Open a new visit or contact Admin on 0784411848: ", null)).build();
        }
        BigDecimal totalBalanceDue = this.invoiceService.calculateTotalBalanceDueForClosedVisits(patientVisit.patient.id);
        boolean bl = creditAllowed = this.specialPrivilegeService.patientAllowsCreditDespiteDebt(patientVisit.patient, null);
        if (totalBalanceDue.compareTo(BigDecimal.ZERO) > 0 && !creditAllowed) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new ResponseMessage("Cannot access any service. Patient has a debt of: " + String.valueOf(totalBalanceDue) + " and doesn't belong to an authorized credit group. Please clear the debt first or contact Admin")).build();
        }
        String itemName = batch != null ? batch.stockItemName : legacyItem.title;
        TreatmentRequested existingTreatment = (TreatmentRequested)(TreatmentRequested.find((String)"visit.id = ?1 and itemName = ?2", (Object[])new Object[]{patientVisit.id, itemName}).firstResult());
        if (existingTreatment != null) {
            return Response.ok(new ResponseMessage("Treatment already exists please update")).build();
        }
        TreatmentRequested treatmentRequested = new TreatmentRequested();
        treatmentRequested.quantity = roundedQuantity;
        treatmentRequested.provisionalQuantity = roundedQuantity;
        treatmentRequested.status = "pending";
        treatmentRequested.paid = true;
        treatmentRequested.dispensed = false;
        treatmentRequested.administered = false;
        treatmentRequested.visit = patientVisit;
        treatmentRequested.amountPerFrequencyValue = request.amountPerFrequencyValue;
        treatmentRequested.amountPerFrequencyUnit = request.amountPerFrequencyUnit;
        treatmentRequested.durationValue = request.durationValue;
        this.applyDurationUnit(treatmentRequested, request.durationUnit);
        treatmentRequested.instructions = request.instructions;
        treatmentRequested.route = request.route;
        treatmentRequested.frequencyValue = request.frequencyValue;
        this.applyFrequencyUnit(treatmentRequested, request.frequencyUnit);
        treatmentRequested.totalUnits = request.totalUnits;
        if (batch != null) {
            this.applyBatchToTreatment(treatmentRequested, batch, roundedQuantity, request.unitSellingModelId);
        } else {
            this.applyLegacyItemToTreatment(treatmentRequested, legacyItem, roundedQuantity, request.unitSellingModelId);
        }
        this.applyDiagnosisLink(treatmentRequested, request.diagnosisId, patientVisit.id);
        this.treatmentRequestedRepository.persist(treatmentRequested);
        this.invoiceService.syncInvoiceTotalsForVisit(patientVisit.id);
        TreatmentRequestedDTO dto = new TreatmentRequestedDTO(treatmentRequested);
        return Response.ok(new ResponseMessage("New treatment request created successfully", dto)).build();
    }

    @Transactional
    public Response updateTreatmentRequested(Long treatmentId, TreatmentRequestedRequest request) {
        PatientVisit visit;
        BigDecimal roundedQuantity = request.quantity.setScale(0, RoundingMode.CEILING);
        TreatmentRequested treatment = (TreatmentRequested)(this.treatmentRequestedRepository.findById(treatmentId));
        if (treatment == null) {
            return Response.status(Response.Status.NOT_FOUND).entity(new ResponseMessage("Treatment request not found", null)).build();
        }
        PatientVisit patientVisit = visit = request.visitId != null ? (PatientVisit)(this.patientVisitRepository.findById(request.visitId)) : treatment.visit;
        if (visit == null) {
            visit = treatment.visit;
        }
        if (visit == null || "closed".equals(visit.visitStatus)) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new ResponseMessage("Cannot update treatment. Visit is closed.", null)).build();
        }
        ResolvedMedicine medicine = this.resolveMedicine(request);
        StockBatch batch = medicine.batch;
        Item legacyItem = medicine.legacyItem;
        if (batch == null && legacyItem == null && treatment.stockBatch != null) {
            batch = treatment.stockBatch;
        }
        if (batch == null && legacyItem == null && treatment.itemId != null) {
            legacyItem = (Item)(this.itemRepository.findById(treatment.itemId));
        }
        if (batch == null && legacyItem == null) {
            return Response.status(Response.Status.NOT_FOUND).entity(new ResponseMessage("Item not found", null)).build();
        }
        treatment.quantity = roundedQuantity;
        treatment.provisionalQuantity = roundedQuantity;
        treatment.lastUpDateQuantity = roundedQuantity;
        // Pharmacy qty edits may send only quantity + ids — do not wipe prescribing fields.
        if (request.durationValue != null) {
            treatment.durationValue = request.durationValue;
        }
        if (request.totalUnits != null) {
            treatment.totalUnits = request.totalUnits;
        } else {
            treatment.totalUnits = roundedQuantity;
        }
        if (request.amountPerFrequencyValue != null) {
            treatment.amountPerFrequencyValue = request.amountPerFrequencyValue;
        }
        if (request.amountPerFrequencyUnit != null) {
            treatment.amountPerFrequencyUnit = request.amountPerFrequencyUnit;
        }
        this.applyDurationUnit(treatment, request.durationUnit);
        if (request.frequencyValue != null) {
            treatment.frequencyValue = request.frequencyValue;
        }
        this.applyFrequencyUnit(treatment, request.frequencyUnit);
        if (request.route != null) {
            treatment.route = request.route;
        }
        if (request.instructions != null) {
            treatment.instructions = request.instructions;
        }
        if (batch != null) {
            this.applyBatchToTreatment(treatment, batch, roundedQuantity, request.unitSellingModelId);
        } else {
            this.applyLegacyItemToTreatment(treatment, legacyItem, roundedQuantity, request.unitSellingModelId);
        }
        if (request.diagnosisId != null) {
            this.applyDiagnosisLink(treatment, request.diagnosisId, visit.id);
        }
        this.treatmentRequestedRepository.persist(treatment);
        this.invoiceService.syncInvoiceTotalsForVisit(treatment.visit.id);
        TreatmentRequestedDTO dto = new TreatmentRequestedDTO(treatment);
        return Response.ok(new ResponseMessage("Treatment request updated successfully", dto)).build();
    }

    @Transactional
    public Response updateTreatmentStatus(Long treatmentId, TreatmentStatusUpdateRequest request) {
        boolean wasDispensed;
        boolean hasFlags;
        if (request == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new ResponseMessage("Status or PD/DSP/ADM flags are required", null)).build();
        }
        boolean hasStatus = request.status != null && !request.status.isBlank();
        boolean bl = hasFlags = request.paid != null || request.dispensed != null || request.administered != null;
        if (!hasStatus && !hasFlags) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new ResponseMessage("Status or PD/DSP/ADM flags are required", null)).build();
        }
        TreatmentRequested treatment = (TreatmentRequested)(this.treatmentRequestedRepository.findById(treatmentId));
        if (treatment == null) {
            return Response.status(Response.Status.NOT_FOUND).entity(new ResponseMessage("Treatment request not found", null)).build();
        }
        if (treatment.visit != null && "closed".equals(treatment.visit.visitStatus)) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new ResponseMessage("Visit is closed.", null)).build();
        }
        String previousStatus = treatment.status != null ? treatment.status.trim().toLowerCase() : "pending";
        boolean nowDispensed = wasDispensed = this.isDispensedStatus(previousStatus) || Boolean.TRUE.equals(treatment.dispensed);
        String normalized = previousStatus;
        if (hasStatus) {
            normalized = request.status.trim().toLowerCase();
            if (!List.of("pending", "given", "dispensed", "cancelled").contains(normalized)) {
                return Response.status(Response.Status.BAD_REQUEST).entity(new ResponseMessage("Invalid status. Use pending, given, dispensed, or cancelled.", null)).build();
            }
            nowDispensed = this.isDispensedStatus(normalized);
            treatment.status = normalized;
            if (nowDispensed) {
                treatment.dispensed = true;
                treatment.paid = true;
            } else if ("cancelled".equals(normalized)) {
                treatment.paid = false;
                treatment.dispensed = false;
            } else if ("pending".equals(normalized)) {
                treatment.dispensed = false;
            }
        }
        if (request.paid != null) {
            treatment.paid = request.paid;
        }
        if (request.dispensed != null) {
            nowDispensed = Boolean.TRUE.equals(request.dispensed);
            treatment.dispensed = nowDispensed;
            if (nowDispensed) {
                if (treatment.status == null || "pending".equalsIgnoreCase(treatment.status.trim())) {
                    treatment.status = "dispensed";
                }
                if (treatment.paid == Boolean.FALSE) {
                    treatment.paid = true;
                } else if (treatment.paid == null) {
                    treatment.paid = true;
                }
            } else if (this.isDispensedStatus(treatment.status)) {
                treatment.status = "pending";
            }
        }
        if (request.administered != null) {
            treatment.administered = request.administered;
        }
        if (nowDispensed && !wasDispensed) {
            Response stockError = this.deductStockForTreatmentDispense(treatment);
            if (stockError != null) {
                return stockError;
            }
        } else if (wasDispensed && !nowDispensed) {
            this.restoreStockForTreatmentDispense(treatment);
        }
        treatment.persist();
        if (treatment.visit != null) {
            this.invoiceService.syncInvoiceTotalsForVisit(treatment.visit.id);
        }
        return Response.ok(new ResponseMessage("Prescription updated", new TreatmentRequestedDTO(treatment))).build();
    }

    public List<TreatmentRequestedDTO> getTreatmentRequestedByVisit(Long visitId) {
        List<TreatmentRequested> treatmentGive = TreatmentRequested.find((String)"visit.id = ?1 ORDER BY id DESC", (Object[])new Object[]{visitId}).list();
        return treatmentGive.stream().map(TreatmentRequestedDTO::new).toList();
    }

    @Transactional
    public Response deleteTreatmentRequestById(Long id) {
        TreatmentRequested treatmentRequested = (TreatmentRequested)(this.treatmentRequestedRepository.findById(id));
        if (treatmentRequested == null) {
            return Response.status(Response.Status.NOT_FOUND).entity(new ResponseMessage("Treatment not found", null)).build();
        }
        if ("closed".equals(treatmentRequested.visit.visitStatus)) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new ResponseMessage("Visit is closed. You cannot add anything. Please Open a new visit or contact Admin on 0784411848: ", null)).build();
        }
        Long visitId = treatmentRequested.visit.id;
        boolean wasDispensed = this.isDispensedStatus(treatmentRequested.status);
        this.treatmentRequestedRepository.delete(treatmentRequested);
        if (wasDispensed) {
            this.restoreStockForTreatmentDispense(treatmentRequested);
        }
        this.invoiceService.syncInvoiceTotalsForVisit(visitId);
        return Response.ok(new ResponseMessage(ActionMessages.DELETED.label)).build();
    }

    private void applyDiagnosisLink(TreatmentRequested treatment, Long diagnosisId, Long visitId) {
        Long diagnosisVisitId;
        if (diagnosisId == null) {
            treatment.diagnosis = null;
            return;
        }
        Diagnosis diagnosis = (Diagnosis)(this.diagnosisRepository.findById(diagnosisId));
        if (diagnosis == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(new ResponseMessage("Diagnosis not found: " + diagnosisId, null)).build());
        }
        Long l = diagnosisVisitId = diagnosis.consultation != null && diagnosis.consultation.visit != null ? diagnosis.consultation.visit.id : null;
        if (visitId != null && diagnosisVisitId != null && !visitId.equals(diagnosisVisitId)) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(new ResponseMessage("Diagnosis does not belong to this visit", null)).build());
        }
        treatment.diagnosis = diagnosis;
    }

    private ResolvedMedicine resolveMedicine(TreatmentRequestedRequest request) {
        ResolvedMedicine result = new ResolvedMedicine();
        if (request == null) {
            return result;
        }
        if (request.stockBatchId != null) {
            result.batch = (StockBatch)StockBatch.findById(request.stockBatchId);
            if (result.batch != null) {
                return result;
            }
        }
        if (request.itemId != null) {
            result.legacyItem = (Item)(this.itemRepository.findById(request.itemId));
            if (result.legacyItem != null) {
                return result;
            }
            result.batch = (StockBatch)(StockBatch.find((String)"stockItemId = ?1 ORDER BY stockAtHand DESC", (Object[])new Object[]{request.itemId}).firstResult());
        }
        return result;
    }

    private void applyBatchToTreatment(TreatmentRequested treatment, StockBatch batch, BigDecimal roundedQuantity, Long unitSellingModelId) {
        Long modelId;
        treatment.stockBatch = batch;
        treatment.itemId = batch.stockItemId != null ? batch.stockItemId : treatment.itemId;
        treatment.itemName = batch.stockItemName != null ? batch.stockItemName : "Item";
        treatment.shelfNumber = batch.shelfNumber;
        treatment.unitSellingModelId = modelId = unitSellingModelId != null ? unitSellingModelId : batch.unitSellingModelId;
        treatment.unitSellingPrice = this.unitSellingModelService.resolveUnitSellingPrice(batch.stockItemId, modelId, batch.unitSellingPrice);
        treatment.unitBuy = TreatmentRequestService.nz(batch.unitCostPrice);
        treatment.lastUnitValue = batch.lastUnitValue;
        treatment.availableQuantity = TreatmentRequestService.nz(batch.stockAtHand);
        treatment.provisionalTotalAmount = treatment.totalAmount = roundedQuantity.multiply(treatment.unitSellingPrice);
        treatment.lastStockAtHand = TreatmentRequestService.nz(batch.stockAtHand);
        treatment.lastUpDateQuantity = roundedQuantity;
    }

    private void applyLegacyItemToTreatment(TreatmentRequested treatment, Item item, BigDecimal roundedQuantity, Long unitSellingModelId) {
        BigDecimal sell;
        Long modelId;
        treatment.stockBatch = null;
        treatment.itemId = item.id;
        treatment.itemName = item.title;
        treatment.shelfNumber = item.shelfNumber;
        treatment.unitSellingModelId = modelId = unitSellingModelId != null ? unitSellingModelId : item.unitSellingModelId;
        treatment.unitSellingPrice = sell = this.itemUnitSellingModelService.resolveUnitSellingPrice(item.id, modelId, item.sellingPrice);
        treatment.unitBuy = item.costPrice;
        treatment.lastUnitValue = item.lastUnitValue;
        treatment.availableQuantity = item.stockAtHand;
        treatment.provisionalTotalAmount = treatment.totalAmount = roundedQuantity.multiply(sell);
        treatment.lastStockAtHand = TreatmentRequestService.nz(item.stockAtHand);
        treatment.lastUpDateQuantity = roundedQuantity;
    }

    private boolean isDispensedStatus(String status) {
        if (status == null) {
            return false;
        }
        String normalized = status.trim().toLowerCase();
        return "given".equals(normalized) || "dispensed".equals(normalized);
    }

    private Response deductStockForTreatmentDispense(TreatmentRequested treatment) {
        Item item;
        BigDecimal qty = TreatmentRequestService.nz(treatment.quantity);
        if (qty.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        if (treatment.stockBatch != null) {
            StockBatch batch = treatment.stockBatch;
            BigDecimal stock = TreatmentRequestService.nz(batch.stockAtHand);
            if (qty.compareTo(stock) > 0) {
                return Response.status(Response.Status.BAD_REQUEST).entity(new ResponseMessage("Insufficient stock. Available: " + String.valueOf(stock) + ", Required: " + String.valueOf(qty), "INSUFFICIENT_STOCK")).build();
            }
            batch.stockAtHand = stock.subtract(qty);
            batch.persist();
            treatment.lastStockAtHand = batch.stockAtHand;
        } else if (treatment.itemId != null && (item = (Item)(this.itemRepository.findById(treatment.itemId))) != null) {
            BigDecimal stock = TreatmentRequestService.nz(item.stockAtHand);
            if (qty.compareTo(stock) > 0) {
                return Response.status(Response.Status.BAD_REQUEST).entity(new ResponseMessage("Insufficient stock. Available: " + String.valueOf(stock) + ", Required: " + String.valueOf(qty), "INSUFFICIENT_STOCK")).build();
            }
            this.itemService.updateItemStockAtHandAfterSelling(qty, item);
            treatment.lastStockAtHand = TreatmentRequestService.nz(item.stockAtHand);
        }
        return null;
    }

    private void restoreStockForTreatmentDispense(TreatmentRequested treatment) {
        Item item;
        BigDecimal qty = TreatmentRequestService.nz(treatment.quantity);
        if (qty.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        if (treatment.stockBatch != null) {
            this.restoreBatchStock(treatment.stockBatch, qty);
            treatment.lastStockAtHand = TreatmentRequestService.nz(treatment.stockBatch.stockAtHand);
        } else if (treatment.itemId != null && (item = (Item)(this.itemRepository.findById(treatment.itemId))) != null) {
            this.itemService.updateItemStockAtHandAfterDeleting(qty, item);
            treatment.lastStockAtHand = TreatmentRequestService.nz(item.stockAtHand);
        }
    }

    private void restoreBatchStock(StockBatch batch, BigDecimal qty) {
        if (batch == null || qty == null) {
            return;
        }
        batch.stockAtHand = TreatmentRequestService.nz(batch.stockAtHand).add(qty);
        batch.persist();
    }

    private void applyDurationUnit(TreatmentRequested treatment, BigDecimal durationUnit) {
        if (durationUnit == null) {
            return;
        }
        treatment.durationUnit = this.treatmentPeriodLabel(durationUnit, true);
    }

    private void applyFrequencyUnit(TreatmentRequested treatment, BigDecimal frequencyUnit) {
        if (frequencyUnit == null) {
            return;
        }
        treatment.frequencyUnit = this.treatmentPeriodLabel(frequencyUnit, false);
    }

    private String treatmentPeriodLabel(BigDecimal unit, boolean plural) {
        if (this.isSamePeriodUnit(unit, new BigDecimal("0.00069444"))) {
            return plural ? "Minute(s)" : "Minute";
        }
        if (this.isSamePeriodUnit(unit, new BigDecimal("0.04166667"))) {
            return plural ? "Hour(s)" : "Hour";
        }
        if (this.isSamePeriodUnit(unit, BigDecimal.ONE)) {
            return plural ? "Day(s)" : "Day";
        }
        if (this.isSamePeriodUnit(unit, new BigDecimal("7"))) {
            return plural ? "Week(s)" : "Week";
        }
        if (this.isSamePeriodUnit(unit, new BigDecimal("30"))) {
            return plural ? "Month(s)" : "Month";
        }
        return plural ? "Day(s)" : "Day";
    }

    private boolean isSamePeriodUnit(BigDecimal left, BigDecimal right) {
        return left.setScale(8, RoundingMode.HALF_UP).compareTo(right.setScale(8, RoundingMode.HALF_UP)) == 0;
    }

    private static BigDecimal nz(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private static final class ResolvedMedicine {
        StockBatch batch;
        Item legacyItem;

        private ResolvedMedicine() {
        }
    }
}
