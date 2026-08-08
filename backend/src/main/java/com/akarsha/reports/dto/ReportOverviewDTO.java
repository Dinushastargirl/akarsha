package com.akarsha.reports.dto;

import java.math.BigDecimal;

public record ReportOverviewDTO(
    BigDecimal grossRevenue,
    BigDecimal totalPaid,
    BigDecimal averageTransactionValue,
    long numberOfPaidInvoices,
    long totalAppointments,
    long completedAppointments,
    long cancelledAppointments,
    long noShows,
    long totalCustomers,
    long newCustomers,
    long returningCustomers
) {}
