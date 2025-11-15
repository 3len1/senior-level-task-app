-- Add deadline and expiration notification tracking to tasks
ALTER TABLE tasks ADD COLUMN deadline TIMESTAMP NULL;
ALTER TABLE tasks ADD COLUMN expired_notified BOOLEAN NOT NULL DEFAULT FALSE;