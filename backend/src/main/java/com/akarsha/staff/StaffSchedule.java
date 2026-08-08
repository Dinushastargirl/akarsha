package com.akarsha.staff;

import com.akarsha.core.entity.TenantAwareEntity;
import com.akarsha.core.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

import java.time.LocalTime;

@Entity
@Table(name = "staff_schedules")
@Getter
@Setter
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public class StaffSchedule extends TenantAwareEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "staff_id", nullable = false)
    private User staff;

    @Column(name = "day_of_week", nullable = false)
    private int dayOfWeek; // 1 = Monday, 7 = Sunday

    @Column(nullable = false)
    private boolean working = true;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime = LocalTime.of(9, 0);

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime = LocalTime.of(18, 0);

    @Column(name = "break_start_time")
    private LocalTime breakStartTime;

    @Column(name = "break_end_time")
    private LocalTime breakEndTime;
}
