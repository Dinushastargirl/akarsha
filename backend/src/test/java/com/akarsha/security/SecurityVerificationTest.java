package com.akarsha.security;

import com.akarsha.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SecurityVerificationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @AfterEach
    public void tearDown() {
        // Double check that the ThreadLocal tenant context is cleared after every request execution
        assertNull(TenantContext.getCurrentTenant(), "TenantContext must be null after request completion!");
    }

    @Test
    public void whenUnauthenticated_thenRejected() throws Exception {
        mockMvc.perform(get("/api/v1/test/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void whenInvalidToken_thenRejected() throws Exception {
        mockMvc.perform(get("/api/v1/test/users")
                        .header("Authorization", "Bearer invalid.token.value"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void whenAuthenticatedSalonAlpha_thenCanOnlyAccessAlphaData() throws Exception {
        // Generate token for owner@alpha.com (tenant: alpha, role: SALON_OWNER)
        String token = jwtService.generateToken("owner@alpha.com", "alpha", "SALON_OWNER");

        mockMvc.perform(get("/api/v1/test/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$", containsInAnyOrder("alpha_owner", "alpha_manager", "alpha_receptionist", "alpha_staff")))
                .andExpect(jsonPath("$", not(containsInAnyOrder("beta_owner"))));
    }

    @Test
    public void whenAuthenticatedSalonBeta_thenCanOnlyAccessBetaData() throws Exception {
        // Generate token for owner@beta.com (tenant: beta, role: SALON_OWNER)
        String token = jwtService.generateToken("owner@beta.com", "beta", "SALON_OWNER");

        mockMvc.perform(get("/api/v1/test/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$", containsInAnyOrder("beta_owner")))
                .andExpect(jsonPath("$", not(containsInAnyOrder("alpha_owner", "alpha_manager"))));
    }

    @Test
    public void whenLoginWithCorrectCredentials_thenReceiveToken() throws Exception {
        String loginPayload = """
                {
                    "tenantId": "alpha",
                    "email": "owner@alpha.com",
                    "password": "Owner123!"
                }
                """;

        mockMvc.perform(post("/api/v1/public/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    public void whenLoginWithIncorrectPassword_thenUnauthorized() throws Exception {
        String loginPayload = """
                {
                    "tenantId": "alpha",
                    "email": "owner@alpha.com",
                    "password": "wrongpassword"
                }
                """;

        mockMvc.perform(post("/api/v1/public/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isUnauthorized());
    }
}
