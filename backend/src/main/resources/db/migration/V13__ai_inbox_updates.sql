-- Phase 3C: Salon Inbox, Conversations & Human Handoff

ALTER TABLE ai_interactions ADD COLUMN unread_count INT NOT NULL DEFAULT 0;
ALTER TABLE ai_interactions ADD COLUMN assigned_staff_id BIGINT REFERENCES users(id);
