package com.akarsha.chat.ai;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@ConditionalOnProperty(name = "akarsha.ai.provider", havingValue = "mock", matchIfMissing = true)
public class MockAiProvider implements AiProvider {

    @Override
    public String generateResponse(List<AiMessage> history) {
        if (history == null || history.isEmpty()) {
            return "Hello! How can I help you today?";
        }

        // Get the last user message
        String lastUserMessage = history.stream()
                .filter(m -> "user".equals(m.getRole()))
                .map(AiMessage::getContent)
                .reduce((first, second) -> second)
                .orElse("");
        
        String lowerMsg = lastUserMessage.toLowerCase();

        // Check language mode from system prompt (the first message should be the system prompt)
        String systemPrompt = history.isEmpty() ? "" : history.get(0).getContent();
        boolean isSinhala = systemPrompt.contains("RESPOND IN SINHALA UNICODE");
        boolean isTamil = systemPrompt.contains("RESPOND IN TAMIL UNICODE");

        if (lowerMsg.contains("book") || lowerMsg.contains("appointment")) {
            if (isSinhala) return "ඔබට වේලාවක් වෙන්කරවා ගැනීමට අවශ්‍යද?";
            if (isTamil) return "உங்களுக்கு நேரம் ஒதுக்க வேண்டுமா?";
            return "I can help you book an appointment. What service are you looking for?";
        } else if (lowerMsg.contains("reschedule")) {
            if (isSinhala) return "ඔබේ වේලාව වෙනස් කිරීමට අවශ්‍යද?";
            if (isTamil) return "உங்கள் நேரத்தை மாற்ற வேண்டுமா?";
            return "I can help you reschedule. When would you like to move your appointment to?";
        } else if (lowerMsg.contains("cancel")) {
            if (isSinhala) return "ඔබේ වේලාව අවලංගු කිරීමට අවශ්‍යද?";
            if (isTamil) return "உங்கள் நேரத்தை ரத்து செய்ய வேண்டுமா?";
            return "I can help you cancel your appointment. Are you sure?";
        } else if (lowerMsg.contains("human") || lowerMsg.contains("speak to someone")) {
            if (isSinhala) return "මම ඔබව සේවකයෙකුට සම්බන්ධ කරන්නම්.";
            if (isTamil) return "நான் உங்களை ஒரு பணியாளரிடம் இணைக்கிறேன்.";
            return "I will connect you to a staff member shortly.";
        }

        if (isSinhala) return "ආයුබෝවන්! මට ඔබට උදව් කළ හැක්කේ කෙසේද?";
        if (isTamil) return "வணக்கம்! நான் உங்களுக்கு எப்படி உதவ முடியும்?";
        return "I am a mock AI assistant. I understand you said: '" + lastUserMessage + "'";
    }
}
