package com.akarsha.dashboard;

import java.math.BigDecimal;
import java.util.List;

/**
 * Aggregate dashboard response for a single tenant.
 * All counts and appointment summaries are pre-scoped to the authenticated tenant.
 */
public record DashboardStats(
        long todayTotal,
        long todayCompleted,
        long todayCancelled,
        BigDecimal todayEstimatedRevenue,
        long totalCustomers,
        long activeStaff,
        long activeServices,
        List<AppointmentSummary> todayTimeline,
        List<AppointmentSummary> upcomingAppointments
) {
    /**
     * Lightweight appointment summary — no raw PII beyond display names.
     */
    public record AppointmentSummary(
            Long id,
            String customerName,
            String staffName,
            String serviceName,
            String appointmentDate,
            String startTime,
            String endTime,
            String status,
            BigDecimal price
    ) {}
}
