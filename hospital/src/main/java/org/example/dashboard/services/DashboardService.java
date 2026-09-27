package org.example.dashboard.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.example.client.domains.Patient;
import org.example.dashboard.services.payloads.DashboardActivityDTO;
import org.example.dashboard.services.payloads.DashboardAppointmentDTO;
import org.example.dashboard.services.payloads.DashboardSummaryDTO;
import org.example.inventory.item.domain.Item;
import org.example.procedure.procedure.domains.Procedure;
import org.example.procedure.procedure.domains.ProcedureCategory;
import org.example.procedure.procedureRequested.domains.ProcedureRequested;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class DashboardService {

    private static final String LAB_PARENT_CATEGORY = "labtest";
    private static final String SCAN_CATEGORY = "ultrasound scan";

    @Inject
    EntityManager em;

    public DashboardSummaryDTO getSummary() {
        return getSummary(null, null);
    }

    public DashboardSummaryDTO getSummary(LocalDate rangeFrom, LocalDate rangeTo) {
        DashboardSummaryDTO summary = new DashboardSummaryDTO();
        LocalDate today = LocalDate.now();
        boolean ranged = rangeFrom != null && rangeTo != null;
        if (ranged && rangeTo.isBefore(rangeFrom)) {
            LocalDate swap = rangeFrom;
            rangeFrom = rangeTo;
            rangeTo = swap;
        }
        LocalDate periodFrom = ranged ? rangeFrom : today;
        LocalDate periodTo = ranged ? rangeTo : today;
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate weekStart = today.minusDays(7);
        LocalDate thirtyDaysAgo = today.minusDays(30);
        LocalDate nearExpiryUntil = today.plusDays(30);

        summary.totalServices = Procedure.count();
        summary.totalCategories = ProcedureCategory.count();
        summary.totalPatients = Patient.count();
        summary.totalItems = Item.count();
        summary.totalReorderItems = Item.count("stockAtHand <= reOrderLevel AND reOrderLevel IS NOT NULL");
        summary.criticalStockItems = summary.totalReorderItems;
        summary.nearExpiryItems = Item.count(
                "expiryDate IS NOT NULL AND expiryDate <= ?1 AND expiryDate >= ?2",
                nearExpiryUntil,
                today
        );

        summary.totalVisits = countDistinctVisits(null);
        summary.totalVisitsLast30 = ranged
                ? countDistinctVisitsBetween(periodFrom, periodTo)
                : countDistinctVisits(thirtyDaysAgo);

        summary.pendingLabTests = countLabProcedures(false);
        summary.completedLabTests = countLabProcedures(true);
        summary.pendingScanTests = countScanProcedures(false);
        summary.completedScanTests = countScanProcedures(true);

        summary.pendingAppointments = summary.pendingLabTests + summary.pendingScanTests;
        summary.completedAppointments = summary.completedLabTests + summary.completedScanTests;
        summary.todayAppointments = ranged
                ? countProceduresBetween(periodFrom, periodTo)
                : countProceduresOnDate(today);
        summary.emergencyCases = ProcedureRequested.count(
                "LOWER(COALESCE(category, '')) LIKE '%emergency%' OR LOWER(COALESCE(procedureRequestedName, '')) LIKE '%emergency%'"
        );

        summary.admittedPatients = 0;
        summary.newAdmissionsToday = ranged
                ? Patient.count("creationDate >= ?1 AND creationDate <= ?2", periodFrom, periodTo)
                : 0;
        summary.dischargesToday = 0;
        summary.criticalPatients = 0;

        summary.todayRevenue = sumPaymentsBetween(periodFrom, periodTo);
        summary.totalPaymentsReceived = ranged
                ? summary.todayRevenue
                : sumPaymentsBetween(monthStart, today);
        summary.weekRevenue = ranged
                ? summary.todayRevenue
                : sumPaymentsBetween(weekStart, today);
        summary.outstandingPayments = sumOutstandingPayments();

        summary.patientsTrend = monthlyCounts(Patient.class, "creationDate");
        summary.itemsTrend = monthlyCounts(Item.class, "creationDate");
        summary.servicesTrend = monthlyProcedureRequestCounts();
        summary.visitsTrend = monthlyVisitCounts();
        summary.paymentsTrend = monthlyPaymentSums();
        summary.reorderTrend = monthlyReorderCounts();

        fillItemCategoryBreakdown(summary);
        fillPatientAgeBreakdown(summary);
        summary.recentActivities = loadRecentActivities(today);
        summary.upcomingAppointments = loadUpcomingAppointments();

        return summary;
    }

    private long countDistinctVisits(LocalDate since) {
        if (since == null) {
            Long count = em.createQuery(
                    "SELECT COUNT(DISTINCT p.visit.id) FROM ProcedureRequested p WHERE p.visit IS NOT NULL",
                    Long.class
            ).getSingleResult();
            return count != null ? count : 0L;
        }
        Long count = em.createQuery(
                "SELECT COUNT(DISTINCT p.visit.id) FROM ProcedureRequested p WHERE p.visit IS NOT NULL AND p.dateOfProcedure >= :since",
                Long.class
        ).setParameter("since", since).getSingleResult();
        return count != null ? count : 0L;
    }

    private long countLabProcedures(boolean completed) {
        String statusClause = completed
                ? "LOWER(COALESCE(p.status, '')) LIKE '%complete%'"
                : "(p.status IS NULL OR LOWER(p.status) NOT LIKE '%complete%')";
        Long count = em.createQuery(
                "SELECT COUNT(p) FROM ProcedureRequested p JOIN p.procedure pr JOIN pr.parentCategory pc "
                        + "WHERE LOWER(pc.name) = LOWER(:category) AND " + statusClause,
                Long.class
        ).setParameter("category", LAB_PARENT_CATEGORY).getSingleResult();
        return count != null ? count : 0L;
    }

    private long countScanProcedures(boolean completed) {
        String statusClause = completed
                ? "LOWER(COALESCE(p.status, '')) LIKE '%complete%'"
                : "(p.status IS NULL OR LOWER(p.status) NOT LIKE '%complete%')";
        Long count = em.createQuery(
                "SELECT COUNT(p) FROM ProcedureRequested p JOIN p.procedure pr JOIN pr.category c "
                        + "WHERE LOWER(c.name) = LOWER(:category) AND " + statusClause,
                Long.class
        ).setParameter("category", SCAN_CATEGORY).getSingleResult();
        return count != null ? count : 0L;
    }

    private long countProceduresBetween(LocalDate from, LocalDate to) {
        Long count = em.createQuery(
                "SELECT COUNT(p) FROM ProcedureRequested p WHERE p.dateOfProcedure >= :from AND p.dateOfProcedure <= :to",
                Long.class
        ).setParameter("from", from).setParameter("to", to).getSingleResult();
        return count != null ? count : 0L;
    }

    private long countDistinctVisitsBetween(LocalDate from, LocalDate to) {
        Long count = em.createQuery(
                "SELECT COUNT(DISTINCT p.visit.id) FROM ProcedureRequested p "
                        + "WHERE p.visit IS NOT NULL AND p.dateOfProcedure >= :from AND p.dateOfProcedure <= :to",
                Long.class
        ).setParameter("from", from).setParameter("to", to).getSingleResult();
        return count != null ? count : 0L;
    }

    private long countProceduresOnDate(LocalDate date) {
        Long count = em.createQuery(
                "SELECT COUNT(p) FROM ProcedureRequested p WHERE p.dateOfProcedure = :date",
                Long.class
        ).setParameter("date", date).getSingleResult();
        return count != null ? count : 0L;
    }

    private BigDecimal sumPaymentsBetween(LocalDate from, LocalDate to) {
        BigDecimal sum = em.createQuery(
                "SELECT COALESCE(SUM(p.amountToPay), 0) FROM Payments p "
                        + "WHERE p.dateOfPayment IS NOT NULL AND p.dateOfPayment >= :from AND p.dateOfPayment <= :to",
                BigDecimal.class
        ).setParameter("from", from).setParameter("to", to).getSingleResult();
        return sum != null ? sum : BigDecimal.ZERO;
    }

    private BigDecimal sumOutstandingPayments() {
        BigDecimal sum = em.createQuery(
                "SELECT COALESCE(SUM(p.amountToPay), 0) FROM Payments p "
                        + "WHERE LOWER(COALESCE(p.status, '')) LIKE '%pending%'",
                BigDecimal.class
        ).getSingleResult();
        return sum != null ? sum : BigDecimal.ZERO;
    }

    private List<Long> monthlyCounts(Class<?> entityClass, String dateField) {
        List<Long> counts = new ArrayList<>();
        LocalDate now = LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            LocalDate start = now.minusMonths(i).withDayOfMonth(1);
            LocalDate end = start.plusMonths(1).minusDays(1);
            if (Patient.class.equals(entityClass)) {
                counts.add(Patient.count("creationDate >= ?1 AND creationDate <= ?2", start, end));
            } else if (Item.class.equals(entityClass)) {
                counts.add(Item.count("creationDate >= ?1 AND creationDate <= ?2", start, end));
            } else {
                counts.add(0L);
            }
        }
        return counts;
    }

    private List<Long> monthlyProcedureRequestCounts() {
        List<Long> counts = new ArrayList<>();
        LocalDate now = LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            LocalDate start = now.minusMonths(i).withDayOfMonth(1);
            LocalDate end = start.plusMonths(1).minusDays(1);
            counts.add(ProcedureRequested.count("dateOfProcedure >= ?1 AND dateOfProcedure <= ?2", start, end));
        }
        return counts;
    }

    private List<Long> monthlyReorderCounts() {
        List<Long> counts = new ArrayList<>();
        LocalDate now = LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            LocalDate start = now.minusMonths(i).withDayOfMonth(1);
            LocalDate end = start.plusMonths(1).minusDays(1);
            counts.add(Item.count(
                    "creationDate >= ?1 AND creationDate <= ?2 AND stockAtHand <= reOrderLevel AND reOrderLevel IS NOT NULL",
                    start,
                    end
            ));
        }
        return counts;
    }

    private List<Long> monthlyVisitCounts() {
        List<Long> counts = new ArrayList<>();
        LocalDate now = LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            LocalDate start = now.minusMonths(i).withDayOfMonth(1);
            LocalDate end = start.plusMonths(1).minusDays(1);
            Long count = em.createQuery(
                    "SELECT COUNT(DISTINCT p.visit.id) FROM ProcedureRequested p "
                            + "WHERE p.dateOfProcedure >= :start AND p.dateOfProcedure <= :end",
                    Long.class
            ).setParameter("start", start).setParameter("end", end).getSingleResult();
            counts.add(count != null ? count : 0L);
        }
        return counts;
    }

    private List<Long> monthlyPaymentSums() {
        List<Long> counts = new ArrayList<>();
        LocalDate now = LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            LocalDate start = now.minusMonths(i).withDayOfMonth(1);
            LocalDate end = start.plusMonths(1).minusDays(1);
            BigDecimal sum = sumPaymentsBetween(start, end);
            counts.add(sum.longValue());
        }
        return counts;
    }

    private void fillItemCategoryBreakdown(DashboardSummaryDTO summary) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createQuery(
                "SELECT COALESCE(i.category.name, i.subCategory, 'Other'), COUNT(i) "
                        + "FROM Item i GROUP BY COALESCE(i.category.name, i.subCategory, 'Other') "
                        + "ORDER BY COUNT(i) DESC"
        ).setMaxResults(8).getResultList();

        for (Object[] row : rows) {
            String label = row[0] != null ? row[0].toString() : "Other";
            if (label.length() > 20) {
                label = label.substring(0, 20) + "…";
            }
            summary.itemsCategoryLabels.add(label);
            summary.itemsCategoryData.add(((Number) row[1]).longValue());
        }
    }

    private void fillPatientAgeBreakdown(DashboardSummaryDTO summary) {
        String[] labels = {"0-10", "10-20", "20-30", "30-40", "40-50", "50-60", "60+", "Unknown"};
        int[][] ranges = {{0, 10}, {10, 20}, {20, 30}, {30, 40}, {40, 50}, {50, 60}, {60, 200}};
        long withAge = 0;

        for (int i = 0; i < ranges.length; i++) {
            long count = Patient.count(
                    "patientAge >= ?1 AND patientAge < ?2",
                    BigDecimal.valueOf(ranges[i][0]),
                    BigDecimal.valueOf(ranges[i][1])
            );
            summary.patientsAgeLabels.add(labels[i]);
            summary.patientsAgeData.add(count);
            withAge += count;
        }

        summary.patientsAgeLabels.add("Unknown");
        summary.patientsAgeData.add(Math.max(0, summary.totalPatients - withAge));
    }

    private List<DashboardActivityDTO> loadRecentActivities(LocalDate today) {
        List<DashboardActivityDTO> activities = new ArrayList<>();
        List<Patient> patients = Patient.find("ORDER BY id DESC").range(0, 4).list();
        for (Patient patient : patients) {
            String name = ((patient.patientFirstName != null ? patient.patientFirstName : "")
                    + " " + (patient.patientSecondName != null ? patient.patientSecondName : "")).trim();
            activities.add(new DashboardActivityDTO(
                    "patient",
                    "bi-person-plus",
                    "New Patient Registered",
                    name.isEmpty() ? "Patient" : name,
                    formatDate(patient.creationDate),
                    "#2563eb"
            ));
        }

        List<ProcedureRequested> labsToday = ProcedureRequested.find(
                "dateOfProcedure = ?1 ORDER BY id DESC",
                today
        ).range(0, 2).list();
        for (ProcedureRequested lab : labsToday) {
            activities.add(new DashboardActivityDTO(
                    "lab",
                    "bi-flask",
                    "Lab Test Ordered",
                    lab.procedureRequestedName != null ? lab.procedureRequestedName : "Lab Test",
                    formatDate(lab.dateOfProcedure),
                    "#10b981"
            ));
        }
        return activities;
    }

    private List<DashboardAppointmentDTO> loadUpcomingAppointments() {
        List<DashboardAppointmentDTO> appointments = new ArrayList<>();
        List<ProcedureRequested> pending = ProcedureRequested.find(
                "status IS NULL OR LOWER(status) NOT LIKE '%complete%' ORDER BY id DESC"
        ).range(0, 4).list();

        for (ProcedureRequested procedure : pending) {
            appointments.add(new DashboardAppointmentDTO(
                    procedure.patientName != null ? procedure.patientName : "Patient",
                    procedure.procedureRequestedName != null ? procedure.procedureRequestedName : "Service",
                    formatDate(procedure.dateOfProcedure),
                    procedure.status != null ? procedure.status : "Pending",
                    "Normal"
            ));
        }
        return appointments;
    }

    private String formatDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
