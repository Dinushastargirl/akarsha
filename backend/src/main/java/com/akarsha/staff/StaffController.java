package com.akarsha.staff;

import com.akarsha.core.entity.ServiceEntity;
import com.akarsha.core.entity.User;
import com.akarsha.core.repository.ServiceRepository;
import com.akarsha.core.repository.UserRepository;
import com.akarsha.tenant.TenantContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/staff")
public class StaffController {

    private final UserRepository userRepository;
    private final StaffScheduleRepository staffScheduleRepository;
    private final ServiceRepository serviceRepository;
    private final StaffTimeOffRepository staffTimeOffRepository;
    private final com.akarsha.appointment.AppointmentRepository appointmentRepository;

    public StaffController(
            UserRepository userRepository,
            StaffScheduleRepository staffScheduleRepository,
            ServiceRepository serviceRepository,
            StaffTimeOffRepository staffTimeOffRepository,
            com.akarsha.appointment.AppointmentRepository appointmentRepository) {
        this.userRepository = userRepository;
        this.staffScheduleRepository = staffScheduleRepository;
        this.serviceRepository = serviceRepository;
        this.staffTimeOffRepository = staffTimeOffRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @GetMapping
    public ResponseEntity<Page<User>> getStaff(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("fullName").ascending());
        String queryParam = (query != null && !query.trim().isEmpty()) ? query.trim() : null;
        Page<User> results = userRepository.searchStaff(queryParam, active, pageable);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getStaffById(@PathVariable Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        User user = userOpt.get();
        String currentTenant = TenantContext.getCurrentTenant();
        if (currentTenant != null && !currentTenant.equals(user.getTenantId())) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SALON_OWNER', 'MANAGER')")
    public ResponseEntity<?> createStaff(@RequestBody StaffRequest request) {
        String currentTenant = TenantContext.getCurrentTenant();
        if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Full name is required");
        }
        if (request.getEmail() == null || !request.getEmail().contains("@")) {
            return ResponseEntity.badRequest().body("Valid email is required");
        }
        if (request.getRole() == null || request.getRole().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Role is required");
        }

        // Check unique email constraint
        Optional<User> existing = userRepository.findByEmail(request.getEmail().trim());
        if (existing.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email is already registered");
        }

        User user = new User();
        user.setFullName(request.getFullName().trim());
        user.setUsername("staff_" + UUID.randomUUID().toString().substring(0, 8));
        user.setEmail(request.getEmail().trim());
        user.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
        user.setRole(request.getRole().toUpperCase().trim());
        user.setActive(request.isActive());
        user.setTenantId(currentTenant);
        // Default locked password hash for staff members not logging in directly
        user.setPasswordHash("N/A_STAFF_MEMBER");

        User saved = userRepository.save(user);

        // Initialize default working schedule: 09:00 - 18:00 for Mon-Sat
        for (int i = 1; i <= 7; i++) {
            StaffSchedule schedule = new StaffSchedule();
            schedule.setStaff(saved);
            schedule.setDayOfWeek(i);
            schedule.setWorking(i != 7); // Default off on Sundays (Day 7)
            schedule.setStartTime(LocalTime.of(9, 0));
            schedule.setEndTime(LocalTime.of(18, 0));
            schedule.setTenantId(currentTenant);
            staffScheduleRepository.save(schedule);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SALON_OWNER', 'MANAGER')")
    public ResponseEntity<?> updateStaff(@PathVariable Long id, @RequestBody StaffRequest request) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        User user = userOpt.get();
        String currentTenant = TenantContext.getCurrentTenant();
        if (currentTenant != null && !currentTenant.equals(user.getTenantId())) {
            return ResponseEntity.notFound().build();
        }

        if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Full name is required");
        }
        if (request.getRole() == null || request.getRole().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Role is required");
        }

        user.setFullName(request.getFullName().trim());
        user.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
        user.setRole(request.getRole().toUpperCase().trim());
        user.setActive(request.isActive());

        User saved = userRepository.save(user);
        return ResponseEntity.ok(saved);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> patchStatus(@PathVariable Long id, @RequestParam boolean active) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        User user = userOpt.get();
        String currentTenant = TenantContext.getCurrentTenant();
        if (currentTenant != null && !currentTenant.equals(user.getTenantId())) {
            return ResponseEntity.notFound().build();
        }

        user.setActive(active);
        User saved = userRepository.save(user);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SALON_OWNER', 'MANAGER')")
    public ResponseEntity<?> deleteStaff(@PathVariable Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        User user = userOpt.get();
        String currentTenant = TenantContext.getCurrentTenant();
        if (currentTenant != null && !currentTenant.equals(user.getTenantId())) {
            return ResponseEntity.notFound().build();
        }

        // Historical data retention requirement: prefer deactivation where historical appointments exist
        user.setActive(false);
        userRepository.save(user);
        return ResponseEntity.ok("Staff member deactivated successfully");
    }

    @GetMapping("/{id}/schedule")
    public ResponseEntity<?> getSchedule(@PathVariable Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        User user = userOpt.get();
        String currentTenant = TenantContext.getCurrentTenant();
        if (currentTenant != null && !currentTenant.equals(user.getTenantId())) {
            return ResponseEntity.notFound().build();
        }

        List<StaffSchedule> schedule = staffScheduleRepository.findByStaffId(id);
        schedule.sort(Comparator.comparingInt(StaffSchedule::getDayOfWeek));
        return ResponseEntity.ok(schedule);
    }

    @PutMapping("/{id}/schedule")
    @PreAuthorize("hasAnyRole('SALON_OWNER', 'MANAGER')")
    public ResponseEntity<?> updateStaffSchedule(@PathVariable Long id, @RequestBody List<ScheduleRequest> requests) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        User user = userOpt.get();
        String currentTenant = TenantContext.getCurrentTenant();
        if (currentTenant != null && !currentTenant.equals(user.getTenantId())) {
            return ResponseEntity.notFound().build();
        }

        for (ScheduleRequest req : requests) {
            if (req.getDayOfWeek() < 1 || req.getDayOfWeek() > 7) {
                return ResponseEntity.badRequest().body("Day of week must be between 1 and 7");
            }
            if (req.isWorking() && req.getStartTime().isAfter(req.getEndTime())) {
                return ResponseEntity.badRequest().body("Start time must be before end time");
            }
            if (req.isWorking() && req.getBreakStartTime() != null && req.getBreakEndTime() != null) {
                if (req.getBreakStartTime().isAfter(req.getBreakEndTime())) {
                    return ResponseEntity.badRequest().body("Break start time must be before break end time");
                }
                if (req.getBreakStartTime().isBefore(req.getStartTime()) || req.getBreakEndTime().isAfter(req.getEndTime())) {
                    return ResponseEntity.badRequest().body("Break time must be within working hours");
                }
            }
        }

        List<StaffSchedule> savedSchedules = new ArrayList<>();
        for (ScheduleRequest req : requests) {
            Optional<StaffSchedule> existing = staffScheduleRepository.findByStaffIdAndDayOfWeek(id, req.getDayOfWeek());
            StaffSchedule schedule = existing.orElseGet(StaffSchedule::new);
            
            schedule.setStaff(user);
            schedule.setDayOfWeek(req.getDayOfWeek());
            schedule.setWorking(req.isWorking());
            schedule.setStartTime(req.getStartTime());
            schedule.setEndTime(req.getEndTime());
            schedule.setBreakStartTime(req.getBreakStartTime());
            schedule.setBreakEndTime(req.getBreakEndTime());
            schedule.setTenantId(currentTenant);
            
            savedSchedules.add(staffScheduleRepository.save(schedule));
        }

        savedSchedules.sort(Comparator.comparingInt(StaffSchedule::getDayOfWeek));
        return ResponseEntity.ok(savedSchedules);
    }

    @GetMapping("/{id}/services")
    public ResponseEntity<?> getServices(@PathVariable Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        User user = userOpt.get();
        String currentTenant = TenantContext.getCurrentTenant();
        if (currentTenant != null && !currentTenant.equals(user.getTenantId())) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(user.getServices());
    }

    @PutMapping("/{id}/services")
    @PreAuthorize("hasAnyRole('SALON_OWNER', 'MANAGER')")
    public ResponseEntity<?> updateServices(@PathVariable Long id, @RequestBody List<Long> serviceIds) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        User user = userOpt.get();
        String currentTenant = TenantContext.getCurrentTenant();
        if (currentTenant != null && !currentTenant.equals(user.getTenantId())) {
            return ResponseEntity.notFound().build();
        }

        Set<ServiceEntity> targetServices = new HashSet<>();
        for (Long sid : serviceIds) {
            Optional<ServiceEntity> sOpt = serviceRepository.findById(sid);
            if (sOpt.isEmpty() || (currentTenant != null && !currentTenant.equals(sOpt.get().getTenantId()))) {
                return ResponseEntity.badRequest().body("Invalid or cross-tenant service reference");
            }
            targetServices.add(sOpt.get());
        }

        user.setServices(targetServices);
        User saved = userRepository.save(user);
        return ResponseEntity.ok(saved.getServices());
    }

