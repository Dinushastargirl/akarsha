-- Create customers table
CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(100) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    phone VARCHAR(50) NOT NULL,
    email VARCHAR(255),
    birthday DATE,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Optimize queries by indexing tenant partitioning and searching fields
CREATE INDEX idx_customers_tenant ON customers(tenant_id);
CREATE INDEX idx_customers_search_name ON customers(tenant_id, full_name);
CREATE INDEX idx_customers_search_phone ON customers(tenant_id, phone);
