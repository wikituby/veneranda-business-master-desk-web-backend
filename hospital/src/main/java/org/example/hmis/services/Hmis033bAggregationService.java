package org.example.hmis.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.example.consultations.domains.Consultation;
import org.example.consultations.domains.Diagnosis;
import org.example.hmis.domains.HmisTracerItem;
import org.example.hmis.services.payloads.Hmis033bAggregateResponse;
import org.example.inventory.item.domain.Item;
import org.example.inventory.stock.domains.StockBatch;
import org.example.inventory.stock.domains.StockItem;
import org.example.lab.singleStatementReport.malaria.domains.Malaria;
import org.example.referrals.domains.ReferralForm;
import org.example.subscription.domains.FacilityBusinessSettings;
import org.example.subscription.domains.HealthFacility;
import org.example.subscription.domains.repositories.FacilityBusinessSettingsRepository;
import org.example.subscription.domains.repositories.FacilitySubscriptionRepository;
import org.example.subscription.domains.repositories.HealthFacilityRepository;
import org.example.treatment.domains.TreatmentRequested;
import org.example.visit.domains.PatientVisit;

@ApplicationScoped
public class Hmis033bAggregationService {

    private static final List<String> STANDARD_DISEASE_CODES = List.of(
            "MA.", "DY.", "SA.", "AF.", "AE.", "AB.", "MG.", "CH.", "GW.", "ME.",
            "NT.", "PL.", "TF.", "HB.", "DR.", "YF.", "VF.", "CV.", "LP.", "AX.");

    @Inject HmisCodeResolver hmisCodeResolver;
    @Inject HealthFacilityRepository healthFacilityRepository;
    @Inject FacilityBusinessSettingsRepository businessSettingsRepository;
    @Inject FacilitySubscriptionRepository facilitySubscriptionRepository;

