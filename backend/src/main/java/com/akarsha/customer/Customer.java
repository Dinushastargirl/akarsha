package com.akarsha.customer;

import com.akarsha.core.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import java.time.LocalDate;

@Entity
@Table(name = "customers")
@Getter
@Setter
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public class Customer extends TenantAwareEntity {

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String phone;

    @Column
    private String email;

    @Column
    private LocalDate birthday;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
