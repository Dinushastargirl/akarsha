package com.akarsha.reports.dto;

import java.math.BigDecimal;

public record StaffPerformanceDTO(
    Long staffId,
    String staffName,
    long appointmentsAssigned,
    long completedAppointments,
    long cancelledAppointments,
    long noShowAppointments,
    BigDecimal revenueGenerated,
    double completionRate
) {}
