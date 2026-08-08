package com.akarsha.staff;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffScheduleRepository extends JpaRepository<StaffSchedule, Long> {
    List<StaffSchedule> findByStaffId(Long staffId);
    Optional<StaffSchedule> findByStaffIdAndDayOfWeek(Long staffId, int dayOfWeek);
}
