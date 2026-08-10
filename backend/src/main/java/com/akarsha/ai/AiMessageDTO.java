package com.akarsha.ai;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AiMessageDTO {
    private Long id;
    private String senderType;
    private String content;
    private LocalDateTime timestamp;
    private String externalId;
    private String deliveryStatus;
    
    public static AiMessageDTO fromEntity(AiMessage message) {
        AiMessageDTO dto = new AiMessageDTO();
        dto.setId(message.getId());
        dto.setSenderType(message.getSenderType().name());
        dto.setContent(message.getContent());
        dto.setTimestamp(message.getTimestamp());
        dto.setExternalId(message.getExternalId());
        dto.setDeliveryStatus(message.getDeliveryStatus());
        return dto;
    }
}
