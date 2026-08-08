-- Add status to salons
ALTER TABLE salons 
ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';

-- Platform Subscription Plans
CREATE TABLE subscription_plans (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL, -- FREE, STARTER, PROFESSIONAL, ENTERPRISE
    max_staff INT NOT NULL DEFAULT 1,
    max_customers INT NOT NULL DEFAULT 50,
    max_monthly_appointments INT NOT NULL DEFAULT 100,
    max_ai_messages INT NOT NULL DEFAULT 0,
    max_whatsapp_messages INT NOT NULL DEFAULT 0,
    price_cents INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Seed basic plans
INSERT INTO subscription_plans (name, max_staff, max_customers, max_monthly_appointments, max_ai_messages, max_whatsapp_messages, price_cents) VALUES
('FREE', 1, 50, 100, 0, 0, 0),
('STARTER', 3, 500, 500, 100, 0, 2900),
('PROFESSIONAL', 10, 5000, 5000, 1000, 500, 9900),
('ENTERPRISE', 9999, 99999, 99999, 10000, 5000, 29900);

-- Tenant Subscriptions
CREATE TABLE tenant_subscriptions (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(100) UNIQUE NOT NULL, -- The subdomain from salons
    plan_id BIGINT NOT NULL REFERENCES subscription_plans(id),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE', -- TRIAL, ACTIVE, PAST_DUE, CANCELLED, EXPIRED
    start_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    renewal_date TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Seed initial subscriptions for our demo salons
INSERT INTO tenant_subscriptions (tenant_id, plan_id) VALUES 
('alpha', (SELECT id FROM subscription_plans WHERE name = 'PROFESSIONAL')),
('beta', (SELECT id FROM subscription_plans WHERE name = 'FREE'));

-- Tenant Feature Flags
CREATE TABLE tenant_features (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(100) NOT NULL,
    feature_name VARCHAR(100) NOT NULL, -- AI_RECEPTIONIST, WHATSAPP, ONLINE_BOOKING, BILLING
    is_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_tenant_feature UNIQUE (tenant_id, feature_name)
);

-- Seed initial features
INSERT INTO tenant_features (tenant_id, feature_name, is_enabled) VALUES
('alpha', 'AI_RECEPTIONIST', TRUE),
('alpha', 'WHATSAPP', TRUE),
('alpha', 'ONLINE_BOOKING', TRUE),
('alpha', 'BILLING', TRUE),
('beta', 'ONLINE_BOOKING', TRUE);

-- Platform Audit Logs
CREATE TABLE platform_audit_logs (
    id BIGSERIAL PRIMARY KEY,
    actor_email VARCHAR(255) NOT NULL,
    action VARCHAR(100) NOT NULL,
    target_tenant_id VARCHAR(100),
    metadata_json TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
