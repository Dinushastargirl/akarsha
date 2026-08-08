package com.akarsha.core;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class DemoEnvironmentVerificationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void verifyEndToEndRBAC() {
        System.out.println("Starting Demo Environment Verification...");

        // 1. Authenticate Owner
        String ownerToken = login("alpha", "owner@alpha.com", "Owner123!");
        assertThat(ownerToken).isNotNull();
        System.out.println("owner -> successful login");

        // 2. Authenticate Manager
        String managerToken = login("alpha", "manager@alpha.com", "Manager123!");
        assertThat(managerToken).isNotNull();
        System.out.println("manager -> successful login");

        // 3. Authenticate Receptionist
        String receptionistToken = login("alpha", "receptionist@alpha.com", "Receptionist123!");
        assertThat(receptionistToken).isNotNull();
        System.out.println("receptionist -> successful login");

        // 4. Authenticate Staff
        String staffToken = login("alpha", "staff@alpha.com", "Staff123!");
        assertThat(staffToken).isNotNull();
        System.out.println("staff -> successful login");

        // Test RBAC: staff attempting to modify services -> 403
        HttpHeaders staffHeaders = new HttpHeaders();
        staffHeaders.setBearerAuth(staffToken);
        staffHeaders.set("X-Tenant-ID", "alpha");
        HttpEntity<?> staffRequest = new HttpEntity<>(Map.of("name", "Haircut", "price", 50, "durationMinutes", 30), staffHeaders);
        ResponseEntity<String> staffServiceCreate = restTemplate.postForEntity("/services", staffRequest, String.class);
        assertThat(staffServiceCreate.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        System.out.println("staff attempting to modify services -> 403");

        // Test RBAC: receptionist attempting owner-only settings -> 403
        HttpHeaders receptionistHeaders = new HttpHeaders();
        receptionistHeaders.setBearerAuth(receptionistToken);
        receptionistHeaders.set("X-Tenant-ID", "alpha");
        HttpEntity<?> receptionistRequest = new HttpEntity<>(Map.of("name", "New Name"), receptionistHeaders);
        ResponseEntity<String> receptionistSettingsUpdate = restTemplate.exchange("/settings", HttpMethod.PUT, receptionistRequest, String.class);
        assertThat(receptionistSettingsUpdate.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        System.out.println("receptionist attempting owner-only settings -> 403");

        // Test RBAC: manager accessing reports -> 200
        HttpHeaders managerHeaders = new HttpHeaders();
        managerHeaders.setBearerAuth(managerToken);
        managerHeaders.set("X-Tenant-ID", "alpha");
        HttpEntity<?> managerRequest = new HttpEntity<>(managerHeaders);
        ResponseEntity<String> managerReports = restTemplate.exchange("/reports/overview?startDate=2026-08-01&endDate=2026-08-31", HttpMethod.GET, managerRequest, String.class);
        assertThat(managerReports.getStatusCode()).isEqualTo(HttpStatus.OK);
        System.out.println("manager accessing reports -> 200");

        // Test RBAC: owner accessing reports -> 200
        HttpHeaders ownerHeaders = new HttpHeaders();
        ownerHeaders.setBearerAuth(ownerToken);
        ownerHeaders.set("X-Tenant-ID", "alpha");
        HttpEntity<?> ownerRequest = new HttpEntity<>(ownerHeaders);
        ResponseEntity<String> ownerReports = restTemplate.exchange("/reports/overview?startDate=2026-08-01&endDate=2026-08-31", HttpMethod.GET, ownerRequest, String.class);
        assertThat(ownerReports.getStatusCode()).isEqualTo(HttpStatus.OK);
        System.out.println("owner accessing reports -> 200");
    }

    private String login(String tenant, String email, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Tenant-ID", tenant);
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, String> request = Map.of("email", email, "password", password);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity("/public/auth/login", entity, Map.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return (String) response.getBody().get("token");
        }
        return null;
    }
}
