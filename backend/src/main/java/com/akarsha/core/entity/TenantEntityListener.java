package com.akarsha.core.entity;

import com.akarsha.tenant.TenantContext;
import jakarta.persistence.PrePersist;

public class TenantEntityListener {

    @PrePersist
    public void prePersist(Object target) {
        if (target instanceof TenantAwareEntity) {
            TenantAwareEntity tenantAwareEntity = (TenantAwareEntity) target;
            String tenantId = TenantContext.getCurrentTenant();
            
            if (tenantId == null || tenantId.trim().isEmpty()) {
                throw new IllegalStateException("Tenant context is missing! Cannot persist tenant-aware entity without a tenant ID.");
            }
            
            tenantAwareEntity.setTenantId(tenantId);
        }
    }
}
