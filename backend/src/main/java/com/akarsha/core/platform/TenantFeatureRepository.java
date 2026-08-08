package com.akarsha.core.platform;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TenantFeatureRepository extends JpaRepository<TenantFeature, Long> {
    List<TenantFeature> findByTenantId(String tenantId);
}
