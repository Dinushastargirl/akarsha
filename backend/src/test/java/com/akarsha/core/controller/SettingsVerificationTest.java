package com.akarsha.core.controller;

import com.akarsha.core.entity.Salon;
import com.akarsha.core.repository.SalonRepository;
import com.akarsha.security.JwtService;
import com.akarsha.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class SettingsVerificationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private SalonRepository salonRepository;

    @AfterEach
    public void tearDown() {
        TenantContext.clear();
    }

    private String getAuthHeader(String email, String tenantId) {
        String token = jwtService.generateToken(email, tenantId, "SALON_OWNER");
        return "Bearer " + token;
    }

    @Test
    public void whenGetSettings_thenReturnsCorrectTenantSalon() throws Exception {
        String alphaAuth = getAuthHeader("owner@alpha.com", "alpha");

        mockMvc.perform(get("/settings")
                        .header("Authorization", alphaAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Salon Alpha"))
                .andExpect(jsonPath("$.subdomain").value("alpha"));
    }

    @Test
    public void whenUpdateSettings_thenUpdatesOnlyAllowedFields() throws Exception {
        String alphaAuth = getAuthHeader("owner@alpha.com", "alpha");

        String updatePayload = """
                {
                    "name": "Alpha Premium",
                    "phone": "0771234567",
                    "address": "123 Main St",
                    "city": "Colombo",
                    "businessType": "Spa",
                    "openingTime": "09:00",
                    "closingTime": "18:00"
                }
                """;

        mockMvc.perform(put("/settings")
                        .header("Authorization", alphaAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alpha Premium"))
                .andExpect(jsonPath("$.phone").value("0771234567"))
                .andExpect(jsonPath("$.city").value("Colombo"))
                .andExpect(jsonPath("$.businessType").value("Spa"))
                .andExpect(jsonPath("$.subdomain").value("alpha")); // Ensure subdomain didn't change
    }

    @Test
    public void whenUpdateSettingsWithBlankName_thenReturnsBadRequest() throws Exception {
        String alphaAuth = getAuthHeader("owner@alpha.com", "alpha");

        String updatePayload = """
                {
                    "name": "",
                    "phone": "0771234567"
                }
                """;

        mockMvc.perform(put("/settings")
                        .header("Authorization", alphaAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void whenUnauthenticated_thenRejected() throws Exception {
        mockMvc.perform(get("/settings"))
                .andExpect(status().isForbidden());
    }
}
