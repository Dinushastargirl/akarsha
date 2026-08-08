package com.akarsha.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class PlatformAuthorizationVerificationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "admin@akarsha.com", roles = {"SUPER_ADMIN"})
    public void superAdminCanAccessPlatform() throws Exception {
        mockMvc.perform(get("/api/v1/platform/dashboard")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "owner@alpha.com", roles = {"SALON_OWNER"})
    public void salonOwnerCannotAccessPlatform() throws Exception {
        mockMvc.perform(get("/api/v1/platform/dashboard")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "manager@alpha.com", roles = {"MANAGER"})
    public void managerCannotAccessPlatform() throws Exception {
        mockMvc.perform(get("/api/v1/platform/dashboard")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
