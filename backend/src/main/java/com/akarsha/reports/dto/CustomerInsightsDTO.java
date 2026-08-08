package com.akarsha.reports.dto;

public record CustomerInsightsDTO(
    long totalCustomers,
    long newCustomers,
    long returningCustomers,
    double repeatVisitRate,
    double averageVisitsPerCustomer,
    long customersWithCompletedVisits,
    long customersWithNoShows
) {}
