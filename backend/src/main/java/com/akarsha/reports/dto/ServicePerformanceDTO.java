package com.akarsha.reports.dto;

import java.math.BigDecimal;

public record ServicePerformanceDTO(
    Long serviceId,
    String serviceName,
    long appointmentCount,
    long completedAppointments,
    long cancelledAppointments,
    long noShowAppointments,
    BigDecimal revenue,
    BigDecimal averagePrice
) {}
