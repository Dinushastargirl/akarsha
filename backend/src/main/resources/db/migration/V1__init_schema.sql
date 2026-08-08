-- Let's create salons (tenants)
CREATE TABLE salons (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    subdomain VARCHAR(100) UNIQUE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Let's create users
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(100) NOT NULL,
    username VARCHAR(150) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_users_tenant_email UNIQUE (tenant_id, email)
);

CREATE INDEX idx_users_tenant ON users(tenant_id);

-- Seed Initial Salons
INSERT INTO salons (name, subdomain) VALUES 
('Salon Alpha', 'alpha'),
('Salon Beta', 'beta');

-- Seed Initial Users (Password: password123, BCrypt hash: $2a$10$dYIvd0JOu5bdNi/AhIS2BuFUQll5iToGNNtModCXTXn26s/8yskme)
-- Hash generated using standard BCrypt
INSERT INTO users (tenant_id, username, email, password_hash, role) VALUES
('platform-system', 'platform_admin', 'admin@akarsha.com', '$2a$10$dYIvd0JOu5bdNi/AhIS2BuFUQll5iToGNNtModCXTXn26s/8yskme', 'SUPER_ADMIN'),
('alpha', 'alpha_owner', 'owner@alpha.com', '$2a$10$dYIvd0JOu5bdNi/AhIS2BuFUQll5iToGNNtModCXTXn26s/8yskme', 'SALON_OWNER'),
('alpha', 'alpha_manager', 'manager@alpha.com', '$2a$10$dYIvd0JOu5bdNi/AhIS2BuFUQll5iToGNNtModCXTXn26s/8yskme', 'MANAGER'),
('alpha', 'alpha_receptionist', 'receptionist@alpha.com', '$2a$10$dYIvd0JOu5bdNi/AhIS2BuFUQll5iToGNNtModCXTXn26s/8yskme', 'RECEPTIONIST'),
('alpha', 'alpha_staff', 'staff@alpha.com', '$2a$10$dYIvd0JOu5bdNi/AhIS2BuFUQll5iToGNNtModCXTXn26s/8yskme', 'STAFF'),
('beta', 'beta_owner', 'owner@beta.com', '$2a$10$dYIvd0JOu5bdNi/AhIS2BuFUQll5iToGNNtModCXTXn26s/8yskme', 'SALON_OWNER');
