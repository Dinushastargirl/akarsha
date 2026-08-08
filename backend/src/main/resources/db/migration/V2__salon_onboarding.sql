-- Alter users table to support onboarding phase
ALTER TABLE users ALTER COLUMN tenant_id DROP NOT NULL;
ALTER TABLE users ADD COLUMN full_name VARCHAR(255);

-- Ensure user email is unique globally to prevent duplicate registrations
ALTER TABLE users ADD CONSTRAINT uq_users_email UNIQUE (email);

-- Alter salons table to store setup and operational details
ALTER TABLE salons ADD COLUMN phone VARCHAR(50);
ALTER TABLE salons ADD COLUMN address TEXT;
ALTER TABLE salons ADD COLUMN city VARCHAR(100);
ALTER TABLE salons ADD COLUMN logo_url VARCHAR(255);
ALTER TABLE salons ADD COLUMN business_type VARCHAR(50);
ALTER TABLE salons ADD COLUMN opening_time VARCHAR(10);
ALTER TABLE salons ADD COLUMN closing_time VARCHAR(10);
ALTER TABLE salons ADD COLUMN setup_completed BOOLEAN DEFAULT FALSE;

-- Create services table for salon offerings
CREATE TABLE services (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    price NUMERIC(10, 2) NOT NULL,
    duration_minutes INT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_services_tenant ON services(tenant_id);
