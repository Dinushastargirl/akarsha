-- Phase 3B: AI Receptionist Core

CREATE TABLE ai_configurations (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT,
    
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    assistant_name VARCHAR(255) NOT NULL DEFAULT 'Akarsha Assistant',
    greeting VARCHAR(1000) NOT NULL,
    supported_languages VARCHAR(255) NOT NULL,
    tone VARCHAR(255) NOT NULL,
    booking_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    cancellation_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    rescheduling_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    human_handoff_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    business_context VARCHAR(5000),
    system_instructions VARCHAR(5000),
    provider_name VARCHAR(255) NOT NULL DEFAULT 'mock'
);

CREATE TABLE ai_interactions (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT,
    
    customer_id BIGINT REFERENCES customers(id),
    guest_identifier VARCHAR(255),
    channel VARCHAR(255) NOT NULL,
    session_id VARCHAR(255) NOT NULL UNIQUE,
    language_preference VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    metadata VARCHAR(2000),
    last_activity TIMESTAMP NOT NULL
);

CREATE TABLE ai_messages (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT,
    
    interaction_id BIGINT NOT NULL REFERENCES ai_interactions(id),
    sender_type VARCHAR(50) NOT NULL,
    content VARCHAR(5000) NOT NULL,
    timestamp TIMESTAMP NOT NULL
);

CREATE INDEX idx_ai_config_tenant ON ai_configurations(tenant_id);
CREATE INDEX idx_ai_interact_tenant ON ai_interactions(tenant_id);
CREATE INDEX idx_ai_interact_session ON ai_interactions(session_id);
CREATE INDEX idx_ai_message_tenant ON ai_messages(tenant_id);
CREATE INDEX idx_ai_message_interaction ON ai_messages(interaction_id);
