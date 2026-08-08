package com.akarsha.dashboard;

import com.akarsha.appointment.Appointment;
import com.akarsha.appointment.AppointmentRepository;
import com.akarsha.appointment.AppointmentStatus;
import com.akarsha.core.repository.ServiceRepository;
import com.akarsha.core.repository.UserRepository;
import com.akarsha.customer.CustomerRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private static final List<AppointmentStatus> REVENUE_STATUSES = List.of(
            AppointmentStatus.BOOKED,
            AppointmentStatus.CONFIRMED,
            AppointmentStatus.COMPLETED
    );

    private static final List<AppointmentStatus> UPCOMING_STATUSES = List.of(
            AppointmentStatus.BOOKED,
            AppointmentStatus.CONFIRMED
    );

    private final AppointmentRepository appointmentRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;

    public DashboardController(
            AppointmentRepository appointmentRepository,
            CustomerRepository customerRepository,
            UserRepository userRepository,
            ServiceRepository serviceRepository) {
        this.appointmentRepository = appointmentRepository;
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.serviceRepository = serviceRepository;
    }

    @GetMapping
    public ResponseEntity<DashboardStats> getDashboard() {
        LocalDate today = LocalDate.now();

        // — Today's appointment counts
        long todayTotal = appointmentRepository.countByDateAndStatuses(
                today,
                List.of(AppointmentStatus.BOOKED, AppointmentStatus.CONFIRMED,
                        AppointmentStatus.COMPLETED, AppointmentStatus.CANCELLED, AppointmentStatus.NO_SHOW)
        );
        long todayCompleted = appointmentRepository.countByDateAndStatuses(
                today, List.of(AppointmentStatus.COMPLETED)
        );
        long todayCancelled = appointmentRepository.countByDateAndStatuses(
                today, List.of(AppointmentStatus.CANCELLED, AppointmentStatus.NO_SHOW)
        );

        // — Revenue: BOOKED + CONFIRMED + COMPLETED (not cancelled/no-show)
        BigDecimal revenue = appointmentRepository.sumRevenueByDateAndStatuses(today, REVENUE_STATUSES);
        if (revenue == null) revenue = BigDecimal.ZERO;

        // — Aggregate counts (all auto-tenant-filtered by Hibernate @Filter)
        long totalCustomers = customerRepository.count();
        long activeStaff = userRepository.countActiveStaff();
        long activeServices = serviceRepository.countByActive(true);

        // — Today's timeline (all statuses, ordered by start time)
        List<Appointment> timelineEntities = appointmentRepository.findTodayTimeline(today);
        List<DashboardStats.AppointmentSummary> timeline = timelineEntities.stream()
                .map(DashboardController::toSummary)
                .toList();

        // — Upcoming: BOOKED + CONFIRMED from today onward, max 10
        List<Appointment> upcomingEntities = appointmentRepository.findUpcoming(
                today, UPCOMING_STATUSES, PageRequest.of(0, 10)
        );
        List<DashboardStats.AppointmentSummary> upcoming = upcomingEntities.stream()
                .map(DashboardController::toSummary)
                .toList();

        DashboardStats stats = new DashboardStats(
                todayTotal,
                todayCompleted,
                todayCancelled,
                revenue,
                totalCustomers,
                activeStaff,
                activeServices,
                timeline,
                upcoming
        );

        return ResponseEntity.ok(stats);
    }

    private static DashboardStats.AppointmentSummary toSummary(Appointment a) {
        return new DashboardStats.AppointmentSummary(
                a.getId(),
                a.getCustomer() != null ? a.getCustomer().getFullName() : "—",
                a.getStaff() != null ? a.getStaff().getFullName() : "—",
                a.getService() != null ? a.getService().getName() : "—",
                a.getAppointmentDate().toString(),
                a.getStartTime().toString(),
                a.getEndTime().toString(),
                a.getStatus().name(),
                a.getService() != null ? a.getService().getPrice() : BigDecimal.ZERO
        );
    }
}