    @GetMapping("/{id}/timeoff")
    public ResponseEntity<?> getTimeOff(@PathVariable Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        User user = userOpt.get();
        String currentTenant = TenantContext.getCurrentTenant();
        if (currentTenant != null && !currentTenant.equals(user.getTenantId())) {
            return ResponseEntity.notFound().build();
        }
        
        List<StaffTimeOff> timeOffs = staffTimeOffRepository.findByStaffId(id);
        timeOffs.sort(Comparator.comparing(StaffTimeOff::getStartTime));
        return ResponseEntity.ok(timeOffs);
    }

    @PostMapping("/{id}/timeoff")
    @PreAuthorize("hasAnyRole('SALON_OWNER', 'MANAGER')")
    public ResponseEntity<?> addTimeOff(@PathVariable Long id, @RequestBody TimeOffRequest request) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        User user = userOpt.get();
        String currentTenant = TenantContext.getCurrentTenant();
        if (currentTenant != null && !currentTenant.equals(user.getTenantId())) {
            return ResponseEntity.notFound().build();
        }

        if (request.getStartTime() == null || request.getEndTime() == null || request.getStartTime().isAfter(request.getEndTime())) {
            return ResponseEntity.badRequest().body("Valid start and end time are required");
        }

        StaffTimeOff timeOff = new StaffTimeOff();
        timeOff.setStaff(user);
        timeOff.setStartTime(request.getStartTime());
        timeOff.setEndTime(request.getEndTime());
        timeOff.setReason(request.getReason());
        timeOff.setTenantId(currentTenant);

