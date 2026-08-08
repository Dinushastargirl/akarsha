package com.akarsha.reports.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import com.akarsha.billing.PaymentMethod;

public record RevenueReportDTO(
    List<RevenueByDate> revenueByDate,
    Map<PaymentMethod, BigDecimal> revenueByMethod
) {
    public record RevenueByDate(
        LocalDate date,
        BigDecimal amount
    ) {}
}
