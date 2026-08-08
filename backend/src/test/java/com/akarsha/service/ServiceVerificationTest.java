package com.akarsha.service;

import com.akarsha.appointment.Appointment;
import com.akarsha.appointment.AppointmentRepository;
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

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class ServiceVerificationTest {

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
    private AppointmentRepository appointmentRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private Salon salonAlpha;
    private Salon salonBeta;
    private User staffAlpha;
    private ServiceEntity serviceAlpha;
    private ServiceEntity serviceBeta;
    private Customer customerAlpha;

    @BeforeEach
    public void setUp() {
        // Salon Alpha
        salonAlpha = new Salon();
        salonAlpha.setName("Salon Alpha");
        salonAlpha.setSubdomain("alpha-" + UUID.randomUUID().toString().substring(0, 5));
        salonAlpha.setPhone("0112222222");
        salonAlpha.setAddress("Colombo 3");
        salonAlpha.setCity("Colombo");
        salonAlpha.setBusinessType("Salon");
        salonAlpha = salonRepository.save(salonAlpha);

        // Salon Beta
        salonBeta = new Salon();
        salonBeta.setName("Salon Beta");
        salonBeta.setSubdomain("beta-" + UUID.randomUUID().toString().substring(0, 5));
        salonBeta.setPhone("0113333333");
        salonBeta.setAddress("Kandy");
        salonBeta.setCity("Kandy");
        salonBeta.setBusinessType("Salon");
        salonBeta = salonRepository.save(salonBeta);

        // Staff member for Alpha (User entities do not require TenantContext per existing test patterns)
        staffAlpha = new User();
        staffAlpha.setFullName("Staff Alpha");
        staffAlpha.setUsername("staff_alpha_" + UUID.randomUUID().toString().substring(0, 5));
        staffAlpha.setEmail("staff_alpha_" + UUID.randomUUID().toString().substring(0, 5) + "@alpha.com");
        staffAlpha.setPasswordHash("hash");
        staffAlpha.setRole("STAFF");
        staffAlpha.setActive(true);
        staffAlpha.setTenantId(salonAlpha.getSubdomain());
        staffAlpha = userRepository.save(staffAlpha);

        // Set TenantContext BEFORE saving any tenant-aware entities (Customer, Service)
        TenantContext.setCurrentTenant(salonAlpha.getSubdomain());

        // Customer for Alpha
        customerAlpha = new Customer();
        customerAlpha.setFullName("Customer Alpha");
        customerAlpha.setPhone("0771111111");
        customerAlpha.setTenantId(salonAlpha.getSubdomain());
        customerAlpha = customerRepository.save(customerAlpha);

        // Service for Alpha (active by default)
        serviceAlpha = new ServiceEntity();
        serviceAlpha.setName("Haircut Alpha");
        serviceAlpha.setPrice(BigDecimal.valueOf(1500));
        serviceAlpha.setDurationMinutes(30);
        serviceAlpha.setActive(true);
        serviceAlpha.setTenantId(salonAlpha.getSubdomain());
        serviceAlpha = serviceRepository.save(serviceAlpha);

        // Service for Beta
        TenantContext.setCurrentTenant(salonBeta.getSubdomain());
        serviceBeta = new ServiceEntity();
        serviceBeta.setName("Colour Beta");
        serviceBeta.setPrice(BigDecimal.valueOf(5000));
        serviceBeta.setDurationMinutes(60);
        serviceBeta.setActive(true);
        serviceBeta.setTenantId(salonBeta.getSubdomain());
        serviceBeta = serviceRepository.save(serviceBeta);

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

    // ─── 1. CREATE ───────────────────────────────────────────────────────────────

    @Test
    public void testCreateService() throws Exception {
        String auth = getAuthHeader("owner@alpha.com", salonAlpha.getSubdomain());
        String payload = """
                {
                    "name": "Deep Conditioning",
                    "price": 2500.00,
                    "durationMinutes": 45,
                    "active": true
                }
                """;

        mockMvc.perform(post("/api/v1/services")
                .header("Authorization", auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Deep Conditioning"))
                .andExpect(jsonPath("$.price").value(2500.00))
                .andExpect(jsonPath("$.durationMinutes").value(45))
                .andExpect(jsonPath("$.active").value(true));
    }

    // ─── 2. UPDATE ───────────────────────────────────────────────────────────────

    @Test
    public void testUpdateService() throws Exception {
        String auth = getAuthHeader("owner@alpha.com", salonAlpha.getSubdomain());
        String payload = """
                {
                    "name": "Haircut Alpha Updated",
                    "price": 1800.00,
                    "durationMinutes": 45,
                    "active": true
                }
                """;

        mockMvc.perform(put("/api/v1/services/" + serviceAlpha.getId())
                .header("Authorization", auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Haircut Alpha Updated"))
                .andExpect(jsonPath("$.price").value(1800.00))
                .andExpect(jsonPath("$.durationMinutes").value(45));
    }

    // ─── 3. PAGINATION AND SEARCH ─────────────────────────────────────────────

    @Test
    public void testPaginatedSearchReturnsOnlyTenantServices() throws Exception {
        String auth = getAuthHeader("owner@alpha.com", salonAlpha.getSubdomain());

        // Alpha can see its own services
        mockMvc.perform(get("/api/v1/services")
                .header("Authorization", auth)
                .param("query", "Haircut"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("Haircut Alpha"));
    }

    @Test
    public void testPaginationParameters() throws Exception {
        String auth = getAuthHeader("owner@alpha.com", salonAlpha.getSubdomain());

        // Add a second service to Alpha
        TenantContext.setCurrentTenant(salonAlpha.getSubdomain());
        ServiceEntity svc2 = new ServiceEntity();
        svc2.setName("Blow Dry");
        svc2.setPrice(BigDecimal.valueOf(800));
        svc2.setDurationMinutes(20);
        svc2.setActive(true);
        svc2.setTenantId(salonAlpha.getSubdomain());
        serviceRepository.save(svc2);
        TenantContext.clear();

        // Page 0, size 1 — should get exactly 1 result
        mockMvc.perform(get("/api/v1/services")
                .header("Authorization", auth)
                .param("page", "0")
                .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(2)));
    }

    // ─── 4. ACTIVE/INACTIVE STATUS CHANGES ───────────────────────────────────

    @Test
    public void testDeactivateAndReactivateService() throws Exception {
        String auth = getAuthHeader("owner@alpha.com", salonAlpha.getSubdomain());

        // Deactivate
        mockMvc.perform(patch("/api/v1/services/" + serviceAlpha.getId() + "/status")
                .header("Authorization", auth)
                .param("active", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        // Reactivate
        mockMvc.perform(patch("/api/v1/services/" + serviceAlpha.getId() + "/status")
                .header("Authorization", auth)
                .param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    public void testActiveFilterReturnsOnlyActiveServices() throws Exception {
        String auth = getAuthHeader("owner@alpha.com", salonAlpha.getSubdomain());

        // Deactivate serviceAlpha
        serviceAlpha.setActive(false);
        TenantContext.setCurrentTenant(salonAlpha.getSubdomain());
        serviceRepository.save(serviceAlpha);
        TenantContext.clear();

        // Filter active only — should be empty for Alpha
        mockMvc.perform(get("/api/v1/services")
                .header("Authorization", auth)
                .param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));

        // Filter inactive only — should have 1
        mockMvc.perform(get("/api/v1/services")
                .header("Authorization", auth)
                .param("active", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    // ─── 5. VALIDATION ────────────────────────────────────────────────────────

    @Test
    public void testValidationBlankName() throws Exception {
        String auth = getAuthHeader("owner@alpha.com", salonAlpha.getSubdomain());
        String payload = """
                {
                    "name": "",
                    "price": 500.00,
                    "durationMinutes": 30,
                    "active": true
                }
                """;

        mockMvc.perform(post("/api/v1/services")
                .header("Authorization", auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testValidationZeroDuration() throws Exception {
        String auth = getAuthHeader("owner@alpha.com", salonAlpha.getSubdomain());
        String payload = """
                {
                    "name": "Bad Service",
                    "price": 500.00,
                    "durationMinutes": 0,
                    "active": true
                }
                """;

        mockMvc.perform(post("/api/v1/services")
                .header("Authorization", auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testValidationNegativePrice() throws Exception {
        String auth = getAuthHeader("owner@alpha.com", salonAlpha.getSubdomain());
        String payload = """
                {
                    "name": "Bad Service",
                    "price": -100.00,
                    "durationMinutes": 30,
                    "active": true
                }
                """;

        mockMvc.perform(post("/api/v1/services")
                .header("Authorization", auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isBadRequest());
    }

    // ─── 6. TENANT ISOLATION ─────────────────────────────────────────────────

    @Test
    public void testTenantIsolationOnList() throws Exception {
        // Beta authenticated user should NOT see Alpha services
        String authBeta = getAuthHeader("owner@beta.com", salonBeta.getSubdomain());

        mockMvc.perform(get("/api/v1/services")
                .header("Authorization", authBeta)
                .param("query", "Haircut Alpha"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    public void testTenantIsolationOnGetById() throws Exception {
        // Beta cannot read Alpha's service by ID
        String authBeta = getAuthHeader("owner@beta.com", salonBeta.getSubdomain());

        mockMvc.perform(get("/api/v1/services/" + serviceAlpha.getId())
                .header("Authorization", authBeta))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testTenantIsolationOnUpdate() throws Exception {
        // Beta cannot update Alpha's service
        String authBeta = getAuthHeader("owner@beta.com", salonBeta.getSubdomain());
        String payload = """
                {
                    "name": "Hijacked",
                    "price": 0.00,
                    "durationMinutes": 15,
                    "active": false
                }
                """;

        mockMvc.perform(put("/api/v1/services/" + serviceAlpha.getId())
                .header("Authorization", authBeta)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testTenantIsolationOnStatusPatch() throws Exception {
        // Beta cannot patch status of Alpha's service
        String authBeta = getAuthHeader("owner@beta.com", salonBeta.getSubdomain());

        mockMvc.perform(patch("/api/v1/services/" + serviceAlpha.getId() + "/status")
                .header("Authorization", authBeta)
                .param("active", "false"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testTenantIsolationOnDelete() throws Exception {
        // Beta cannot delete Alpha's service
        String authBeta = getAuthHeader("owner@beta.com", salonBeta.getSubdomain());

        mockMvc.perform(delete("/api/v1/services/" + serviceAlpha.getId())
                .header("Authorization", authBeta))
                .andExpect(status().isNotFound());
    }

    // ─── 7. DELETE (NO APPOINTMENTS) ─────────────────────────────────────────

    @Test
    public void testDeleteServiceWithNoAppointments() throws Exception {
        String auth = getAuthHeader("owner@alpha.com", salonAlpha.getSubdomain());

        // Confirm service exists
        long idToDelete = serviceAlpha.getId();

        mockMvc.perform(delete("/api/v1/services/" + idToDelete)
                .header("Authorization", auth))
                .andExpect(status().isOk());

        // Confirm it is gone — use SYSTEM_BYPASS to skip tenant filter for verification
        TenantContext.setCurrentTenant("SYSTEM_BYPASS");
        assertFalse(serviceRepository.findById(idToDelete).isPresent());
        TenantContext.clear();
    }

    // ─── 8. SAFE ARCHIVE (HAS APPOINTMENTS) ─────────────────────────────────

    @Test
    public void testSafeArchiveWhenAppointmentHistoryExists() throws Exception {
        // Create a real appointment referencing serviceAlpha
        TenantContext.setCurrentTenant(salonAlpha.getSubdomain());
        Appointment appt = new Appointment();
        appt.setCustomer(customerAlpha);
        appt.setService(serviceAlpha);
        appt.setStaff(staffAlpha);
        appt.setAppointmentDate(LocalDate.now().minusDays(7));
        appt.setStartTime(LocalTime.of(10, 0));
        appt.setEndTime(LocalTime.of(10, 30));
        appt.setTenantId(salonAlpha.getSubdomain());
        appointmentRepository.save(appt);
        TenantContext.clear();

        String auth = getAuthHeader("owner@alpha.com", salonAlpha.getSubdomain());

        // DELETE should archive, not hard delete
        mockMvc.perform(delete("/api/v1/services/" + serviceAlpha.getId())
                .header("Authorization", auth))
                .andExpect(status().isOk());

        // Service must still exist in DB but inactive
        TenantContext.setCurrentTenant(salonAlpha.getSubdomain());
        ServiceEntity archived = serviceRepository.findById(serviceAlpha.getId()).orElseThrow();
        TenantContext.clear();
        assertFalse(archived.isActive(), "Service should be archived (inactive), not deleted");
    }

    // ─── 9. APPOINTMENT HISTORY PRESERVED AFTER ARCHIVAL ─────────────────────

    @Test
    public void testAppointmentHistoryPreservedAfterServiceArchival() throws Exception {
        // Create appointment
        TenantContext.setCurrentTenant(salonAlpha.getSubdomain());
        Appointment appt = new Appointment();
        appt.setCustomer(customerAlpha);
        appt.setService(serviceAlpha);
        appt.setStaff(staffAlpha);
        appt.setAppointmentDate(LocalDate.now().minusDays(3));
        appt.setStartTime(LocalTime.of(11, 0));
        appt.setEndTime(LocalTime.of(11, 30));
        appt.setTenantId(salonAlpha.getSubdomain());
        Appointment savedAppt = appointmentRepository.save(appt);
        TenantContext.clear();

        // Archive the service via DELETE (which degrades to archive)
        String auth = getAuthHeader("owner@alpha.com", salonAlpha.getSubdomain());
        mockMvc.perform(delete("/api/v1/services/" + serviceAlpha.getId())
                .header("Authorization", auth))
                .andExpect(status().isOk());

        // Appointment still references the service and is not modified
        TenantContext.setCurrentTenant(salonAlpha.getSubdomain());
        Appointment stillExists = appointmentRepository.findById(savedAppt.getId()).orElseThrow();
        TenantContext.clear();
        assertEquals(serviceAlpha.getId(), stillExists.getService().getId(),
                "Appointment must still reference the archived service");
    }

    // ─── 10. STAFF ↔ SERVICE ASSIGNMENT COMPATIBILITY ───────────────────────

    @Test
    public void testStaffServiceAssignmentAndCompatibility() throws Exception {
        String auth = getAuthHeader("owner@alpha.com", salonAlpha.getSubdomain());

        // Assign serviceAlpha to staffAlpha via the staff/services endpoint
        String assignPayload = "[" + serviceAlpha.getId() + "]";

        mockMvc.perform(put("/api/v1/staff/" + staffAlpha.getId() + "/services")
                .header("Authorization", auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(assignPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(serviceAlpha.getId().intValue()));
    }

    @Test
    public void testCrossTenantServiceAssignmentRejected() throws Exception {
        // Assign Beta's service to Alpha's staff — should be rejected
        String authAlpha = getAuthHeader("owner@alpha.com", salonAlpha.getSubdomain());
        String assignPayload = "[" + serviceBeta.getId() + "]";

        mockMvc.perform(put("/api/v1/staff/" + staffAlpha.getId() + "/services")
                .header("Authorization", authAlpha)
                .contentType(MediaType.APPLICATION_JSON)
                .content(assignPayload))
                .andExpect(status().isBadRequest());
    }

    // ─── 11. ACTIVE SERVICE CAN BE BOOKED ────────────────────────────────────

    @Test
    public void testAppointmentSucceedsWithActiveService() throws Exception {
        String auth = getAuthHeader("owner@alpha.com", salonAlpha.getSubdomain());
        String payload = String.format("""
                {
                    "customerId": %d,
                    "serviceId": %d,
                    "staffId": %d,
                    "appointmentDate": "%s",
                    "startTime": "10:00:00",
                    "endTime": "10:30:00"
                }
                """, customerAlpha.getId(), serviceAlpha.getId(), staffAlpha.getId(), LocalDate.now().toString());

        // serviceAlpha is active — booking should succeed
        mockMvc.perform(post("/api/v1/appointments")
                .header("Authorization", auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.service.id").value(serviceAlpha.getId().intValue()));
    }

    // ─── 12. INACTIVE SERVICE CANNOT BE NEWLY BOOKED ─────────────────────────

    @Test
    public void testAppointmentFailsWithInactiveService() throws Exception {
        // Deactivate serviceAlpha first
        serviceAlpha.setActive(false);
        TenantContext.setCurrentTenant(salonAlpha.getSubdomain());
        serviceRepository.save(serviceAlpha);
        TenantContext.clear();

        String auth = getAuthHeader("owner@alpha.com", salonAlpha.getSubdomain());
        String payload = String.format("""
                {
                    "customerId": %d,
                    "serviceId": %d,
                    "staffId": %d,
                    "appointmentDate": "%s",
                    "startTime": "10:00:00",
                    "endTime": "10:30:00"
                }
                """, customerAlpha.getId(), serviceAlpha.getId(), staffAlpha.getId(), LocalDate.now().toString());

        // serviceAlpha is inactive — booking must fail
        mockMvc.perform(post("/api/v1/appointments")
                .header("Authorization", auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isBadRequest());
    }

    // ─── 13. UNAUTHENTICATED ACCESS ──────────────────────────────────────────

    @Test
    public void testUnauthenticatedAccessRejection() throws Exception {
        mockMvc.perform(get("/api/v1/services"))
                .andExpect(status().isForbidden());
    }
}
