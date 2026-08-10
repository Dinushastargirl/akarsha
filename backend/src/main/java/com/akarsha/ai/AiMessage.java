package com.akarsha.ai;

import com.akarsha.core.entity.TenantAwareEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_messages")
public class AiMessage extends TenantAwareEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interaction_id", nullable = false)
    private AiInteraction interaction;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender_type", nullable = false)
    private MessageSender senderType;

    @Column(name = "content", length = 5000, nullable = false)
    private String content;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    @Column(name = "external_id", unique = true)
    private String externalId;

    @Column(name = "delivery_status", length = 50)
    private String deliveryStatus;

    public AiInteraction getInteraction() { return interaction; }
    public void setInteraction(AiInteraction interaction) { this.interaction = interaction; }

    public MessageSender getSenderType() { return senderType; }
    public void setSenderType(MessageSender senderType) { this.senderType = senderType; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }

    public String getDeliveryStatus() { return deliveryStatus; }
    public void setDeliveryStatus(String deliveryStatus) { this.deliveryStatus = deliveryStatus; }
}
