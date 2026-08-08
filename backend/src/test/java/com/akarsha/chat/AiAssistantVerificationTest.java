package com.akarsha.chat;

import com.akarsha.chat.ai.AiAssistantService;
import com.akarsha.chat.domain.LanguageMode;
import com.akarsha.tenant.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@ActiveProfiles("test")
public class AiAssistantVerificationTest {

    @Autowired
    private AiAssistantService aiAssistantService;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant("alpha");
    }

    @Test
    void testLanguageSelection_English() {
        String response = aiAssistantService.processMessage("I want to book an appointment", LanguageMode.ENGLISH, "alpha");
        assertTrue(response.contains("I can help you book an appointment"));
    }

    @Test
    void testLanguageSelection_SinhalaUnicode() {
        String response = aiAssistantService.processMessage("හලෝ", LanguageMode.SINHALA, "alpha");
        assertTrue(response.contains("ආයුබෝවන්!"));
        assertFalse(response.contains("I can help")); // Should not fall back to English
    }

    @Test
    void testLanguageSelection_TamilUnicode() {
        String response = aiAssistantService.processMessage("வணக்கம்", LanguageMode.TAMIL, "alpha");
        assertTrue(response.contains("வணக்கம்!"));
        assertFalse(response.contains("I can help")); // Should not fall back to English
    }

    @Test
    void testFallback_NoIntentMatched() {
        String responseEn = aiAssistantService.processMessage("Just saying hello", LanguageMode.ENGLISH, "alpha");
        assertTrue(responseEn.contains("I understand you said"));

        String responseSi = aiAssistantService.processMessage("හලෝ", LanguageMode.SINHALA, "alpha");
        assertTrue(responseSi.contains("ආයුබෝවන්!"));

        String responseTa = aiAssistantService.processMessage("வணக்கம்", LanguageMode.TAMIL, "alpha");
        assertTrue(responseTa.contains("வணக்கம்!"));
    }
}
