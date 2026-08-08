package com.akarsha.core.platform;

import jakarta.persistence.*;
import lombok.Data;
import java.time.ZonedDateTime;

@Data
@Entity
@Table(name = "subscription_plans")
public class SubscriptionPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    private int maxStaff;
    private int maxCustomers;
    private int maxMonthlyAppointments;
    private int maxAiMessages;
    private int maxWhatsappMessages;
    private int priceCents;

    private ZonedDateTime createdAt;
}
