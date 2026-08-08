package com.akarsha.core.platform;

import jakarta.persistence.*;
import lombok.Data;
import java.time.ZonedDateTime;

@Data
@Entity
@Table(name = "platform_audit_logs")
public class PlatformAuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String actorEmail;

    @Column(nullable = false)
    private String action;

    private String targetTenantId;

    private String metadataJson;

    private ZonedDateTime createdAt;
}
