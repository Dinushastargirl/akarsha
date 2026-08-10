package com.akarsha.ai;

import com.akarsha.core.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "ai_configurations")
public class AiConfiguration extends TenantAwareEntity {

    private boolean enabled = false;
    
    @Column(name = "assistant_name")
    private String assistantName = "Akarsha Assistant";

    @Column(length = 1000)
    private String greeting = "Hello! I am your AI receptionist. How can I help you today?";

    @Column(name = "supported_languages")
    private String supportedLanguages = "English,සිංහල,தமிழ்,Singlish,Tanglish";

    private String tone = "Professional, helpful, and concise.";

    @Column(name = "booking_enabled")
    private boolean bookingEnabled = true;

    @Column(name = "cancellation_enabled")
    private boolean cancellationEnabled = true;

    @Column(name = "rescheduling_enabled")
    private boolean reschedulingEnabled = true;

    @Column(name = "human_handoff_enabled")
    private boolean humanHandoffEnabled = true;

    @Column(name = "business_context", length = 5000)
    private String businessContext;

    @Column(name = "system_instructions", length = 5000)
    private String systemInstructions;

    @Column(name = "provider_name")
    private String providerName = "mock";

    // Getters and Setters

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    
    public String getAssistantName() { return assistantName; }
    public void setAssistantName(String assistantName) { this.assistantName = assistantName; }
    
    public String getGreeting() { return greeting; }
    public void setGreeting(String greeting) { this.greeting = greeting; }
    
    public String getSupportedLanguages() { return supportedLanguages; }
    public void setSupportedLanguages(String supportedLanguages) { this.supportedLanguages = supportedLanguages; }
    
    public String getTone() { return tone; }
    public void setTone(String tone) { this.tone = tone; }
    
    public boolean isBookingEnabled() { return bookingEnabled; }
    public void setBookingEnabled(boolean bookingEnabled) { this.bookingEnabled = bookingEnabled; }
    
    public boolean isCancellationEnabled() { return cancellationEnabled; }
    public void setCancellationEnabled(boolean cancellationEnabled) { this.cancellationEnabled = cancellationEnabled; }
    
    public boolean isReschedulingEnabled() { return reschedulingEnabled; }
    public void setReschedulingEnabled(boolean reschedulingEnabled) { this.reschedulingEnabled = reschedulingEnabled; }
    
    public boolean isHumanHandoffEnabled() { return humanHandoffEnabled; }
    public void setHumanHandoffEnabled(boolean humanHandoffEnabled) { this.humanHandoffEnabled = humanHandoffEnabled; }
    
    public String getBusinessContext() { return businessContext; }
    public void setBusinessContext(String businessContext) { this.businessContext = businessContext; }
    
    public String getSystemInstructions() { return systemInstructions; }
    public void setSystemInstructions(String systemInstructions) { this.systemInstructions = systemInstructions; }
    
    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }
}
