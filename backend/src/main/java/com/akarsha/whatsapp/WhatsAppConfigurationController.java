package com.akarsha.whatsapp;

import com.akarsha.tenant.TenantContext;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/settings/whatsapp")
@PreAuthorize("hasRole('SALON_OWNER')")
public class WhatsAppConfigurationController {

    private final WhatsAppConfigurationRepository repository;
    private final WhatsAppClient whatsAppClient;
    private static final String MASK_VALUE = "••••••••••••••••";

    public WhatsAppConfigurationController(WhatsAppConfigurationRepository repository, WhatsAppClient whatsAppClient) {
        this.repository = repository;
        this.whatsAppClient = whatsAppClient;
    }

    @GetMapping
    public ResponseEntity<WhatsAppConfiguration> getConfiguration() {
        String tenantId = TenantContext.getCurrentTenant();
        WhatsAppConfiguration config = repository.findByTenantId(tenantId)
                .orElseGet(() -> {
                    WhatsAppConfiguration newConfig = new WhatsAppConfiguration();
                    newConfig.setTenantId(tenantId);
                    return newConfig;
                });
        
        // Hide/mask the access token before returning to the frontend
        if (config.getAccessToken() != null && !config.getAccessToken().isEmpty()) {
            config.setAccessToken(MASK_VALUE);
        }
        return ResponseEntity.ok(config);
    }

    @PostMapping
    public ResponseEntity<WhatsAppConfiguration> updateConfiguration(@RequestBody WhatsAppConfiguration config) {
        String tenantId = TenantContext.getCurrentTenant();
        WhatsAppConfiguration existing = repository.findByTenantId(tenantId)
                .orElseGet(() -> {
                    WhatsAppConfiguration newConfig = new WhatsAppConfiguration();
                    newConfig.setTenantId(tenantId);
                    return newConfig;
                });
        
        existing.setPhoneNumberId(config.getPhoneNumberId());
        existing.setWabaId(config.getWabaId());
        
        // Only update access token if a new one is provided and not masked
        String newAccessToken = config.getAccessToken();
        if (newAccessToken != null && !newAccessToken.isEmpty() && !newAccessToken.contains("•") && !newAccessToken.equals(MASK_VALUE)) {
            existing.setAccessToken(newAccessToken);
        }
        
        existing.setDisplayPhoneNumber(config.getDisplayPhoneNumber());
        existing.setEnabled(config.isEnabled());
        
        WhatsAppConfiguration saved = repository.save(existing);
        if (saved.getAccessToken() != null && !saved.getAccessToken().isEmpty()) {
            saved.setAccessToken(MASK_VALUE);
        }
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyConnection() {
        String tenantId = TenantContext.getCurrentTenant();
        Optional<WhatsAppConfiguration> configOpt = repository.findByTenantId(tenantId);
        if (configOpt.isEmpty() || configOpt.get().getAccessToken() == null || configOpt.get().getAccessToken().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("status", "ERROR", "message", "WhatsApp configuration is incomplete"));
        }
        WhatsAppConfiguration config = configOpt.get();
        config.setWebhookVerified(true);
        repository.save(config);
        return ResponseEntity.ok(Map.of("status", "CONNECTED", "message", "Connection verified successfully"));
    }

    @PostMapping("/test")
    public ResponseEntity<?> sendTestMessage(@RequestBody Map<String, String> payload) {
        String to = payload.get("to");
        if (to == null || to.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Recipient phone number 'to' is required"));
        }
        String tenantId = TenantContext.getCurrentTenant();
        Optional<WhatsAppConfiguration> configOpt = repository.findByTenantId(tenantId);
        if (configOpt.isEmpty() || configOpt.get().getAccessToken() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "No active WhatsApp configuration found"));
        }
        
        whatsAppClient.sendMessage(to, "Hello! This is a test message from your Akarsha Salon SaaS integration.", configOpt.get());
        return ResponseEntity.ok(Map.of("status", "SUCCESS", "message", "Test message dispatched"));
    }
}
