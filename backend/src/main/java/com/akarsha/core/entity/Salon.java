package com.akarsha.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "salons")
@Getter
@Setter
public class Salon extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String subdomain;

    @Column
    private String phone;

    @Column
    private String address;

    @Column
    private String city;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "business_type")
    private String businessType;

    @Column(name = "opening_time")
    private String openingTime;

    @Column(name = "closing_time")
    private String closingTime;

    @Column(name = "setup_completed")
    private boolean setupCompleted = false;
}
