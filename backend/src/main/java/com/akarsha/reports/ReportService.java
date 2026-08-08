package com.akarsha.reports;

import com.akarsha.appointment.Appointment;
import com.akarsha.appointment.AppointmentRepository;
import com.akarsha.appointment.AppointmentStatus;
import com.akarsha.billing.Payment;
import com.akarsha.billing.PaymentMethod;
import com.akarsha.billing.PaymentRepository;
import com.akarsha.core.entity.ServiceEntity;
import com.akarsha.core.entity.User;
import com.akarsha.core.repository.ServiceRepository;
import com.akarsha.core.repository.UserRepository;
import com.akarsha.customer.CustomerRepository;
import com.akarsha.reports.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private final PaymentRepository paymentRepository;
    private final AppointmentRepository appointmentRepository;
    private final CustomerRepository customerRepository;
    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;

    public ReportService(PaymentRepository paymentRepository, AppointmentRepository appointmentRepository,
                         CustomerRepository customerRepository, ServiceRepository serviceRepository,
                         UserRepository userRepository) {
        this.paymentRepository = paymentRepository;
        this.appointmentRepository = appointmentRepository;
        this.customerRepository = customerRepository;
        this.serviceRepository = serviceRepository;
        this.userRepository = userRepository;
    }

    public ReportOverviewDTO getOverview(LocalDate startDate, LocalDate endDate, String timezone) {
        ZoneId zone = ZoneId.of(timezone);
        ZonedDateTime startZDT = startDate.atStartOfDay(zone);
        ZonedDateTime endZDT = endDate.plusDays(1).atStartOfDay(zone).minusNanos(1);

        // 1. Financials
        List<Payment> payments = paymentRepository.findByPaymentDateBetweenAndStatus(startZDT, endZDT, "SUCCESS");
        BigDecimal totalPaid = payments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        long paidInvoicesCount = payments.stream()
                .map(p -> p.getInvoice().getId())
                .distinct()
                .count();
        
        BigDecimal avgTxValue = paidInvoicesCount > 0 
                ? totalPaid.divide(BigDecimal.valueOf(paidInvoicesCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // 2. Appointments
        List<Appointment> appointments = appointmentRepository.findByAppointmentDateBetween(startDate, endDate);
        long totalAppointments = appointments.size();
        long completedAppointments = appointments.stream().filter(a -> a.getStatus() == AppointmentStatus.COMPLETED).count();
        long cancelledAppointments = appointments.stream().filter(a -> a.getStatus() == AppointmentStatus.CANCELLED).count();
        long noShows = appointments.stream().filter(a -> a.getStatus() == AppointmentStatus.NO_SHOW).count();
        
        // Gross revenue is sum of service prices for completed appointments
        BigDecimal grossRevenue = appointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
                .map(a -> a.getService().getPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. Customers
        long totalCustomers = customerRepository.count();
        long newCustomers = customerRepository.countByCreatedAtBetween(startZDT.toInstant(), endZDT.toInstant());
        
        // Returning customers in this period: customers with a completed appointment in this period who had an appointment before this period.
        // For simplicity and performance, we can just calculate unique customers in this period.
        long uniqueCustomersInPeriod = appointments.stream()
                .map(a -> a.getCustomer().getId())
                .distinct()
                .count();
        
        long returningCustomers = Math.max(0, uniqueCustomersInPeriod - newCustomers);

        return new ReportOverviewDTO(
                grossRevenue, totalPaid, avgTxValue, paidInvoicesCount,
                totalAppointments, completedAppointments, cancelledAppointments, noShows,
                totalCustomers, newCustomers, returningCustomers
        );
    }

    public RevenueReportDTO getRevenueReport(LocalDate startDate, LocalDate endDate, String timezone) {
        ZoneId zone = ZoneId.of(timezone);
        ZonedDateTime startZDT = startDate.atStartOfDay(zone);
        ZonedDateTime endZDT = endDate.plusDays(1).atStartOfDay(zone).minusNanos(1);

        List<Payment> payments = paymentRepository.findByPaymentDateBetweenAndStatus(startZDT, endZDT, "SUCCESS");

        Map<LocalDate, BigDecimal> dailyRevenue = payments.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getPaymentDate().withZoneSameInstant(zone).toLocalDate(),
                        Collectors.reducing(BigDecimal.ZERO, Payment::getAmount, BigDecimal::add)
                ));

        List<RevenueReportDTO.RevenueByDate> revenueByDate = dailyRevenue.entrySet().stream()
                .map(e -> new RevenueReportDTO.RevenueByDate(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(RevenueReportDTO.RevenueByDate::date))
                .toList();

        Map<PaymentMethod, BigDecimal> revenueByMethod = payments.stream()
                .collect(Collectors.groupingBy(
                        Payment::getPaymentMethod,
                        Collectors.reducing(BigDecimal.ZERO, Payment::getAmount, BigDecimal::add)
                ));

        return new RevenueReportDTO(revenueByDate, revenueByMethod);
    }

    public AppointmentReportDTO getAppointmentsReport(LocalDate startDate, LocalDate endDate) {
        List<Appointment> appointments = appointmentRepository.findByAppointmentDateBetween(startDate, endDate);
        
        Map<LocalDate, List<Appointment>> byDate = appointments.stream()
                .collect(Collectors.groupingBy(Appointment::getAppointmentDate));
        
        List<AppointmentReportDTO.AppointmentByDate> list = new ArrayList<>();
        
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            List<Appointment> dayAppts = byDate.getOrDefault(current, Collections.emptyList());
            long total = dayAppts.size();
            long completed = dayAppts.stream().filter(a -> a.getStatus() == AppointmentStatus.COMPLETED).count();
            long cancelled = dayAppts.stream().filter(a -> a.getStatus() == AppointmentStatus.CANCELLED).count();
            long noShow = dayAppts.stream().filter(a -> a.getStatus() == AppointmentStatus.NO_SHOW).count();
            
            list.add(new AppointmentReportDTO.AppointmentByDate(current, total, completed, cancelled, noShow));
            current = current.plusDays(1);
        }
        
        return new AppointmentReportDTO(list);
    }

    public List<ServicePerformanceDTO> getServicePerformance(LocalDate startDate, LocalDate endDate) {
        List<Appointment> appointments = appointmentRepository.findByAppointmentDateBetween(startDate, endDate);
        List<ServiceEntity> allServices = serviceRepository.findAll();
        
        return allServices.stream().map(service -> {
            List<Appointment> serviceAppts = appointments.stream()
                    .filter(a -> a.getService().getId().equals(service.getId()))
                    .toList();
            
            long total = serviceAppts.size();
            long completed = serviceAppts.stream().filter(a -> a.getStatus() == AppointmentStatus.COMPLETED).count();
            long cancelled = serviceAppts.stream().filter(a -> a.getStatus() == AppointmentStatus.CANCELLED).count();
            long noShow = serviceAppts.stream().filter(a -> a.getStatus() == AppointmentStatus.NO_SHOW).count();
            
            BigDecimal revenue = serviceAppts.stream()
                    .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
                    .map(a -> a.getService().getPrice())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
            return new ServicePerformanceDTO(
                    service.getId(), service.getName(),
                    total, completed, cancelled, noShow, revenue, service.getPrice()
            );
        }).filter(dto -> dto.appointmentCount() > 0 || serviceRepository.findById(dto.serviceId()).map(ServiceEntity::isActive).orElse(false))
        .sorted(Comparator.comparing(ServicePerformanceDTO::revenue).reversed())
        .toList();
    }

    public List<StaffPerformanceDTO> getStaffPerformance(LocalDate startDate, LocalDate endDate) {
        List<Appointment> appointments = appointmentRepository.findByAppointmentDateBetween(startDate, endDate);
        List<User> allStaff = userRepository.findAll().stream()
                .filter(u -> "STAFF".equals(u.getRole()) || "SALON_OWNER".equals(u.getRole()) || "MANAGER".equals(u.getRole()))
                .toList();

        return allStaff.stream().map(staff -> {
            List<Appointment> staffAppts = appointments.stream()
                    .filter(a -> a.getStaff().getId().equals(staff.getId()))
                    .toList();
            
            long total = staffAppts.size();
            long completed = staffAppts.stream().filter(a -> a.getStatus() == AppointmentStatus.COMPLETED).count();
            long cancelled = staffAppts.stream().filter(a -> a.getStatus() == AppointmentStatus.CANCELLED).count();
            long noShow = staffAppts.stream().filter(a -> a.getStatus() == AppointmentStatus.NO_SHOW).count();
            
            BigDecimal revenue = staffAppts.stream()
                    .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
                    .map(a -> a.getService().getPrice())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
            double completionRate = total > 0 ? (double) completed / total : 0.0;
                    
            return new StaffPerformanceDTO(
                    staff.getId(), staff.getFullName(),
                    total, completed, cancelled, noShow, revenue, completionRate
            );
        }).filter(dto -> dto.appointmentsAssigned() > 0 || userRepository.findById(dto.staffId()).map(User::isActive).orElse(false))
        .sorted(Comparator.comparing(StaffPerformanceDTO::revenueGenerated).reversed())
        .toList();
    }

    public CustomerInsightsDTO getCustomerInsights(LocalDate startDate, LocalDate endDate, String timezone) {
        ZoneId zone = ZoneId.of(timezone);
        ZonedDateTime startZDT = startDate.atStartOfDay(zone);
        ZonedDateTime endZDT = endDate.plusDays(1).atStartOfDay(zone).minusNanos(1);

        long totalCustomers = customerRepository.count();
        long newCustomers = customerRepository.countByCreatedAtBetween(startZDT.toInstant(), endZDT.toInstant());
        
        List<Appointment> appointments = appointmentRepository.findByAppointmentDateBetween(startDate, endDate);
        Map<Long, List<Appointment>> customerAppts = appointments.stream()
                .collect(Collectors.groupingBy(a -> a.getCustomer().getId()));
                
        long returningCustomers = customerAppts.size() - newCustomers;
        if (returningCustomers < 0) returningCustomers = 0;
        
        long totalUniqueActive = customerAppts.size();
        
        double avgVisits = totalUniqueActive > 0 ? (double) appointments.size() / totalUniqueActive : 0;
        double repeatRate = totalUniqueActive > 0 ? (double) returningCustomers / totalUniqueActive : 0;
        
        long withCompleted = customerAppts.values().stream()
                .filter(list -> list.stream().anyMatch(a -> a.getStatus() == AppointmentStatus.COMPLETED))
                .count();
                
        long withNoShow = customerAppts.values().stream()
                .filter(list -> list.stream().anyMatch(a -> a.getStatus() == AppointmentStatus.NO_SHOW))
                .count();

        return new CustomerInsightsDTO(totalCustomers, newCustomers, returningCustomers, repeatRate, avgVisits, withCompleted, withNoShow);
    }

    public NoShowReportDTO getNoShowReport(LocalDate startDate, LocalDate endDate) {
        List<Appointment> appointments = appointmentRepository.findByAppointmentDateBetween(startDate, endDate);
        long total = appointments.size();
        long noShows = appointments.stream().filter(a -> a.getStatus() == AppointmentStatus.NO_SHOW).count();
        double rate = total > 0 ? (double) noShows / total : 0;
        
        Map<Long, Long> byService = appointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.NO_SHOW)
                .collect(Collectors.groupingBy(a -> a.getService().getId(), Collectors.counting()));
                
        List<NoShowReportDTO.NoShowByService> serviceList = byService.entrySet().stream()
                .map(e -> {
                    String name = serviceRepository.findById(e.getKey()).map(ServiceEntity::getName).orElse("Unknown");
                    return new NoShowReportDTO.NoShowByService(e.getKey(), name, e.getValue());
                })
                .sorted(Comparator.comparing(NoShowReportDTO.NoShowByService::count).reversed())
                .toList();

        Map<Long, Long> byStaff = appointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.NO_SHOW)
                .collect(Collectors.groupingBy(a -> a.getStaff().getId(), Collectors.counting()));
                
        List<NoShowReportDTO.NoShowByStaff> staffList = byStaff.entrySet().stream()
                .map(e -> {
                    String name = userRepository.findById(e.getKey()).map(User::getFullName).orElse("Unknown");
                    return new NoShowReportDTO.NoShowByStaff(e.getKey(), name, e.getValue());
                })
                .sorted(Comparator.comparing(NoShowReportDTO.NoShowByStaff::count).reversed())
                .toList();
                
        Map<LocalDate, Long> byDate = appointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.NO_SHOW)
                .collect(Collectors.groupingBy(Appointment::getAppointmentDate, Collectors.counting()));
                
        List<NoShowReportDTO.NoShowByDate> dateList = byDate.entrySet().stream()
                .map(e -> new NoShowReportDTO.NoShowByDate(e.getKey().toString(), e.getValue()))
                .sorted(Comparator.comparing(NoShowReportDTO.NoShowByDate::date))
                .toList();

        return new NoShowReportDTO(noShows, rate, serviceList, staffList, dateList);
    }
}
