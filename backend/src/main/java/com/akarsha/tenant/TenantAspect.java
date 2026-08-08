package com.akarsha.tenant;

import jakarta.persistence.EntityManager;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TenantAspect {

    private static final Logger log = LoggerFactory.getLogger(TenantAspect.class);
    private static final String BLOCKED_TENANT = "SYSTEM_BLOCKED_NO_TENANT_CONTEXT";

    @Autowired
    private EntityManager entityManager;

    @Pointcut("execution(* org.springframework.data.repository.Repository+.*(..))")
    public void repositoryMethods() {}

    @Before("repositoryMethods()")
    public void beforeRepositoryMethod() {
        String tenantId = TenantContext.getCurrentTenant();
        Session session = entityManager.unwrap(Session.class);
        
        if (tenantId != null && !tenantId.trim().isEmpty()) {
            if ("SYSTEM_BYPASS".equals(tenantId)) {
                session.disableFilter("tenantFilter");
            } else {
                session.enableFilter("tenantFilter").setParameter("tenantId", tenantId);
            }
        } else {
            // Production safeguard: If no tenant is specified, apply a dummy ID that matches nothing 
            // to guarantee that no tenant-aware data leaks.
            log.warn("Missing tenant context for repository query! Enabling tenantFilter with fallback blocked ID.");
            session.enableFilter("tenantFilter").setParameter("tenantId", BLOCKED_TENANT);
        }
    }
}
