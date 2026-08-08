package com.akarsha.appointment;

import com.akarsha.core.entity.Salon;
import com.akarsha.core.entity.ServiceEntity;
import com.akarsha.core.entity.User;
import com.akarsha.core.repository.SalonRepository;
import com.akarsha.core.repository.ServiceRepository;
import com.akarsha.core.repository.UserRepository;
import com.akarsha.customer.Customer;
import com.akarsha.customer.CustomerRepository;
import com.akarsha.tenant.TenantContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import com.akarsha.staff.StaffSchedule;
import com.akarsha.staff.StaffScheduleRepository;

@RestController
@RequestMapping("/api/v1/appointments")
public class AppointmentController {

    private final AppointmentRepository appointmentRepository;
    private final CustomerRepository customerRepository;
    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;
    private final SalonRepository salonRepository;
    private final StaffScheduleRepository staffScheduleRepository;
    private final com.akarsha.staff.StaffTimeOffRepository staffTimeOffRepository;

    public AppointmentController(
            AppointmentRepository appointmentRepository,
            CustomerRepository customerRepository,
            ServiceRepository serviceRepository,
            UserRepository userRepository,
            SalonRepository salonRepository,
            StaffScheduleRepository staffScheduleRepository,
            com.akarsha.staff.StaffTimeOffRepository staffTimeOffRepository) {
        this.appointmentRepository = appointmentRepository;
        this.customerRepository = customerRepository;
        this.serviceRepository = serviceRepository;
        this.userRepository = userRepository;
        this.salonRepository = salonRepository;
        this.staffScheduleRepository = staffScheduleRepository;
        this.staffTimeOffRepository = staffTimeOffRepository;
    }

    @GetMapping
    public ResponseEntity<Page<Appointment>> getAppointments(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long staffId,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("appointmentDate").ascending().and(Sort.by("startTime").ascending()));
        
