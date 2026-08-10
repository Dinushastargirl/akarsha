package com.akarsha.ai;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AiInteractionDTO {
    private Long id;
    private String guestIdentifier;
    private String channel;
    private String sessionId;
    private String languagePreference;
    private InteractionStatus status;
    private LocalDateTime lastActivity;
    private Integer unreadCount;
    private String assignedStaffName;
    private Long assignedStaffId;
    private List<AiMessageDTO> messages;
    
    public static AiInteractionDTO fromEntity(AiInteraction interaction) {
        AiInteractionDTO dto = new AiInteractionDTO();
        dto.setId(interaction.getId());
        dto.setGuestIdentifier(interaction.getGuestIdentifier());
        if (interaction.getCustomer() != null) {
            dto.setGuestIdentifier(interaction.getCustomer().getFullName());
        }
        dto.setChannel(interaction.getChannel());
        dto.setSessionId(interaction.getSessionId());
        dto.setLanguagePreference(interaction.getLanguagePreference());
        dto.setStatus(interaction.getStatus());
        dto.setLastActivity(interaction.getLastActivity());
        dto.setUnreadCount(interaction.getUnreadCount());
        if (interaction.getAssignedStaff() != null) {
            dto.setAssignedStaffId(interaction.getAssignedStaff().getId());
            dto.setAssignedStaffName(interaction.getAssignedStaff().getFullName());
        }
        return dto;
    }
}
