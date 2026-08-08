package com.akarsha.chat;

import com.akarsha.chat.domain.Conversation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/conversations")
@PreAuthorize("hasAnyRole('SALON_OWNER', 'MANAGER', 'RECEPTIONIST')")
public class SalonConversationController {

    private final ConversationRepository conversationRepository;

    public SalonConversationController(ConversationRepository conversationRepository) {
        this.conversationRepository = conversationRepository;
    }

    @GetMapping
    public List<Conversation> getConversations() {
        return conversationRepository.findAll();
    }
}
