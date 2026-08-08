-- Create appointments table
CREATE TABLE appointments (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(100) NOT NULL,
    customer_id BIGINT NOT NULL,
    service_id BIGINT NOT NULL,
    staff_id BIGINT NOT NULL,
    appointment_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'BOOKED',
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    
    CONSTRAINT fk_appointments_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT fk_appointments_service FOREIGN KEY (service_id) REFERENCES services(id),
    CONSTRAINT fk_appointments_staff FOREIGN KEY (staff_id) REFERENCES users(id)
);

-- Optimize typical operational scheduler queries
CREATE INDEX idx_appointments_tenant_date ON appointments(tenant_id, appointment_date);
CREATE INDEX idx_appointments_staff_date ON appointments(tenant_id, staff_id, appointment_date);
CREATE INDEX idx_appointments_customer ON appointments(tenant_id, customer_id);
CREATE INDEX idx_appointments_status ON appointments(tenant_id, status);
