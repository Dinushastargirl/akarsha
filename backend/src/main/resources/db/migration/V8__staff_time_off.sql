CREATE TABLE staff_time_off (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    staff_id BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    reason VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_staff_time_off_staff FOREIGN KEY (staff_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_staff_time_off_tenant ON staff_time_off(tenant_id);
CREATE INDEX idx_staff_time_off_staff ON staff_time_off(staff_id);

ALTER TABLE staff_schedules ADD COLUMN break_start_time TIME;
ALTER TABLE staff_schedules ADD COLUMN break_end_time TIME;
