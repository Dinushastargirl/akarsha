package com.akarsha.core.platform;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlatformAuditLogRepository extends JpaRepository<PlatformAuditLog, Long> {
}
