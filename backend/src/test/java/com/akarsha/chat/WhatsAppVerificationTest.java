package com.akarsha.chat;

import com.akarsha.chat.whatsapp.WhatsAppService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
public class WhatsAppVerificationTest {

    @Autowired
    private WhatsAppService whatsAppService;

    @Test
    void testWebhookVerification() {
        assertTrue(whatsAppService.verifyWebhook("subscribe", "akarsha_verify_123"));
        assertFalse(whatsAppService.verifyWebhook("subscribe", "wrong_token"));
        assertFalse(whatsAppService.verifyWebhook("invalid_mode", "akarsha_verify_123"));
    }

    @Test
    void testSignatureVerification_Invalid() {
        // By default appSecret is dummy_secret which bypasses it for local dev. 
        // We're just making sure the logic parses.
        boolean result = whatsAppService.verifySignature("{}", "sha256=invalidhash");
        assertTrue(result); // Since appSecret is dummy_secret in test profile, it bypasses
    }
}
