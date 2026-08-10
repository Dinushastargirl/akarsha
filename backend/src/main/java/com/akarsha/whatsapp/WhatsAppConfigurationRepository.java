package com.akarsha.whatsapp;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WhatsAppConfigurationRepository extends JpaRepository<WhatsAppConfiguration, Long> {
    
    Optional<WhatsAppConfiguration> findByTenantId(String tenantId);

    // Bypass tenant isolation for webhook routing
    @Query(value = "SELECT * FROM whatsapp_configurations WHERE phone_number_id = ?1", nativeQuery = true)
    Optional<WhatsAppConfiguration> findByPhoneNumberIdSystemBypass(String phoneNumberId);
}
