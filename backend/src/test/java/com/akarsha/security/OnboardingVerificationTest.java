package com.akarsha.security;

import com.akarsha.core.entity.User;
import com.akarsha.core.repository.UserRepository;
import com.akarsha.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class OnboardingVerificationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @AfterEach
    public void tearDown() {
        TenantContext.clear();
    }

    @Test
    public void whenSignupSuccessful_thenPasswordIsHashedAndCanLogin() throws Exception {
        String signupPayload = """
                {
                    "fullName": "Onboarding Owner",
                    "email": "owner@onboard.com",
                    "password": "securePassword123"
                }
                """;

        // 1. Perform Signup
        MvcResult signupResult = mockMvc.perform(post("/public/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        // 2. Verify password is encrypted in database
        TenantContext.setCurrentTenant("SYSTEM_BYPASS");
        Optional<User> userOpt = userRepository.findByEmail("owner@onboard.com");
        assertTrue(userOpt.isPresent());
        User user = userOpt.get();
        assertNotEquals("securePassword123", user.getPasswordHash());
        assertTrue(passwordEncoder.matches("securePassword123", user.getPasswordHash()));
        assertNull(user.getTenantId()); // Tenant is null at first signup stage
        TenantContext.clear();

        // 3. Perform Login
        String loginPayload = """
                {
                    "email": "owner@onboard.com",
                    "password": "securePassword123"
                }
                """;

        mockMvc.perform(post("/public/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    public void whenSignupDuplicateEmail_thenRejected() throws Exception {
        String duplicatePayload = """
                {
                    "fullName": "Duplicate Owner",
                    "email": "owner@alpha.com",
                    "password": "securePassword123"
                }
                """;

        mockMvc.perform(post("/public/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(duplicatePayload))
                .andExpect(status().isBadRequest())
                .andExpect(content -> assertEquals("Email is already registered", content.getResponse().getContentAsString()));
    }

    @Test
    public void whenSignupValidationFails_thenRejected() throws Exception {
        String badPayload = """
                {
                    "fullName": "",
                    "email": "invalid@example.com",
                    "password": "pass"
                }
                """;

        mockMvc.perform(post("/public/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badPayload))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void whenSalonCreationAndSetupFlowExecuted_thenTenantIsolationWorks() throws Exception {
        // 1. Signup fresh user
        String token = jwtService.generateToken("owner@onboard-flow.com", null, "SALON_OWNER");
        
        TenantContext.setCurrentTenant("SYSTEM_BYPASS");
        User user = new User();
        user.setFullName("Flow Owner");
        user.setUsername("owner@onboard-flow.com");
        user.setEmail("owner@onboard-flow.com");
        user.setPasswordHash(passwordEncoder.encode("password"));
        user.setRole("SALON_OWNER");
        userRepository.save(user);
        TenantContext.clear();

        // 2. Create Salon
        String salonPayload = """
                {
                    "name": "Nirvana Flow Salon",
                    "subdomain": "nirvana-flow",
                    "phone": "0771234567",
                    "address": "45 Galle Road",
                    "city": "Colombo 03",
                    "businessType": "Salon"
                }
                """;

        MvcResult salonResult = mockMvc.perform(post("/onboarding/create-salon")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(salonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        // Get updated token with new tenantId claim
        String responseBody = salonResult.getResponse().getContentAsString();
        String updatedToken = responseBody.substring(responseBody.indexOf("token\":\"") + 8, responseBody.lastIndexOf("\""));

        // 3. Configure Setup Steps (hours, service, staff)
        String setupPayload = """
                {
                    "openingTime": "08:30",
                    "closingTime": "19:00",
                    "firstServiceName": "Flow Haircut",
                    "firstServicePrice": 3500.0,
                    "firstServiceDuration": 45,
                    "firstStaffName": "Flow Stylist",
                    "firstStaffEmail": "stylist@nirvana-flow.com"
                }
                """;

        mockMvc.perform(post("/onboarding/setup")
                        .header("Authorization", "Bearer " + updatedToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(setupPayload))
                .andExpect(status().isOk());

        // 4. Verify tenant isolation (a user from 'alpha' salon cannot access 'nirvana-flow' services)
        String alphaToken = jwtService.generateToken("owner@alpha.com", "alpha", "SALON_OWNER");

        mockMvc.perform(get("/test/users")
                        .header("Authorization", "Bearer " + alphaToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(containsString("Flow Stylist"))));
    }
}