    @Transactional
    public Hmis033bAggregateResponse aggregate(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("from and to dates are required");
        }
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("to date must be on or after from date");
        }

        Hmis033bAggregateResponse response = new Hmis033bAggregateResponse();
        response.period.from = from;
        response.period.to = to;
        response.period.reportDate = LocalDate.now();
        response.period.weekNumber = from.get(WeekFields.ISO.weekOfWeekBasedYear());

        loadFacilityHeader(response);
        aggregateOpd(from, to, response);
        aggregateReferrals(from, to, response);
        aggregateDiseases(from, to, response);
        aggregateMalaria(from, to, response);
        aggregateTracerStock(from, to, response);
        response.notes.add("Reattendance: patient with another visit in the prior 7 days.");
        response.notes.add("Diagnosis counts use HMIS codes, catalog types, or keyword matching.");
        return response;
    }

    private void loadFacilityHeader(Hmis033bAggregateResponse response) {
        Long facilityId = resolveDefaultFacilityId();
        HealthFacility facility = facilityId != null ? healthFacilityRepository.findById(facilityId) : null;
        FacilityBusinessSettings settings = facilityId != null
                ? businessSettingsRepository.findByFacilityId(facilityId).orElse(null)
                : null;

        Hmis033bAggregateResponse.FacilityHeader header = response.facility;
        header.name = facility != null && facility.name != null ? facility.name : "Health Facility";
        header.address = facility != null && facility.address != null ? facility.address : "";
        if (settings != null) {
            header.code = settings.hmisFacilityCode;
            header.level = settings.hmisFacilityLevel;
            header.district = settings.hmisDistrict;
            header.hsd = settings.hmisHsd;
            header.subCounty = settings.hmisSubCounty;
            header.parish = settings.hmisParish;
        }
    }

    private void aggregateOpd(LocalDate from, LocalDate to, Hmis033bAggregateResponse response) {
        List<PatientVisit> visits = PatientVisit.find(
                "visitDate >= ?1 and visitDate <= ?2 order by visitDate asc, id asc", from, to).list();
        int total = visits != null ? visits.size() : 0;
        int reattendance = 0;
        if (visits != null) {
            for (PatientVisit visit : visits) {
                if (visit == null || visit.visitDate == null || visit.patient == null || visit.patient.id == null) {
                    continue;
                }
                long prior = PatientVisit.count(
                        "patient.id = ?1 and visitDate >= ?2 and visitDate < ?3 and id <> ?4",
                        visit.patient.id,
                        visit.visitDate.minusDays(7),
                        visit.visitDate,
                        visit.id != null ? visit.id : -1L);
                if (prior > 0) {
                    reattendance++;
                }
            }
        }
        response.opd.totalAttendance = total;
        response.opd.reattendance = reattendance;
        response.opd.newAttendance = Math.max(0, total - reattendance);
    }

    private void aggregateReferrals(LocalDate from, LocalDate to, Hmis033bAggregateResponse response) {
        long out = ReferralForm.count("dateOfReferral >= ?1 and dateOfReferral <= ?2", from, to);
        response.referrals.referralsOut = (int) out;
        response.referrals.referralsIn = 0;
    }

    private void aggregateDiseases(LocalDate from, LocalDate to, Hmis033bAggregateResponse response) {
        Map<String, Hmis033bAggregateResponse.DiseaseLine> lines = new HashMap<>();
        for (String code : STANDARD_DISEASE_CODES) {
            Hmis033bAggregateResponse.DiseaseLine line = new Hmis033bAggregateResponse.DiseaseLine();
            line.code = code;
            line.label = code;
            lines.put(code, line);
        }

        List<Diagnosis> diagnoses = Diagnosis.find(
                "consultation.visit.visitDate >= ?1 and consultation.visit.visitDate <= ?2", from, to).list();
        if (diagnoses != null) {
            for (Diagnosis diagnosis : diagnoses) {
                incrementDiseaseLine(lines, hmisCodeResolver.resolveForDiagnosis(diagnosis));
            }
        }

        List<Consultation> legacyConsultations = Consultation.find(
                "visit.visitDate >= ?1 and visit.visitDate <= ?2 and diagnosis is not null", from, to).list();
        if (legacyConsultations != null) {
            for (Consultation consultation : legacyConsultations) {
                if (consultation.diagnosis == null || consultation.diagnosis.isBlank()) {
                    continue;
                }
                if (consultation.diagnoses != null && !consultation.diagnoses.isEmpty()) {
                    continue;
                }
                incrementDiseaseLine(lines, hmisCodeResolver.resolveFromText(consultation.diagnosis));
            }
        }

        response.diseases.addAll(lines.values().stream().sorted((a, b) -> a.code.compareTo(b.code)).toList());
    }

    private void incrementDiseaseLine(Map<String, Hmis033bAggregateResponse.DiseaseLine> lines, String code) {
        if (code == null) {
            return;
        }
        Hmis033bAggregateResponse.DiseaseLine line = lines.computeIfAbsent(code, c -> {
            Hmis033bAggregateResponse.DiseaseLine created = new Hmis033bAggregateResponse.DiseaseLine();
            created.code = c;
            created.label = c;
            return created;
        });
        line.casesThisWeek++;
    }

    private void aggregateMalaria(LocalDate from, LocalDate to, Hmis033bAggregateResponse response) {
        Hmis033bAggregateResponse.MalariaSummary summary = response.malaria;
        List<Malaria> rows = Malaria.find("visit.visitDate >= ?1 and visit.visitDate <= ?2", from, to).list();
        Set<Long> treatedVisitIds = new HashSet<>();

        if (rows != null) {
            for (Malaria row : rows) {
                if (row == null) {
                    continue;
                }
                summary.suspectedFever++;
                if (hmisCodeResolver.hasLabValue(row.mrdt)) {
                    summary.testedRdt++;
                    if (hmisCodeResolver.isPositiveLabResult(row.mrdt)) {
                        summary.rdtPositive++;
                    }
                }
                if (hmisCodeResolver.hasLabValue(row.bs)) {
                    summary.testedMicroscopy++;
                    if (hmisCodeResolver.isPositiveLabResult(row.bs)) {
                        summary.microscopyPositive++;
                    }
                }
            }
        }

        List<TreatmentRequested> treatments = TreatmentRequested.find(
                "visit.visitDate >= ?1 and visit.visitDate <= ?2", from, to).list();
        if (treatments != null) {
            for (TreatmentRequested treatment : treatments) {
                if (treatment == null || treatment.visit == null || treatment.visit.id == null) {
                    continue;
                }
                if (!hmisCodeResolver.isAntimalarialDrugName(treatment.itemName)) {
                    continue;
                }
                Long visitId = treatment.visit.id;
                treatedVisitIds.add(visitId);
                boolean tested = rows != null && rows.stream().anyMatch(m -> m != null && m.visit != null
                        && Objects.equals(m.visit.id, visitId)
                        && (hmisCodeResolver.hasLabValue(m.mrdt) || hmisCodeResolver.hasLabValue(m.bs)));
                if (!tested) {
                    summary.notTestedTreated++;
                }
            }
        }

        if (rows != null) {
            for (Malaria row : rows) {
                if (row == null || row.visit == null || row.visit.id == null) {
                    continue;
                }
                if (!treatedVisitIds.contains(row.visit.id)) {
                    continue;
                }
                if (hmisCodeResolver.hasLabValue(row.mrdt) && hmisCodeResolver.isPositiveLabResult(row.mrdt)) {
                    summary.rdtPositiveTreated++;
                }
                if (hmisCodeResolver.hasLabValue(row.bs) && hmisCodeResolver.isPositiveLabResult(row.bs)) {
                    summary.microscopyPositiveTreated++;
                }
            }
        }
    }

    private void aggregateTracerStock(LocalDate from, LocalDate to, Hmis033bAggregateResponse response) {
        List<HmisTracerItem> tracers = HmisTracerItem.find("active = true order by sortOrder, id").list();
        if (tracers == null) {
            return;
        }
        for (HmisTracerItem tracer : tracers) {
            if (tracer == null) {
                continue;
            }
            Hmis033bAggregateResponse.TracerStockLine line = new Hmis033bAggregateResponse.TracerStockLine();
            line.tracerCode = tracer.hmisTracerCode;
            line.tracerName = tracer.tracerName;
            line.stockItemId = tracer.stockItemId;
            line.shopItemId = tracer.shopItemId;
            if (tracer.stockItemId != null) {
                StockItem stockItem = StockItem.findById(tracer.stockItemId);
                if (stockItem != null) {
                    line.stockItemName = stockItem.stockItemName;
                }
            }
            if (tracer.shopItemId != null) {
                Item shopItem = Item.findById(tracer.shopItemId);
                if (shopItem != null) {
                    line.shopItemName = shopItem.title;
                }
            }
            line.balance = resolveTracerBalance(tracer);
            line.dispensedInPeriod = countTracerTreatments(tracer, from, to);
            response.tracerStock.add(line);
        }
    }

    public double resolveTracerBalance(HmisTracerItem tracer) {
        if (tracer == null) {
            return 0d;
        }
        double total = 0d;
        if (tracer.stockItemId != null) {
            total += resolveStockBatchBalance(tracer.stockItemId);
        }
        if (tracer.shopItemId != null) {
            Item shopItem = Item.findById(tracer.shopItemId);
            if (shopItem != null && shopItem.stockAtHand != null) {
                total += shopItem.stockAtHand.doubleValue();
            }
        }
        return total;
    }

    private int countTracerTreatments(HmisTracerItem tracer, LocalDate from, LocalDate to) {
        if (tracer.stockItemId == null && tracer.shopItemId == null) {
            return 0;
        }
        List<TreatmentRequested> treatments = TreatmentRequested.find(
                "visit.visitDate >= ?1 and visit.visitDate <= ?2", from, to).list();
        if (treatments == null) {
            return 0;
        }
        int count = 0;
        for (TreatmentRequested treatment : treatments) {
            if (treatment == null) {
                continue;
            }
            if (treatment.itemId != null) {
                if (tracer.stockItemId != null && treatment.itemId.equals(tracer.stockItemId)) {
                    count++;
                    continue;
                }
                if (tracer.shopItemId != null && treatment.itemId.equals(tracer.shopItemId)) {
                    count++;
                    continue;
                }
            }
            if (tracer.stockItemId != null && treatment.stockBatch != null
                    && treatment.stockBatch.stockItemId != null
                    && treatment.stockBatch.stockItemId.equals(tracer.stockItemId)) {
                count++;
            }
        }
        return count;
    }

    private double resolveStockBatchBalance(Long stockItemId) {
        if (stockItemId == null) {
            return 0d;
        }
        List<StockBatch> batches = StockBatch.find("stockItemId", stockItemId).list();
        BigDecimal total = BigDecimal.ZERO;
        if (batches != null) {
            for (StockBatch batch : batches) {
                if (batch != null && batch.stockAtHand != null) {
                    total = total.add(batch.stockAtHand);
                }
            }
        }
        return total.doubleValue();
    }

    private Long resolveDefaultFacilityId() {
        return facilitySubscriptionRepository.find("order by id desc").firstResultOptional()
                .map(s -> s.facilityId).orElse(null);
    }
}