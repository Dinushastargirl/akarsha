package com.akarsha.staff;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface StaffTimeOffRepository extends JpaRepository<StaffTimeOff, Long> {
    List<StaffTimeOff> findByStaffId(Long staffId);

    @Query("SELECT s FROM StaffTimeOff s WHERE s.staff.id = :staffId " +
           "AND s.startTime < :end AND s.endTime > :start")
    List<StaffTimeOff> findOverlappingTimeOff(
            @Param("staffId") Long staffId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
