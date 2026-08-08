package com.akarsha.core.platform;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SuperAdminDashboardDTO {
    private long totalSalons;
    private long activeSalons;
    private long suspendedSalons;
    private long totalUsers;
    private long totalCustomers;
    private long totalAppointments;
    private long platformRevenueCents;
    private long aiEnabledSalons;
}
