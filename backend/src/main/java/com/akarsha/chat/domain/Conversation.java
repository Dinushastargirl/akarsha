package com.akarsha.chat.domain;

import com.akarsha.core.entity.TenantAwareEntity;
import com.akarsha.customer.Customer;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

@Entity
@Table(name = "conversations")
@Getter
@Setter
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public class Conversation extends TenantAwareEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChannelType channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LanguageMode language = LanguageMode.ENGLISH;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConversationStatus status = ConversationStatus.AI;
}
