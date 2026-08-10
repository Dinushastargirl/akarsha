package com.akarsha.ai;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.akarsha.tenant.TenantContext;

@RestController
@RequestMapping("/api/v1/settings/ai")
@PreAuthorize("hasAnyRole('SALON_OWNER', 'MANAGER', 'SUPER_ADMIN')")
public class AiConfigurationController {

    private final AiConfigurationRepository repository;

    public AiConfigurationController(AiConfigurationRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public ResponseEntity<AiConfiguration> getConfiguration() {
        String tenantId = TenantContext.getCurrentTenant();
        AiConfiguration config = repository.findByTenantId(tenantId)
                .orElseGet(() -> repository.save(new AiConfiguration()));
        return ResponseEntity.ok(config);
    }

    @PutMapping
    public ResponseEntity<AiConfiguration> updateConfiguration(@RequestBody AiConfiguration updated) {
        String tenantId = TenantContext.getCurrentTenant();
        AiConfiguration config = repository.findByTenantId(tenantId)
                .orElseGet(() -> new AiConfiguration());
        
        config.setEnabled(updated.isEnabled());
        config.setAssistantName(updated.getAssistantName());
        config.setGreeting(updated.getGreeting());
        config.setSupportedLanguages(updated.getSupportedLanguages());
        config.setTone(updated.getTone());
        config.setBookingEnabled(updated.isBookingEnabled());
        config.setCancellationEnabled(updated.isCancellationEnabled());
        config.setReschedulingEnabled(updated.isReschedulingEnabled());
        config.setHumanHandoffEnabled(updated.isHumanHandoffEnabled());
        config.setBusinessContext(updated.getBusinessContext());
        config.setSystemInstructions(updated.getSystemInstructions());
        
        return ResponseEntity.ok(repository.save(config));
    }
}
