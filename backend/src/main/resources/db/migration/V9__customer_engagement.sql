-- Migration for Customer Engagement & Chatbot module

CREATE TABLE conversations (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    customer_id BIGINT NOT NULL,
    channel VARCHAR(50) NOT NULL, -- e.g., WEB_CHAT, WHATSAPP
    language VARCHAR(50) NOT NULL DEFAULT 'ENGLISH',
    status VARCHAR(50) NOT NULL DEFAULT 'AI', -- e.g., AI, HUMAN, CLOSED
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_conversation_customer FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
);

CREATE INDEX idx_conversations_tenant ON conversations(tenant_id);
CREATE INDEX idx_conversations_customer ON conversations(customer_id);

CREATE TABLE conversation_messages (
    id BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    sender_type VARCHAR(50) NOT NULL, -- CUSTOMER, AI, HUMAN_STAFF, SYSTEM
    message TEXT NOT NULL,
    message_timestamp TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    metadata JSONB,
    CONSTRAINT fk_message_conversation FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE
);

CREATE INDEX idx_conversation_messages_conversation ON conversation_messages(conversation_id);
CREATE INDEX idx_conversation_messages_timestamp ON conversation_messages(message_timestamp);
