package com.akarsha.customer;

import com.akarsha.appointment.Appointment;
import com.akarsha.appointment.AppointmentRepository;
import com.akarsha.appointment.AppointmentRequest;
import com.akarsha.appointment.AppointmentStatus;
import com.akarsha.core.entity.Salon;
import com.akarsha.core.entity.ServiceEntity;
import com.akarsha.core.entity.User;
import com.akarsha.core.repository.SalonRepository;
import com.akarsha.core.repository.ServiceRepository;
import com.akarsha.core.repository.UserRepository;
import com.akarsha.staff.StaffSchedule;
import com.akarsha.staff.StaffScheduleRepository;
import com.akarsha.staff.StaffTimeOffRepository;
import com.akarsha.security.JwtService;
import com.akarsha.tenant.TenantContext;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/public/booking/{salonSlug}")
public class PublicBookingController {

    private final SalonRepository salonRepository;
    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final AppointmentRepository appointmentRepository;
    private final StaffScheduleRepository staffScheduleRepository;
    private final StaffTimeOffRepository staffTimeOffRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public PublicBookingController(
            SalonRepository salonRepository,
            ServiceRepository serviceRepository,
            UserRepository userRepository,
            CustomerRepository customerRepository,
            AppointmentRepository appointmentRepository,
            StaffScheduleRepository staffScheduleRepository,
            StaffTimeOffRepository staffTimeOffRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.salonRepository = salonRepository;
        this.serviceRepository = serviceRepository;
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.appointmentRepository = appointmentRepository;
        this.staffScheduleRepository = staffScheduleRepository;
        this.staffTimeOffRepository = staffTimeOffRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @GetMapping("/info")
    public ResponseEntity<?> getSalonInfo(@PathVariable String salonSlug) {
        // We must temporarily clear TenantContext to lookup by slug if the filter failed, 
        // but the filter already set it if found.
        String currentTenant = TenantContext.getCurrentTenant();
        if (currentTenant == null) {
            return ResponseEntity.notFound().build();
        }
        
        TenantContext.setCurrentTenant("SYSTEM_BYPASS");
        Optional<Salon> salonOpt = salonRepository.findBySubdomain(salonSlug);
        TenantContext.setCurrentTenant(currentTenant);
        
        if (salonOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(salonOpt.get());
    }

    @GetMapping("/services")
    public ResponseEntity<List<ServiceEntity>> getServices(@PathVariable String salonSlug) {
        return ResponseEntity.ok(serviceRepository.findAll());
    }

    @GetMapping("/staff")
    public ResponseEntity<List<User>> getStaffMembers(@PathVariable String salonSlug) {
        List<User> allUsers = userRepository.findAll();
        List<User> staffMembers = allUsers.stream()
                .filter(u -> !"PLATFORM_ADMIN".equalsIgnoreCase(u.getRole()) && u.isActive())
                .map(u -> {
                    // Remove sensitive info
                    User safeUser = new User();
                    safeUser.setId(u.getId());
                    safeUser.setFullName(u.getFullName());
                    safeUser.setRole(u.getRole());
                    return safeUser;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(staffMembers);
    }

    @GetMapping("/availability")
    public ResponseEntity<List<String>> getAvailableSlots(
            @PathVariable String salonSlug,
            @RequestParam Long staffId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam int durationMinutes) {
        
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
            
            if (!hasTimeConflict(staffId, date, slotStart, slotEnd, null)) {
                availableSlots.add(slotStart.toString());
            }
            current = current.plusMinutes(30);
        }

        return ResponseEntity.ok(availableSlots);
    }
    
    private boolean hasTimeConflict(Long staffId, LocalDate date, LocalTime start, LocalTime end, Long ignoreId) {
        List<Appointment> active = appointmentRepository.findActiveAppointmentsByStaffAndDate(staffId, date);
        for (Appointment a : active) {
            if (ignoreId != null && ignoreId.equals(a.getId())) continue;
            if (start.isBefore(a.getEndTime()) && end.isAfter(a.getStartTime())) return true;
        }

        int dayOfWeek = date.getDayOfWeek().getValue();
        Optional<StaffSchedule> schedOpt = staffScheduleRepository.findByStaffIdAndDayOfWeek(staffId, dayOfWeek);
        if (schedOpt.isPresent()) {
            StaffSchedule schedule = schedOpt.get();
            if (schedule.getBreakStartTime() != null && schedule.getBreakEndTime() != null) {
                if (start.isBefore(schedule.getBreakEndTime()) && end.isAfter(schedule.getBreakStartTime())) return true;
            }
        }

        java.time.LocalDateTime dayStart = date.atStartOfDay();
        java.time.LocalDateTime dayEnd = date.atTime(LocalTime.MAX);
        List<com.akarsha.staff.StaffTimeOff> timeOffs = staffTimeOffRepository.findOverlappingTimeOff(staffId, dayStart, dayEnd);
        
        java.time.LocalDateTime slotStartLdt = date.atTime(start);
        java.time.LocalDateTime slotEndLdt = date.atTime(end);
        for (com.akarsha.staff.StaffTimeOff to : timeOffs) {
            if (slotStartLdt.isBefore(to.getEndTime()) && slotEndLdt.isAfter(to.getStartTime())) return true;
        }
        return false;
    }

    public static class PublicBookingRequest {
        public Long serviceId;
        public Long staffId;
        public LocalDate date;
        public LocalTime time;
        public String customerName;
        public String customerPhone;
        public String customerEmail;
        public String customerPassword; // Used to optionally create an account
        public String notes;
    }

    @PostMapping("/book")
    public ResponseEntity<?> bookAppointment(@PathVariable String salonSlug, @RequestBody PublicBookingRequest req) {
        Optional<ServiceEntity> serviceOpt = serviceRepository.findById(req.serviceId);
        if (serviceOpt.isEmpty()) return ResponseEntity.badRequest().body("Service not found");
        
        Optional<User> staffOpt = userRepository.findById(req.staffId);
        if (staffOpt.isEmpty() || !staffOpt.get().isActive()) return ResponseEntity.badRequest().body("Staff not found");

        LocalTime startTime = req.time;
        LocalTime endTime = startTime.plusMinutes(serviceOpt.get().getDurationMinutes());

        if (hasTimeConflict(req.staffId, req.date, startTime, endTime, null)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Time slot no longer available");
        }

        // Find or create customer
        List<Customer> existingCustomers = customerRepository.findByPhone(req.customerPhone);
        Customer customer;
        if (existingCustomers.isEmpty()) {
            customer = new Customer();
            customer.setFullName(req.customerName);
            customer.setPhone(req.customerPhone);
            customer.setEmail(req.customerEmail);
            if (req.customerPassword != null && !req.customerPassword.isEmpty()) {
                customer.setPasswordHash(passwordEncoder.encode(req.customerPassword));
            }
            customer = customerRepository.save(customer);
        } else {
            customer = existingCustomers.get(0);
        }

        Appointment appt = new Appointment();
        appt.setCustomer(customer);
        appt.setService(serviceOpt.get());
        appt.setStaff(staffOpt.get());
        appt.setAppointmentDate(req.date);
        appt.setStartTime(startTime);
        appt.setEndTime(endTime);
        appt.setStatus(AppointmentStatus.BOOKED);
        appt.setNotes(req.notes);

        Appointment saved = appointmentRepository.save(appt);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    public static class CustomerLoginRequest {
        public String phone;
        public String password;
    }

    public static class CustomerLoginResponse {
        public String token;
        public Customer customer;
        
        public CustomerLoginResponse(String token, Customer customer) {
            this.token = token;
            this.customer = customer;
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@PathVariable String salonSlug, @RequestBody CustomerLoginRequest req) {
        String currentTenant = TenantContext.getCurrentTenant();
        if (currentTenant == null) {
            return ResponseEntity.notFound().build();
        }

        List<Customer> customers = customerRepository.findByPhone(req.phone);
        if (customers.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
        
        Customer customer = customers.get(0);
        if (customer.getPasswordHash() == null || !passwordEncoder.matches(req.password, customer.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }

        String token = jwtService.generateToken(customer.getPhone(), currentTenant, "CUSTOMER");
        
        // Remove password hash from response
        Customer safeCustomer = new Customer();
        safeCustomer.setId(customer.getId());
        safeCustomer.setFullName(customer.getFullName());
        safeCustomer.setPhone(customer.getPhone());
        safeCustomer.setEmail(customer.getEmail());

        return ResponseEntity.ok(new CustomerLoginResponse(token, safeCustomer));
    }
}
