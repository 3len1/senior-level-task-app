-- Add MODERATOR role to users.role check constraint in a forward-only, idempotent way
-- Note: Flyway runs in order; this migration assumes V1__init.sql created users_role_ck

-- PostgreSQL syntax
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.table_constraints tc
        WHERE tc.constraint_name = 'users_role_ck'
          AND tc.table_name = 'users'
          AND tc.constraint_type = 'CHECK'
    ) THEN
        EXECUTE 'ALTER TABLE users DROP CONSTRAINT users_role_ck';
    END IF;

    EXECUTE 'ALTER TABLE users ADD CONSTRAINT users_role_ck CHECK (role IN (''ROLE_USER'',''ROLE_ADMIN'',''ROLE_MODERATOR''))';
END $$;