package com.akarsha.ai;

import com.akarsha.tenant.TenantContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/public/chat/{salonSlug}")
public class PublicChatController {

    private final AiOrchestratorService aiOrchestratorService;
    private final AiInteractionRepository interactionRepository;
    private final AiMessageRepository messageRepository;
    private final AiConfigurationRepository configRepository;

    public PublicChatController(AiOrchestratorService aiOrchestratorService,
                                AiInteractionRepository interactionRepository,
                                AiMessageRepository messageRepository,
                                AiConfigurationRepository configRepository) {
        this.aiOrchestratorService = aiOrchestratorService;
        this.interactionRepository = interactionRepository;
        this.messageRepository = messageRepository;
        this.configRepository = configRepository;
    }

    public static class ChatRequest {
        public String sessionId;
        public String identifier;
        public String message;
        public String language;
    }

    public static class ChatResponse {
        public String sessionId;
        public String message;
        
        public ChatResponse(String sessionId, String message) {
            this.sessionId = sessionId;
            this.message = message;
        }
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@PathVariable String salonSlug, @RequestBody ChatRequest req) {
        // TenantContext is populated by PublicTenantFilter
        String tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            AiMessage aiMessage = aiOrchestratorService.processMessage(
                    req.sessionId, req.identifier, req.message, req.language);
            return ResponseEntity.ok(new ChatResponse(aiMessage.getInteraction().getSessionId(), aiMessage.getContent()));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/history/{sessionId}")
    public ResponseEntity<?> getHistory(@PathVariable String salonSlug, @PathVariable String sessionId) {
        String tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) return ResponseEntity.notFound().build();

        return interactionRepository.findBySessionId(sessionId)
                .map(interaction -> {
                    List<AiMessage> messages = messageRepository.findByInteractionIdOrderByTimestampAsc(interaction.getId());
                    return ResponseEntity.ok(messages.stream().map(m -> Map.of(
                            "id", m.getId(),
                            "sender", m.getSenderType().name(),
                            "text", m.getContent(),
                            "timestamp", m.getTimestamp()
                    )).collect(Collectors.toList()));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/config")
    public ResponseEntity<?> getChatConfig(@PathVariable String salonSlug) {
        String tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) return ResponseEntity.notFound().build();
        
        return configRepository.findByTenantId(tenantId)
                .map(config -> ResponseEntity.ok(Map.of(
                        "enabled", config.isEnabled(),
                        "assistantName", config.getAssistantName(),
                        "greeting", config.getGreeting(),
                        "supportedLanguages", config.getSupportedLanguages()
                )))
                .orElse(ResponseEntity.ok(Map.of(
                        "enabled", false,
                        "assistantName", "Akarsha Assistant",
                        "greeting", "Hello! I am your AI receptionist. How can I help you today?",
                        "supportedLanguages", "English,සිංහල,தமிழ்,Singlish,Tanglish"
                )));
    }
}
