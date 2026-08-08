package com.akarsha.appointment;

import com.akarsha.core.entity.Salon;
import com.akarsha.core.entity.ServiceEntity;
import com.akarsha.core.entity.User;
import com.akarsha.core.repository.SalonRepository;
import com.akarsha.core.repository.ServiceRepository;
import com.akarsha.core.repository.UserRepository;
import com.akarsha.customer.Customer;
import com.akarsha.customer.CustomerRepository;
import com.akarsha.security.JwtService;
import com.akarsha.tenant.TenantContext;
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
import java.time.LocalTime;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class AppointmentVerificationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private SalonRepository salonRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    private Salon salonAlpha;
    private Salon salonBeta;
    private User staffAlpha;
    private User staffBeta;
    private ServiceEntity serviceAlpha;
    private ServiceEntity serviceBeta;
    private Customer customerAlpha;
    private Customer customerBeta;

    @BeforeEach
    public void setUp() {
        // 1. Create salons
        salonAlpha = new Salon();
        salonAlpha.setName("Salon Alpha");
        salonAlpha.setSubdomain("alpha-" + UUID.randomUUID().toString().substring(0, 5));
        salonAlpha.setPhone("0112222222");
        salonAlpha.setAddress("Colombo 3");
        salonAlpha.setCity("Colombo");
        salonAlpha.setBusinessType("Salon");
        salonAlpha = salonRepository.save(salonAlpha);

        salonBeta = new Salon();
        salonBeta.setName("Salon Beta");
        salonBeta.setSubdomain("beta-" + UUID.randomUUID().toString().substring(0, 5));
        salonBeta.setPhone("0113333333");
        salonBeta.setAddress("Kandy");
        salonBeta.setCity("Kandy");
        salonBeta.setBusinessType("Salon");
        salonBeta = salonRepository.save(salonBeta);

        // 2. Create users (staff)
        staffAlpha = new User();
        staffAlpha.setFullName("Staff Alpha");
        staffAlpha.setUsername("staff_alpha_" + UUID.randomUUID().toString().substring(0, 5));
        staffAlpha.setEmail("staff_alpha_" + UUID.randomUUID().toString().substring(0, 5) + "@alpha.com");
        staffAlpha.setPasswordHash("hash");
        staffAlpha.setRole("STAFF");
        staffAlpha.setTenantId(salonAlpha.getSubdomain());
        staffAlpha = userRepository.save(staffAlpha);

        staffBeta = new User();
        staffBeta.setFullName("Staff Beta");
        staffBeta.setUsername("staff_beta_" + UUID.randomUUID().toString().substring(0, 5));
        staffBeta.setEmail("staff_beta_" + UUID.randomUUID().toString().substring(0, 5) + "@beta.com");
        staffBeta.setPasswordHash("hash");
        staffBeta.setRole("STAFF");
        staffBeta.setTenantId(salonBeta.getSubdomain());
        staffBeta = userRepository.save(staffBeta);

        // 3. Create services
        TenantContext.setCurrentTenant(salonAlpha.getSubdomain());
        serviceAlpha = new ServiceEntity();
        serviceAlpha.setName("Haircut");
        serviceAlpha.setPrice(BigDecimal.valueOf(1500));
        serviceAlpha.setDurationMinutes(30);
        serviceAlpha.setTenantId(salonAlpha.getSubdomain());
        serviceAlpha = serviceRepository.save(serviceAlpha);

        // 4. Create customers
        customerAlpha = new Customer();
        customerAlpha.setFullName("Customer Alpha");
        customerAlpha.setPhone("0771111111");
        customerAlpha.setTenantId(salonAlpha.getSubdomain());
        customerAlpha = customerRepository.save(customerAlpha);

        TenantContext.setCurrentTenant(salonBeta.getSubdomain());
        serviceBeta = new ServiceEntity();
        serviceBeta.setName("Hair Colour");
        serviceBeta.setPrice(BigDecimal.valueOf(5000));
        serviceBeta.setDurationMinutes(60);
        serviceBeta.setTenantId(salonBeta.getSubdomain());
        serviceBeta = serviceRepository.save(serviceBeta);

        customerBeta = new Customer();
        customerBeta.setFullName("Customer Beta");
        customerBeta.setPhone("0772222222");
        customerBeta.setTenantId(salonBeta.getSubdomain());
        customerBeta = customerRepository.save(customerBeta);
        
        TenantContext.clear();
    }

    @AfterEach
    public void tearDown() {
        TenantContext.clear();
    }

    private String getAuthHeader(String email, String tenantId) {
        String token = jwtService.generateToken(email, tenantId, "SALON_OWNER");
        return "Bearer " + token;
    }

    @Test
    public void testCreateAndGetAppointment() throws Exception {
        String authHeader = getAuthHeader("owner@alpha.com", salonAlpha.getSubdomain());
        String payload = String.format("""
                {
                    "customerId": %d,
                    "serviceId": %d,
                    "staffId": %d,
                    "appointmentDate": "%s",
                    "startTime": "10:00:00",
                    "endTime": "10:30:00",
                    "notes": "Prefers warm tea"
                }
                """, customerAlpha.getId(), serviceAlpha.getId(), staffAlpha.getId(), LocalDate.now().toString());

        // Create
        mockMvc.perform(post("/appointments")
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("BOOKED"))
                .andExpect(jsonPath("$.notes").value("Prefers warm tea"));

        // List
        mockMvc.perform(get("/appointments")
                .header("Authorization", authHeader)
                .param("date", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].customer.fullName").value("Customer Alpha"));
    }

    @Test
    public void testDoubleBookingOverlapRejection() throws Exception {
        String authHeader = getAuthHeader("owner@alpha.com", salonAlpha.getSubdomain());
        String dateStr = LocalDate.now().toString();

        // 1. Create first appointment (10:00 - 10:45)
        String payload1 = String.format("""
                {
                    "customerId": %d,
                    "serviceId": %d,
                    "staffId": %d,
                    "appointmentDate": "%s",
                    "startTime": "10:00:00",
                    "endTime": "10:30:00"
                }
                """, customerAlpha.getId(), serviceAlpha.getId(), staffAlpha.getId(), dateStr);

        mockMvc.perform(post("/appointments")
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload1))
                .andExpect(status().isCreated());

        // 2. Try booking overlapping appointment (10:15 - 10:45) for same staff member -> Should reject with 409
        String payload2 = String.format("""
                {
                    "customerId": %d,
                    "serviceId": %d,
                    "staffId": %d,
                    "appointmentDate": "%s",
                    "startTime": "10:15:00",
                    "endTime": "10:45:00"
                }
                """, customerAlpha.getId(), serviceAlpha.getId(), staffAlpha.getId(), dateStr);

        mockMvc.perform(post("/appointments")
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload2))
                .andExpect(status().isConflict());
    }

    @Test
    public void testCrossTenantReferenceAttack() throws Exception {
        // Authenticated as Salon Alpha
        String authHeader = getAuthHeader("owner@alpha.com", salonAlpha.getSubdomain());
        String dateStr = LocalDate.now().toString();

        // Referencing Customer Beta belonging to Salon Beta -> Should fail with 400 Bad Request
        String payload = String.format("""
                {
                    "customerId": %d,
                    "serviceId": %d,
                    "staffId": %d,
                    "appointmentDate": "%s",
                    "startTime": "11:00:00",
                    "endTime": "11:30:00"
                }
                """, customerBeta.getId(), serviceAlpha.getId(), staffAlpha.getId(), dateStr);

        mockMvc.perform(post("/appointments")
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCrossTenantAccessRejection() throws Exception {
        // 1. Authenticated as Salon Alpha: Book a valid appointment
        String authHeaderAlpha = getAuthHeader("owner@alpha.com", salonAlpha.getSubdomain());
        String dateStr = LocalDate.now().toString();

        TenantContext.setCurrentTenant(salonAlpha.getSubdomain());
        Appointment appointment = new Appointment();
        appointment.setCustomer(customerAlpha);
        appointment.setService(serviceAlpha);
        appointment.setStaff(staffAlpha);
        appointment.setAppointmentDate(LocalDate.now());
        appointment.setStartTime(LocalTime.of(12, 0));
        appointment.setEndTime(LocalTime.of(12, 30));
        appointment.setTenantId(salonAlpha.getSubdomain());
        appointment = appointmentRepository.save(appointment);
        TenantContext.clear();

        // 2. Authenticated as Salon Beta: Attempt to load appointment details of Salon Alpha -> Should reject with 404
        String authHeaderBeta = getAuthHeader("owner@beta.com", salonBeta.getSubdomain());
        mockMvc.perform(get("/appointments/" + appointment.getId())
                .header("Authorization", authHeaderBeta))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUnauthenticatedAccessRejection() throws Exception {
        mockMvc.perform(get("/appointments"))
                .andExpect(status().isForbidden());
    }
}
