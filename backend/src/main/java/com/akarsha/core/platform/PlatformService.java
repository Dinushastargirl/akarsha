package com.akarsha.core.platform;

import com.akarsha.core.entity.Salon;
import com.akarsha.core.repository.SalonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlatformService {

    private final SalonRepository salonRepository;
    private final TenantSubscriptionRepository subscriptionRepository;

    public PlatformService(SalonRepository salonRepository, 
                           TenantSubscriptionRepository subscriptionRepository) {
        this.salonRepository = salonRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    @Transactional(readOnly = true)
    public SuperAdminDashboardDTO getDashboardMetrics() {
        long totalSalons = salonRepository.count();
        // Native queries or manual counting for specific metrics could be used here.
        // For simplicity, doing basic aggregations.
        long activeSalons = salonRepository.findAll().stream()
                .filter(s -> "ACTIVE".equals(s.getStatus())).count();
        long suspendedSalons = totalSalons - activeSalons;

        return SuperAdminDashboardDTO.builder()
                .totalSalons(totalSalons)
                .activeSalons(activeSalons)
                .suspendedSalons(suspendedSalons)
                // The remaining metrics will be queried via native queries or cross-tenant repos 
                // in a production scenario. Using placeholders for demonstration in Phase 2
                .totalUsers(0)
                .totalCustomers(0)
                .totalAppointments(0)
                .platformRevenueCents(0)
                .aiEnabledSalons(0)
                .build();
    }

    @Transactional
    public void suspendSalon(Long id) {
        Salon salon = salonRepository.findById(id).orElseThrow();
        salon.setStatus("SUSPENDED");
        salonRepository.save(salon);
    }

    @Transactional
    public void reactivateSalon(Long id) {
        Salon salon = salonRepository.findById(id).orElseThrow();
        salon.setStatus("ACTIVE");
        salonRepository.save(salon);
    }
}
