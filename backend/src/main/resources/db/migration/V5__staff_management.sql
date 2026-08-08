-- Alter users to support operational staff properties
ALTER TABLE users ADD COLUMN phone VARCHAR(50);
ALTER TABLE users ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;

-- Create staff schedules table
CREATE TABLE staff_schedules (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(100) NOT NULL,
    staff_id BIGINT NOT NULL,
    day_of_week INT NOT NULL, -- 1 = Monday, 7 = Sunday
    working BOOLEAN NOT NULL DEFAULT TRUE,
    start_time TIME NOT NULL DEFAULT '09:00:00',
    end_time TIME NOT NULL DEFAULT '18:00:00',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_schedule_staff FOREIGN KEY (staff_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_staff_schedule_day UNIQUE (staff_id, day_of_week)
);

-- Optimize schedule checks
CREATE INDEX idx_staff_schedules_tenant ON staff_schedules(tenant_id);

-- Create many-to-many join table for staff service assignments
CREATE TABLE staff_services (
    tenant_id VARCHAR(100) NOT NULL,
    staff_id BIGINT NOT NULL,
    service_id BIGINT NOT NULL,
    
    PRIMARY KEY (staff_id, service_id),
    CONSTRAINT fk_staff_services_staff FOREIGN KEY (staff_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_staff_services_service FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE CASCADE
);

-- Optimize assigned services queries
CREATE INDEX idx_staff_services_tenant ON staff_services(tenant_id);
