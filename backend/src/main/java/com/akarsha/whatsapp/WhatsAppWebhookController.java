package com.akarsha.whatsapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/public/whatsapp/webhook")
public class WhatsAppWebhookController {

    private static final Logger logger = LoggerFactory.getLogger(WhatsAppWebhookController.class);

    @Value("${whatsapp.verify-token:}")
    private String verifyToken;

    private final WhatsAppWebhookProcessor webhookProcessor;

    public WhatsAppWebhookController(WhatsAppWebhookProcessor webhookProcessor) {
        this.webhookProcessor = webhookProcessor;
    }

    @GetMapping
    public ResponseEntity<String> verifyWebhook(
            @RequestParam(name = "hub.mode", required = false) String mode,
            @RequestParam(name = "hub.verify_token", required = false) String token,
            @RequestParam(name = "hub.challenge", required = false) String challenge) {

        if ("subscribe".equals(mode) && verifyToken.equals(token)) {
            logger.info("Webhook verified successfully");
            return ResponseEntity.ok(challenge);
        }
        
        logger.warn("Webhook verification failed. Expected token: {}, Received token: {}", verifyToken, token);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @PostMapping
    public ResponseEntity<String> handleIncomingMessage(@RequestBody Map<String, Object> payload) {
        try {
            webhookProcessor.process(payload);
            return ResponseEntity.ok("EVENT_RECEIVED");
        } catch (Exception e) {
            logger.error("Error processing WhatsApp webhook", e);
            // Always return 200 to Meta so they don't disable the webhook, 
            // unless we specifically want them to retry. For general processing errors, return 200.
            return ResponseEntity.ok("EVENT_RECEIVED"); 
        }
    }
}
