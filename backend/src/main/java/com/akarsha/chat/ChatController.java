package com.akarsha.chat;

import com.akarsha.chat.ai.AiAssistantService;
import com.akarsha.chat.domain.ChannelType;
import com.akarsha.chat.domain.Conversation;
import com.akarsha.chat.domain.ConversationMessage;
import com.akarsha.chat.domain.SenderType;
import com.akarsha.chat.dto.ChatMessageRequest;
import com.akarsha.chat.dto.ChatMessageResponse;
import com.akarsha.tenant.TenantContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ConversationService conversationService;
    private final AiAssistantService aiAssistantService;

    public ChatController(ConversationService conversationService, AiAssistantService aiAssistantService) {
        this.conversationService = conversationService;
        this.aiAssistantService = aiAssistantService;
    }

    // Unauthenticated public endpoint. Security is based on the provided identifier for now.
    // Tenant context MUST be injected here (via interceptor or passed directly) for this to work.
    // For demo purposes, we can assume a header or path variable sets the tenant if the tenant interceptor
    // only triggers on authenticated routes. We'll set it manually for the "alpha" salon demo.
    
    @PostMapping("/send")
    public ResponseEntity<ChatMessageResponse> sendMessage(@RequestBody ChatMessageRequest request) {
        
        // Ensure tenant context is set for public chat requests (normally handled by interceptor, but interceptors might require auth)
        if (TenantContext.getCurrentTenant() == null) {
            TenantContext.setCurrentTenant("alpha"); // Defaulting to demo tenant for public chat widget
        }

        Conversation conversation = conversationService.getOrCreateConversation(
                request.getIdentifier(),
                ChannelType.WEB_CHAT,
                request.getLanguage()
        );

        // Save customer message
        conversationService.saveMessage(conversation, request.getMessage(), SenderType.CUSTOMER);

        // Process via AI
        String aiResponseText = aiAssistantService.processMessage(
                request.getMessage(),
                request.getLanguage(),
                TenantContext.getCurrentTenant()
        );

        // Save AI message
        ConversationMessage savedMessage = conversationService.saveMessage(conversation, aiResponseText, SenderType.AI);

        return ResponseEntity.ok(new ChatMessageResponse(
                savedMessage.getMessage(),
                savedMessage.getSenderType(),
                savedMessage.getMessageTimestamp()
        ));
    }

    @GetMapping("/history")
    public ResponseEntity<List<ChatMessageResponse>> getHistory(
            @RequestParam String identifier,
            @RequestParam(defaultValue = "ENGLISH") com.akarsha.chat.domain.LanguageMode language) {
        
        if (TenantContext.getCurrentTenant() == null) {
            TenantContext.setCurrentTenant("alpha");
        }

        Conversation conversation = conversationService.getOrCreateConversation(
                identifier,
                ChannelType.WEB_CHAT,
                language
        );

        List<ChatMessageResponse> history = conversationService.getMessages(conversation.getId())
                .stream()
                .map(m -> new ChatMessageResponse(m.getMessage(), m.getSenderType(), m.getMessageTimestamp()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(history);
    }
}
