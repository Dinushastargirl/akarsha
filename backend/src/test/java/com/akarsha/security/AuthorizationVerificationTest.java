package com.akarsha.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthorizationVerificationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Test
    public void whenStaffUpdatesSettings_thenForbidden() throws Exception {
        String token = jwtService.generateToken("staff@alpha.com", "alpha", "STAFF");

        String payload = """
                {
                    "name": "Hacked Name"
                }
                """;

        mockMvc.perform(put("/settings")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());
    }

    @Test
    public void whenOwnerUpdatesSettings_thenAllowed() throws Exception {
        String token = jwtService.generateToken("owner@alpha.com", "alpha", "SALON_OWNER");

        String payload = """
                {
                    "name": "Valid Owner Name",
                    "subdomain": "alpha",
                    "phone": "555-1234",
                    "address": "123 Alpha St",
                    "city": "Alpha City",
                    "businessType": "Salon"
                }
                """;

        mockMvc.perform(put("/settings")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());
    }

    @Test
    public void whenReceptionistCreatesStaff_thenForbidden() throws Exception {
        String token = jwtService.generateToken("rec@alpha.com", "alpha", "RECEPTIONIST");

        String payload = """
                {
                    "fullName": "New Staff",
                    "email": "new@staff.com",
                    "role": "STAFF",
                    "active": true
                }
                """;

        mockMvc.perform(post("/staff")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());
    }

    @Test
    public void whenStaffViewsCustomers_thenAllowed() throws Exception {
        String token = jwtService.generateToken("staff@alpha.com", "alpha", "STAFF");

        mockMvc.perform(get("/customers")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
