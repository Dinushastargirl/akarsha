package com.akarsha.customer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerStats {
    private long totalVisits;
    private long completedVisits;
    private long noShowCount;
    private BigDecimal totalRevenue;
    private LocalDate lastVisitDate;
}
