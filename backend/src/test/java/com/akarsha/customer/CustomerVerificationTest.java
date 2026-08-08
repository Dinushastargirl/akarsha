package com.akarsha.customer;

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

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class CustomerVerificationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    @AfterEach
    public void tearDown() {
        TenantContext.clear();
    }

    private String getAuthHeader(String email, String tenantId) {
        String token = jwtService.generateToken(email, tenantId, "SALON_OWNER");
        return "Bearer " + token;
    }

    @Test
    public void whenCreateCustomer_thenSavedCorrectly() throws Exception {
        String authHeader = getAuthHeader("owner@alpha.com", "alpha");
        String payload = """
                {
                    "fullName": "Sunil Perera",
                    "phone": "0771234567",
                    "email": "sunil@gmail.com",
                    "birthday": "1990-05-15",
                    "notes": "Prefers hot water wash"
                }
                """;

        mockMvc.perform(post("/api/v1/customers")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fullName").value("Sunil Perera"))
                .andExpect(jsonPath("$.phone").value("0771234567"))
                .andExpect(jsonPath("$.tenantId").value("alpha"));
    }

    @Test
    public void whenGetCustomer_thenScopedToTenant() throws Exception {
        // Seed customer in alpha
        TenantContext.setCurrentTenant("alpha");
        Customer c1 = new Customer();
        c1.setFullName("Alpha Customer");
        c1.setPhone("0771112222");
        c1 = customerRepository.save(c1);
        entityManager.flush();
        entityManager.clear();
        TenantContext.clear();

        String alphaAuth = getAuthHeader("owner@alpha.com", "alpha");
        String betaAuth = getAuthHeader("owner@beta.com", "beta");

        // Alpha user can view it
        mockMvc.perform(get("/api/v1/customers/" + c1.getId())
                        .header("Authorization", alphaAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Alpha Customer"));

        // Beta user gets 404 due to tenant aspect filtering
        mockMvc.perform(get("/api/v1/customers/" + c1.getId())
                        .header("Authorization", betaAuth))
                .andExpect(status().isNotFound());
    }

    @Test
    public void whenUpdateCustomer_thenUpdatedAndRestricted() throws Exception {
        TenantContext.setCurrentTenant("alpha");
        Customer c1 = new Customer();
        c1.setFullName("Old Name");
        c1.setPhone("0773334444");
        c1 = customerRepository.save(c1);
        entityManager.flush();
        entityManager.clear();
        TenantContext.clear();

        String alphaAuth = getAuthHeader("owner@alpha.com", "alpha");
        String betaAuth = getAuthHeader("owner@beta.com", "beta");

        String updatePayload = """
                {
                    "fullName": "New Name",
                    "phone": "0773335555"
                }
                """;

        // Beta user trying to update gets 404
        mockMvc.perform(put("/api/v1/customers/" + c1.getId())
                        .header("Authorization", betaAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isNotFound());

        // Alpha user succeeds
        mockMvc.perform(put("/api/v1/customers/" + c1.getId())
                        .header("Authorization", alphaAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("New Name"))
                .andExpect(jsonPath("$.phone").value("0773335555"));
    }

    @Test
    public void whenDeleteCustomer_thenIsolated() throws Exception {
        TenantContext.setCurrentTenant("alpha");
        Customer c1 = new Customer();
        c1.setFullName("ToDelete");
        c1.setPhone("0775556666");
        c1 = customerRepository.save(c1);
        entityManager.flush();
        entityManager.clear();
        TenantContext.clear();

        String alphaAuth = getAuthHeader("owner@alpha.com", "alpha");
        String betaAuth = getAuthHeader("owner@beta.com", "beta");

        // Beta user trying to delete gets 404
        mockMvc.perform(delete("/api/v1/customers/" + c1.getId())
                        .header("Authorization", betaAuth))
                .andExpect(status().isNotFound());

        // Alpha user deletes successfully
        mockMvc.perform(delete("/api/v1/customers/" + c1.getId())
                        .header("Authorization", alphaAuth))
                .andExpect(status().isOk());

        TenantContext.setCurrentTenant("alpha");
        assertFalse(customerRepository.findById(c1.getId()).isPresent());
        TenantContext.clear();
    }

    @Test
    public void whenSearchCustomer_thenMatchesCorrectly() throws Exception {
        TenantContext.setCurrentTenant("alpha");
        Customer c1 = new Customer();
        c1.setFullName("Sunil Perera");
        c1.setPhone("0777777777");
        customerRepository.save(c1);

        Customer c2 = new Customer();
        c2.setFullName("Nimal Silva");
        c2.setPhone("0711111111");
        customerRepository.save(c2);
        TenantContext.clear();

        String alphaAuth = getAuthHeader("owner@alpha.com", "alpha");

        // Search by name
        mockMvc.perform(get("/api/v1/customers/search?query=sunil")
                        .header("Authorization", alphaAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].fullName").value("Sunil Perera"));

        // Search by phone
        mockMvc.perform(get("/api/v1/customers/search?query=07111")
                        .header("Authorization", alphaAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].fullName").value("Nimal Silva"));
    }

    @Test
    public void whenValidationErrors_thenReturnsBadRequest() throws Exception {
        String authHeader = getAuthHeader("owner@alpha.com", "alpha");

        // Missing Name
        String payloadNoName = """
                {
                    "phone": "0771234567"
                }
                """;
        mockMvc.perform(post("/api/v1/customers")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadNoName))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason(nullValue())) // Message returned in response body
                .andExpect(jsonPath("$").value("Name is required"));

        // Invalid Email Format
        String payloadBadEmail = """
                {
                    "fullName": "Sunil Perera",
                    "phone": "0771234567",
                    "email": "bademail"
                }
                """;
        mockMvc.perform(post("/api/v1/customers")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadBadEmail))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Invalid email address format"));
    }

    @Test
    public void whenUnauthenticated_thenRejected() throws Exception {
        mockMvc.perform(get("/api/v1/customers"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void whenPagination_thenCalculatesCorrectly() throws Exception {
        TenantContext.setCurrentTenant("alpha");
        for (int i = 1; i <= 15; i++) {
            Customer c = new Customer();
            c.setFullName("Customer " + String.format("%02d", i));
            c.setPhone("07700000" + String.format("%02d", i));
            customerRepository.save(c);
        }
        TenantContext.clear();

        String authHeader = getAuthHeader("owner@alpha.com", "alpha");

        // Page 0 should have 10 elements
        mockMvc.perform(get("/api/v1/customers?page=0&size=10")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(10)))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.totalElements").value(15));

        // Page 1 should have 5 elements
        mockMvc.perform(get("/api/v1/customers?page=1&size=10")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)));
    }

    @Test
    public void whenGetCustomerStats_thenReturnsCorrectDefaults() throws Exception {
        TenantContext.setCurrentTenant("alpha");
        Customer c1 = new Customer();
        c1.setFullName("Stats Customer");
        c1.setPhone("0778889999");
        c1 = customerRepository.save(c1);
        entityManager.flush();
        entityManager.clear();
        TenantContext.clear();

        String alphaAuth = getAuthHeader("owner@alpha.com", "alpha");

        mockMvc.perform(get("/api/v1/customers/" + c1.getId() + "/stats")
                        .header("Authorization", alphaAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalVisits").value(0))
                .andExpect(jsonPath("$.completedVisits").value(0))
                .andExpect(jsonPath("$.noShowCount").value(0))
                .andExpect(jsonPath("$.totalRevenue").value(0))
                .andExpect(jsonPath("$.lastVisitDate").doesNotExist());
    }
}
