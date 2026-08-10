package com.akarsha.ai;

import com.akarsha.core.entity.User;
import com.akarsha.core.repository.UserRepository;
import com.akarsha.tenant.TenantContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/inbox/conversations")
@PreAuthorize("hasAnyRole('SALON_OWNER', 'MANAGER', 'RECEPTIONIST')")
public class InboxController {

    private final ExtendedAiInteractionRepository interactionRepository;
    private final AiMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final com.akarsha.whatsapp.WhatsAppClient whatsAppClient;
    private final com.akarsha.whatsapp.WhatsAppConfigurationRepository whatsAppConfigRepository;

    public InboxController(ExtendedAiInteractionRepository interactionRepository,
                           AiMessageRepository messageRepository,
                           UserRepository userRepository,
                           com.akarsha.whatsapp.WhatsAppClient whatsAppClient,
                           com.akarsha.whatsapp.WhatsAppConfigurationRepository whatsAppConfigRepository) {
        this.interactionRepository = interactionRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.whatsAppClient = whatsAppClient;
        this.whatsAppConfigRepository = whatsAppConfigRepository;
    }

    @GetMapping
    public ResponseEntity<Page<AiInteractionDTO>> listConversations(
            @RequestParam(required = false) InteractionStatus status,
            @RequestParam(required = false) Boolean unread,
            @RequestParam(required = false) String channel,
            Pageable pageable) {
        
        String tenantId = TenantContext.getCurrentTenant();
        Page<AiInteraction> interactions = interactionRepository.findFiltered(tenantId, status, unread, channel, pageable);
        
        Page<AiInteractionDTO> dtos = interactions.map(interaction -> {
            AiInteractionDTO dto = AiInteractionDTO.fromEntity(interaction);
            // Include latest message for the list view
            List<AiMessage> messages = messageRepository.findByInteractionIdOrderByTimestampAsc(interaction.getId());
            if (!messages.isEmpty()) {
                dto.setMessages(List.of(AiMessageDTO.fromEntity(messages.get(messages.size() - 1))));
            }
            return dto;
        });
        
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AiInteractionDTO> getConversation(@PathVariable Long id) {
        return interactionRepository.findById(id).map(interaction -> {
            // Clear unread count when opened by staff
            if (interaction.getUnreadCount() > 0) {
                interaction.setUnreadCount(0);
                interactionRepository.save(interaction);
            }
            
            AiInteractionDTO dto = AiInteractionDTO.fromEntity(interaction);
            List<AiMessage> messages = messageRepository.findByInteractionIdOrderByTimestampAsc(interaction.getId());
            dto.setMessages(messages.stream().map(AiMessageDTO::fromEntity).collect(Collectors.toList()));
            return ResponseEntity.ok(dto);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<AiMessageDTO> sendReply(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        return interactionRepository.findById(id).map(interaction -> {
            String content = payload.get("message");
            if (content == null || content.trim().isEmpty()) {
                throw new IllegalArgumentException("Message cannot be empty");
            }
            
            // Mark as HANDED_OFF if staff replies manually and it's active
            if (interaction.getStatus() == InteractionStatus.ACTIVE || interaction.getStatus() == InteractionStatus.WAITING_FOR_STAFF) {
                interaction.setStatus(InteractionStatus.HANDED_OFF);
                interactionRepository.save(interaction);
            }

            AiMessage msg = new AiMessage();
            msg.setInteraction(interaction);
            msg.setSenderType(MessageSender.STAFF);
            msg.setContent(content);
            AiMessage saved = messageRepository.save(msg);
            
            if ("WHATSAPP".equals(interaction.getChannel())) {
                whatsAppConfigRepository.findByTenantId(TenantContext.getCurrentTenant())
                        .filter(com.akarsha.whatsapp.WhatsAppConfiguration::isEnabled)
                        .ifPresent(config -> {
                            String sentMsgId = whatsAppClient.sendMessage(interaction.getGuestIdentifier(), content, config);
                            if (sentMsgId != null) {
                                saved.setExternalId(sentMsgId);
                                saved.setDeliveryStatus("SENT");
                                messageRepository.save(saved);
                            }
                        });
            }
            
            return ResponseEntity.ok(AiMessageDTO.fromEntity(saved));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/takeover")
    public ResponseEntity<AiInteractionDTO> takeover(@PathVariable Long id) {
        return interactionRepository.findById(id).map(interaction -> {
            interaction.setStatus(InteractionStatus.HANDED_OFF);
            interaction.setUnreadCount(0); // clear unread on takeover
            return ResponseEntity.ok(AiInteractionDTO.fromEntity(interactionRepository.save(interaction)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/return-to-ai")
    public ResponseEntity<AiInteractionDTO> returnToAi(@PathVariable Long id) {
        return interactionRepository.findById(id).map(interaction -> {
            interaction.setStatus(InteractionStatus.ACTIVE);
            return ResponseEntity.ok(AiInteractionDTO.fromEntity(interactionRepository.save(interaction)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<AiInteractionDTO> resolve(@PathVariable Long id) {
        return interactionRepository.findById(id).map(interaction -> {
            interaction.setStatus(InteractionStatus.RESOLVED);
            interaction.setUnreadCount(0);
            return ResponseEntity.ok(AiInteractionDTO.fromEntity(interactionRepository.save(interaction)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/assign")
    public ResponseEntity<AiInteractionDTO> assign(@PathVariable Long id, @RequestBody Map<String, Long> payload) {
        return interactionRepository.findById(id).map(interaction -> {
            Long staffId = payload.get("staffId");
            if (staffId == null) {
                interaction.setAssignedStaff(null);
            } else {
                User staff = userRepository.findById(staffId)
                        .orElseThrow(() -> new IllegalArgumentException("Staff not found"));
                interaction.setAssignedStaff(staff);
            }
            return ResponseEntity.ok(AiInteractionDTO.fromEntity(interactionRepository.save(interaction)));
        }).orElse(ResponseEntity.notFound().build());
    }
}
