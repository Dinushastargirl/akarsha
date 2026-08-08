package com.akarsha.staff;

import com.akarsha.core.entity.Salon;
import com.akarsha.core.entity.ServiceEntity;
import com.akarsha.core.entity.User;
import com.akarsha.core.repository.SalonRepository;
import com.akarsha.core.repository.ServiceRepository;
import com.akarsha.core.repository.UserRepository;
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
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class StaffVerificationTest {

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
    private StaffScheduleRepository staffScheduleRepository;

    private Salon salonAlpha;
    private Salon salonBeta;
    private User staffAlpha;
    private User staffBeta;
    private ServiceEntity serviceAlpha;
    private ServiceEntity serviceBeta;

    @BeforeEach
    public void setUp() {
        // Create Salon Alpha
        salonAlpha = new Salon();
        salonAlpha.setName("Salon Alpha");
        salonAlpha.setSubdomain("alpha-" + UUID.randomUUID().toString().substring(0, 5));
        salonAlpha.setPhone("0112222222");
        salonAlpha.setAddress("Colombo 3");
        salonAlpha.setCity("Colombo");
        salonAlpha.setBusinessType("Salon");
        salonAlpha = salonRepository.save(salonAlpha);

        // Create Salon Beta
        salonBeta = new Salon();
        salonBeta.setName("Salon Beta");
        salonBeta.setSubdomain("beta-" + UUID.randomUUID().toString().substring(0, 5));
        salonBeta.setPhone("0113333333");
        salonBeta.setAddress("Kandy");
        salonBeta.setCity("Kandy");
        salonBeta.setBusinessType("Salon");
        salonBeta = salonRepository.save(salonBeta);

        // Create Staff Alpha
        staffAlpha = new User();
        staffAlpha.setFullName("Staff Alpha");
        staffAlpha.setUsername("staff_alpha_" + UUID.randomUUID().toString().substring(0, 5));
        staffAlpha.setEmail("staff_alpha_" + UUID.randomUUID().toString().substring(0, 5) + "@alpha.com");
        staffAlpha.setPasswordHash("hash");
        staffAlpha.setRole("STAFF");
        staffAlpha.setTenantId(salonAlpha.getSubdomain());
        staffAlpha = userRepository.save(staffAlpha);

        // Create Staff Beta
        staffBeta = new User();
        staffBeta.setFullName("Staff Beta");
        staffBeta.setUsername("staff_beta_" + UUID.randomUUID().toString().substring(0, 5));
        staffBeta.setEmail("staff_beta_" + UUID.randomUUID().toString().substring(0, 5) + "@beta.com");
        staffBeta.setPasswordHash("hash");
        staffBeta.setRole("STAFF");
        staffBeta.setTenantId(salonBeta.getSubdomain());
        staffBeta = userRepository.save(staffBeta);

        // Create Services (ensure TenantContext is set)
        TenantContext.setCurrentTenant(salonAlpha.getSubdomain());
        serviceAlpha = new ServiceEntity();
        serviceAlpha.setName("Haircut");
        serviceAlpha.setPrice(BigDecimal.valueOf(1500));
        serviceAlpha.setDurationMinutes(30);
        serviceAlpha.setTenantId(salonAlpha.getSubdomain());
        serviceAlpha = serviceRepository.save(serviceAlpha);

        TenantContext.setCurrentTenant(salonBeta.getSubdomain());
        serviceBeta = new ServiceEntity();
        serviceBeta.setName("Hair Colour");
        serviceBeta.setPrice(BigDecimal.valueOf(5000));
        serviceBeta.setDurationMinutes(60);
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

    @Test
    public void testCreateRetrieveAndUpdateStaff() throws Exception {
        String authHeader = getAuthHeader("owner@alpha.com", salonAlpha.getSubdomain());
        String email = "new_staff_" + UUID.randomUUID().toString().substring(0, 5) + "@alpha.com";
        String payload = String.format("""
                {
                    "fullName": "New Operator",
                    "email": "%s",
                    "phone": "0779999999",
                    "role": "STAFF",
                    "active": true
                }
                """, email);

        // Create Staff Member
        mockMvc.perform(post("/api/v1/staff")
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fullName").value("New Operator"))
                .andExpect(jsonPath("$.active").value(true));

        // Search Staff Members
        mockMvc.perform(get("/api/v1/staff")
                .header("Authorization", authHeader)
                .param("query", "New Operator"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].fullName").value("New Operator"));
    }

    @Test
    public void testDeactivateStaff() throws Exception {
        String authHeader = getAuthHeader("owner@alpha.com", salonAlpha.getSubdomain());

        mockMvc.perform(patch("/api/v1/staff/" + staffAlpha.getId() + "/status")
                .header("Authorization", authHeader)
                .param("active", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    public void testCrossTenantAccessRejection() throws Exception {
        // Authenticated as Salon Beta, attempt to read Salon Alpha Staff member -> Should return 404
        String authHeaderBeta = getAuthHeader("owner@beta.com", salonBeta.getSubdomain());
        mockMvc.perform(get("/api/v1/staff/" + staffAlpha.getId())
                .header("Authorization", authHeaderBeta))
                .andExpect(status().isNotFound());

        // Attempt to assign cross-tenant services -> Should return 400 Bad Request
        mockMvc.perform(put("/api/v1/staff/" + staffBeta.getId() + "/services")
                .header("Authorization", authHeaderBeta)
                .contentType(MediaType.APPLICATION_JSON)
                .content("[" + serviceAlpha.getId() + "]"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testScheduleUpdateAndRetention() throws Exception {
        String authHeader = getAuthHeader("owner@alpha.com", salonAlpha.getSubdomain());
        String payload = """
                [
                    {
                        "dayOfWeek": 1,
                        "working": true,
                        "startTime": "08:00:00",
                        "endTime": "17:00:00"
                    },
                    {
                        "dayOfWeek": 2,
                        "working": false,
                        "startTime": "09:00:00",
                        "endTime": "18:00:00"
                    }
                ]
                """;

        mockMvc.perform(put("/api/v1/staff/" + staffAlpha.getId() + "/schedule")
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].startTime").value("08:00:00"))
                .andExpect(jsonPath("$[1].working").value(false));
    }

    @Test
    public void testUnauthenticatedAccessRejection() throws Exception {
        mockMvc.perform(get("/api/v1/staff"))
                .andExpect(status().isForbidden());
    }
}
