package com.akarsha.core.platform;

import jakarta.persistence.*;
import lombok.Data;
import java.time.ZonedDateTime;

@Data
@Entity
@Table(name = "tenant_subscriptions")
public class TenantSubscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String tenantId;

    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
    private SubscriptionPlan plan;

    private String status;
    private ZonedDateTime startDate;
    private ZonedDateTime renewalDate;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}
