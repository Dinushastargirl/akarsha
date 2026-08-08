package com.akarsha.staff;

import com.akarsha.appointment.Appointment;
import com.akarsha.appointment.AppointmentRepository;
import com.akarsha.appointment.AppointmentRequest;
import com.akarsha.appointment.AppointmentStatus;
import com.akarsha.customer.Customer;
import com.akarsha.core.entity.ServiceEntity;
import com.akarsha.core.entity.User;
import com.akarsha.customer.CustomerRepository;
import com.akarsha.core.repository.ServiceRepository;
import com.akarsha.core.repository.UserRepository;
import com.akarsha.security.JwtService;
import com.akarsha.tenant.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class StaffSchedulingVerificationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private StaffScheduleRepository staffScheduleRepository;

    @Autowired
    private StaffTimeOffRepository staffTimeOffRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private JwtService jwtService;

    private final String TENANT_ID = "tenant-sched-123";
    private User owner;
    private User staff;
    private Customer customer;
    private ServiceEntity service;
    private String ownerToken;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(TENANT_ID);

        owner = new User();
        owner.setFullName("Salon Owner");
        owner.setEmail("owner.sched@akarsha.com");
        owner.setUsername("owner.sched");
        owner.setPasswordHash("hashed_password");
        owner.setRole("SALON_OWNER");
        owner.setTenantId(TENANT_ID);
        userRepository.save(owner);

        staff = new User();
        staff.setFullName("Test Staff");
        staff.setEmail("staff.sched@akarsha.com");
        staff.setUsername("staff.sched");
        staff.setPasswordHash("hashed_password");
        staff.setRole("STAFF");
        staff.setTenantId(TENANT_ID);
        userRepository.save(staff);

        customer = new Customer();
        customer.setFullName("John Doe");
        customer.setEmail("john.doe@example.com");
        customer.setPhone("1234567890");
        customer.setTenantId(TENANT_ID);
        customerRepository.save(customer);

        service = new ServiceEntity();
        service.setName("Haircut");
        service.setPrice(new BigDecimal("25.00"));
        service.setDurationMinutes(60);
        service.setTenantId(TENANT_ID);
        serviceRepository.save(service);

        // Assign service to staff
        staff.getServices().add(service);
        userRepository.save(staff);

        // Schedule: Working 09:00 - 18:00, Break 12:00 - 13:00 on Monday
        StaffSchedule schedule = new StaffSchedule();
        schedule.setStaff(staff);
        schedule.setDayOfWeek(1); // Monday
        schedule.setWorking(true);
        schedule.setStartTime(LocalTime.of(9, 0));
        schedule.setEndTime(LocalTime.of(18, 0));
        schedule.setBreakStartTime(LocalTime.of(12, 0));
        schedule.setBreakEndTime(LocalTime.of(13, 0));
        schedule.setTenantId(TENANT_ID);
        staffScheduleRepository.save(schedule);

        ownerToken = "Bearer " + jwtService.generateToken(owner.getEmail(), owner.getTenantId(), owner.getRole());
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void testBookingDuringBreak_ReturnsConflict() throws Exception {
        LocalDate monday = LocalDate.of(2026, 8, 10); // Aug 10, 2026 is a Monday
        
        AppointmentRequest req = new AppointmentRequest();
        req.setCustomerId(customer.getId());
        req.setStaffId(staff.getId());
        req.setServiceId(service.getId());
        req.setAppointmentDate(monday);
        req.setStartTime(LocalTime.of(12, 30)); // Inside break
        req.setEndTime(LocalTime.of(13, 30)); 

        mockMvc.perform(post("/appointments")
                .header("Authorization", ownerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    void testBookingDuringTimeOff_ReturnsConflict() throws Exception {
        LocalDate monday = LocalDate.of(2026, 8, 10);
        
        StaffTimeOff timeOff = new StaffTimeOff();
        timeOff.setStaff(staff);
        timeOff.setStartTime(LocalDateTime.of(monday, LocalTime.of(14, 0)));
        timeOff.setEndTime(LocalDateTime.of(monday, LocalTime.of(16, 0)));
        timeOff.setReason("Doctor appointment");
        timeOff.setTenantId(TENANT_ID);
        staffTimeOffRepository.save(timeOff);

        AppointmentRequest req = new AppointmentRequest();
        req.setCustomerId(customer.getId());
        req.setStaffId(staff.getId());
        req.setServiceId(service.getId());
        req.setAppointmentDate(monday);
        req.setStartTime(LocalTime.of(14, 30)); // Inside time off
        req.setEndTime(LocalTime.of(15, 30)); 

        mockMvc.perform(post("/appointments")
                .header("Authorization", ownerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    void testBookingOutsideWorkingHours_ReturnsBadRequest() throws Exception {
        LocalDate monday = LocalDate.of(2026, 8, 10);
        
        AppointmentRequest req = new AppointmentRequest();
        req.setCustomerId(customer.getId());
        req.setStaffId(staff.getId());
        req.setServiceId(service.getId());
        req.setAppointmentDate(monday);
        req.setStartTime(LocalTime.of(8, 0)); // Before start time (09:00)
        req.setEndTime(LocalTime.of(9, 0)); 

        mockMvc.perform(post("/appointments")
                .header("Authorization", ownerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testBookingWithWrongDuration_ReturnsBadRequest() throws Exception {
        LocalDate monday = LocalDate.of(2026, 8, 10);
        
        AppointmentRequest req = new AppointmentRequest();
        req.setCustomerId(customer.getId());
        req.setStaffId(staff.getId());
        req.setServiceId(service.getId());
        req.setAppointmentDate(monday);
        req.setStartTime(LocalTime.of(10, 0)); // Valid time
        req.setEndTime(LocalTime.of(15, 0)); // Wrong duration (duration is 60 min, so end should be 11:00)

        mockMvc.perform(post("/appointments")
                .header("Authorization", ownerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}
