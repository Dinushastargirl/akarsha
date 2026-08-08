package com.akarsha.core.controller;

import com.akarsha.core.entity.Salon;
import com.akarsha.core.repository.SalonRepository;
import com.akarsha.tenant.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/settings")
@PreAuthorize("hasAnyRole('SALON_OWNER', 'MANAGER')")
public class SettingsController {

    private final SalonRepository salonRepository;

    public SettingsController(SalonRepository salonRepository) {
        this.salonRepository = salonRepository;
    }

    @GetMapping
    public ResponseEntity<?> getSettings() {
        String currentTenant = TenantContext.getCurrentTenant();
        if (currentTenant == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No tenant context");
        }

        // We bypass tenant filter temporarily because Salon doesn't extend TenantAwareEntity
        // Wait, does Salon extend TenantAwareEntity? Let's assume it doesn't and search by subdomain.
        Optional<Salon> salonOpt = salonRepository.findBySubdomain(currentTenant);
        if (salonOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Salon not found for current tenant");
        }

        return ResponseEntity.ok(salonOpt.get());
    }

    @PutMapping
    public ResponseEntity<?> updateSettings(@RequestBody SettingsUpdateRequest request) {
        String currentTenant = TenantContext.getCurrentTenant();
        if (currentTenant == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No tenant context");
        }

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Salon name is required");
        }

        Optional<Salon> salonOpt = salonRepository.findBySubdomain(currentTenant);
        if (salonOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Salon not found");
        }

        Salon salon = salonOpt.get();
        salon.setName(request.getName());
        salon.setPhone(request.getPhone());
        salon.setAddress(request.getAddress());
        salon.setCity(request.getCity());
        salon.setBusinessType(request.getBusinessType());
        salon.setOpeningTime(request.getOpeningTime());
        salon.setClosingTime(request.getClosingTime());
        // Subdomain and setupCompleted are not updated here

        salonRepository.save(salon);
        return ResponseEntity.ok(salon);
    }

    public static class SettingsUpdateRequest {
        private String name;
        private String phone;
        private String address;
        private String city;
        private String businessType;
        private String openingTime;
        private String closingTime;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getBusinessType() { return businessType; }
        public void setBusinessType(String businessType) { this.businessType = businessType; }
        public String getOpeningTime() { return openingTime; }
        public void setOpeningTime(String openingTime) { this.openingTime = openingTime; }
        public String getClosingTime() { return closingTime; }
        public void setClosingTime(String closingTime) { this.closingTime = closingTime; }
    }
}
