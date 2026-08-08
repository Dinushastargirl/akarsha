package com.akarsha.reports;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ReportsVerificationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "owner@akarsha.com", roles = "SALON_OWNER")
    void testOwnerCanAccessOverview() throws Exception {
        mockMvc.perform(get("/reports/overview?startDate=2026-08-01&endDate=2026-08-31"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "manager@akarsha.com", roles = "MANAGER")
    void testManagerCanAccessOverview() throws Exception {
        mockMvc.perform(get("/reports/overview?startDate=2026-08-01&endDate=2026-08-31"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "receptionist@akarsha.com", roles = "RECEPTIONIST")
    void testReceptionistCannotAccessOverview() throws Exception {
        mockMvc.perform(get("/reports/overview?startDate=2026-08-01&endDate=2026-08-31"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "staff@akarsha.com", roles = "STAFF")
    void testStaffCannotAccessOverview() throws Exception {
        mockMvc.perform(get("/reports/overview?startDate=2026-08-01&endDate=2026-08-31"))
                .andExpect(status().isForbidden());
    }
}