        StaffTimeOff saved = staffTimeOffRepository.save(timeOff);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @DeleteMapping("/timeoff/{timeOffId}")
    @PreAuthorize("hasAnyRole('SALON_OWNER', 'MANAGER')")
    public ResponseEntity<?> deleteTimeOff(@PathVariable Long timeOffId) {
        Optional<StaffTimeOff> toOpt = staffTimeOffRepository.findById(timeOffId);
        if (toOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        StaffTimeOff to = toOpt.get();
        String currentTenant = TenantContext.getCurrentTenant();
        if (currentTenant != null && !currentTenant.equals(to.getTenantId())) {
            return ResponseEntity.notFound().build();
        }
        staffTimeOffRepository.delete(to);
        return ResponseEntity.ok("Time off deleted successfully");
    }

    @GetMapping("/{id}/availability")
    public ResponseEntity<?> getAvailability(
            @PathVariable Long id,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate date) {
        
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        User user = userOpt.get();
        String currentTenant = TenantContext.getCurrentTenant();
        if (currentTenant != null && !currentTenant.equals(user.getTenantId())) {
            return ResponseEntity.notFound().build();
        }

        if (!user.isActive()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        // Get schedule for the day
        int dayOfWeek = date.getDayOfWeek().getValue();
        Optional<StaffSchedule> schedOpt = staffScheduleRepository.findByStaffIdAndDayOfWeek(id, dayOfWeek);
        if (schedOpt.isEmpty() || !schedOpt.get().isWorking()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        StaffSchedule schedule = schedOpt.get();
        LocalTime workingStart = schedule.getStartTime();
        LocalTime workingEnd = schedule.getEndTime();

        // Get existing appointments
        List<com.akarsha.appointment.Appointment> activeAppointments = appointmentRepository.findActiveAppointmentsByStaffAndDate(id, date);
        
        // Get time off for the day
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.atTime(LocalTime.MAX);
        List<StaffTimeOff> timeOffs = staffTimeOffRepository.findOverlappingTimeOff(id, dayStart, dayEnd);

        List<String> availableSlots = new ArrayList<>();
        LocalTime current = workingStart;

        while (current.plusMinutes(30).isBefore(workingEnd) || current.plusMinutes(30).equals(workingEnd)) {
            LocalTime slotStart = current;
            LocalTime slotEnd = current.plusMinutes(30);
            boolean conflict = false;

            // Check breaks
            if (schedule.getBreakStartTime() != null && schedule.getBreakEndTime() != null) {
                if (slotStart.isBefore(schedule.getBreakEndTime()) && slotEnd.isAfter(schedule.getBreakStartTime())) {
                    conflict = true;
                }
            }

            // Check appointments
            if (!conflict) {
                for (com.akarsha.appointment.Appointment a : activeAppointments) {
                    if (slotStart.isBefore(a.getEndTime()) && slotEnd.isAfter(a.getStartTime())) {
                        conflict = true;
                        break;
                    }
                }
            }

            // Check time-offs
            if (!conflict) {
                LocalDateTime slotStartLdt = date.atTime(slotStart);
                LocalDateTime slotEndLdt = date.atTime(slotEnd);
                for (StaffTimeOff to : timeOffs) {
                    if (slotStartLdt.isBefore(to.getEndTime()) && slotEndLdt.isAfter(to.getStartTime())) {
                        conflict = true;
                        break;
                    }
                }
            }

            if (!conflict) {
                availableSlots.add(slotStart.toString());
            }
            current = current.plusMinutes(30);
        }

        return ResponseEntity.ok(availableSlots);
    }
}
