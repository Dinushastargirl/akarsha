package com.akarsha.ai;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AiConfigurationRepository extends JpaRepository<AiConfiguration, Long> {
    Optional<AiConfiguration> findByTenantId(String tenantId);
}
