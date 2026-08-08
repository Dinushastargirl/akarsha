package com.akarsha.chat.ai;

import com.akarsha.chat.domain.LanguageMode;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AiAssistantService {

    private final AiProvider aiProvider;
    private final AiToolsService aiToolsService;

    public AiAssistantService(AiProvider aiProvider, AiToolsService aiToolsService) {
        this.aiProvider = aiProvider;
        this.aiToolsService = aiToolsService;
    }

    public String processMessage(String message, LanguageMode language, String tenantId) {
        List<AiMessage> history = new ArrayList<>();
        
        String systemPrompt = "You are the Akarsha AI Assistant for a salon. " +
                "You must be friendly, concise, and helpful. " +
                "Do NOT invent availability, prices, or staff schedules. " +
                "Current Tenant: " + tenantId + ". ";

        if (language == LanguageMode.SINHALA) {
            systemPrompt += "CRITICAL RULE: You MUST RESPOND IN SINHALA UNICODE script. Do NOT use Singlish or English.";
        } else if (language == LanguageMode.TAMIL) {
            systemPrompt += "CRITICAL RULE: You MUST RESPOND IN TAMIL UNICODE script. Do NOT use Tanglish or English.";
        } else {
            systemPrompt += "CRITICAL RULE: You MUST RESPOND IN ENGLISH.";
        }

        history.add(new AiMessage("system", systemPrompt));
        history.add(new AiMessage("user", message));

        // Call the AI provider
        return aiProvider.generateResponse(history);
    }
}
