CREATE TABLE whatsapp_configurations (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(255) NOT NULL,
    phone_number_id VARCHAR(255) NOT NULL UNIQUE,
    waba_id VARCHAR(255) NOT NULL,
    access_token VARCHAR(1000) NOT NULL,
    display_phone_number VARCHAR(50),
    enabled BOOLEAN DEFAULT true,
    webhook_verified BOOLEAN DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_whatsapp_tenant ON whatsapp_configurations(tenant_id);
CREATE INDEX idx_whatsapp_phone_id ON whatsapp_configurations(phone_number_id);

ALTER TABLE ai_messages ADD COLUMN external_id VARCHAR(255) UNIQUE;
ALTER TABLE ai_messages ADD COLUMN delivery_status VARCHAR(50);
