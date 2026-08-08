package com.akarsha.chat;

import com.akarsha.chat.ai.AiToolsService;
import com.akarsha.core.entity.ServiceEntity;
import com.akarsha.core.repository.ServiceRepository;
import com.akarsha.tenant.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@ActiveProfiles("test")
public class AiToolsVerificationTest {

    @Autowired
    private AiToolsService aiToolsService;
    
    @Autowired
    private ServiceRepository serviceRepository;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant("alpha");
    }

    @Test
    void testGetServices() {
        // Ensure there's a service in the DB for tenant alpha
        ServiceEntity service = new ServiceEntity();
        service.setName("Haircut Mock");
        service.setDurationMinutes(30);
        service.setPrice(new java.math.BigDecimal("25.00"));
        serviceRepository.save(service);

        String result = aiToolsService.getServices();
        assertNotNull(result);
        assertTrue(result.contains("Haircut Mock"));
    }

    @Test
    void testTenantIsolation_CrossTenantDataDenied() {
        TenantContext.setCurrentTenant("beta");
        String result = aiToolsService.getServices();
        // Since we only seeded Haircut Mock for 'alpha' earlier, or at least 'alpha' is separate from 'beta'
        // If we clear beta, it shouldn't see alpha's services.
        assertFalse(result.contains("Haircut Mock"));
    }
}
