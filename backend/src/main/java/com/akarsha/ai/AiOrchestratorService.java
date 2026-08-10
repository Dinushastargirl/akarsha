package com.akarsha.ai;

import com.akarsha.tenant.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AiOrchestratorService {

    private final AiConfigurationRepository configRepository;
    private final AiInteractionRepository interactionRepository;
    private final AiMessageRepository messageRepository;
    private final List<AiProvider> providers;

    public AiOrchestratorService(AiConfigurationRepository configRepository,
                                 AiInteractionRepository interactionRepository,
                                 AiMessageRepository messageRepository,
                                 List<AiProvider> providers) {
        this.configRepository = configRepository;
        this.interactionRepository = interactionRepository;
        this.messageRepository = messageRepository;
        this.providers = providers;
    }

    @Transactional
    public AiMessage processMessage(String sessionId, String guestIdentifier, String content, String language) {
        return processMessage(sessionId, guestIdentifier, content, language, null);
    }

    @Transactional
    public AiMessage processMessage(String sessionId, String guestIdentifier, String content, String language, String externalId) {
        String currentTenant = TenantContext.getCurrentTenant();
        
        AiConfiguration config = configRepository.findByTenantId(currentTenant)
            .orElseGet(() -> {
                AiConfiguration newConfig = new AiConfiguration();
                // Rely on TenantAspect to set tenantId on save, or set it explicitly if needed
                return configRepository.save(newConfig);
            });

        if (!config.isEnabled()) {
            throw new IllegalStateException("AI Receptionist is currently disabled for this salon.");
        }

        AiLanguage aiLang = AiLanguage.fromCode(language);

        AiInteraction interaction;
        if (sessionId == null || sessionId.isEmpty()) {
            interaction = new AiInteraction();
            interaction.setGuestIdentifier(guestIdentifier);
            interaction.setLanguage(aiLang);
            interaction = interactionRepository.save(interaction);
        } else {
            interaction = interactionRepository.findBySessionId(sessionId)
                .orElseGet(() -> {
                    AiInteraction newInteraction = new AiInteraction();
                    newInteraction.setSessionId(sessionId);
                    newInteraction.setGuestIdentifier(guestIdentifier);
                    newInteraction.setLanguage(aiLang);
                    return interactionRepository.save(newInteraction);
                });
        }
        
        // Detect language from text dynamically
        AiLanguage detected = detectLanguage(content, interaction.getLanguage() != null ? interaction.getLanguage() : aiLang);
        interaction.setLanguage(detected);
        
        // Un-archive if resolved or archived
        if (interaction.getStatus() == InteractionStatus.RESOLVED || interaction.getStatus() == InteractionStatus.ARCHIVED) {
            interaction.setStatus(InteractionStatus.ACTIVE);
        }

        interaction.setUnreadCount(interaction.getUnreadCount() + 1);
        interaction.setLastActivity(java.time.LocalDateTime.now());

        // Save user message
        AiMessage userMessage = new AiMessage();
        userMessage.setInteraction(interaction);
        userMessage.setSenderType(MessageSender.USER);
        userMessage.setContent(content);
        if (externalId != null) {
            userMessage.setExternalId(externalId);
        }
        messageRepository.save(userMessage);

        if (interaction.getStatus() == InteractionStatus.HANDED_OFF || interaction.getStatus() == InteractionStatus.WAITING_FOR_STAFF) {
            interactionRepository.save(interaction);
            return userMessage; // Do not generate AI response
        }

        // Fetch history
        List<AiMessage> history = messageRepository.findByInteractionIdOrderByTimestampAsc(interaction.getId());
        
        // Limit context size to last 12 messages for production token economy
        if (history.size() > 12) {
            history = history.subList(history.size() - 12, history.size());
        }

        // Select provider
        AiProvider provider = providers.stream()
                .filter(p -> p.getProviderName().equalsIgnoreCase(config.getProviderName()))
                .findFirst()
                .orElse(providers.get(0));

        // Generate response
        AiContext context = new AiContext(config, history, content, interaction.getLanguage());
        AiResponse response = provider.generateResponse(context);

        if (response.isHandoffRequested() && config.isHumanHandoffEnabled()) {
            interaction.setStatus(InteractionStatus.WAITING_FOR_STAFF);
        }

        interactionRepository.save(interaction);

        // Save AI message
        AiMessage aiMessage = new AiMessage();
        aiMessage.setInteraction(interaction);
        aiMessage.setSenderType(MessageSender.AI);
        aiMessage.setContent(response.getMessage());
        return messageRepository.save(aiMessage);
    }

    private AiLanguage detectLanguage(String text, AiLanguage currentLang) {
        if (text == null || text.trim().isEmpty()) return currentLang;
        
        // Check for Sinhala script characters
        if (text.matches(".*[\\u0D80-\\u0DFF]+.*")) {
            return AiLanguage.SI;
        }
        // Check for Tamil script characters
        if (text.matches(".*[\\u0B80-\\u0BFF]+.*")) {
            return AiLanguage.TA;
        }
        
        String lower = text.toLowerCase().trim();
        // Common Sinhala written in English (Singlish) triggers
        if (lower.contains("ayubowan") || lower.contains("kohomada") || lower.contains("machan") || lower.contains("hariya") || lower.contains("puluwanda") || lower.contains("karannako") || lower.contains("subha")) {
            return AiLanguage.SI_LATN;
        }
        
        // Common Tamil written in English (Tanglish) triggers
        if (lower.contains("vanakam") || lower.contains("eppadi") || lower.contains("nalla") || lower.contains("pannunga") || lower.contains("irukkinga") || lower.contains("ponga") || lower.contains("engha")) {
            return AiLanguage.TA_LATN;
        }

        if (lower.equals("english") || lower.contains("speak in english") || lower.contains("use english")) {
            return AiLanguage.EN;
        }

        return currentLang;
    }
}
