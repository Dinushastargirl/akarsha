package com.akarsha.ai;

import com.akarsha.customer.Customer;
import com.akarsha.core.entity.TenantAwareEntity;
import com.akarsha.core.entity.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ai_interactions")
public class AiInteraction extends TenantAwareEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(name = "guest_identifier")
    private String guestIdentifier;

    @Column(name = "channel")
    private String channel = "WEB_CHAT";

    @Column(name = "session_id", unique = true, nullable = false)
    private String sessionId = UUID.randomUUID().toString();

    @Column(name = "language_preference")
    private String languagePreference = "English";

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private InteractionStatus status = InteractionStatus.ACTIVE;

    @Column(name = "metadata", length = 2000)
    private String metadata;
    
    @Column(name = "last_activity")
    private LocalDateTime lastActivity = LocalDateTime.now();

    @Column(name = "unread_count", nullable = false)
    private Integer unreadCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_staff_id")
    private User assignedStaff;

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public String getGuestIdentifier() { return guestIdentifier; }
    public void setGuestIdentifier(String guestIdentifier) { this.guestIdentifier = guestIdentifier; }

    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getLanguagePreference() { return languagePreference; }
    public void setLanguagePreference(String languagePreference) { this.languagePreference = languagePreference; }

    @Transient
    public AiLanguage getLanguage() {
        return AiLanguage.fromCode(this.languagePreference);
    }
    
    public void setLanguage(AiLanguage language) {
        this.languagePreference = language.getCode();
    }

    public InteractionStatus getStatus() { return status; }
    public void setStatus(InteractionStatus status) { this.status = status; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    public LocalDateTime getLastActivity() { return lastActivity; }
    public void setLastActivity(LocalDateTime lastActivity) { this.lastActivity = lastActivity; }

    public Integer getUnreadCount() { return unreadCount; }
    public void setUnreadCount(Integer unreadCount) { this.unreadCount = unreadCount; }

    public User getAssignedStaff() { return assignedStaff; }
    public void setAssignedStaff(User assignedStaff) { this.assignedStaff = assignedStaff; }
}
