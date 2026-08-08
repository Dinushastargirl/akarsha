package com.akarsha.customer;

import com.akarsha.core.entity.Salon;
import com.akarsha.core.repository.SalonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class PublicBookingVerificationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SalonRepository salonRepository;

    private String validSlug;

    @BeforeEach
    public void setup() {
        Salon s = new Salon();
        s.setName("Test Salon A");
        s.setSubdomain("test-salon-a");
        s.setCity("Test City");
        s.setAddress("123 Test St");
        s.setBusinessType("Salon");
        s.setStatus("ACTIVE");
        s = salonRepository.save(s);
        validSlug = s.getSubdomain();
    }

    @Test
    public void testGetSalonInfo_ValidSlug() throws Exception {
        mockMvc.perform(get("/api/v1/public/booking/" + validSlug + "/info"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.name").value("Test Salon A"))
               .andExpect(jsonPath("$.subdomain").value(validSlug));
    }

    @Test
    public void testGetSalonInfo_InvalidSlug() throws Exception {
        mockMvc.perform(get("/api/v1/public/booking/invalid-slug-123/info"))
               .andExpect(status().isNotFound());
    }

    @Test
    public void testGetServices_ValidSlug() throws Exception {
        mockMvc.perform(get("/api/v1/public/booking/" + validSlug + "/services"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$").isArray());
    }
}
