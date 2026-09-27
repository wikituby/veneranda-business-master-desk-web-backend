package org.example.dashboard.services.payloads;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class DashboardSummaryDTO {
    public long totalServices;
    public long totalCategories;
    public long totalPatients;
    public long totalItems;
    public long totalVisits;
    public long totalVisitsLast30;
    public long totalReorderItems;
    public BigDecimal totalPaymentsReceived = BigDecimal.ZERO;
    public long pendingLabTests;
    public long completedLabTests;
    public long pendingScanTests;
    public long completedScanTests;
    public long todayAppointments;
    public long pendingAppointments;
    public long completedAppointments;
    public long admittedPatients;
    public long newAdmissionsToday;
    public long dischargesToday;
    public long criticalStockItems;
    public long nearExpiryItems;
    public BigDecimal todayRevenue = BigDecimal.ZERO;
    public BigDecimal weekRevenue = BigDecimal.ZERO;
    public BigDecimal outstandingPayments = BigDecimal.ZERO;
    public long emergencyCases;
    public long criticalPatients;

    public List<Long> patientsTrend = new ArrayList<>();
    public List<Long> itemsTrend = new ArrayList<>();
    public List<Long> servicesTrend = new ArrayList<>();
    public List<Long> visitsTrend = new ArrayList<>();
    public List<Long> paymentsTrend = new ArrayList<>();
    public List<Long> reorderTrend = new ArrayList<>();

    public List<String> itemsCategoryLabels = new ArrayList<>();
    public List<Long> itemsCategoryData = new ArrayList<>();
    public List<String> patientsAgeLabels = new ArrayList<>();
    public List<Long> patientsAgeData = new ArrayList<>();

    public List<DashboardActivityDTO> recentActivities = new ArrayList<>();
    public List<DashboardAppointmentDTO> upcomingAppointments = new ArrayList<>();
}
