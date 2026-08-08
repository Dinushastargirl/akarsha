-- Add password_hash to customers table for customer portal login
ALTER TABLE customers ADD COLUMN password_hash VARCHAR(255);
