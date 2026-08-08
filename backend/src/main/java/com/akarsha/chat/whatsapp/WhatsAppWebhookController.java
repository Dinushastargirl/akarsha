package com.akarsha.chat.whatsapp;

import com.akarsha.chat.ConversationService;
import com.akarsha.chat.ai.AiAssistantService;
import com.akarsha.chat.domain.ChannelType;
import com.akarsha.chat.domain.Conversation;
import com.akarsha.chat.domain.LanguageMode;
import com.akarsha.chat.domain.SenderType;
import com.akarsha.tenant.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/whatsapp")
public class WhatsAppWebhookController {

    private final WhatsAppService whatsAppService;
    private final ConversationService conversationService;
    private final AiAssistantService aiAssistantService;

    public WhatsAppWebhookController(WhatsAppService whatsAppService,
                                     ConversationService conversationService,
                                     AiAssistantService aiAssistantService) {
        this.whatsAppService = whatsAppService;
        this.conversationService = conversationService;
        this.aiAssistantService = aiAssistantService;
    }

    // Webhook verification endpoint (Meta sends a GET request here when configuring)
    @GetMapping("/webhook")
    public ResponseEntity<String> verifyWebhook(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String token,
            @RequestParam("hub.challenge") String challenge) {
        
        if (whatsAppService.verifyWebhook(mode, token)) {
            return ResponseEntity.ok(challenge);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    // Webhook event endpoint (Meta sends POST requests here for incoming messages)
    @PostMapping("/webhook")
    public ResponseEntity<String> handleIncomingMessage(
            @RequestBody String payload,
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String signatureHeader) {
        
        if (!whatsAppService.verifySignature(payload, signatureHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Mock Payload Parsing (In a real app, parse the complex JSON structure from Meta)
        // We will assume the payload contains the sender phone number and message text.
        // For simplicity in this implementation, we will use a naive extraction or hardcode for demo.
        
        String senderPhone = extractPhone(payload);
        String messageText = extractMessage(payload);

        if (senderPhone == null || messageText == null) {
            return ResponseEntity.ok("EVENT_RECEIVED");
        }

        // Must determine tenant. A real app might look up the WABA ID to find the tenant.
        // We will default to "alpha" for the demo.
        TenantContext.setCurrentTenant("alpha");

        // Assume LanguageMode is ENGLISH by default for WhatsApp unless detected otherwise
        Conversation conversation = conversationService.getOrCreateConversation(
                senderPhone,
                ChannelType.WHATSAPP,
                LanguageMode.ENGLISH
        );

        conversationService.saveMessage(conversation, messageText, SenderType.CUSTOMER);

        String aiResponse = aiAssistantService.processMessage(
                messageText,
                LanguageMode.ENGLISH,
                "alpha"
        );

        conversationService.saveMessage(conversation, aiResponse, SenderType.AI);

        // Send back via WhatsApp API
        whatsAppService.sendMessage(senderPhone, aiResponse);

        return ResponseEntity.ok("EVENT_RECEIVED");
    }

    private String extractPhone(String payload) {
        // Naive mock extraction
        if (payload.contains("\"from\":\"")) {
            int start = payload.indexOf("\"from\":\"") + 8;
            int end = payload.indexOf("\"", start);
            return payload.substring(start, end);
        }
        return "unknown_phone";
    }

    private String extractMessage(String payload) {
        // Naive mock extraction
        if (payload.contains("\"body\":\"")) {
            int start = payload.indexOf("\"body\":\"") + 8;
            int end = payload.indexOf("\"", start);
            return payload.substring(start, end);
        }
        return "mock message";
    }
}
