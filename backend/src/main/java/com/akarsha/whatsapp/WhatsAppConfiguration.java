package com.akarsha.whatsapp;

import com.akarsha.core.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "whatsapp_configurations")
@Getter
@Setter
public class WhatsAppConfiguration extends TenantAwareEntity {

    @Column(name = "phone_number_id", nullable = false, unique = true)
    private String phoneNumberId;

    @Column(name = "waba_id", nullable = false)
    private String wabaId;

    @jakarta.persistence.Convert(converter = com.akarsha.core.security.EncryptedStringConverter.class)
    @Column(name = "access_token", nullable = false, length = 1000)
    private String accessToken;

    @Column(name = "display_phone_number")
    private String displayPhoneNumber;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "webhook_verified", nullable = false)
    private boolean webhookVerified = false;
}
