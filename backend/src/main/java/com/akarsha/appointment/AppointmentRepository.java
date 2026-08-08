package com.akarsha.appointment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    Page<Appointment> findByAppointmentDate(LocalDate date, Pageable pageable);

    Page<Appointment> findByAppointmentDateAndStaffId(LocalDate date, Long staffId, Pageable pageable);

    Page<Appointment> findByAppointmentDateAndStatus(LocalDate date, AppointmentStatus status, Pageable pageable);

    Page<Appointment> findByAppointmentDateAndStaffIdAndStatus(LocalDate date, Long staffId, AppointmentStatus status, Pageable pageable);

    Page<Appointment> findByAppointmentDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);
    
    List<Appointment> findByAppointmentDateBetween(LocalDate startDate, LocalDate endDate);

    Page<Appointment> findByStaffId(Long staffId, Pageable pageable);

    Page<Appointment> findByCustomerId(Long customerId, Pageable pageable);

    Page<Appointment> findByStatus(AppointmentStatus status, Pageable pageable);

    @Query("SELECT a FROM Appointment a WHERE a.staff.id = :staffId AND a.appointmentDate = :date AND a.status <> 'CANCELLED'")
    List<Appointment> findActiveAppointmentsByStaffAndDate(@Param("staffId") Long staffId, @Param("date") LocalDate date);

    boolean existsByServiceId(Long serviceId);

    // ─── Dashboard aggregate queries ──────────────────────────────────────────

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.appointmentDate = :date AND a.status IN :statuses")
    long countByDateAndStatuses(@Param("date") LocalDate date, @Param("statuses") List<AppointmentStatus> statuses);

    @Query("SELECT COALESCE(SUM(a.service.price), 0) FROM Appointment a WHERE a.appointmentDate = :date AND a.status IN :statuses")
    BigDecimal sumRevenueByDateAndStatuses(@Param("date") LocalDate date, @Param("statuses") List<AppointmentStatus> statuses);

    @Query("SELECT a FROM Appointment a WHERE a.appointmentDate = :date ORDER BY a.startTime ASC")
    List<Appointment> findTodayTimeline(@Param("date") LocalDate date);

    @Query("SELECT a FROM Appointment a WHERE a.appointmentDate >= :fromDate AND a.status IN :statuses ORDER BY a.appointmentDate ASC, a.startTime ASC")
    List<Appointment> findUpcoming(@Param("fromDate") LocalDate fromDate, @Param("statuses") List<AppointmentStatus> statuses, Pageable pageable);

    // ─── Customer Stats Queries ───────────────────────────────────────────────

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.customer.id = :customerId")
    long countTotalVisitsByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.customer.id = :customerId AND a.status = 'COMPLETED'")
    long countCompletedVisitsByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.customer.id = :customerId AND a.status IN ('CANCELLED', 'NO_SHOW')")
    long countNoShowsByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT COALESCE(SUM(a.service.price), 0) FROM Appointment a WHERE a.customer.id = :customerId AND a.status = 'COMPLETED'")
    BigDecimal sumRevenueByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT MAX(a.appointmentDate) FROM Appointment a WHERE a.customer.id = :customerId AND a.status = 'COMPLETED'")
    LocalDate findLastVisitDateByCustomerId(@Param("customerId") Long customerId);
}
