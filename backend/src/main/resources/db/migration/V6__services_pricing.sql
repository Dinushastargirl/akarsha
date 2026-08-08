-- Alter services to support activation/deactivation and archiving
ALTER TABLE services ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;
