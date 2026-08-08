package com.akarsha.core.platform;

import jakarta.persistence.*;
import lombok.Data;
import java.time.ZonedDateTime;

@Data
@Entity
@Table(name = "tenant_features")
public class TenantFeature {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private String featureName;

    private boolean isEnabled;

    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}
