package org.example.pharmacy.otc.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ResponseMessage;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.security.AuthenticatedUserResolver;
import org.example.user.domains.User;
import org.example.finance.billing.services.PatientBillingService;
import org.example.finance.payments.cash.services.PaymentService;
import org.example.finance.payments.cash.services.payloads.requests.PaymentRequest;
import org.example.inventory.item.domain.Item;
import org.example.inventory.item.domain.repositories.ItemRepository;
import org.example.inventory.item.services.ItemUnitSellingModelService;
import org.example.inventory.item.services.ShopItemService;
import org.example.inventory.stock.domains.StockBatch;
import org.example.inventory.stock.domains.StockItem;
import org.example.inventory.stock.services.StockTrackingService;
import org.example.inventory.stock.services.UnitSellingModelService;
import org.example.pharmacy.otc.domains.OtcPharmacySale;
import org.example.pharmacy.otc.domains.OtcPharmacySaleLine;
import org.example.pharmacy.otc.services.payloads.requests.OtcSaleCompleteRequest;
import org.example.pharmacy.otc.services.payloads.requests.OtcSaleLineRequest;
import org.example.pharmacy.otc.services.payloads.responses.OtcPharmacySaleCompleteResult;
import org.example.pharmacy.otc.services.payloads.responses.OtcPharmacySaleDTO;
import org.example.pharmacy.otc.services.payloads.responses.OtcPharmacySaleLineDTO;
import org.example.pharmacy.otc.services.payloads.responses.OtcStockBatchDefaultsDTO;
import org.example.pharmacy.otc.services.payloads.responses.PharmacyDoctorPrescriptionDTO;
import org.example.pharmacy.sundry.domains.VisitSundry;
import org.example.queue.domains.HospitalModule;
import org.example.queue.domains.PatientQueueEntry;
import org.example.queue.domains.repositories.PatientQueueEntryRepository;
import org.example.subscription.services.FacilityBrandingService;
import org.example.subscription.services.SpecialPrivilegeService;
import org.example.treatment.domains.TreatmentRequested;
import org.example.treatment.services.TreatmentRequestService;
import org.example.visit.domains.PatientVisit;
import org.example.visit.services.PatientVisitService;
import org.example.visit.services.paloads.requests.PatientVisitRequest;
import org.example.visit.services.paloads.responses.PatientVisitDTO;
import org.example.finance.invoice.services.InvoiceService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class OtcPharmacySaleService {

    @Inject
    StockTrackingService stockTrackingService;

    /** Kept for CDI compatibility with older Quarkus-generated beans. */
    @Inject
    PatientQueueEntryRepository patientQueueEntryRepository;

    @Inject
    PatientBillingService patientBillingService;

    @Inject
    AuthenticatedUserResolver authenticatedUserResolver;

    @Inject
    TreatmentRequestService treatmentRequestService;

    @Inject
    UnitSellingModelService unitSellingModelService;

    @Inject
    ItemUnitSellingModelService itemUnitSellingModelService;

    @Inject
    ShopItemService shopItemService;

    @Inject
    ItemRepository itemRepository;

    @Inject
    FacilityBrandingService facilityBrandingService;

    @Inject
    PatientVisitService patientVisitService;

    @Inject
    SpecialPrivilegeService specialPrivilegeService;

    @Inject
    InvoiceService invoiceService;

    @Inject
    PaymentService paymentService;

    @Transactional
    public Response completeSale(OtcSaleCompleteRequest request) {
        if (request == null || request.lines == null || request.lines.isEmpty()) {
            return badRequest("Add at least one item to complete the sale.");
        }
        String documentType = normalizeDocumentType(request.documentType);
        boolean deductStock = resolveDeductStock(request.deductStock, documentType);
        boolean documentOnly = !deductStock;
        boolean collectPayment = !Boolean.FALSE.equals(request.recordPayment);
        // Quotation / proforma may still accept payment; it is stored on receipt/invoice/DN.
        if ("receipt".equals(documentType) || "efris_receipt".equals(documentType)) {
            collectPayment = true;
        }
        if (collectPayment && (request.paymentForm == null || request.paymentForm.isBlank())) {
            return badRequest("Payment method is required.");
        }
        if (!collectPayment && (request.paymentForm == null || request.paymentForm.isBlank())) {
            request.paymentForm = documentOnly ? documentTypeLabel(documentType) : "Not paid";
        }

        Map<Long, OtcSaleLineRequest> lineByBatchId = new LinkedHashMap<>();
        Map<Long, OtcSaleLineRequest> lineByItemId = new LinkedHashMap<>();
        Map<Long, OtcSaleLineRequest> lineByStockItemId = new LinkedHashMap<>();
        List<OtcSaleLineRequest> prescriptionLines = new ArrayList<>();
        List<OtcSaleLineRequest> sundryLines = new ArrayList<>();

        for (OtcSaleLineRequest lineReq : request.lines) {
            if (lineReq == null) {
                continue;
            }
            if (Boolean.FALSE.equals(lineReq.given)) {
                continue;
            }
            if (lineReq.visitSundryId != null) {
                sundryLines.add(lineReq);
                continue;
            }
            if (lineReq.treatmentRequestedId != null) {
                prescriptionLines.add(lineReq);
                continue;
            }
            BigDecimal qty = roundQty(lineReq.quantity);
            if (qty.compareTo(BigDecimal.ZERO) <= 0) {
                return badRequest("Quantity must be greater than zero.");
            }
            Long batchId = positiveId(lineReq.stockBatchId);
            Long stockItemId = positiveId(lineReq.stockItemId);
            Long itemId = positiveId(lineReq.itemId);
            if (batchId != null) {
                OtcSaleLineRequest existing = lineByBatchId.get(batchId);
                if (existing == null) {
                    OtcSaleLineRequest merged = new OtcSaleLineRequest();
                    merged.stockBatchId = batchId;
                    merged.quantity = qty;
                    copyPrescribingFields(merged, lineReq);
                    lineByBatchId.put(batchId, merged);
                } else {
                    existing.quantity = nz(existing.quantity).add(qty);
                    copyPrescribingFields(existing, lineReq);
                }
                continue;
            }
            if (stockItemId != null) {
                OtcSaleLineRequest existing = lineByStockItemId.get(stockItemId);
                if (existing == null) {
                    OtcSaleLineRequest merged = new OtcSaleLineRequest();
                    merged.stockItemId = stockItemId;
                    merged.quantity = qty;
                    copyPrescribingFields(merged, lineReq);
                    lineByStockItemId.put(stockItemId, merged);
                } else {
                    existing.quantity = nz(existing.quantity).add(qty);
                    copyPrescribingFields(existing, lineReq);
                }
                continue;
            }
            if (itemId != null) {
                OtcSaleLineRequest existing = lineByItemId.get(itemId);
                if (existing == null) {
                    OtcSaleLineRequest merged = new OtcSaleLineRequest();
                    merged.itemId = itemId;
                    merged.quantity = qty;
                    copyPrescribingFields(merged, lineReq);
                    lineByItemId.put(itemId, merged);
                } else {
                    existing.quantity = nz(existing.quantity).add(qty);
                    copyPrescribingFields(existing, lineReq);
                }
                continue;
            }
            return badRequest("Each OTC line must include a stock batch, stock item, or shop item id.");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<ResolvedLine> resolved = new ArrayList<>();

        for (OtcSaleLineRequest merged : lineByBatchId.values()) {
            ResolvedLine resolvedLine = resolveStockBatchLine(merged, documentOnly);
            if (resolvedLine.error != null) {
                return badRequest(resolvedLine.error);
            }
            totalAmount = totalAmount.add(resolvedLine.lineAmount);
            resolved.add(resolvedLine);
        }

        for (OtcSaleLineRequest merged : lineByStockItemId.values()) {
            ResolvedLine resolvedLine = resolveCatalogStockItemLine(merged, documentOnly);
            if (resolvedLine.error != null) {
                return badRequest(resolvedLine.error);
            }
            totalAmount = totalAmount.add(resolvedLine.lineAmount);
            resolved.add(resolvedLine);
        }

        for (OtcSaleLineRequest merged : lineByItemId.values()) {
            ResolvedLine resolvedLine = resolveShopItemLine(merged, documentOnly);
            if (resolvedLine.error != null) {
                return badRequest(resolvedLine.error);
            }
            totalAmount = totalAmount.add(resolvedLine.lineAmount);
            resolved.add(resolvedLine);
        }

        for (OtcSaleLineRequest rxReq : prescriptionLines) {
            ResolvedLine resolvedLine = resolvePrescriptionLine(rxReq);
            if (resolvedLine.error != null) {
                return badRequest(resolvedLine.error);
            }
            totalAmount = totalAmount.add(resolvedLine.lineAmount);
            resolved.add(resolvedLine);
        }

        for (OtcSaleLineRequest sundryReq : sundryLines) {
            ResolvedLine resolvedLine = resolveVisitSundryLine(sundryReq);
            if (resolvedLine.error != null) {
                return badRequest(resolvedLine.error);
            }
            totalAmount = totalAmount.add(resolvedLine.lineAmount);
            resolved.add(resolvedLine);
        }

        if (totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return badRequest("Sale total must be greater than zero.");
        }

        BigDecimal discount = nz(request.discount);
        if (discount.compareTo(BigDecimal.ZERO) < 0) {
            return badRequest("Discount cannot be negative.");
        }
        if (discount.compareTo(totalAmount) > 0) {
            discount = totalAmount;
        }
        BigDecimal tax = nz(request.tax);
        if (tax.compareTo(BigDecimal.ZERO) < 0) {
            return badRequest("Tax cannot be negative.");
        }
        // Final document total = line subtotal − overall discount + tax.
        // Prefer explicit payableTotal from POS when provided.
        BigDecimal amountDue = totalAmount.subtract(discount).add(tax);
        if (request.payableTotal != null && request.payableTotal.compareTo(BigDecimal.ZERO) > 0) {
            amountDue = request.payableTotal;
        }

        BigDecimal amountReceived;
        if (!collectPayment) {
            amountReceived = request.amountReceived != null
                    ? request.amountReceived.max(BigDecimal.ZERO)
                    : BigDecimal.ZERO;
            if (amountReceived.compareTo(BigDecimal.ZERO) < 0) {
                amountReceived = BigDecimal.ZERO;
            }
        } else {
            amountReceived = request.amountReceived != null
                    ? request.amountReceived
                    : amountDue;
            if (amountReceived.compareTo(BigDecimal.ZERO) < 0) {
                return badRequest("Amount received cannot be negative.");
            }
        }
        // Tendered cash for the order (may be stored on receipt/invoice/DN only).
        BigDecimal tenderedPayment = amountReceived;
        // Quotation / proforma themselves never show paid amounts.
        BigDecimal amountOnThisDocument = documentIndicatesPayment(documentType)
                ? tenderedPayment
                : BigDecimal.ZERO;
        Long patientId = positiveId(request.patientId);
        // Visit prescriptions are already tagged to a client — resolve if missing.
        if (patientId == null && request.visitId != null) {
            PatientVisit visitForPatient = PatientVisit.findById(request.visitId);
            if (visitForPatient != null && visitForPatient.patient != null) {
                patientId = visitForPatient.patient.id;
            }
        }
        // Every completed sale must be attached to a client (via patient or visit).
        if (patientId == null && request.visitId == null) {
            return badRequest("Attach a client before completing the sale.");
        }
        if (collectPayment && amountReceived.compareTo(amountDue) < 0 && patientId == null
                && request.visitId == null) {
            return badRequest("Select a client to tag unpaid balance, or pay the full amount.");
        }
        if (amountReceived.compareTo(amountDue) > 0) {
            // keep change; no-op
        }

        OtcPharmacySale existing = findSaleForUpsert(request.visitId, documentType, request.saleId);
        boolean revising = existing != null && existing.id != null;
        BigDecimal previousDue = revising ? nz(existing.totalAmount) : BigDecimal.ZERO;
        BigDecimal previousPaid = revising
                ? nz(existing.amountReceived).min(previousDue)
                : BigDecimal.ZERO;
        BigDecimal previousUnpaid = previousDue.subtract(previousPaid);
        if (previousUnpaid.compareTo(BigDecimal.ZERO) < 0) {
            previousUnpaid = BigDecimal.ZERO;
        }
        OtcPharmacySale sale;
        if (revising) {
            sale = existing;
            clearSaleLines(sale);
            sale.saleDate = LocalDate.now();
            sale.saleTime = LocalTime.now();
            sale.totalAmount = amountDue;
            sale.amountReceived = amountOnThisDocument;
            sale.changeAmount = amountOnThisDocument.compareTo(amountDue) > 0
                    ? amountOnThisDocument.subtract(amountDue)
                    : BigDecimal.ZERO;
            sale.paymentForm = request.paymentForm.trim();
            sale.receivedBy = request.receivedBy != null ? request.receivedBy.trim() : null;
            sale.documentType = documentType;
        } else {
            sale = new OtcPharmacySale();
            sale.salePlainNo = nextSalePlainNo();
            sale.saleDate = LocalDate.now();
            sale.saleTime = LocalTime.now();
            sale.totalAmount = amountDue;
            sale.amountReceived = amountOnThisDocument;
            sale.changeAmount = amountOnThisDocument.compareTo(amountDue) > 0
                    ? amountOnThisDocument.subtract(amountDue)
                    : BigDecimal.ZERO;
            sale.paymentForm = request.paymentForm.trim();
            sale.receivedBy = request.receivedBy != null ? request.receivedBy.trim() : null;
            sale.documentType = documentType;
            sale.active = true;
        }
        String notes = request.notes;
        if (documentOnly) {
            String docNote = documentTypeLabel(documentType) + " — stock not deducted";
            notes = (notes == null || notes.isBlank())
                    ? docNote
                    : notes.trim() + " | " + docNote;
        } else if (!collectPayment && tenderedPayment.compareTo(BigDecimal.ZERO) <= 0) {
            String unpaidNote = "Dispensed — money not received";
            notes = (notes == null || notes.isBlank())
                    ? unpaidNote
                    : notes.trim() + " | " + unpaidNote;
        }
        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            String discountNote = "Discount: " + discount;
            notes = (notes == null || notes.isBlank())
                    ? discountNote
                    : notes.trim() + " | " + discountNote;
        }
        if (tax.compareTo(BigDecimal.ZERO) > 0) {
            String taxNote = "Tax: " + tax;
            notes = (notes == null || notes.isBlank())
                    ? taxNote
                    : notes.trim() + " | " + taxNote;
        }
        // Visit invoice keeps the balance when money is not collected — do not
        // bump patient.totalAmountDue for no-payment dispenses.
        boolean hasTender = tenderedPayment.compareTo(BigDecimal.ZERO) > 0;
        BigDecimal unpaid = (collectPayment || hasTender)
                ? amountDue.subtract(tenderedPayment.min(amountDue))
                : BigDecimal.ZERO;
        if (unpaid.compareTo(BigDecimal.ZERO) < 0) {
            unpaid = BigDecimal.ZERO;
        }
        if (collectPayment && unpaid.compareTo(BigDecimal.ZERO) > 0) {
            String debtNote = "Unpaid on account: " + unpaid;
            notes = (notes == null || notes.isBlank())
                    ? debtNote
                    : notes.trim() + " | " + debtNote;
        }
        if (revising) {
            String reviseNote = "Updated " + documentTypeLabel(documentType);
            notes = (notes == null || notes.isBlank())
                    ? reviseNote
                    : notes.trim() + " | " + reviseNote;
        }
        sale.notes = notes;

        org.example.client.domains.Patient taggedPatient = null;
        if (patientId != null) {
            taggedPatient = org.example.client.domains.Patient.findById(patientId);
            if (taggedPatient == null) {
                return badRequest("Client not found (id " + patientId + ").");
            }
            sale.patientId = patientId;
            String resolvedName = trimOrNull(request.patientName);
            if (resolvedName == null) {
                String first = taggedPatient.patientFirstName != null ? taggedPatient.patientFirstName : "";
                String second = taggedPatient.patientSecondName != null ? taggedPatient.patientSecondName : "";
                resolvedName = (first + " " + second).trim();
            }
            sale.patientName = resolvedName.isBlank() ? null : resolvedName;
        } else if (request.patientName != null && !request.patientName.isBlank()) {
            sale.patientName = request.patientName.trim();
        }
        if (taggedPatient != null && request.visitId != null && request.visitId > 0) {
            attachClientToExistingOrder(request.visitId, taggedPatient, sale.patientName);
        }

        // OTC debt without an existing visit → create an "Over the counter" visit and
        // record lines as treatmentrequested so debt appears on the client's visits.
        String priorDebtWarning = null;
        PatientVisit otcDebtVisit = null;
        if (!revising
                && collectPayment
                && unpaid.compareTo(BigDecimal.ZERO) > 0
                && taggedPatient != null
                && request.visitId == null) {
            Response debtGate = gateOtcDebtAgainstPriorVisits(taggedPatient);
            if (debtGate != null) {
                return debtGate;
            }
            BigDecimal priorVisitDebt = invoiceService.calculateTotalBalanceDueForAllVisits(taggedPatient.id);
            BigDecimal accountDue = taggedPatient.totalAmountDue != null
                    ? taggedPatient.totalAmountDue
                    : BigDecimal.ZERO;
            if (priorVisitDebt.compareTo(BigDecimal.ZERO) > 0 || accountDue.compareTo(BigDecimal.ZERO) > 0) {
                priorDebtWarning = "Client has a debt on one of the visits";
            }
            otcDebtVisit = createOtcCounterVisit(taggedPatient);
            if (otcDebtVisit == null || otcDebtVisit.id == null) {
                return badRequest("Could not create over-the-counter visit for this debt.");
            }
            request.visitId = otcDebtVisit.id;
            sale.visitId = otcDebtVisit.id;
        }

        if (request.visitId != null) {
            sale.visitId = request.visitId;
            if (sale.patientName == null) {
                sale.patientName = trimOrNull(request.patientName);
            }
            if (!revising || sale.saleNo == null || sale.saleNo.isBlank()) {
                PatientVisit visit = PatientVisit.findById(request.visitId);
                if (visit != null && visit.visitNumber > 0) {
                    sale.saleNo = "ORDER-" + visit.visitNumber;
                } else {
                    sale.saleNo = "ORDER-" + request.visitId;
                }
            }
        } else if (!revising) {
            if (documentOnly) {
                String prefix = switch (documentType) {
                    case "quotation" -> "Q";
                    case "proforma" -> "PF";
                    case "sales_order" -> "SO";
                    case "sales_invoice" -> "INV";
                    case "efris_receipt" -> "EFR";
                    case "delivery_note" -> "DN";
                    default -> "DOC";
                };
                sale.saleNo = prefix + "-" + sale.salePlainNo;
            } else if ("efris_receipt".equals(documentType)) {
                sale.saleNo = "EFR-" + sale.salePlainNo;
            } else if ("delivery_note".equals(documentType)) {
                sale.saleNo = "DN-" + sale.salePlainNo;
            } else {
                sale.saleNo = "OTC-" + sale.salePlainNo;
            }
        }
        if (!revising) {
            sale.persist();
        }

        // Revising never re-deducts stock — inventory already moved on first stock sale.
        boolean skipStockMoves = revising || documentOnly;
        for (ResolvedLine r : resolved) {
            if (r.visitSundry != null) {
                recordVisitSundrySaleLine(sale, r);
            } else if (skipStockMoves || r.deferStockDeduction || r.catalogStockItem != null) {
                // Catalog StockItem lines (services / batchless) never deduct shop Item stock here.
                recordLineWithoutStockDeduction(sale, r);
            } else if (r.shopItem != null) {
                deductShopItemAndRecord(sale, r);
            } else {
                deductBatchAndRecord(sale, r);
            }
            if (!collectPayment && r.treatment != null) {
                r.treatment.paid = false;
                r.treatment.persist();
            }
        }

        // Attach OTC product lines to the linked visit invoice so Payment / Receipt
        // tabs on Client Transactions show charges and can record payment.
        // (Prescription lines already have TreatmentRequested rows on the visit.)
        if (request.visitId != null && !documentOnly) {
            PatientVisit linkedVisit = PatientVisit.findById(request.visitId);
            if (linkedVisit != null) {
                if (!revising) {
                    for (ResolvedLine r : resolved) {
                        if (r.visitSundry != null) {
                            continue;
                        }
                        if (r.treatment != null && r.treatment.id != null) {
                            continue;
                        }
                        // paid=true means "include on visit invoice" (billing flag), not cash settled.
                        createOtcDebtTreatment(linkedVisit, r);
                    }
                } else {
                    // Revision added products / raised total: push only the new unpaid
                    // delta onto the visit invoice so balanceDue can be collected later.
                    BigDecimal unpaidDelta = unpaid.subtract(previousUnpaid);
                    if (unpaidDelta.compareTo(BigDecimal.ZERO) > 0) {
                        if (createOtcBalanceAdjustmentTreatment(linkedVisit, unpaidDelta)) {
                            invoiceService.syncInvoiceTotalsForVisit(linkedVisit.id);
                        } else {
                            bumpVisitInvoiceOpenBalance(linkedVisit, unpaidDelta);
                        }
                    } else {
                        invoiceService.syncInvoiceTotalsForVisit(linkedVisit.id);
                    }
                }
                if (!revising) {
                    invoiceService.syncInvoiceTotalsForVisit(linkedVisit.id);
                }
            }
        }

        // Prefer visit invoice balance over raw patient.totalAmountDue bump when we created an OTC visit.
        if (!revising && collectPayment && taggedPatient != null && unpaid.compareTo(BigDecimal.ZERO) > 0
                && otcDebtVisit == null && request.visitId == null) {
            BigDecimal current = taggedPatient.totalAmountDue != null
                    ? taggedPatient.totalAmountDue
                    : BigDecimal.ZERO;
            taggedPatient.totalAmountDue = current.add(unpaid);
            taggedPatient.persist();
        } else if (revising && collectPayment && taggedPatient != null && request.visitId == null) {
            BigDecimal unpaidDelta = unpaid.subtract(previousUnpaid);
            if (unpaidDelta.compareTo(BigDecimal.ZERO) != 0) {
                BigDecimal current = taggedPatient.totalAmountDue != null
                        ? taggedPatient.totalAmountDue
                        : BigDecimal.ZERO;
                BigDecimal next = current.add(unpaidDelta);
                if (next.compareTo(BigDecimal.ZERO) < 0) {
                    next = BigDecimal.ZERO;
                }
                taggedPatient.totalAmountDue = next;
                taggedPatient.persist();
            }
        }

        OtcPharmacySaleCompleteResult result = new OtcPharmacySaleCompleteResult(new OtcPharmacySaleDTO(sale));
        String message;
        if (revising) {
            message = documentTypeLabel(documentType) + " updated — " + sale.saleNo
                    + ". Earlier order documents synced.";
            if (unpaid.compareTo(BigDecimal.ZERO) > 0) {
                message += ". Amount due Sh:" + unpaid + " remains on the order.";
            }
        } else if (documentOnly) {
            message = documentTypeLabel(documentType) + " saved — " + sale.saleNo + " (stock not deducted)";
        } else if (collectPayment) {
            message = "OTC sale completed — " + sale.saleNo;
        } else {
            message = "Prescription dispensed (no payment) — " + sale.saleNo;
        }
        if (priorDebtWarning != null) {
            message = priorDebtWarning + ". " + message;
        }
        if (!revising && collectPayment && unpaid.compareTo(BigDecimal.ZERO) > 0 && sale.patientName != null) {
            if (otcDebtVisit != null) {
                message += ". Balance Sh:" + unpaid + " recorded on Over the counter visit for "
                        + sale.patientName;
            } else {
                message += ". Balance Sh:" + unpaid + " tagged to " + sale.patientName;
            }
        }

        if (!revising && request.visitId != null && amountDue.compareTo(BigDecimal.ZERO) > 0) {
            List<Long> treatmentIds = new ArrayList<>();
            for (OtcSaleLineRequest line : prescriptionLines) {
                if (line != null && line.treatmentRequestedId != null) {
                    treatmentIds.add(line.treatmentRequestedId);
                }
            }
            for (ResolvedLine r : resolved) {
                if (r.treatment != null && r.treatment.id != null && !treatmentIds.contains(r.treatment.id)) {
                    treatmentIds.add(r.treatment.id);
                }
            }
            String paymentNotes = request.notes != null && !request.notes.isBlank()
                    ? request.notes
                    : ("receipt".equals(documentType)
                            ? "OTC receipt " + sale.saleNo
                            : ("efris_receipt".equals(documentType)
                                    ? "OTC EFRIS receipt " + sale.saleNo
                                    : "OTC counter payment"));
            if (otcDebtVisit != null) {
                // OTC debt visit: treatments already marked given/paid; record only cash received.
                invoiceService.syncInvoiceTotalsForVisit(request.visitId);
                if (tenderedPayment.compareTo(BigDecimal.ZERO) > 0) {
                    PaymentRequest paymentTemplate = PatientBillingService.pharmacyPaymentTemplate(
                            request.paymentForm,
                            request.receivedBy,
                            paymentNotes);
                    boolean paymentRecorded = patientBillingService.settlePharmacyDispense(
                            request.visitId,
                            treatmentIds,
                            tenderedPayment.min(amountDue),
                            paymentTemplate);
                    result.visitPaymentRecorded = paymentRecorded;
                    if (paymentRecorded) {
                        message += ". Visit payment recorded.";
                    }
                } else {
                    result.visitPaymentRecorded = false;
                }
            } else if (documentOnly && hasTender) {
                // Quotation/proforma with cash: invoice lines are not on the visit yet.
                // Record the Payments row after pipeline docs + invoice balance are prepared below.
                patientBillingService.syncInvoiceAfterPharmacyDispense(request.visitId);
                result.visitPaymentRecorded = false;
            } else if (collectPayment || hasTender) {
                PaymentRequest paymentTemplate = PatientBillingService.pharmacyPaymentTemplate(
                        request.paymentForm,
                        request.receivedBy,
                        paymentNotes);
                BigDecimal payAmount = hasTender
                        ? tenderedPayment.min(amountDue)
                        : amountDue;
                boolean paymentRecorded = patientBillingService.settlePharmacyDispense(
                        request.visitId,
                        treatmentIds,
                        payAmount,
                        paymentTemplate);
                result.visitPaymentRecorded = paymentRecorded;
                if (paymentRecorded) {
                    message += ". Visit payment recorded.";
                }
            } else {
                patientBillingService.syncInvoiceAfterPharmacyDispense(request.visitId);
                result.visitPaymentRecorded = false;
                message += ". No payment recorded — balance remains on visit invoice.";
            }
            PatientVisit visit = PatientVisit.findById(request.visitId);
            if (visit != null) {
                result.amountPaid = visit.amountPaid;
                result.balanceDue = visit.balanceDue;
                result.totalAmount = visit.totalAmount;
                result.visitId = visit.id;
            }
        }

        // Order pipeline: Quotation → Proforma → Sales Invoice → Receipt → Delivery.
        // Starting at any later stage creates missing earlier docs with the same
        // products / qty / prices / totals, then keeps them synced on every save/edit.
        if (sale.visitId != null) {
            int created = ensureEarlierPipelineDocuments(sale);
            int synced = syncEarlierPipelineDocuments(sale);
            if (created > 0 && !revising) {
                message += ". " + created + " earlier document(s) auto-filled with the same products and amounts.";
            }
            if (synced > 0) {
                message += revising
                        ? " (" + synced + " earlier document(s) updated)."
                        : (created > 0 ? "" : ". " + synced + " earlier document(s) kept in sync.");
            }
            // Payment at any stage (including quotation): create Q → PF → INV → Receipt
            // if missing, open invoice balance, then insert a real Payments row.
            if (tenderedPayment.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal payAmount = tenderedPayment.min(amountDue);
                int payDocs = ensurePaymentStageDocuments(sale.visitId, sale);
                ensureReceiptDocumentForOrder(sale.visitId);
                PatientVisit payVisit = PatientVisit.findById(sale.visitId);
                ensureVisitInvoiceCoversAmount(payVisit, payAmount);

                if (!result.visitPaymentRecorded) {
                    PaymentRequest payReq = PatientBillingService.pharmacyPaymentTemplate(
                            request.paymentForm,
                            request.receivedBy,
                            request.notes != null && !request.notes.isBlank()
                                    ? request.notes.trim()
                                    : ("Order payment on " + documentTypeLabel(documentType)
                                            + " " + sale.saleNo));
                    payReq.amountToPay = payAmount;
                    payReq.status = "Approved";
                    Response payRes = paymentService.createNewPayment(sale.visitId, payReq);
                    if (payRes.getStatus() < 400) {
                        result.visitPaymentRecorded = true;
                        message += ". Payment of Sh:" + payAmount + " recorded in payments.";
                    } else {
                        message += ". Could not record visit payment — check invoice balance.";
                    }
                }

                BigDecimal paidNow = nz(paymentService.getTotalPaymentOfVisit(sale.visitId));
                syncPaymentIndicationDocuments(
                        sale.visitId,
                        paidNow.min(amountDue),
                        request.paymentForm,
                        request.receivedBy);
                if (payDocs > 0) {
                    message += ". " + payDocs
                            + " document(s) prepared (quotation / proforma / invoice / receipt).";
                }
                message += ". Paid amount indicated on receipt / sales invoice / delivery note.";

                payVisit = PatientVisit.findById(sale.visitId);
                if (payVisit != null) {
                    result.amountPaid = payVisit.amountPaid;
                    result.balanceDue = payVisit.balanceDue;
                    result.totalAmount = payVisit.totalAmount;
                    result.visitId = payVisit.id;
                }
            }
        }

        return Response.ok(new ResponseMessage(message, result)).build();
    }

    @Transactional
    public Response listSales(LocalDate dateFrom, LocalDate dateTo) {
        LocalDate from = dateFrom != null ? dateFrom : LocalDate.now().minusDays(7);
        LocalDate to = dateTo != null ? dateTo : LocalDate.now();

        List<OtcPharmacySaleLineDTO> rows = new ArrayList<>();

        List<OtcPharmacySaleLine> lines = OtcPharmacySaleLine.find(
                "sale.saleDate >= ?1 and sale.saleDate <= ?2 order by sale.saleDate desc, sale.saleTime desc, id desc",
                from,
                to
        ).list();
        for (OtcPharmacySaleLine line : lines) {
            rows.add(new OtcPharmacySaleLineDTO(line));
        }

        Set<Long> visitIdsWithPharmacySale = new HashSet<>();
        @SuppressWarnings("unchecked")
        List<OtcPharmacySale> pharmacySales = OtcPharmacySale.find(
                "saleDate >= ?1 and saleDate <= ?2 and visitId is not null",
                from,
                to
        ).list();
        for (OtcPharmacySale sale : pharmacySales) {
            if (sale.visitId != null) {
                visitIdsWithPharmacySale.add(sale.visitId);
            }
        }

        @SuppressWarnings("unchecked")
        List<TreatmentRequested> treatments = TreatmentRequested.find(
                "visit.visitDate >= ?1 and visit.visitDate <= ?2 order by visit.visitDate desc, id desc",
                from,
                to
        ).list();
        for (TreatmentRequested treatment : treatments) {
            if (!isDispensedStatus(treatment.status)) {
                continue;
            }
            if (treatment.visit != null && visitIdsWithPharmacySale.contains(treatment.visit.id)) {
                continue;
            }
            rows.add(new OtcPharmacySaleLineDTO(treatment));
        }

        rows.sort(Comparator
                .comparing((OtcPharmacySaleLineDTO r) -> r.saleDate, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing((OtcPharmacySaleLineDTO r) -> r.saleTime, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing((OtcPharmacySaleLineDTO r) -> r.saleId, Comparator.nullsLast(Comparator.reverseOrder())));

        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, rows)).build();
    }

    /**
     * Sale-level commercial documents for a visit and/or patient (quotation, proforma, etc.).
     */
    @Transactional
    public Response listSaleDocuments(Long visitId, Long patientId, String documentType) {
        if ((visitId == null || visitId <= 0) && (patientId == null || patientId <= 0)) {
            return badRequest("Provide visitId and/or patientId.");
        }
        String wanted = documentType != null && !documentType.isBlank()
                ? normalizeDocumentType(documentType)
                : null;

        List<OtcPharmacySale> sales;
        if (visitId != null && visitId > 0 && patientId != null && patientId > 0) {
            sales = OtcPharmacySale.find(
                    "visitId = ?1 or patientId = ?2 order by saleDate desc, saleTime desc, id desc",
                    visitId,
                    patientId
            ).list();
        } else if (visitId != null && visitId > 0) {
            sales = OtcPharmacySale.find(
                    "visitId = ?1 order by saleDate desc, saleTime desc, id desc",
                    visitId
            ).list();
        } else {
            sales = OtcPharmacySale.find(
                    "patientId = ?1 order by saleDate desc, saleTime desc, id desc",
                    patientId
            ).list();
        }

        LinkedHashMap<Long, OtcPharmacySale> unique = new LinkedHashMap<>();
        for (OtcPharmacySale sale : sales) {
            if (sale == null || sale.id == null) {
                continue;
            }
            if (wanted != null) {
                String saleType = normalizeDocumentType(sale.documentType);
                if (!wanted.equals(saleType)) {
                    continue;
                }
            }
            unique.putIfAbsent(sale.id, sale);
        }

        List<OtcPharmacySaleDTO> rows = unique.values().stream()
                .map(OtcPharmacySaleDTO::new)
                .collect(Collectors.toList());
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, rows)).build();
    }

    /**
     * Create a missing commercial document for an order (visit), cloning products
     * from the latest previous pipeline document. Also creates any missing stages
     * between the source and the requested type (e.g. Q only → create PF then INV).
     */
    @Transactional
    public Response ensureOrderDocument(Long visitId, String documentType) {
        if (visitId == null || visitId <= 0) {
            return badRequest("A valid order (visit) id is required.");
        }
        String wanted = normalizeDocumentType(documentType);
        if (wanted == null || wanted.isBlank()) {
            return badRequest("documentType is required (quotation, proforma, sales_invoice, receipt, delivery_note).");
        }
        if ("efris_receipt".equals(wanted)) {
            wanted = "receipt";
        }
        int targetRank = pipelineRank(wanted);
        if (targetRank < 0) {
            return badRequest("Unsupported document type: " + documentType);
        }

        @SuppressWarnings("unchecked")
        List<OtcPharmacySale> sales = OtcPharmacySale.find(
                "visitId = ?1 order by id desc",
                visitId
        ).list();
        if (sales == null || sales.isEmpty()) {
            return badRequest("No documents exist on this order yet. Create a quotation or sale first.");
        }

        if (visitHasDocumentType(visitId, wanted)) {
            OtcPharmacySale existing = null;
            for (OtcPharmacySale sale : sales) {
                if (sale != null && wanted.equals(normalizeDocumentType(sale.documentType))) {
                    existing = sale;
                    break;
                }
            }
            java.util.Map<String, Object> already = new java.util.LinkedHashMap<>();
            already.put("created", 0);
            already.put("documentType", wanted);
            already.put("document", existing != null ? new OtcPharmacySaleDTO(existing) : null);
            return Response.ok(new ResponseMessage(
                    documentTypeLabel(wanted) + " already exists on this order.",
                    already)).build();
        }

        OtcPharmacySale source = null;
        int sourceRank = -1;
        for (OtcPharmacySale sale : sales) {
            if (sale == null) {
                continue;
            }
            int rank = pipelineRank(sale.documentType);
            // Prefer the highest stage that is at or before the target (previous docs).
            if (rank <= targetRank && rank >= sourceRank) {
                source = sale;
                sourceRank = rank;
            }
        }
        if (source == null) {
            // Fall back to any sale on the order.
            source = sales.get(0);
        }
        if (source == null) {
            return badRequest("No source document found to copy products from.");
        }

        String[] pipeline = {
                "quotation", "proforma", "sales_invoice", "receipt", "delivery_note"
        };
        List<OtcPharmacySaleLine> sourceLines = loadActiveSaleLines(source);
        int created = 0;
        for (String type : pipeline) {
            int rank = pipelineRank(type);
            if (rank > targetRank) {
                break;
            }
            if (visitHasDocumentType(visitId, type)) {
                continue;
            }
            cloneSaleAsPipelineDocument(source, sourceLines, type);
            created++;
        }

        OtcPharmacySale createdDoc = null;
        @SuppressWarnings("unchecked")
        List<OtcPharmacySale> refreshed = OtcPharmacySale.find(
                "visitId = ?1 order by id desc",
                visitId
        ).list();
        for (OtcPharmacySale sale : refreshed) {
            if (sale != null && wanted.equals(normalizeDocumentType(sale.documentType))) {
                createdDoc = sale;
                break;
            }
        }

        // Keep paid figures aligned on indicating docs after cloning.
        BigDecimal visitPaid = nz(paymentService.getTotalPaymentOfVisit(visitId));
        BigDecimal orderDue = computeOrderPayableTotal(visitId);
        if (orderDue.compareTo(BigDecimal.ZERO) > 0) {
            syncPaymentIndicationDocuments(
                    visitId,
                    visitPaid.min(orderDue),
                    null,
                    null);
        }

        java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("created", created);
        result.put("documentType", wanted);
        result.put("document", createdDoc != null ? new OtcPharmacySaleDTO(createdDoc) : null);
        result.put("sourceSaleNo", source.saleNo);
        return Response.ok(new ResponseMessage(
                documentTypeLabel(wanted) + " created from "
                        + documentTypeLabel(source.documentType)
                        + (source.saleNo != null ? " " + source.saleNo : "")
                        + (created > 1 ? " (" + created + " stage(s) filled)." : "."),
                result)).build();
    }

    /**
     * After a payment is reversed (or otherwise changed), realign OTC sale
     * amountReceived figures with the sum of active visit payments.
     */
    @Transactional
    public Response reconcileOrderPaidFromVisitPayments(Long visitId) {
        if (visitId == null || visitId <= 0) {
            return badRequest("A valid order (visit) id is required.");
        }
        BigDecimal visitPaid = nz(paymentService.getTotalPaymentOfVisit(visitId));
        BigDecimal orderDue = computeOrderPayableTotal(visitId);
        BigDecimal indicatedPaid = orderDue.compareTo(BigDecimal.ZERO) > 0
                ? visitPaid.min(orderDue)
                : visitPaid;
        syncPaymentIndicationDocuments(visitId, indicatedPaid, null, null);
        invoiceService.syncInvoiceTotalsForVisit(visitId);

        java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("visitId", visitId);
        result.put("amountPaid", indicatedPaid);
        result.put("totalAmount", orderDue);
        result.put("visitPaid", visitPaid);
        return Response.ok(new ResponseMessage(
                "Order paid amounts reconciled from active payments.",
                result)).build();
    }

    /**
     * Activate or deactivate an order. When visitId is set, all OTC sales on that
     * visit are updated; otherwise the single saleId is updated.
     */
    @Transactional
    public Response setOrderActive(Long visitId, Long saleId, boolean active) {
        List<OtcPharmacySale> sales;
        if (visitId != null && visitId > 0) {
            @SuppressWarnings("unchecked")
            List<OtcPharmacySale> byVisit = OtcPharmacySale.find("visitId", visitId).list();
            sales = byVisit;
        } else if (saleId != null && saleId > 0) {
            OtcPharmacySale sale = OtcPharmacySale.findById(saleId);
            if (sale == null) {
                return badRequest("Sale not found.");
            }
            if (sale.visitId != null && sale.visitId > 0) {
                @SuppressWarnings("unchecked")
                List<OtcPharmacySale> byVisit = OtcPharmacySale.find("visitId", sale.visitId).list();
                sales = byVisit;
            } else {
                sales = List.of(sale);
            }
        } else {
            return badRequest("Provide visitId or saleId.");
        }
        if (sales == null || sales.isEmpty()) {
            return badRequest("No sale documents found for this order.");
        }
        int updated = 0;
        for (OtcPharmacySale sale : sales) {
            if (sale == null) {
                continue;
            }
            sale.active = active;
            sale.persist();
            updated++;
        }
        java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("active", active);
        result.put("updated", updated);
        result.put("visitId", visitId);
        result.put("saleId", saleId);
        return Response.ok(new ResponseMessage(
                active ? "Order turned on." : "Order turned off.",
                result)).build();
    }

    @Transactional
    public Response listPendingDoctorPrescriptions(Long toModuleId) {
        // Only visits on the pharmacy queue board (not cancelled/discharged).
        Set<Long> queuedVisitIds = pharmacyQueuedVisitIds(toModuleId);
        if (queuedVisitIds.isEmpty()) {
            return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, List.of())).build();
        }

        @SuppressWarnings("unchecked")
        List<TreatmentRequested> treatments = TreatmentRequested.find(
                "lower(status) = 'pending' and visit is not null and visit.visitStatus <> 'closed' "
                        + "and (dispensed is null or dispensed = false) "
                        + "and visit.id in ?1 "
                        + "order by visit.visitDate desc, visit.visitTime desc, id desc",
                queuedVisitIds
        ).list();
        List<PharmacyDoctorPrescriptionDTO> rows = new ArrayList<>(treatments.stream()
                .map(PharmacyDoctorPrescriptionDTO::new)
                .toList());

        // Undispensed visit sundries — same queue / dispense flow as drugs (stock already deducted).
        @SuppressWarnings("unchecked")
        List<VisitSundry> sundries = VisitSundry.find(
                "dispensed is null or dispensed = false order by id desc"
        ).list();
        for (VisitSundry sundry : sundries) {
            if (sundry == null || sundry.patientVisitId == null) {
                continue;
            }
            if (!queuedVisitIds.contains(sundry.patientVisitId)) {
                continue;
            }
            PatientVisit visit = PatientVisit.findById(sundry.patientVisitId);
            if (visit == null) {
                continue;
            }
            String status = visit.visitStatus != null ? visit.visitStatus.trim() : "";
            if ("closed".equalsIgnoreCase(status)) {
                continue;
            }
            rows.add(new PharmacyDoctorPrescriptionDTO(sundry, visit));
        }
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, rows)).build();
    }

    /**
     * Visit ids currently on the pharmacy queue board (not cancelled/discharged).
     * Uses {@code toModuleId} when provided; otherwise resolves the PHARMACY module.
     */
    private Set<Long> pharmacyQueuedVisitIds(Long toModuleId) {
        Long moduleId = toModuleId != null && toModuleId > 0 ? toModuleId : null;
        if (moduleId == null) {
            HospitalModule pharmacy = HospitalModule.find("code", "PHARMACY").firstResult();
            if (pharmacy == null) {
                pharmacy = HospitalModule.find("lower(routeKey) in ('pharmacy', 'otc-dispensary')").firstResult();
            }
            if (pharmacy != null) {
                moduleId = pharmacy.id;
            }
        }
        if (moduleId == null) {
            return Set.of();
        }

        // Match pharmacy queue board: keep COMPLETED until discharged/cancelled.
        @SuppressWarnings("unchecked")
        List<PatientQueueEntry> entries = patientQueueEntryRepository.list(
                "toModule.id = ?1 and upper(status) not in ('CANCELLED', 'DISCHARGED')",
                moduleId);
        Set<Long> visitIds = new HashSet<>();
        for (PatientQueueEntry entry : entries) {
            if (entry == null || entry.patientVisit == null || entry.patientVisit.id == null) {
                continue;
            }
            visitIds.add(entry.patientVisit.id);
        }
        return visitIds;
    }

    @Transactional
    public Response reverseSoldLine(Long lineId, String source) {
        if (lineId == null || lineId <= 0) {
            return badRequest("Line id is required.");
        }
        // OTC and RX prescription sale lines reverse the same way; DSP is cleared
        // when no active (non-reversed) sale lines remain for the treatment.
        return reverseOtcSoldLine(lineId);
    }

    /**
     * Receive payment on one order (visit): records a visit payment and applies the
     * same amount to unpaid OTC sales for that visit (oldest first).
     */
    @Transactional
    public Response payOrderDebt(Long visitId, PaymentRequest request) {
        if (visitId == null || visitId <= 0) {
            return badRequest("A valid order (visit) id is required.");
        }
        if (request == null || request.amountToPay == null
                || request.amountToPay.compareTo(BigDecimal.ZERO) <= 0) {
            return badRequest("Amount to pay must be greater than zero.");
        }
        if (request.paymentForm == null || request.paymentForm.isBlank()) {
            request.paymentForm = "Cash At Hand";
        }
        if (request.status == null || request.status.isBlank()) {
            request.status = "Approved";
        }
        if (request.notes == null || request.notes.isBlank()) {
            request.notes = "Order payment received";
        }

        PatientVisit visit = PatientVisit.findById(visitId);
        if (visit == null) {
            return badRequest("Order (visit) not found.");
        }

        // Payment may start at quotation: ensure Q → PF → INV → Receipt all exist.
        OtcPharmacySale ensuredReceipt = ensureReceiptDocumentForOrder(visitId);
        if (ensuredReceipt == null) {
            return badRequest(
                    "No sale documents found on this order to create a receipt from.");
        }
        ensurePaymentStageDocuments(visitId, ensuredReceipt);
        ensuredReceipt = ensureReceiptDocumentForOrder(visitId);
        if (ensuredReceipt == null) {
            return badRequest("Could not create a receipt for this order payment.");
        }

        // Paid on Products sold must match real visit payments (not phantom sale.amountReceived).
        BigDecimal visitPaid = nz(paymentService.getTotalPaymentOfVisit(visitId));
        BigDecimal orderDue = computeOrderPayableTotal(visitId);
        if (orderDue.compareTo(BigDecimal.ZERO) <= 0) {
            orderDue = nz(ensuredReceipt.totalAmount);
        }
        BigDecimal indicatedPaid = visitPaid.min(orderDue);
        syncPaymentIndicationDocuments(visitId, indicatedPaid, null, null);

        BigDecimal otcUnpaid = orderDue.subtract(indicatedPaid);
        if (otcUnpaid.compareTo(BigDecimal.ZERO) < 0) {
            otcUnpaid = BigDecimal.ZERO;
        }
        if (otcUnpaid.compareTo(BigDecimal.ZERO) <= 0) {
            java.util.Map<String, Object> settled = new java.util.LinkedHashMap<>();
            settled.put("visitId", visitId);
            settled.put("amountPaid", visitPaid);
            settled.put("totalAmount", orderDue);
            settled.put("receipt", new OtcPharmacySaleDTO(
                    OtcPharmacySale.findById(ensuredReceipt.id)));
            return Response.ok(new ResponseMessage(
                    "This order is already fully paid according to payment records.",
                    settled)).build();
        }
        if (request.amountToPay.compareTo(otcUnpaid) > 0) {
            request.amountToPay = otcUnpaid;
        }

        // Visit invoice may have no OTC lines (e.g. quotation-only) or may already
        // show balanceDue=0 while OTC sales still show debt — align before tendering.
        invoiceService.syncInvoiceTotalsForVisit(visitId);
        visit = PatientVisit.findById(visitId);
        ensureVisitInvoiceCoversAmount(visit, request.amountToPay);

        Response paymentResponse = paymentService.createNewPayment(visitId, request);
        if (paymentResponse.getStatus() >= 400) {
            return paymentResponse;
        }

        BigDecimal remaining = request.amountToPay;
        BigDecimal appliedToSales = BigDecimal.ZERO;
        @SuppressWarnings("unchecked")
        List<OtcPharmacySale> allSales = OtcPharmacySale.find(
                "visitId = ?1 order by saleDate asc, saleTime asc, id asc",
                visitId
        ).list();
        // Prefer the receipt (then EFRIS / invoice) so Paid/Debt and the printable
        // receipt stay in sync — do not soak payment into quotation/proforma clones.
        List<OtcPharmacySale> sales = prioritizeSalesForOrderPayment(allSales);
        OtcPharmacySale updatedReceipt = null;
        for (OtcPharmacySale sale : sales) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            BigDecimal due = nz(sale.totalAmount);
            BigDecimal paid = nz(sale.amountReceived).min(due);
            BigDecimal unpaid = due.subtract(paid);
            if (unpaid.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            BigDecimal slice = remaining.min(unpaid);
            sale.amountReceived = nz(sale.amountReceived).add(slice);
            sale.changeAmount = nz(sale.amountReceived).compareTo(due) > 0
                    ? nz(sale.amountReceived).subtract(due)
                    : BigDecimal.ZERO;
            if (request.paymentForm != null && !request.paymentForm.isBlank()) {
                sale.paymentForm = request.paymentForm.trim();
            }
            if (request.receivedBy != null && !request.receivedBy.isBlank()) {
                sale.receivedBy = request.receivedBy.trim();
            }
            String payNote = "Payment +" + slice
                    + (request.notes != null && !request.notes.isBlank()
                            ? " — " + request.notes.trim()
                            : "");
            sale.notes = sale.notes == null || sale.notes.isBlank()
                    ? payNote
                    : sale.notes + " | " + payNote;
            sale.persist();
            appliedToSales = appliedToSales.add(slice);
            remaining = remaining.subtract(slice);
            String t = normalizeDocumentType(sale.documentType);
            if ("receipt".equals(t) || "efris_receipt".equals(t)) {
                updatedReceipt = sale;
            } else if (updatedReceipt == null && "sales_invoice".equals(t)) {
                updatedReceipt = sale;
            }
        }

        // Prefer returning the ensured receipt even if payment applied elsewhere first.
        if (updatedReceipt == null) {
            updatedReceipt = ensuredReceipt;
        }
        if (updatedReceipt != null) {
            String urType = normalizeDocumentType(updatedReceipt.documentType);
            if (!"receipt".equals(urType) && !"efris_receipt".equals(urType)
                    && ensuredReceipt != null) {
                // Mirror paid amount onto the receipt so print shows the payment.
                BigDecimal receiptDue = nz(ensuredReceipt.totalAmount);
                BigDecimal already = nz(ensuredReceipt.amountReceived);
                BigDecimal toAdd = appliedToSales.min(
                        receiptDue.subtract(already).max(BigDecimal.ZERO));
                if (toAdd.compareTo(BigDecimal.ZERO) > 0) {
                    ensuredReceipt.amountReceived = already.add(toAdd);
                    ensuredReceipt.changeAmount =
                            nz(ensuredReceipt.amountReceived).compareTo(receiptDue) > 0
                                    ? nz(ensuredReceipt.amountReceived).subtract(receiptDue)
                                    : BigDecimal.ZERO;
                    if (request.paymentForm != null && !request.paymentForm.isBlank()) {
                        ensuredReceipt.paymentForm = request.paymentForm.trim();
                    }
                    if (request.receivedBy != null && !request.receivedBy.isBlank()) {
                        ensuredReceipt.receivedBy = request.receivedBy.trim();
                    }
                    ensuredReceipt.persist();
                }
                updatedReceipt = ensuredReceipt;
            }
        }

        // After applying slices, keep receipt/invoice/DN at visit payment total.
        BigDecimal paidNow = nz(paymentService.getTotalPaymentOfVisit(visitId));
        syncPaymentIndicationDocuments(
                visitId,
                paidNow.min(orderDue),
                request.paymentForm,
                request.receivedBy);
        if (updatedReceipt != null && updatedReceipt.id != null) {
            updatedReceipt = OtcPharmacySale.findById(updatedReceipt.id);
        }

        // Keep patient account debt in sync when sales were updated.
        if (appliedToSales.compareTo(BigDecimal.ZERO) > 0 && visit.patient != null) {
            org.example.client.domains.Patient patient = visit.patient;
            BigDecimal patientDue = nz(patient.totalAmountDue);
            if (patientDue.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal next = patientDue.subtract(appliedToSales.min(patientDue));
                if (next.compareTo(BigDecimal.ZERO) < 0) {
                    next = BigDecimal.ZERO;
                }
                patient.totalAmountDue = next;
                patient.persist();
            }
        }

        java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("visitId", visitId);
        result.put("amountPaid", request.amountToPay);
        result.put("appliedToSales", appliedToSales);
        result.put("receiptCreated", ensuredReceipt != null
                && "receipt".equals(normalizeDocumentType(ensuredReceipt.documentType)));
        if (updatedReceipt != null) {
            result.put("receipt", new OtcPharmacySaleDTO(updatedReceipt));
            result.put("documentType", normalizeDocumentType(updatedReceipt.documentType));
            result.put("amountReceived", updatedReceipt.amountReceived);
            result.put("totalAmount", updatedReceipt.totalAmount);
            result.put("saleId", updatedReceipt.id);
            result.put("saleNo", updatedReceipt.saleNo);
        }
        visit = PatientVisit.findById(visitId);
        if (visit != null) {
            result.put("balanceDue", visit.balanceDue);
            result.put("visitAmountPaid", visit.amountPaid);
        }
        return Response.ok(new ResponseMessage(
                "Payment of Sh:" + request.amountToPay + " recorded on the order.",
                result)).build();
    }

    /** Payable total for the order — prefer receipt / invoice / delivery note totals. */
    private BigDecimal computeOrderPayableTotal(Long visitId) {
        if (visitId == null || visitId <= 0) {
            return BigDecimal.ZERO;
        }
        @SuppressWarnings("unchecked")
        List<OtcPharmacySale> sales = OtcPharmacySale.find("visitId", visitId).list();
        BigDecimal indicatingBest = BigDecimal.ZERO;
        int indicatingRank = -1;
        BigDecimal anyBest = BigDecimal.ZERO;
        for (OtcPharmacySale sale : sales) {
            if (sale == null) {
                continue;
            }
            BigDecimal due = nz(sale.totalAmount);
            if (due.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            if (due.compareTo(anyBest) > 0) {
                anyBest = due;
            }
            String t = normalizeDocumentType(sale.documentType);
            if (!documentIndicatesPayment(t)) {
                continue;
            }
            int rank = pipelineRank(t);
            if (rank > indicatingRank || (rank == indicatingRank && due.compareTo(indicatingBest) > 0)) {
                indicatingBest = due;
                indicatingRank = rank;
            }
        }
        return indicatingRank >= 0 ? indicatingBest : anyBest;
    }

    /**
     * When the visit invoice has less open balance than the amount we need to receive
     * (common for quotation/proforma orders with no invoice lines), add an adjustment
     * line so createNewPayment can succeed.
     */
    private void ensureVisitInvoiceCoversAmount(PatientVisit visit, BigDecimal amountNeeded) {
        if (visit == null || amountNeeded == null || amountNeeded.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        invoiceService.syncInvoiceTotalsForVisit(visit.id);
        visit = PatientVisit.findById(visit.id);
        if (visit == null) {
            return;
        }
        BigDecimal balance = visit.balanceDue != null ? visit.balanceDue : BigDecimal.ZERO;
        if (balance.compareTo(amountNeeded) >= 0) {
            return;
        }
        BigDecimal gap = amountNeeded.subtract(balance);
        if (!createOtcBalanceAdjustmentTreatment(visit, gap)) {
            bumpVisitInvoiceOpenBalance(visit, gap);
        } else {
            invoiceService.syncInvoiceTotalsForVisit(visit.id);
        }
    }

    /** Documents that display / store paid amounts for an order. */
    private static boolean documentIndicatesPayment(String documentType) {
        return switch (normalizeDocumentType(documentType)) {
            case "receipt", "efris_receipt", "sales_invoice", "delivery_note" -> true;
            default -> false;
        };
    }

    /**
     * Mirror the order paid total onto receipt / EFRIS / sales invoice / delivery note.
     * Quotation and proforma stay at amountReceived = 0.
     */
    private void syncPaymentIndicationDocuments(
            Long visitId,
            BigDecimal paidAmount,
            String paymentForm,
            String receivedBy) {
        if (visitId == null || visitId <= 0 || paidAmount == null) {
            return;
        }
        BigDecimal paid = paidAmount.max(BigDecimal.ZERO);
        @SuppressWarnings("unchecked")
        List<OtcPharmacySale> sales = OtcPharmacySale.find("visitId", visitId).list();
        if (sales == null || sales.isEmpty()) {
            return;
        }
        for (OtcPharmacySale sale : sales) {
            if (sale == null) {
                continue;
            }
            String t = normalizeDocumentType(sale.documentType);
            if (!documentIndicatesPayment(t)) {
                // Keep quote / proforma clean of payment figures.
                if (nz(sale.amountReceived).compareTo(BigDecimal.ZERO) != 0) {
                    sale.amountReceived = BigDecimal.ZERO;
                    sale.changeAmount = BigDecimal.ZERO;
                    sale.persist();
                }
                continue;
            }
            BigDecimal due = nz(sale.totalAmount);
            BigDecimal applied = paid.min(due);
            sale.amountReceived = applied;
            sale.changeAmount = paid.compareTo(due) > 0 ? paid.subtract(due) : BigDecimal.ZERO;
            if (paymentForm != null && !paymentForm.isBlank()) {
                sale.paymentForm = paymentForm.trim();
            }
            if (receivedBy != null && !receivedBy.isBlank()) {
                sale.receivedBy = receivedBy.trim();
            }
            sale.persist();
        }
    }

    /**
     * Guarantee a Receipt (or EFRIS receipt) exists for this order before applying
     * payment. Clones from the latest sales invoice / proforma / quotation when needed.
     */
    private OtcPharmacySale ensureReceiptDocumentForOrder(Long visitId) {
        if (visitId == null || visitId <= 0) {
            return null;
        }
        @SuppressWarnings("unchecked")
        List<OtcPharmacySale> sales = OtcPharmacySale.find(
                "visitId = ?1 order by id desc",
                visitId
        ).list();
        if (sales == null || sales.isEmpty()) {
            return null;
        }
        OtcPharmacySale existingReceipt = null;
        OtcPharmacySale source = null;
        for (OtcPharmacySale sale : sales) {
            if (sale == null) {
                continue;
            }
            String t = normalizeDocumentType(sale.documentType);
            if ("receipt".equals(t) || "efris_receipt".equals(t)) {
                if (existingReceipt == null) {
                    existingReceipt = sale;
                }
            }
            if (source == null) {
                source = sale;
            } else {
                int existingRank = pipelineRank(source.documentType);
                int candidateRank = pipelineRank(sale.documentType);
                // Prefer highest pipeline stage below delivery note as clone source.
                if (candidateRank > existingRank && candidateRank < 4) {
                    source = sale;
                } else if (candidateRank == existingRank
                        && "sales_invoice".equals(t)
                        && !"sales_invoice".equals(normalizeDocumentType(source.documentType))) {
                    source = sale;
                }
            }
        }
        if (existingReceipt != null) {
            return existingReceipt;
        }
        if (source == null) {
            return null;
        }
        List<OtcPharmacySaleLine> sourceLines = source.lines != null && !source.lines.isEmpty()
                ? source.lines
                : loadActiveSaleLines(source);
        // Also ensure invoice exists earlier in the pipeline when cloning from quote/proforma.
        ensureEarlierPipelineDocuments(source);
        if (!visitHasDocumentType(visitId, "receipt")) {
            cloneSaleAsPipelineDocument(source, sourceLines, "receipt");
        }
        @SuppressWarnings("unchecked")
        List<OtcPharmacySale> receipts = OtcPharmacySale.find(
                "visitId = ?1 and (documentType = ?2 or documentType = ?3) order by id desc",
                visitId,
                "receipt",
                "efris_receipt"
        ).list();
        return receipts != null && !receipts.isEmpty() ? receipts.get(0) : null;
    }

    /** Receipt / EFRIS / invoice first; skip quotation, proforma, delivery note. */
    private List<OtcPharmacySale> prioritizeSalesForOrderPayment(List<OtcPharmacySale> allSales) {
        if (allSales == null || allSales.isEmpty()) {
            return List.of();
        }
        List<OtcPharmacySale> preferred = new ArrayList<>();
        for (OtcPharmacySale sale : allSales) {
            if (sale == null) {
                continue;
            }
            String t = normalizeDocumentType(sale.documentType);
            if ("receipt".equals(t) || "efris_receipt".equals(t) || "sales_invoice".equals(t)) {
                preferred.add(sale);
            }
        }
        if (preferred.isEmpty()) {
            // Do not apply payment onto quotation / proforma / delivery note only.
            return List.of();
        }
        preferred.sort((a, b) -> Integer.compare(
                orderPaymentDocRank(a.documentType),
                orderPaymentDocRank(b.documentType)));
        return preferred;
    }

    private static int orderPaymentDocRank(String documentType) {
        return switch (normalizeDocumentType(documentType)) {
            case "receipt" -> 0;
            case "efris_receipt" -> 1;
            case "sales_invoice" -> 2;
            default -> 9;
        };
    }

    /**
     * Apply a lump-sum payment to a pharmacy client's OTC unpaid sales (oldest first),
     * then reduce {@code patient.totalAmountDue}.
     */
    @Transactional
    public Response payClientDebt(Long patientId, PaymentRequest request) {
        if (patientId == null || patientId <= 0) {
            return badRequest("A valid client id is required.");
        }
        if (request == null || request.amountToPay == null
                || request.amountToPay.compareTo(BigDecimal.ZERO) <= 0) {
            return badRequest("Amount to pay must be greater than zero.");
        }

        org.example.client.domains.Patient patient =
                org.example.client.domains.Patient.findById(patientId);
        if (patient == null) {
            return badRequest("Client not found (id " + patientId + ").");
        }

        BigDecimal remaining = request.amountToPay;
        BigDecimal applied = BigDecimal.ZERO;

        @SuppressWarnings("unchecked")
        List<OtcPharmacySale> sales = OtcPharmacySale.find(
                "patientId = ?1 order by saleDate asc, saleTime asc, id asc",
                patientId
        ).list();

        for (OtcPharmacySale sale : sales) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            BigDecimal due = nz(sale.totalAmount);
            BigDecimal paid = nz(sale.amountReceived).min(due);
            BigDecimal unpaid = due.subtract(paid);
            if (unpaid.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            BigDecimal slice = remaining.min(unpaid);
            sale.amountReceived = nz(sale.amountReceived).add(slice);
            sale.changeAmount = nz(sale.amountReceived).compareTo(due) > 0
                    ? nz(sale.amountReceived).subtract(due)
                    : BigDecimal.ZERO;
            String note = request.notes != null && !request.notes.isBlank()
                    ? request.notes.trim()
                    : "Debt payment";
            String form = request.paymentForm != null && !request.paymentForm.isBlank()
                    ? request.paymentForm.trim()
                    : "Cash";
            String extra = " | Debt pay " + slice + " via " + form + " (" + note + ")";
            sale.notes = sale.notes == null || sale.notes.isBlank()
                    ? extra.trim().replaceFirst("^\\|\\s*", "")
                    : sale.notes + extra;
            sale.persist();
            applied = applied.add(slice);
            remaining = remaining.subtract(slice);
        }

        BigDecimal patientDue = nz(patient.totalAmountDue);
        if (applied.compareTo(BigDecimal.ZERO) <= 0 && patientDue.compareTo(BigDecimal.ZERO) <= 0) {
            return badRequest("This client has no outstanding pharmacy debt.");
        }

        // Always reduce patient account debt by the amount applied to sales,
        // or by the requested amount when debt exists only on the patient record.
        BigDecimal reduceBy = applied.compareTo(BigDecimal.ZERO) > 0
                ? applied
                : remaining.min(patientDue);
        if (applied.compareTo(BigDecimal.ZERO) <= 0 && reduceBy.compareTo(BigDecimal.ZERO) > 0) {
            applied = reduceBy;
            remaining = remaining.subtract(reduceBy);
        }
        if (reduceBy.compareTo(BigDecimal.ZERO) > 0 && patientDue.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal next = patientDue.subtract(reduceBy.min(patientDue));
            if (next.compareTo(BigDecimal.ZERO) < 0) {
                next = BigDecimal.ZERO;
            }
            patient.totalAmountDue = next;
            patient.persist();
        }

        java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("patientId", patientId);
        result.put("amountApplied", applied);
        result.put("remainingUnapplied", remaining.max(BigDecimal.ZERO));
        result.put("balanceDue", nz(patient.totalAmountDue));
        return Response.ok(new ResponseMessage(
                "Debt payment of " + applied + " applied.",
                result)).build();
    }

    private Response reverseOtcSoldLine(Long lineId) {
        OtcPharmacySaleLine line = OtcPharmacySaleLine.findById(lineId);
        if (line == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Sold line not found.", null))
                    .build();
        }
        if (line.reversed) {
            return badRequest("This line is already reversed.");
        }

        BigDecimal qty = nz(line.quantity);
        if (line.stockBatch != null) {
            StockBatch batch = line.stockBatch;
            BigDecimal stockBefore = nz(batch.stockAtHand);
            batch.stockAtHand = stockBefore.add(qty);
            batch.persist();
            line.stockAtHandAfter = batch.stockAtHand;

            OtcPharmacySale sale = line.sale;
            Long saleRefId = sale != null ? sale.id : null;
            stockTrackingService.recordBatchMovement(
                    batch,
                    stockBefore,
                    batch.stockAtHand,
                    StockTrackingService.TX_IN,
                    qty,
                    StockTrackingService.SRC_OTC_PHARMACY_SALE_VOID,
                    saleRefId,
                    StockTrackingService.REF_OTC_PHARMACY_SALE
            );
        } else if (line.itemId != null) {
            Item item = itemRepository.findById(line.itemId);
            if (item == null) {
                return badRequest("Item not found for this sold line.");
            }
            shopItemService.updateItemStockAtHandBeforeUpdating(qty, item);
            line.stockAtHandAfter = nz(item.stockAtHand);
        } else {
            return badRequest("Sold line has no stock batch or item.");
        }

        OtcPharmacySale sale = line.sale;
        if (sale != null) {
            reverseSalePaymentForLine(sale, line);
        }

        line.reversed = true;
        line.persist();
        clearTreatmentDispenseIfNoActiveSaleLines(line.treatmentRequestedId, sale != null ? sale.visitId : null);
        return Response.ok(new ResponseMessage(
                "Sale line reversed, payment adjusted, and stock restored.",
                new OtcPharmacySaleLineDTO(line))).build();
    }

    /**
     * Reduce sale totals / amount received for a reversed line, and clear the
     * matching unpaid share from the tagged client's account.
     */
    private void reverseSalePaymentForLine(OtcPharmacySale sale, OtcPharmacySaleLine line) {
        BigDecimal saleDue = nz(sale.totalAmount);
        BigDecimal amountReceived = nz(sale.amountReceived);
        BigDecimal lineAmt = nz(line.totalAmount);

        BigDecimal activeLinesSum = BigDecimal.ZERO;
        if (sale.lines != null) {
            for (OtcPharmacySaleLine other : sale.lines) {
                if (other == null || other.reversed) {
                    continue;
                }
                activeLinesSum = activeLinesSum.add(nz(other.totalAmount));
            }
        }
        if (activeLinesSum.compareTo(BigDecimal.ZERO) <= 0) {
            activeLinesSum = lineAmt;
        }

        BigDecimal lineSaleShare = lineAmt;
        if (saleDue.compareTo(BigDecimal.ZERO) > 0 && activeLinesSum.compareTo(BigDecimal.ZERO) > 0) {
            lineSaleShare = saleDue.multiply(lineAmt)
                    .divide(activeLinesSum, 2, RoundingMode.HALF_UP);
            if (lineSaleShare.compareTo(saleDue) > 0) {
                lineSaleShare = saleDue;
            }
        } else if (saleDue.compareTo(BigDecimal.ZERO) <= 0) {
            lineSaleShare = BigDecimal.ZERO;
        }

        BigDecimal paidApplied = amountReceived.min(saleDue);
        BigDecimal paidShare = BigDecimal.ZERO;
        BigDecimal unpaidShare = BigDecimal.ZERO;
        if (saleDue.compareTo(BigDecimal.ZERO) > 0 && lineSaleShare.compareTo(BigDecimal.ZERO) > 0) {
            paidShare = lineSaleShare.multiply(paidApplied)
                    .divide(saleDue, 2, RoundingMode.HALF_UP);
            unpaidShare = lineSaleShare.subtract(paidShare);
            if (unpaidShare.compareTo(BigDecimal.ZERO) < 0) {
                unpaidShare = BigDecimal.ZERO;
            }
        }

        BigDecimal newTotal = saleDue.subtract(lineSaleShare);
        if (newTotal.compareTo(BigDecimal.ZERO) < 0) {
            newTotal = BigDecimal.ZERO;
        }
        BigDecimal newPaid = paidApplied.subtract(paidShare);
        if (newPaid.compareTo(BigDecimal.ZERO) < 0) {
            newPaid = BigDecimal.ZERO;
        }
        if (newTotal.compareTo(BigDecimal.ZERO) <= 0) {
            newTotal = BigDecimal.ZERO;
            newPaid = BigDecimal.ZERO;
        }

        sale.totalAmount = newTotal;
        sale.amountReceived = newPaid;
        sale.changeAmount = newPaid.compareTo(newTotal) > 0
                ? newPaid.subtract(newTotal)
                : BigDecimal.ZERO;
        sale.persist();

        if (sale.patientId != null && unpaidShare.compareTo(BigDecimal.ZERO) > 0) {
            org.example.client.domains.Patient patient =
                    org.example.client.domains.Patient.findById(sale.patientId);
            if (patient != null) {
                BigDecimal current = nz(patient.totalAmountDue);
                BigDecimal next = current.subtract(unpaidShare);
                if (next.compareTo(BigDecimal.ZERO) < 0) {
                    next = BigDecimal.ZERO;
                }
                patient.totalAmountDue = next;
                patient.persist();
            }
        }
    }

    @Transactional
    public Response deleteSoldLine(Long lineId, String source) {
        if (!isMdUser()) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(new ResponseMessage("Only MD can delete sold item lines.", null))
                    .build();
        }
        if (lineId == null || lineId <= 0) {
            return badRequest("Line id is required.");
        }
        String normalizedSource = source != null ? source.trim().toUpperCase() : "";
        if ("RX".equals(normalizedSource)) {
            return deleteRxSoldLine(lineId);
        }
        if ("OTC".equals(normalizedSource)) {
            return deleteOtcSoldLine(lineId);
        }
        return badRequest("Source must be OTC or RX.");
    }

    @Transactional
    public Response getStockBatchDefaults(Long stockBatchId) {
        if (stockBatchId == null) {
            return badRequest("Stock batch id is required.");
        }
        StockBatch batch = StockBatch.findById(stockBatchId);
        if (batch == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Stock batch not found for ID: " + stockBatchId, null))
                    .build();
        }
        return Response.ok(new ResponseMessage(
                ActionMessages.FETCHED.label,
                new OtcStockBatchDefaultsDTO(batch))).build();
    }

    private ResolvedLine resolveStockBatchLine(OtcSaleLineRequest lineReq) {
        return resolveStockBatchLine(lineReq, false);
    }

    private ResolvedLine resolveStockBatchLine(OtcSaleLineRequest lineReq, boolean documentOnly) {
        ResolvedLine result = new ResolvedLine();

        StockBatch batch = StockBatch.findById(lineReq.stockBatchId);
        if (batch == null) {
            result.error = "Stock batch not found (id " + lineReq.stockBatchId + ").";
            return result;
        }

        applyPrescribingDefaultsFromStockItem(lineReq, batch);

        BigDecimal qty = roundQty(lineReq.quantity);
        result.unitBuy = nz(batch.unitCostPrice);
        Long modelId = lineReq.unitSellingModelId != null ? lineReq.unitSellingModelId : batch.unitSellingModelId;
        result.unitSellingPrice = unitSellingModelService.resolveUnitSellingPrice(
                batch.stockItemId,
                modelId,
                batch.unitSellingPrice
        );
        result.unitSellingPrice = preferCartUnitSellingPrice(lineReq, qty, result.unitSellingPrice);
        result.lineRequest = lineReq;

        List<StockBatch> pool = findDeductionPool(batch);
        BigDecimal totalAvailable = totalPoolStock(pool);
        // Allow backorder at sale time — stock is enforced when goods delivery is confirmed.
        if (!documentOnly && qty.compareTo(totalAvailable) > 0) {
            result.deferStockDeduction = true;
        }

        if (documentOnly || result.deferStockDeduction) {
            // Quote / proforma / backorder: price the line without allocating stock.
            BatchAllocation fake = new BatchAllocation();
            fake.batch = batch;
            fake.quantity = qty;
            fake.unitBuy = result.unitBuy;
            fake.unitSellingPrice = result.unitSellingPrice;
            fake.lineAmount = qty.multiply(result.unitSellingPrice);
            result.allocations = List.of(fake);
            result.quantity = qty;
            result.lineAmount = fake.lineAmount;
            return result;
        }

        result.allocations = allocateAcrossStores(
                orderDeductionPool(batch, pool),
                qty,
                result.unitSellingPrice,
                result.unitBuy);
        result.quantity = qty;
        result.lineAmount = result.allocations.stream()
                .map(a -> a.lineAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return result;
    }

    private ResolvedLine resolveShopItemLine(OtcSaleLineRequest lineReq) {
        return resolveShopItemLine(lineReq, false);
    }

    /**
     * Resolve a catalog {@link StockItem} sold without a stock batch
     * (POS services, or products with no allocated batch yet).
     */
    private ResolvedLine resolveCatalogStockItemLine(OtcSaleLineRequest lineReq, boolean documentOnly) {
        ResolvedLine result = new ResolvedLine();
        if (lineReq.stockItemId == null) {
            result.error = "Stock item id is required.";
            return result;
        }
        StockItem item = StockItem.findById(lineReq.stockItemId);
        if (item == null) {
            result.error = "Stock item not found (id " + lineReq.stockItemId + ").";
            return result;
        }

        BigDecimal qty = roundQty(lineReq.quantity);
        boolean isService = item.saleCategory != null
                && item.saleCategory.trim().toLowerCase().contains("service");

        result.catalogStockItem = item;
        result.lineRequest = lineReq;
        result.quantity = qty;
        result.unitBuy = nz(item.costPrice);
        result.unitSellingPrice = unitSellingModelService.resolveUnitSellingPrice(
                item.id,
                lineReq.unitSellingModelId,
                item.sellingPrice
        );
        result.unitSellingPrice = preferCartUnitSellingPrice(lineReq, qty, result.unitSellingPrice);
        if (result.unitSellingPrice.compareTo(BigDecimal.ZERO) <= 0) {
            result.error = "Stock item has no selling price: " + nzStr(item.stockItemName)
                    + ". Set a price in the cart before completing the sale.";
            return result;
        }
        result.lineAmount = qty.multiply(result.unitSellingPrice);
        // Services never hold inventoriable stock; other batchless lines wait for delivery.
        result.deferStockDeduction = !isService && !documentOnly;
        return result;
    }

    private ResolvedLine resolveShopItemLine(OtcSaleLineRequest lineReq, boolean documentOnly) {
        ResolvedLine result = new ResolvedLine();
        if (lineReq.itemId == null) {
            result.error = "Item id is required.";
            return result;
        }
        Item item = itemRepository.findById(lineReq.itemId);
        if (item == null) {
            result.error = "Item not found (id " + lineReq.itemId + ").";
            return result;
        }

        BigDecimal qty = roundQty(lineReq.quantity);
        BigDecimal available = nz(item.stockAtHand);
        // Allow backorder at sale time — stock is enforced when goods delivery is confirmed.
        if (!documentOnly && qty.compareTo(available) > 0) {
            result.deferStockDeduction = true;
        }

        applyPrescribingDefaultsFromShopItem(lineReq, item);

        result.shopItem = item;
        result.lineRequest = lineReq;
        result.quantity = qty;
        result.unitBuy = nz(item.costPrice);
        Long modelId = lineReq.unitSellingModelId != null ? lineReq.unitSellingModelId : item.unitSellingModelId;
        result.unitSellingPrice = itemUnitSellingModelService.resolveUnitSellingPrice(
                item.id,
                modelId,
                item.sellingPrice
        );
        result.unitSellingPrice = preferCartUnitSellingPrice(lineReq, qty, result.unitSellingPrice);
        if (result.unitSellingPrice.compareTo(BigDecimal.ZERO) <= 0) {
            result.error = "Item has no selling price: " + nzStr(item.title)
                    + ". Set a price in the cart before completing the sale.";
            return result;
        }
        result.lineAmount = qty.multiply(result.unitSellingPrice);
        return result;
    }

    /**
     * Prefer open cart price (transport / services with no fixed catalog price)
     * over catalog or unit-selling-model price when the client sent one.
     */
    private static BigDecimal preferCartUnitSellingPrice(
            OtcSaleLineRequest lineReq,
            BigDecimal qty,
            BigDecimal catalogOrModelPrice) {
        if (lineReq == null) {
            return nz(catalogOrModelPrice);
        }
        BigDecimal fromCart = nz(lineReq.unitSellingPrice);
        if (fromCart.compareTo(BigDecimal.ZERO) > 0) {
            return fromCart;
        }
        BigDecimal lineTotal = nz(lineReq.totalAmount);
        if (lineTotal.compareTo(BigDecimal.ZERO) > 0
                && qty != null
                && qty.compareTo(BigDecimal.ZERO) > 0) {
            return lineTotal.divide(qty, 4, java.math.RoundingMode.HALF_UP);
        }
        return nz(catalogOrModelPrice);
    }

    private ResolvedLine resolveVisitSundryLine(OtcSaleLineRequest lineReq) {
        ResolvedLine result = new ResolvedLine();
        if (lineReq.visitSundryId == null) {
            result.error = "Sundry line is missing visit sundry id.";
            return result;
        }
        VisitSundry sundry = VisitSundry.findById(lineReq.visitSundryId);
        if (sundry == null) {
            result.error = "Visit sundry not found (id " + lineReq.visitSundryId + ").";
            return result;
        }
        if (Boolean.TRUE.equals(sundry.dispensed)) {
            result.error = "Sundry already dispensed: "
                    + (sundry.itemName != null ? sundry.itemName : sundry.id);
            return result;
        }
        BigDecimal qty = lineReq.quantity != null ? roundQty(lineReq.quantity) : nz(sundry.quantityUsed);
        if (qty.compareTo(BigDecimal.ZERO) <= 0) {
            result.error = "Invalid quantity on sundry: "
                    + (sundry.itemName != null ? sundry.itemName : sundry.id);
            return result;
        }
        BigDecimal unitSell = nz(sundry.unitSellingPrice);
        BigDecimal unitBuy = nz(sundry.unitCostPrice);
        result.visitSundry = sundry;
        result.lineRequest = lineReq;
        result.quantity = qty;
        result.unitBuy = unitBuy;
        result.unitSellingPrice = unitSell;
        result.lineAmount = qty.multiply(unitSell).setScale(2, RoundingMode.HALF_UP);
        return result;
    }

    /** Record sundry on the sale without deducting stock (already deducted at recording). */
    private void recordVisitSundrySaleLine(OtcPharmacySale sale, ResolvedLine r) {
        VisitSundry sundry = r.visitSundry;
        if (sundry == null) {
            return;
        }
        OtcPharmacySaleLine line = new OtcPharmacySaleLine();
        line.sale = sale;
        line.stockBatch = null;
        line.itemId = sundry.itemId;
        line.itemName = nzStr(sundry.itemName);
        line.quantity = r.quantity;
        line.unitBuy = r.unitBuy;
        line.unitSellingPrice = r.unitSellingPrice;
        line.totalAmount = r.lineAmount;
        line.stockAtHandAfter = null;
        line.visitSundryId = sundry.id;
        line.persist();
        sale.lines.add(line);

        sundry.dispensed = true;
        sundry.persist();
    }

    private ResolvedLine resolvePrescriptionLine(OtcSaleLineRequest lineReq) {
        ResolvedLine result = new ResolvedLine();
        if (lineReq.treatmentRequestedId == null) {
            result.error = "Prescription line is missing treatment id.";
            return result;
        }
        TreatmentRequested treatment = TreatmentRequested.findById(lineReq.treatmentRequestedId);
        if (treatment == null) {
            result.error = "Prescription not found (id " + lineReq.treatmentRequestedId + ").";
            return result;
        }

        BigDecimal qty = resolveDispenseQuantity(lineReq, treatment);
        if (qty.compareTo(BigDecimal.ZERO) <= 0) {
            result.error = "Invalid quantity on prescription: " + nzStr(treatment.itemName);
            return result;
        }

        result.unitBuy = nz(treatment.unitBuy);
        result.unitSellingPrice = nz(treatment.unitSellingPrice);
        result.lineRequest = lineReq;
        result.treatment = treatment;
        result.quantity = qty;

        if (facilityBrandingService.pharmacyUseStockBatchForDefaultFacility()) {
            return resolvePrescriptionStockBatchLine(result, lineReq, treatment, qty);
        }
        return resolvePrescriptionShopItemLine(result, lineReq, treatment, qty);
    }

    private ResolvedLine resolvePrescriptionStockBatchLine(
            ResolvedLine result,
            OtcSaleLineRequest lineReq,
            TreatmentRequested treatment,
            BigDecimal qty) {
        StockBatch batch = treatment.stockBatch;
        Long requestedBatchId = positiveId(lineReq.stockBatchId);
        if (batch == null && requestedBatchId != null) {
            batch = StockBatch.findById(requestedBatchId);
        }
        if (batch == null) {
            result.error = "Prescription has no stock batch: " + nzStr(treatment.itemName);
            return result;
        }

        List<StockBatch> pool = findItemDeductionPool(batch);
        BigDecimal totalAvailable = totalPoolStock(pool);
        if (qty.compareTo(totalAvailable) > 0) {
            result.error = "Insufficient stock for " + nzStr(treatment.itemName)
                    + ". Available: " + totalAvailable + ", Required: " + qty;
            return result;
        }

        result.allocations = allocateAcrossStores(
                orderPrescriptionDeductionPool(batch, pool),
                qty,
                result.unitSellingPrice,
                result.unitBuy);
        result.lineAmount = qty.multiply(result.unitSellingPrice);
        return result;
    }

    private ResolvedLine resolvePrescriptionShopItemLine(
            ResolvedLine result,
            OtcSaleLineRequest lineReq,
            TreatmentRequested treatment,
            BigDecimal qty) {
        Long itemId = positiveId(lineReq.itemId);
        if (itemId == null) {
            itemId = positiveId(treatment.itemId);
        }
        if (itemId == null) {
            result.error = "Prescription has no item: " + nzStr(treatment.itemName);
            return result;
        }
        Item item = itemRepository.findById(itemId);
        if (item == null) {
            result.error = "Item not found (id " + itemId + ").";
            return result;
        }

        BigDecimal available = nz(item.stockAtHand);
        if (qty.compareTo(available) > 0) {
            result.error = "Insufficient stock for " + nzStr(treatment.itemName)
                    + ". Available: " + available + ", Required: " + qty;
            return result;
        }

        if (result.unitSellingPrice.compareTo(BigDecimal.ZERO) <= 0) {
            Long modelId = lineReq.unitSellingModelId != null
                    ? lineReq.unitSellingModelId
                    : treatment.unitSellingModelId != null
                            ? treatment.unitSellingModelId
                            : item.unitSellingModelId;
            result.unitSellingPrice = itemUnitSellingModelService.resolveUnitSellingPrice(
                    item.id,
                    modelId,
                    item.sellingPrice);
        }
        if (result.unitBuy.compareTo(BigDecimal.ZERO) <= 0) {
            result.unitBuy = nz(item.costPrice);
        }
        if (result.unitSellingPrice.compareTo(BigDecimal.ZERO) <= 0) {
            result.error = "Item has no selling price: " + nzStr(treatment.itemName);
            return result;
        }

        result.shopItem = item;
        result.lineAmount = qty.multiply(result.unitSellingPrice);
        return result;
    }

    private Response deleteOtcSoldLine(Long lineId) {
        OtcPharmacySaleLine line = OtcPharmacySaleLine.findById(lineId);
        if (line == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Sold line not found.", null))
                    .build();
        }

        BigDecimal qty = nz(line.quantity);
        if (line.stockBatch != null) {
            StockBatch batch = line.stockBatch;
            BigDecimal stockBefore = nz(batch.stockAtHand);
            batch.stockAtHand = stockBefore.add(qty);
            batch.persist();

            OtcPharmacySale sale = line.sale;
            Long saleRefId = sale != null ? sale.id : null;
            stockTrackingService.recordBatchMovement(
                    batch,
                    stockBefore,
                    batch.stockAtHand,
                    StockTrackingService.TX_IN,
                    qty,
                    StockTrackingService.SRC_OTC_PHARMACY_SALE_VOID,
                    saleRefId,
                    StockTrackingService.REF_OTC_PHARMACY_SALE
            );
        } else if (line.itemId != null) {
            Item item = itemRepository.findById(line.itemId);
            if (item != null) {
                shopItemService.updateItemStockAtHandBeforeUpdating(qty, item);
            }
        } else {
            return badRequest("Sold line has no stock batch or item.");
        }

        OtcPharmacySale sale = line.sale;
        Long treatmentId = line.treatmentRequestedId;
        Long visitId = sale != null ? sale.visitId : null;
        if (sale != null) {
            sale.totalAmount = nz(sale.totalAmount).subtract(nz(line.totalAmount));
            if (nz(sale.amountReceived).compareTo(sale.totalAmount) > 0) {
                sale.changeAmount = sale.amountReceived.subtract(sale.totalAmount);
            } else {
                sale.changeAmount = BigDecimal.ZERO;
            }
            sale.lines.remove(line);
            if (sale.lines.isEmpty()) {
                sale.delete();
            } else {
                sale.persist();
            }
        }

        line.delete();
        clearTreatmentDispenseIfNoActiveSaleLines(treatmentId, visitId);
        return Response.ok(new ResponseMessage("Sold line removed and stock restored.", null)).build();
    }

    /**
     * When every pharmacy sale line for a prescription is reversed/removed, clear DSP
     * so the consultation PD/DSP/ADM column reflects that the drug is no longer dispensed.
     */
    private void clearTreatmentDispenseIfNoActiveSaleLines(Long treatmentRequestedId, Long visitId) {
        if (treatmentRequestedId == null || treatmentRequestedId <= 0) {
            return;
        }
        long active = OtcPharmacySaleLine.count(
                "treatmentRequestedId = ?1 and reversed = false",
                treatmentRequestedId);
        if (active > 0) {
            return;
        }
        TreatmentRequested treatment = TreatmentRequested.findById(treatmentRequestedId);
        if (treatment == null) {
            return;
        }
        treatment.dispensed = false;
        if (isDispensedStatus(treatment.status)) {
            treatment.status = "pending";
        }
        treatment.persist();
        if (visitId != null) {
            patientBillingService.syncInvoiceAfterPharmacyDispense(visitId);
        } else if (treatment.visit != null) {
            patientBillingService.syncInvoiceAfterPharmacyDispense(treatment.visit.id);
        }
    }

    private Response deleteRxSoldLine(Long treatmentId) {
        TreatmentRequested treatment = TreatmentRequested.findById(treatmentId);
        if (treatment == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Prescription line not found.", null))
                    .build();
        }
        if (!isDispensedStatus(treatment.status)) {
            return badRequest("Only dispensed prescription lines can be removed.");
        }
        return treatmentRequestService.deleteTreatmentRequestById(treatmentId);
    }

    private boolean isMdUser() {
        try {
            User user = authenticatedUserResolver.requireCurrentUser();
            return user.role != null && "md".equalsIgnoreCase(user.role.trim());
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static boolean isDispensedStatus(String status) {
        if (status == null) {
            return false;
        }
        String normalized = status.trim().toLowerCase();
        return "given".equals(normalized) || "dispensed".equals(normalized);
    }

    private void deductBatchAndRecord(OtcPharmacySale sale, ResolvedLine r) {
        if (r.shopItem != null) {
            deductShopItemAndRecord(sale, r);
            return;
        }
        if (r.allocations == null || r.allocations.isEmpty()) {
            return;
        }
        for (BatchAllocation allocation : r.allocations) {
            deductSingleAllocation(sale, r, allocation);
        }
        if (r.treatment != null) {
            r.treatment.quantity = r.quantity;
            r.treatment.totalAmount = r.lineAmount;
            r.treatment.status = "given";
            r.treatment.paid = true;
            r.treatment.dispensed = true;
            r.treatment.persist();
        }
    }

    /** Persist sale lines for quotation / proforma / sales order without moving stock. */
    private void recordLineWithoutStockDeduction(OtcPharmacySale sale, ResolvedLine r) {
        if (r.catalogStockItem != null) {
            StockItem item = r.catalogStockItem;
            OtcPharmacySaleLine line = new OtcPharmacySaleLine();
            line.sale = sale;
            line.stockBatch = null;
            line.itemId = null;
            line.stockItemId = item.id;
            line.itemName = nzStr(item.stockItemName);
            line.quantity = r.quantity;
            line.unitBuy = r.unitBuy;
            line.unitSellingPrice = r.unitSellingPrice;
            line.totalAmount = r.lineAmount;
            line.stockAtHandAfter = nz(item.stockQty);
            line.stockDeferred = r.deferStockDeduction;
            if (r.treatment != null) {
                line.treatmentRequestedId = r.treatment.id;
            }
            line.persist();
            sale.lines.add(line);
            return;
        }
        if (r.shopItem != null) {
            Item item = r.shopItem;
            OtcPharmacySaleLine line = new OtcPharmacySaleLine();
            line.sale = sale;
            line.stockBatch = null;
            line.itemId = item.id;
            line.stockItemId = null;
            line.itemName = nzStr(item.title);
            line.quantity = r.quantity;
            line.unitBuy = r.unitBuy;
            line.unitSellingPrice = r.unitSellingPrice;
            line.totalAmount = r.lineAmount;
            line.stockAtHandAfter = nz(item.stockAtHand);
            line.stockDeferred = r.deferStockDeduction;
            if (r.treatment != null) {
                line.treatmentRequestedId = r.treatment.id;
            }
            line.persist();
            sale.lines.add(line);
            return;
        }
        if (r.allocations == null || r.allocations.isEmpty()) {
            return;
        }
        for (BatchAllocation allocation : r.allocations) {
            StockBatch batch = allocation.batch;
            OtcPharmacySaleLine line = new OtcPharmacySaleLine();
            line.sale = sale;
            line.stockBatch = batch;
            line.stockItemId = batch != null ? batch.stockItemId : null;
            line.itemName = nzStr(
                    r.treatment != null ? r.treatment.itemName : (batch != null ? batch.stockItemName : null));
            line.batchNumber = batch != null ? batch.batchNumber : null;
            line.storeId = batch != null ? batch.storeId : null;
            line.storeName = batch != null ? batch.storeName : null;
            line.quantity = allocation.quantity;
            line.unitBuy = allocation.unitBuy;
            line.unitSellingPrice = allocation.unitSellingPrice;
            line.totalAmount = allocation.lineAmount;
            line.stockAtHandAfter = batch != null ? nz(batch.stockAtHand) : null;
            line.stockDeferred = r.deferStockDeduction;
            if (r.treatment != null) {
                line.treatmentRequestedId = r.treatment.id;
            }
            line.persist();
            sale.lines.add(line);
        }
    }

    private void deductShopItemAndRecord(OtcPharmacySale sale, ResolvedLine r) {
        Item item = r.shopItem;
        if (item == null) {
            return;
        }
        // Same pattern as treatment dispense for shop Item: stockAtHand -= qty only.
        shopItemService.updateItemStockAtHandAfterSelling(r.quantity, item);

        OtcPharmacySaleLine line = new OtcPharmacySaleLine();
        line.sale = sale;
        line.stockBatch = null;
        line.itemId = item.id;
        line.stockItemId = null;
        line.itemName = nzStr(item.title);
        line.quantity = r.quantity;
        line.unitBuy = r.unitBuy;
        line.unitSellingPrice = r.unitSellingPrice;
        line.totalAmount = r.lineAmount;
        line.stockAtHandAfter = nz(item.stockAtHand);
        if (r.treatment != null) {
            line.treatmentRequestedId = r.treatment.id;
        }
        // OTC counter sales do not store prescription dosage / duration fields.
        line.persist();
        sale.lines.add(line);
        if (r.treatment != null) {
            r.treatment.quantity = r.quantity;
            r.treatment.totalAmount = r.lineAmount;
            r.treatment.status = "given";
            r.treatment.paid = true;
            r.treatment.dispensed = true;
            r.treatment.persist();
        }
    }

    private void deductSingleAllocation(OtcPharmacySale sale, ResolvedLine r, BatchAllocation allocation) {
        StockBatch batch = allocation.batch;
        BigDecimal stockBefore = nz(batch.stockAtHand);
        batch.stockAtHand = stockBefore.subtract(allocation.quantity);
        batch.persist();

        OtcPharmacySaleLine line = new OtcPharmacySaleLine();
        line.sale = sale;
        line.stockBatch = batch;
        line.stockItemId = batch.stockItemId;
        line.itemName = nzStr(
                r.treatment != null ? r.treatment.itemName : batch.stockItemName);
        line.batchNumber = batch.batchNumber;
        line.storeId = batch.storeId;
        line.storeName = batch.storeName;
        line.quantity = allocation.quantity;
        line.unitBuy = allocation.unitBuy;
        line.unitSellingPrice = allocation.unitSellingPrice;
        line.totalAmount = allocation.lineAmount;
        line.stockAtHandAfter = batch.stockAtHand;
        if (r.treatment != null) {
            line.treatmentRequestedId = r.treatment.id;
        }
        if (r.lineRequest != null) {
            line.amountPerFrequencyValue = r.lineRequest.amountPerFrequencyValue;
            line.amountPerFrequencyUnit = trimOrNull(r.lineRequest.amountPerFrequencyUnit);
            line.frequencyValue = r.lineRequest.frequencyValue;
            line.frequencyUnit = r.lineRequest.frequencyUnit;
            line.durationValue = r.lineRequest.durationValue;
            line.durationUnit = r.lineRequest.durationUnit;
            line.totalUnits = r.lineRequest.totalUnits;
            line.instructions = trimOrNull(r.lineRequest.instructions);
            line.route = trimOrNull(r.lineRequest.route);
        } else if (r.treatment != null) {
            line.amountPerFrequencyValue = r.treatment.amountPerFrequencyValue;
            line.amountPerFrequencyUnit = trimOrNull(r.treatment.amountPerFrequencyUnit);
            line.frequencyValue = r.treatment.frequencyValue;
            line.frequencyUnit = parseFrequencyUnitInt(r.treatment.frequencyUnit);
            line.durationValue = r.treatment.durationValue;
            line.durationUnit = parseDurationUnitInt(r.treatment.durationUnit);
            line.totalUnits = r.treatment.totalUnits;
            line.instructions = trimOrNull(r.treatment.instructions);
            line.route = trimOrNull(r.treatment.route);
        }
        line.persist();
        sale.lines.add(line);

        stockTrackingService.recordBatchMovement(
                batch,
                stockBefore,
                batch.stockAtHand,
                StockTrackingService.TX_OUT,
                allocation.quantity,
                StockTrackingService.SRC_OTC_PHARMACY_SALE,
                sale.id,
                StockTrackingService.REF_OTC_PHARMACY_SALE
        );
    }

    /** Same item + batch number (+ expiry when present) across all stores. */
    @SuppressWarnings("unchecked")
    private List<StockBatch> findDeductionPool(StockBatch anchor) {
        if (anchor == null) {
            return List.of();
        }
        if (anchor.stockItemId == null) {
            return nz(anchor.stockAtHand).compareTo(BigDecimal.ZERO) > 0
                    ? List.of(anchor)
                    : List.of();
        }
        String batchNumber = trimOrNull(anchor.batchNumber);
        List<StockBatch> pool;
        if (batchNumber != null) {
            if (anchor.expiryDate != null) {
                pool = StockBatch.find(
                        "stockItemId = ?1 and batchNumber = ?2 and expiryDate = ?3",
                        anchor.stockItemId,
                        batchNumber,
                        anchor.expiryDate
                ).list();
            } else {
                pool = StockBatch.find(
                        "stockItemId = ?1 and batchNumber = ?2",
                        anchor.stockItemId,
                        batchNumber
                ).list();
            }
        } else {
            pool = List.of(anchor);
        }
        List<StockBatch> withStock = new ArrayList<>();
        for (StockBatch batch : pool) {
            if (batch != null && nz(batch.stockAtHand).compareTo(BigDecimal.ZERO) > 0) {
                withStock.add(batch);
            }
        }
        return withStock;
    }

    private BigDecimal totalPoolStock(List<StockBatch> pool) {
        BigDecimal total = BigDecimal.ZERO;
        for (StockBatch batch : pool) {
            total = total.add(nz(batch.stockAtHand));
        }
        return total;
    }

    private List<StockBatch> orderDeductionPool(StockBatch preferred, List<StockBatch> pool) {
        List<StockBatch> ordered = new ArrayList<>();
        if (preferred != null && nz(preferred.stockAtHand).compareTo(BigDecimal.ZERO) > 0) {
            ordered.add(preferred);
        }
        pool.stream()
                .filter(batch -> preferred == null || !preferred.id.equals(batch.id))
                .sorted(Comparator.comparing(
                        batch -> nzStr(batch.storeName),
                        String.CASE_INSENSITIVE_ORDER))
                .forEach(ordered::add);
        return ordered;
    }

    private BigDecimal resolveDispenseQuantity(OtcSaleLineRequest lineReq, TreatmentRequested treatment) {
        if (lineReq != null
                && lineReq.quantity != null
                && lineReq.quantity.compareTo(BigDecimal.ZERO) > 0) {
            return roundQty(lineReq.quantity);
        }
        return roundQty(treatment.quantity);
    }

    /** All in-stock batches for one item (any store / batch number), earliest expiry first. */
    @SuppressWarnings("unchecked")
    private List<StockBatch> findItemDeductionPool(StockBatch anchor) {
        if (anchor == null) {
            return List.of();
        }
        if (anchor.stockItemId == null) {
            return findDeductionPool(anchor);
        }
        List<StockBatch> pool = StockBatch.find(
                "stockItemId = ?1 order by expiryDate asc nulls last, id asc",
                anchor.stockItemId
        ).list();
        List<StockBatch> withStock = new ArrayList<>();
        for (StockBatch batch : pool) {
            if (batch != null && nz(batch.stockAtHand).compareTo(BigDecimal.ZERO) > 0) {
                withStock.add(batch);
            }
        }
        return withStock;
    }

    private List<StockBatch> orderPrescriptionDeductionPool(StockBatch preferred, List<StockBatch> pool) {
        List<StockBatch> ordered = new ArrayList<>();
        if (preferred != null && nz(preferred.stockAtHand).compareTo(BigDecimal.ZERO) > 0) {
            ordered.add(preferred);
        }
        pool.stream()
                .filter(batch -> preferred == null || !preferred.id.equals(batch.id))
                .forEach(ordered::add);
        return ordered;
    }

    private List<BatchAllocation> allocateAcrossStores(
            List<StockBatch> ordered,
            BigDecimal qty,
            BigDecimal unitSellingPrice,
            BigDecimal unitBuy) {
        List<BatchAllocation> allocations = new ArrayList<>();
        BigDecimal remaining = qty;
        for (StockBatch batch : ordered) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            BigDecimal available = nz(batch.stockAtHand);
            if (available.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            BigDecimal take = remaining.min(available);
            BatchAllocation allocation = new BatchAllocation();
            allocation.batch = batch;
            allocation.quantity = take;
            allocation.unitSellingPrice = unitSellingPrice.compareTo(BigDecimal.ZERO) > 0
                    ? unitSellingPrice
                    : nz(batch.unitSellingPrice);
            allocation.unitBuy = unitBuy.compareTo(BigDecimal.ZERO) > 0
                    ? unitBuy
                    : nz(batch.unitCostPrice);
            allocation.lineAmount = take.multiply(allocation.unitSellingPrice);
            allocations.add(allocation);
            remaining = remaining.subtract(take);
        }
        return allocations;
    }

    private int nextSalePlainNo() {
        List<OtcPharmacySale> sales = OtcPharmacySale.find("order by salePlainNo desc").list();
        if (sales.isEmpty() || sales.get(0).salePlainNo == null) {
            return 1;
        }
        return sales.get(0).salePlainNo + 1;
    }

    private static BigDecimal roundQty(BigDecimal quantity) {
        if (quantity == null) {
            return BigDecimal.ZERO;
        }
        return quantity.setScale(0, RoundingMode.CEILING);
    }

    private static BigDecimal nz(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    /** Treat missing or non-positive ids as absent (clients may send 0). */
    private static Long positiveId(Long value) {
        return value != null && value > 0 ? value : null;
    }

    private static String nzStr(String value) {
        return value != null && !value.isBlank() ? value.trim() : "Item";
    }

    private static String trimOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private static String normalizeDocumentType(String raw) {
        if (raw == null || raw.isBlank()) {
            return "receipt";
        }
        String t = raw.trim().toLowerCase().replace('-', '_').replace(' ', '_');
        return switch (t) {
            case "quotation", "quote" -> "quotation";
            case "sales_order", "salesorder", "order" -> "sales_order";
            case "proforma", "proforma_invoice" -> "proforma";
            case "sales_invoice", "invoice" -> "sales_invoice";
            case "efris_receipt", "efris", "efrisreceipt" -> "efris_receipt";
            case "delivery_note", "delivery", "deliverynote" -> "delivery_note";
            case "receipt", "payment", "payments" -> "receipt";
            default -> "receipt";
        };
    }

    private static boolean resolveDeductStock(Boolean deductStock, String documentType) {
        if (deductStock != null) {
            return Boolean.TRUE.equals(deductStock);
        }
        return switch (documentType) {
            case "quotation", "sales_order", "proforma", "delivery_note" -> false;
            default -> true;
        };
    }

    private static String documentTypeLabel(String documentType) {
        return switch (normalizeDocumentType(documentType)) {
            case "quotation" -> "Quotation";
            case "sales_order" -> "Sales Order";
            case "proforma" -> "Proforma Invoice";
            case "sales_invoice" -> "Sales Invoice";
            case "efris_receipt" -> "EFRIS Receipt";
            case "delivery_note" -> "Delivery Note";
            default -> "Receipt";
        };
    }

    /**
     * When money is received at any stage (including quotation), ensure the pipeline
     * through Receipt exists: Quotation → Proforma → Sales Invoice → Receipt.
     * Missing stages are cloned from {@code source} (same products and totals).
     *
     * @return number of documents newly created
     */
    private int ensurePaymentStageDocuments(Long visitId, OtcPharmacySale source) {
        if (visitId == null || visitId <= 0 || source == null) {
            return 0;
        }
        List<OtcPharmacySaleLine> sourceLines = loadActiveSaleLines(source);
        int created = 0;
        for (String type : List.of("quotation", "proforma", "sales_invoice", "receipt")) {
            if (visitHasDocumentType(visitId, type)) {
                continue;
            }
            cloneSaleAsPipelineDocument(source, sourceLines, type);
            created++;
        }
        return created;
    }

    /**
     * Create every missing earlier pipeline document for this order, copying the
     * exact products, quantities, unit prices, and totals from {@code source}.
     * Pipeline: Quotation → Proforma → Sales Invoice → Receipt → Delivery Note.
     * Never creates a second copy of an existing type.
     *
     * @return number of documents newly created
     */
    private int ensureEarlierPipelineDocuments(OtcPharmacySale source) {
        if (source == null || source.visitId == null) {
            return 0;
        }
        int rank = pipelineRank(source.documentType);
        if (rank <= 0) {
            return 0;
        }
        List<String> earlierTypes = new ArrayList<>();
        // Always fill stages strictly before the current one.
        if (rank > 0) {
            earlierTypes.add("quotation");
        }
        if (rank > 1) {
            earlierTypes.add("proforma");
        }
        if (rank > 2) {
            earlierTypes.add("sales_invoice");
        }
        if (rank > 3) {
            // Delivery note → also ensure a receipt exists before it.
            earlierTypes.add("receipt");
        }
        List<OtcPharmacySaleLine> sourceLines = loadActiveSaleLines(source);
        int created = 0;
        for (String type : earlierTypes) {
            if (visitHasDocumentType(source.visitId, type)) {
                continue;
            }
            cloneSaleAsPipelineDocument(source, sourceLines, type);
            created++;
        }
        return created;
    }

    /** @deprecated use {@link #ensureEarlierPipelineDocuments(OtcPharmacySale)} */
    private void ensureCommercialPipelineFromReceipt(OtcPharmacySale receiptSale) {
        ensureEarlierPipelineDocuments(receiptSale);
    }

    /**
     * Copy the source document's product lines onto every earlier pipeline document
     * for the same order. Edit later stage → earlier stages get the same qty/prices/totals.
     */
    private int syncEarlierPipelineDocuments(OtcPharmacySale source) {
        if (source == null || source.visitId == null) {
            return 0;
        }
        int sourceRank = pipelineRank(source.documentType);
        if (sourceRank <= 0) {
            return 0;
        }
        List<OtcPharmacySaleLine> sourceLines = loadActiveSaleLines(source);
        @SuppressWarnings("unchecked")
        List<OtcPharmacySale> sales = OtcPharmacySale.find("visitId", source.visitId).list();
        int synced = 0;
        for (OtcPharmacySale other : sales) {
            if (other == null || other.id == null || other.id.equals(source.id)) {
                continue;
            }
            if (pipelineRank(other.documentType) >= sourceRank) {
                continue;
            }
            replaceDocumentLinesFromSource(other, sourceLines, source);
            synced++;
        }
        return synced;
    }

    private List<OtcPharmacySaleLine> loadActiveSaleLines(OtcPharmacySale sale) {
        if (sale == null) {
            return List.of();
        }
        if (sale.id != null) {
            @SuppressWarnings("unchecked")
            List<OtcPharmacySaleLine> fromDb = OtcPharmacySaleLine.find(
                    "sale.id = ?1 and (reversed is null or reversed = false) order by id",
                    sale.id
            ).list();
            if (fromDb != null && !fromDb.isEmpty()) {
                return fromDb;
            }
        }
        if (sale.lines != null && !sale.lines.isEmpty()) {
            return sale.lines.stream()
                    .filter(l -> l != null && !l.reversed)
                    .toList();
        }
        return List.of();
    }

    private void replaceDocumentLinesFromSource(
            OtcPharmacySale target,
            List<OtcPharmacySaleLine> sourceLines,
            OtcPharmacySale source) {
        if (target == null || source == null) {
            return;
        }
        clearSaleLines(target);
        // Exact commercial totals from the document being saved/edited.
        target.totalAmount = nz(source.totalAmount);
        if (source.patientId != null) {
            target.patientId = source.patientId;
        }
        if (source.patientName != null && !source.patientName.isBlank()) {
            target.patientName = source.patientName;
        }
        target.paymentForm = documentTypeLabel(target.documentType);
        String syncNote = "Synced from " + documentTypeLabel(source.documentType)
                + (source.saleNo != null ? " " + source.saleNo : "");
        target.notes = syncNote;
        if (sourceLines == null || sourceLines.isEmpty()) {
            return;
        }
        for (OtcPharmacySaleLine src : sourceLines) {
            if (src == null || src.reversed) {
                continue;
            }
            OtcPharmacySaleLine line = copySaleLineOnto(target, src);
            target.lines.add(line);
        }
    }

    private OtcPharmacySaleLine copySaleLineOnto(OtcPharmacySale target, OtcPharmacySaleLine src) {
        OtcPharmacySaleLine line = new OtcPharmacySaleLine();
        line.sale = target;
        line.stockBatch = src.stockBatch;
        line.itemId = src.itemId;
        line.stockItemId = src.stockItemId;
        line.itemName = nzStr(src.itemName);
        line.batchNumber = src.batchNumber;
        line.storeId = src.storeId;
        line.storeName = src.storeName;
        line.quantity = nz(src.quantity);
        line.unitBuy = nz(src.unitBuy);
        line.unitSellingPrice = nz(src.unitSellingPrice);
        line.totalAmount = nz(src.totalAmount);
        line.stockAtHandAfter = src.stockAtHandAfter;
        line.amountPerFrequencyValue = src.amountPerFrequencyValue;
        line.amountPerFrequencyUnit = src.amountPerFrequencyUnit;
        line.frequencyValue = src.frequencyValue;
        line.frequencyUnit = src.frequencyUnit;
        line.durationValue = src.durationValue;
        line.durationUnit = src.durationUnit;
        line.totalUnits = src.totalUnits;
        line.instructions = src.instructions;
        line.route = src.route;
        line.reversed = false;
        line.treatmentRequestedId = src.treatmentRequestedId;
        line.visitSundryId = src.visitSundryId;
        line.persist();
        return line;
    }

    private void clearSaleLines(OtcPharmacySale sale) {
        if (sale == null) {
            return;
        }
        if (sale.lines != null) {
            sale.lines.clear();
        } else {
            sale.lines = new ArrayList<>();
        }
        if (sale.id != null) {
            OtcPharmacySaleLine.delete("sale.id", sale.id);
        }
    }

    /** Find the single sale of this document type for the order (visit), or by saleId. */
    private OtcPharmacySale findSaleForUpsert(Long visitId, String documentType, Long saleId) {
        if (saleId != null && saleId > 0) {
            OtcPharmacySale byId = OtcPharmacySale.findById(saleId);
            if (byId != null) {
                return byId;
            }
        }
        if (visitId == null || visitId <= 0 || documentType == null) {
            return null;
        }
        String wanted = normalizeDocumentType(documentType);
        @SuppressWarnings("unchecked")
        List<OtcPharmacySale> sales = OtcPharmacySale.find(
                "visitId = ?1 order by id desc",
                visitId
        ).list();
        OtcPharmacySale match = null;
        for (OtcPharmacySale sale : sales) {
            if (sale != null && wanted.equals(normalizeDocumentType(sale.documentType))) {
                if (match == null) {
                    match = sale;
                } else {
                    // Collapse duplicates: keep newest, remove older copies.
                    clearSaleLines(sale);
                    sale.delete();
                }
            }
        }
        return match;
    }

    private static int pipelineRank(String documentType) {
        return switch (normalizeDocumentType(documentType)) {
            case "quotation", "sales_order" -> 0;
            case "proforma" -> 1;
            case "sales_invoice" -> 2;
            case "receipt", "efris_receipt" -> 3;
            case "delivery_note" -> 4;
            default -> 3;
        };
    }

    private boolean visitHasDocumentType(Long visitId, String documentType) {
        if (visitId == null || documentType == null) {
            return false;
        }
        @SuppressWarnings("unchecked")
        List<OtcPharmacySale> sales = OtcPharmacySale.find("visitId", visitId).list();
        String wanted = normalizeDocumentType(documentType);
        for (OtcPharmacySale sale : sales) {
            if (sale != null && wanted.equals(normalizeDocumentType(sale.documentType))) {
                return true;
            }
        }
        return false;
    }

    private void cloneSaleAsPipelineDocument(
            OtcPharmacySale source,
            List<OtcPharmacySaleLine> sourceLines,
            String documentType) {
        if (source == null || documentType == null) {
            return;
        }
        if (source.visitId != null && visitHasDocumentType(source.visitId, documentType)) {
            return;
        }
        OtcPharmacySale clone = new OtcPharmacySale();
        clone.salePlainNo = nextSalePlainNo();
        String prefix = switch (documentType) {
            case "quotation" -> "Q";
            case "proforma" -> "PF";
            case "sales_invoice" -> "INV";
            case "receipt" -> "REC";
            case "delivery_note" -> "DN";
            case "efris_receipt" -> "EFR";
            default -> "DOC";
        };
        clone.saleNo = prefix + "-" + clone.salePlainNo;
        clone.saleDate = source.saleDate != null ? source.saleDate : LocalDate.now();
        clone.saleTime = source.saleTime != null ? source.saleTime : LocalTime.now();
        clone.totalAmount = nz(source.totalAmount);
        clone.amountReceived = BigDecimal.ZERO;
        clone.changeAmount = BigDecimal.ZERO;
        clone.paymentForm = documentTypeLabel(documentType);
        clone.receivedBy = source.receivedBy;
        clone.notes = "Auto-filled from " + documentTypeLabel(source.documentType)
                + (source.saleNo != null ? " " + source.saleNo : "")
                + " — same products, quantities, and amounts";
        clone.visitId = source.visitId;
        clone.patientId = source.patientId;
        clone.patientName = source.patientName;
        clone.documentType = documentType;
        clone.active = source.active;
        clone.persist();

        if (sourceLines == null || sourceLines.isEmpty()) {
            return;
        }
        for (OtcPharmacySaleLine src : sourceLines) {
            if (src == null || src.reversed) {
                continue;
            }
            clone.lines.add(copySaleLineOnto(clone, src));
        }
    }

    private static void copyPrescribingFields(OtcSaleLineRequest target, OtcSaleLineRequest source) {
        if (target == null || source == null) {
            return;
        }
        target.amountPerFrequencyValue = source.amountPerFrequencyValue;
        target.amountPerFrequencyUnit = source.amountPerFrequencyUnit;
        target.frequencyValue = source.frequencyValue;
        target.frequencyUnit = source.frequencyUnit;
        target.durationValue = source.durationValue;
        target.durationUnit = source.durationUnit;
        target.totalUnits = source.totalUnits;
        target.instructions = source.instructions;
        target.route = source.route;
        target.unitSellingModelId = source.unitSellingModelId;
        // Open cart price (switch off / no fixed catalog price) must survive line merge.
        if (source.unitSellingPrice != null
                && source.unitSellingPrice.compareTo(BigDecimal.ZERO) > 0) {
            target.unitSellingPrice = source.unitSellingPrice;
        }
        if (source.totalAmount != null
                && source.totalAmount.compareTo(BigDecimal.ZERO) > 0) {
            target.totalAmount = source.totalAmount;
        }
        target.given = source.given;
    }

    private static void applyPrescribingDefaultsFromShopItem(OtcSaleLineRequest lineReq, Item item) {
        if (lineReq == null || item == null) {
            return;
        }
        if (isBlank(lineReq.amountPerFrequencyUnit)) {
            lineReq.amountPerFrequencyUnit = item.dosageUnit != null ? item.dosageUnit : item.lastUnitOfMeasure;
        }
        if (lineReq.amountPerFrequencyValue == null && item.dosage != null) {
            lineReq.amountPerFrequencyValue = item.dosage;
        }
        if (isBlank(lineReq.route)) {
            lineReq.route = item.route;
        }
    }

    private static void applyPrescribingDefaultsFromStockItem(OtcSaleLineRequest lineReq, StockBatch batch) {
        if (lineReq == null || batch == null || batch.stockItemId == null) {
            return;
        }
        StockItem item = StockItem.findById(batch.stockItemId);
        if (item == null) {
            return;
        }
        if (isBlank(lineReq.amountPerFrequencyUnit)) {
            lineReq.amountPerFrequencyUnit = item.lastUnitOfPrescribingMeasureStrengthUnitTitle;
        }
        if (lineReq.amountPerFrequencyValue == null && item.lastUnitOfSellMeasureStrength != null) {
            lineReq.amountPerFrequencyValue = item.lastUnitOfSellMeasureStrength;
        }
        if (isBlank(lineReq.route)) {
            lineReq.route = item.routeOfAdminTitle;
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static Response badRequest(String message) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ResponseMessage(message, null))
                .build();
    }

    private static Integer parseFrequencyUnitInt(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Integer parseDurationUnitInt(String value) {
        return parseFrequencyUnitInt(value);
    }

    private static class BatchAllocation {
        StockBatch batch;
        BigDecimal quantity;
        BigDecimal unitBuy;
        BigDecimal unitSellingPrice;
        BigDecimal lineAmount;
    }

    /**
     * Block OTC debt when the client already owes on visits and is not in a multi-debt credit group.
     * @return error Response to return, or null when allowed
     */
    private Response gateOtcDebtAgainstPriorVisits(org.example.client.domains.Patient patient) {
        if (patient == null || patient.id == null) {
            return null;
        }
        BigDecimal priorVisitDebt = invoiceService.calculateTotalBalanceDueForAllVisits(patient.id);
        BigDecimal accountDue = patient.totalAmountDue != null ? patient.totalAmountDue : BigDecimal.ZERO;
        boolean hasPriorDebt = priorVisitDebt.compareTo(BigDecimal.ZERO) > 0
                || accountDue.compareTo(BigDecimal.ZERO) > 0;
        if (!hasPriorDebt) {
            return null;
        }
        boolean creditAllowed = specialPrivilegeService.patientAllowsCreditDespiteDebt(patient, null);
        if (creditAllowed) {
            return null;
        }
        BigDecimal shown = priorVisitDebt.compareTo(accountDue) >= 0 ? priorVisitDebt : accountDue;
        return badRequest(
                "Client has a debt on one of the visits (Sh:" + shown
                        + "). Clear the debt before giving another, or use a credit-allowed client group.");
    }

    /** Keep the same order and point it at the client chosen while editing. */
    private void attachClientToExistingOrder(
            Long visitId,
            org.example.client.domains.Patient patient,
            String patientName) {
        if (visitId == null || visitId <= 0 || patient == null || patient.id == null) {
            return;
        }
        PatientVisit visit = PatientVisit.findById(visitId);
        if (visit == null) {
            return;
        }
        Long currentId = visit.patient != null ? visit.patient.id : null;
        if (currentId != null && currentId.equals(patient.id)) {
            return;
        }
        visit.patient = patient;
        String name = patientName != null && !patientName.isBlank()
                ? patientName.trim()
                : ((patient.patientFirstName != null ? patient.patientFirstName : "") + " "
                        + (patient.patientSecondName != null ? patient.patientSecondName : "")).trim();
        if (!name.isBlank()) {
            visit.patientName = name;
        }
    }

    private PatientVisit createOtcCounterVisit(org.example.client.domains.Patient patient) {
        PatientVisitRequest req = new PatientVisitRequest();
        req.visitType = "outpatient";
        req.visitReason = "Over the counter";
        req.allowConcurrentOpen = true;
        PatientVisitDTO dto = patientVisitService.createNewPatientVisit(patient.id, req);
        if (dto == null || dto.id == null) {
            return null;
        }
        return PatientVisit.findById(dto.id);
    }

    private void createOtcDebtTreatment(PatientVisit visit, ResolvedLine r) {
        if (visit == null || r == null || r.quantity == null
                || r.quantity.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        BigDecimal qty = r.quantity.setScale(0, RoundingMode.CEILING);
        TreatmentRequested treatment = new TreatmentRequested();
        treatment.visit = visit;
        treatment.quantity = qty;
        treatment.provisionalQuantity = qty;
        treatment.totalUnits = qty;
        treatment.status = "given";
        treatment.paid = true;
        treatment.dispensed = true;
        treatment.administered = false;
        treatment.unitSellingPrice = nz(r.unitSellingPrice);
        treatment.unitBuy = nz(r.unitBuy);
        treatment.totalAmount = nz(r.lineAmount);
        treatment.provisionalTotalAmount = treatment.totalAmount;
        if (r.shopItem != null) {
            treatment.itemId = r.shopItem.id;
            treatment.itemName = nzStr(r.shopItem.title);
            treatment.stockBatch = null;
        } else if (r.catalogStockItem != null) {
            treatment.itemId = r.catalogStockItem.id;
            treatment.itemName = nzStr(r.catalogStockItem.stockItemName);
            treatment.stockBatch = null;
        } else if (r.allocations != null && !r.allocations.isEmpty()) {
            StockBatch batch = r.allocations.get(0).batch;
            if (batch != null) {
                treatment.stockBatch = batch;
                treatment.itemId = batch.stockItemId;
                treatment.itemName = nzStr(batch.stockItemName);
                treatment.shelfNumber = batch.shelfNumber;
                treatment.lastUnitValue = batch.lastUnitValue;
                treatment.availableQuantity = nz(batch.stockAtHand);
                treatment.lastStockAtHand = nz(batch.stockAtHand);
            }
        } else if (r.treatment != null) {
            treatment.itemId = r.treatment.itemId;
            treatment.itemName = r.treatment.itemName;
            treatment.stockBatch = r.treatment.stockBatch;
        } else {
            treatment.itemName = "OTC item";
        }
        if (treatment.itemName == null || treatment.itemName.isBlank()) {
            treatment.itemName = "OTC item";
        }
        treatment.persist();
        r.treatment = treatment;
    }

    /**
     * Single invoice line for extra unpaid balance after a receipt/order update.
     * @return true when a TreatmentRequested row was created
     */
    private boolean createOtcBalanceAdjustmentTreatment(PatientVisit visit, BigDecimal unpaidDelta) {
        if (visit == null || unpaidDelta == null || unpaidDelta.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        Long itemId = resolveAdjustmentItemId(visit);
        if (itemId == null || itemId <= 0) {
            return false;
        }
        TreatmentRequested treatment = new TreatmentRequested();
        treatment.visit = visit;
        treatment.quantity = BigDecimal.ONE;
        treatment.provisionalQuantity = BigDecimal.ONE;
        treatment.totalUnits = BigDecimal.ONE;
        treatment.status = "given";
        treatment.paid = true;
        treatment.dispensed = true;
        treatment.administered = false;
        treatment.unitSellingPrice = unpaidDelta;
        treatment.unitBuy = BigDecimal.ZERO;
        treatment.totalAmount = unpaidDelta;
        treatment.provisionalTotalAmount = unpaidDelta;
        treatment.itemId = itemId;
        treatment.itemName = "Order update — amount due";
        treatment.persist();
        return true;
    }

    /** Prefer an item already on this order; otherwise any catalog item. */
    private Long resolveAdjustmentItemId(PatientVisit visit) {
        if (visit == null || visit.id == null) {
            return null;
        }
        @SuppressWarnings("unchecked")
        List<OtcPharmacySale> sales = OtcPharmacySale.find("visitId", visit.id).list();
        for (OtcPharmacySale sale : sales) {
            if (sale == null) {
                continue;
            }
            List<OtcPharmacySaleLine> lines = loadActiveSaleLines(sale);
            for (OtcPharmacySaleLine line : lines) {
                if (line == null) {
                    continue;
                }
                if (line.itemId != null && line.itemId > 0) {
                    return line.itemId;
                }
                if (line.stockItemId != null && line.stockItemId > 0) {
                    return line.stockItemId;
                }
                if (line.stockBatch != null && line.stockBatch.stockItemId != null
                        && line.stockBatch.stockItemId > 0) {
                    return line.stockBatch.stockItemId;
                }
            }
        }
        TreatmentRequested existing = TreatmentRequested.find(
                "visit.id = ?1 and itemId is not null order by id desc",
                visit.id
        ).firstResult();
        if (existing != null && existing.itemId != null && existing.itemId > 0) {
            return existing.itemId;
        }
        try {
            Item anyItem = Item.find("order by id asc").firstResult();
            if (anyItem != null && anyItem.id != null) {
                return anyItem.id;
            }
        } catch (Exception ignored) {
            // fall through
        }
        try {
            StockItem anyStock = StockItem.find("order by id asc").firstResult();
            if (anyStock != null && anyStock.id != null) {
                return anyStock.id;
            }
        } catch (Exception ignored) {
            // fall through
        }
        return null;
    }

    /**
     * When no catalog itemId is available, open invoice balance directly so
     * createNewPayment can accept the OTC order payment.
     */
    private void bumpVisitInvoiceOpenBalance(PatientVisit visit, BigDecimal gap) {
        if (visit == null || gap == null || gap.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        invoiceService.syncInvoiceTotalsForVisit(visit.id);
        visit = PatientVisit.findById(visit.id);
        if (visit == null) {
            return;
        }
        org.example.finance.invoice.domains.Invoice invoice = null;
        if (visit.invoice != null && !visit.invoice.isEmpty()) {
            invoice = visit.invoice.get(0);
        }
        if (invoice == null) {
            return;
        }
        invoice.subTotal = nz(invoice.subTotal).add(gap);
        invoice.totalAmount = nz(invoice.totalAmount).add(gap);
        BigDecimal paid = nz(invoice.amountPaid);
        invoice.balanceDue = nz(invoice.totalAmount).subtract(paid);
        if (invoice.balanceDue.compareTo(BigDecimal.ZERO) < 0) {
            invoice.balanceDue = BigDecimal.ZERO;
        }
        invoice.persist();
        visit.subTotal = invoice.subTotal;
        visit.totalAmount = invoice.totalAmount;
        visit.balanceDue = invoice.balanceDue;
        visit.persist();
    }

    private static class ResolvedLine {
        List<BatchAllocation> allocations = new ArrayList<>();
        BigDecimal quantity;
        BigDecimal unitBuy;
        BigDecimal unitSellingPrice;
        BigDecimal lineAmount;
        OtcSaleLineRequest lineRequest;
        TreatmentRequested treatment;
        VisitSundry visitSundry;
        Item shopItem;
        /** Catalog StockItem sold without a batch (services / deferred products). */
        StockItem catalogStockItem;
        String error;
        /** When true, sale is allowed without stock; deduct at goods delivery. */
        boolean deferStockDeduction;
    }

    /**
     * Confirm goods delivery for a visit: requires a delivery note, rejects when any
     * backordered (stock-deferred) OTC line is still out of stock, then deducts stock.
     */
    @Transactional
    public Response confirmGoodsDelivery(Long visitId, org.example.pharmacy.otc.services.payloads.requests.OtcConfirmDeliveryRequest request) {
        if (visitId == null || visitId <= 0) {
            return badRequest("Visit id is required.");
        }
        String note = request != null && request.deliveryNote != null ? request.deliveryNote.trim() : "";
        if (note.isBlank()) {
            return badRequest("Delivery note is required before confirming goods delivered.");
        }
        PatientVisit visit = PatientVisit.findById(visitId);
        if (visit == null) {
            return badRequest("Order (visit) not found.");
        }
        if (Boolean.TRUE.equals(visit.goodsDelivered)) {
            return badRequest("Delivery was already confirmed for this order.");
        }

        @SuppressWarnings("unchecked")
        List<OtcPharmacySale> sales = OtcPharmacySale.find("visitId", visitId).list();
        List<OtcPharmacySaleLine> deferred = new ArrayList<>();
        for (OtcPharmacySale sale : sales) {
            if (sale == null || sale.lines == null) {
                continue;
            }
            for (OtcPharmacySaleLine line : sale.lines) {
                if (line != null && line.stockDeferred && !line.reversed) {
                    deferred.add(line);
                }
            }
        }

        List<String> oosNames = new ArrayList<>();
        for (OtcPharmacySaleLine line : deferred) {
            BigDecimal qty = nz(line.quantity);
            if (qty.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            if (line.stockBatch != null) {
                StockBatch batch = StockBatch.findById(line.stockBatch.id);
                if (batch == null) {
                    oosNames.add(nzStr(line.itemName));
                    continue;
                }
                BigDecimal available = totalPoolStock(findDeductionPool(batch));
                if (qty.compareTo(available) > 0) {
                    oosNames.add(nzStr(line.itemName) + " (available " + available + ")");
                }
            } else if (line.itemId != null) {
                Item item = itemRepository.findById(line.itemId);
                if (item == null || qty.compareTo(nz(item.stockAtHand)) > 0) {
                    BigDecimal available = item != null ? nz(item.stockAtHand) : BigDecimal.ZERO;
                    oosNames.add(nzStr(line.itemName) + " (available " + available + ")");
                }
            }
        }
        if (!oosNames.isEmpty()) {
            return badRequest(
                    "Cannot confirm delivery — out of stock: " + String.join(", ", oosNames));
        }

        for (OtcPharmacySaleLine line : deferred) {
            fulfillDeferredSaleLine(line);
        }

        visit.goodsDelivered = true;
        visit.deliveryNote = note;
        visit.persist();

        // Persist a printable Delivery Note document for the commercial pipeline.
        if (!visitHasDocumentType(visitId, "delivery_note")) {
            OtcPharmacySale source = null;
            for (OtcPharmacySale sale : sales) {
                if (sale == null) {
                    continue;
                }
                String t = normalizeDocumentType(sale.documentType);
                if ("receipt".equals(t) || "efris_receipt".equals(t) || "sales_invoice".equals(t)) {
                    source = sale;
                    break;
                }
                if (source == null) {
                    source = sale;
                }
            }
            if (source != null) {
                List<OtcPharmacySaleLine> sourceLines = source.lines != null && !source.lines.isEmpty()
                        ? source.lines
                        : OtcPharmacySaleLine.find("sale.id", source.id).list();
                cloneSaleAsPipelineDocument(source, sourceLines, "delivery_note");
                // Stamp the delivery note text onto the cloned document.
                @SuppressWarnings("unchecked")
                List<OtcPharmacySale> dnSales = OtcPharmacySale.find(
                        "visitId = ?1 and documentType = ?2 order by id desc",
                        visitId,
                        "delivery_note"
                ).list();
                if (!dnSales.isEmpty() && dnSales.get(0) != null) {
                    OtcPharmacySale dn = dnSales.get(0);
                    dn.notes = note;
                    dn.paymentForm = "Delivery Note";
                    dn.persist();
                }
            }
        }

        return Response.ok(new ResponseMessage(
                "Delivery confirmed.",
                new org.example.visit.services.paloads.responses.PatientVisitDTO(visit))).build();
    }

    private void fulfillDeferredSaleLine(OtcPharmacySaleLine line) {
        if (line == null || !line.stockDeferred || line.reversed) {
            return;
        }
        BigDecimal qty = nz(line.quantity);
        if (qty.compareTo(BigDecimal.ZERO) <= 0) {
            line.stockDeferred = false;
            line.persist();
            return;
        }
        if (line.stockBatch != null) {
            StockBatch batch = StockBatch.findById(line.stockBatch.id);
            if (batch == null) {
                return;
            }
            List<StockBatch> pool = orderDeductionPool(batch, findDeductionPool(batch));
            List<BatchAllocation> allocations = allocateAcrossStores(
                    pool,
                    qty,
                    nz(line.unitSellingPrice),
                    nz(line.unitBuy));
            for (BatchAllocation allocation : allocations) {
                StockBatch b = allocation.batch;
                BigDecimal stockBefore = nz(b.stockAtHand);
                b.stockAtHand = stockBefore.subtract(allocation.quantity);
                b.persist();
                stockTrackingService.recordBatchMovement(
                        b,
                        stockBefore,
                        b.stockAtHand,
                        StockTrackingService.TX_OUT,
                        allocation.quantity,
                        StockTrackingService.SRC_OTC_PHARMACY_SALE,
                        line.sale != null ? line.sale.id : null,
                        StockTrackingService.REF_OTC_PHARMACY_SALE);
                line.stockAtHandAfter = b.stockAtHand;
            }
        } else if (line.itemId != null) {
            Item item = itemRepository.findById(line.itemId);
            if (item == null) {
                return;
            }
            shopItemService.updateItemStockAtHandAfterSelling(qty, item);
            line.stockAtHandAfter = nz(item.stockAtHand);
        }
        line.stockDeferred = false;
        line.persist();
    }
}
