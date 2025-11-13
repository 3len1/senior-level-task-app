-- Add deadline, assignee and expiration notification tracking to tasks
ALTER TABLE tasks ADD COLUMN deadline TIMESTAMP NULL;
ALTER TABLE tasks ADD COLUMN expired_notified BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE tasks ADD COLUMN assignee_id BIGINT NULL;

-- Add FK to users
ALTER TABLE tasks
    ADD CONSTRAINT fk_tasks_assignee
        FOREIGN KEY (assignee_id) REFERENCES users(id) ON DELETE SET NULL;