        Page<Appointment> results;
        if (date != null) {
            if (staffId != null && status != null) {
                results = appointmentRepository.findByAppointmentDateAndStaffIdAndStatus(date, staffId, status, pageable);
            } else if (staffId != null) {
                results = appointmentRepository.findByAppointmentDateAndStaffId(date, staffId, pageable);
            } else if (status != null) {
                results = appointmentRepository.findByAppointmentDateAndStatus(date, status, pageable);
            } else {
                results = appointmentRepository.findByAppointmentDate(date, pageable);
            }
        } else if (startDate != null && endDate != null) {
            results = appointmentRepository.findByAppointmentDateBetween(startDate, endDate, pageable);
        } else if (staffId != null) {
            results = appointmentRepository.findByStaffId(staffId, pageable);
        } else if (customerId != null) {
            results = appointmentRepository.findByCustomerId(customerId, pageable);
        } else if (status != null) {
            results = appointmentRepository.findByStatus(status, pageable);
        } else {
            results = appointmentRepository.findAll(pageable);
        }
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Appointment> getAppointmentById(@PathVariable Long id) {
        Optional<Appointment> appOpt = appointmentRepository.findById(id);
        if (appOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        Appointment app = appOpt.get();
        String currentTenant = TenantContext.getCurrentTenant();
        if (currentTenant != null && !currentTenant.equals(app.getTenantId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(app);
    }

    @PostMapping
    public ResponseEntity<?> createAppointment(@RequestBody AppointmentRequest request) {
        String currentTenant = TenantContext.getCurrentTenant();
        
        // 1. Validate referenced entities
        Optional<Customer> customerOpt = customerRepository.findById(request.getCustomerId());
        if (customerOpt.isEmpty() || (currentTenant != null && !currentTenant.equals(customerOpt.get().getTenantId()))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or cross-tenant customer reference");
        }

        Optional<ServiceEntity> serviceOpt = serviceRepository.findById(request.getServiceId());
        if (serviceOpt.isEmpty() || (currentTenant != null && !currentTenant.equals(serviceOpt.get().getTenantId()))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or cross-tenant service reference");
        }
        if (!serviceOpt.get().isActive()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Service is not currently available for booking");
        }

        Optional<User> staffOpt = userRepository.findById(request.getStaffId());
        if (staffOpt.isEmpty() || (currentTenant != null && !currentTenant.equals(staffOpt.get().getTenantId()))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or cross-tenant staff reference");
        }

        // Validate active staff status
        User staff = staffOpt.get();
        if (!staff.isActive()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Staff member is inactive");
        }

        // Validate service assignment rules (only if staff has explicitly assigned services)
        if (!staff.getServices().isEmpty() && !staff.getServices().contains(serviceOpt.get())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Staff member is not assigned to this service");
        }

        // Validate working hours/schedule
        int dayOfWeek = request.getAppointmentDate().getDayOfWeek().getValue(); // 1 = Monday, 7 = Sunday
        Optional<StaffSchedule> schedOpt = staffScheduleRepository.findByStaffIdAndDayOfWeek(request.getStaffId(), dayOfWeek);
        if (schedOpt.isPresent()) {
            StaffSchedule schedule = schedOpt.get();
            if (!schedule.isWorking()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Staff member is not working on this day");
            }
            if (request.getStartTime().isBefore(schedule.getStartTime()) || request.getEndTime().isAfter(schedule.getEndTime())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Appointment time falls outside staff working hours");
            }
        } else {
            // Default fallback: 09:00 - 18:00
            if (request.getStartTime().isBefore(LocalTime.of(9, 0)) || request.getEndTime().isAfter(LocalTime.of(18, 0))) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Appointment time falls outside standard hours");
            }
        }

        // 2. Validate start/end times and enforce service duration
        int duration = serviceOpt.get().getDurationMinutes();
        LocalTime expectedEndTime = request.getStartTime().plusMinutes(duration);
        if (!request.getEndTime().equals(expectedEndTime)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("End time must exactly match the service duration (" + duration + " minutes)");
        }

        if (request.getStartTime().isAfter(request.getEndTime()) || request.getStartTime().equals(request.getEndTime())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Start time must be before end time");
        }

        // 3. Double-booking conflict check
        boolean conflict = hasTimeConflict(request.getStaffId(), request.getAppointmentDate(), request.getStartTime(), request.getEndTime(), null);
        if (conflict) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Staff member is already booked during this time range");
        }

        Appointment appointment = new Appointment();
        appointment.setCustomer(customerOpt.get());
        appointment.setService(serviceOpt.get());
        appointment.setStaff(staffOpt.get());
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setStartTime(request.getStartTime());
        appointment.setEndTime(request.getEndTime());
        appointment.setStatus(AppointmentStatus.BOOKED);
        appointment.setNotes(request.getNotes());

        Appointment saved = appointmentRepository.save(appointment);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAppointment(@PathVariable Long id, @RequestBody AppointmentRequest request) {
        Optional<Appointment> appOpt = appointmentRepository.findById(id);
        if (appOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Appointment not found");
        }
        Appointment appointment = appOpt.get();
        String currentTenant = TenantContext.getCurrentTenant();
        if (currentTenant != null && !currentTenant.equals(appointment.getTenantId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Appointment not found");
        }

        // Validate references
        Optional<Customer> customerOpt = customerRepository.findById(request.getCustomerId());
        if (customerOpt.isEmpty() || (currentTenant != null && !currentTenant.equals(customerOpt.get().getTenantId()))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or cross-tenant customer reference");
        }

        Optional<ServiceEntity> serviceOpt = serviceRepository.findById(request.getServiceId());
        if (serviceOpt.isEmpty() || (currentTenant != null && !currentTenant.equals(serviceOpt.get().getTenantId()))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or cross-tenant service reference");
        }
        if (!serviceOpt.get().isActive()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Service is not currently available for booking");
        }

        Optional<User> staffOpt = userRepository.findById(request.getStaffId());
        if (staffOpt.isEmpty() || (currentTenant != null && !currentTenant.equals(staffOpt.get().getTenantId()))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or cross-tenant staff reference");
        }

        // Validate active staff status
        User staff = staffOpt.get();
        if (!staff.isActive()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Staff member is inactive");
        }

        // Validate service assignment rules (only if staff has explicitly assigned services)
        if (!staff.getServices().isEmpty() && !staff.getServices().contains(serviceOpt.get())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Staff member is not assigned to this service");
        }

        // Validate working hours/schedule
        int dayOfWeek = request.getAppointmentDate().getDayOfWeek().getValue();
        Optional<StaffSchedule> schedOpt = staffScheduleRepository.findByStaffIdAndDayOfWeek(request.getStaffId(), dayOfWeek);
        if (schedOpt.isPresent()) {
            StaffSchedule schedule = schedOpt.get();
            if (!schedule.isWorking()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Staff member is not working on this day");
            }
            if (request.getStartTime().isBefore(schedule.getStartTime()) || request.getEndTime().isAfter(schedule.getEndTime())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Appointment time falls outside staff working hours");
            }
        } else {
            // Default fallback: 09:00 - 18:00
            if (request.getStartTime().isBefore(LocalTime.of(9, 0)) || request.getEndTime().isAfter(LocalTime.of(18, 0))) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Appointment time falls outside standard hours");
            }
        }

        // Validate times and enforce service duration
        int duration = serviceOpt.get().getDurationMinutes();
        LocalTime expectedEndTime = request.getStartTime().plusMinutes(duration);
        if (!request.getEndTime().equals(expectedEndTime)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("End time must exactly match the service duration (" + duration + " minutes)");
        }

        if (request.getStartTime().isAfter(request.getEndTime()) || request.getStartTime().equals(request.getEndTime())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Start time must be before end time");
        }

        // Conflict check
        boolean conflict = hasTimeConflict(request.getStaffId(), request.getAppointmentDate(), request.getStartTime(), request.getEndTime(), id);
        if (conflict) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Staff member is already booked during this time range");
        }

        appointment.setCustomer(customerOpt.get());
        appointment.setService(serviceOpt.get());
        appointment.setStaff(staffOpt.get());
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setStartTime(request.getStartTime());
        appointment.setEndTime(request.getEndTime());
        appointment.setNotes(request.getNotes());

        Appointment saved = appointmentRepository.save(appointment);
        return ResponseEntity.ok(saved);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> patchStatus(@PathVariable Long id, @RequestParam AppointmentStatus status) {
        Optional<Appointment> appOpt = appointmentRepository.findById(id);
        if (appOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Appointment not found");
        }
        Appointment appointment = appOpt.get();
        String currentTenant = TenantContext.getCurrentTenant();
        if (currentTenant != null && !currentTenant.equals(appointment.getTenantId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Appointment not found");
        }

        if (status == AppointmentStatus.COMPLETED) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Status cannot be manually set to COMPLETED. Use the checkout endpoint instead.");
        }

        appointment.setStatus(status);
        Appointment saved = appointmentRepository.save(appointment);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAppointment(@PathVariable Long id) {
        Optional<Appointment> appOpt = appointmentRepository.findById(id);
        if (appOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Appointment not found");
        }
        Appointment appointment = appOpt.get();
        String currentTenant = TenantContext.getCurrentTenant();
        if (currentTenant != null && !currentTenant.equals(appointment.getTenantId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Appointment not found");
        }

        appointmentRepository.delete(appointment);
        return ResponseEntity.ok("Appointment deleted successfully");
    }

    @GetMapping("/services")
    public ResponseEntity<List<ServiceEntity>> getServices() {
        return ResponseEntity.ok(serviceRepository.findAll());
    }

    @GetMapping("/staff")
    public ResponseEntity<List<User>> getStaffMembers() {
        // Automatically filtered by active tenantId context via Hibernate Filter
        List<User> allUsers = userRepository.findAll();
        List<User> staffMembers = allUsers.stream()
                .filter(u -> !"PLATFORM_ADMIN".equalsIgnoreCase(u.getRole()) && u.isActive())
                .collect(Collectors.toList());
        return ResponseEntity.ok(staffMembers);
    }

    @GetMapping("/slots")
    public ResponseEntity<List<String>> getAvailableSlots(
            @RequestParam Long staffId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam int durationMinutes) {
        
        String currentTenant = TenantContext.getCurrentTenant();
        
        // Find staff working hours for this specific day
        int dayOfWeek = date.getDayOfWeek().getValue();
        Optional<StaffSchedule> schedOpt = staffScheduleRepository.findByStaffIdAndDayOfWeek(staffId, dayOfWeek);
        if (schedOpt.isEmpty() || !schedOpt.get().isWorking()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        LocalTime openTime = schedOpt.get().getStartTime();
        LocalTime closeTime = schedOpt.get().getEndTime();

        List<Appointment> existing = appointmentRepository.findActiveAppointmentsByStaffAndDate(staffId, date);
        List<String> availableSlots = new ArrayList<>();

        LocalTime current = openTime;
        while (current.plusMinutes(durationMinutes).isBefore(closeTime) || current.plusMinutes(durationMinutes).equals(closeTime)) {
            LocalTime slotStart = current;
            LocalTime slotEnd = current.plusMinutes(durationMinutes);
            
            boolean conflict = false;
            for (Appointment a : existing) {
                if (slotStart.isBefore(a.getEndTime()) && slotEnd.isAfter(a.getStartTime())) {
                    conflict = true;
                    break;
                }
            }
            if (!conflict) {
                availableSlots.add(slotStart.toString());
            }
            current = current.plusMinutes(30); // 30-minute intervals
        }

        return ResponseEntity.ok(availableSlots);
    }

    private boolean hasTimeConflict(Long staffId, LocalDate date, LocalTime start, LocalTime end, Long ignoreId) {
        // 1. Check existing appointments
        List<Appointment> active = appointmentRepository.findActiveAppointmentsByStaffAndDate(staffId, date);
        for (Appointment a : active) {
            if (ignoreId != null && ignoreId.equals(a.getId())) {
                continue;
            }
            if (start.isBefore(a.getEndTime()) && end.isAfter(a.getStartTime())) {
                return true;
            }
        }

        // 2. Check breaks
        int dayOfWeek = date.getDayOfWeek().getValue();
        Optional<StaffSchedule> schedOpt = staffScheduleRepository.findByStaffIdAndDayOfWeek(staffId, dayOfWeek);
        if (schedOpt.isPresent()) {
            StaffSchedule schedule = schedOpt.get();
            if (schedule.getBreakStartTime() != null && schedule.getBreakEndTime() != null) {
                if (start.isBefore(schedule.getBreakEndTime()) && end.isAfter(schedule.getBreakStartTime())) {
                    return true;
                }
            }
        }

        // 3. Check time-offs
        java.time.LocalDateTime dayStart = date.atStartOfDay();
        java.time.LocalDateTime dayEnd = date.atTime(LocalTime.MAX);
        List<com.akarsha.staff.StaffTimeOff> timeOffs = staffTimeOffRepository.findOverlappingTimeOff(staffId, dayStart, dayEnd);
        
        java.time.LocalDateTime slotStartLdt = date.atTime(start);
        java.time.LocalDateTime slotEndLdt = date.atTime(end);
        for (com.akarsha.staff.StaffTimeOff to : timeOffs) {
            if (slotStartLdt.isBefore(to.getEndTime()) && slotEndLdt.isAfter(to.getStartTime())) {
                return true;
            }
        }

        return false;
    }
}
