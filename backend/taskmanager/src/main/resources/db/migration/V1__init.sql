-- Users
CREATE TABLE IF NOT EXISTS users (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username     VARCHAR(100)  NOT NULL UNIQUE,
    password     VARCHAR(255)  NOT NULL,
    role         VARCHAR(50)   NOT NULL,
    CONSTRAINT users_role_ck CHECK (role IN ('ROLE_USER','ROLE_ADMIN'))
);

-- Projects
CREATE TABLE IF NOT EXISTS projects (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name          VARCHAR(150) NOT NULL,
    description   TEXT,
    created_date  TIMESTAMP    NOT NULL DEFAULT NOW()  -- store UTC instants
);

-- Tasks
CREATE TABLE IF NOT EXISTS tasks (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title       VARCHAR(150) NOT NULL,
    description TEXT,
    status      VARCHAR(30)  NOT NULL,
    project_id  BIGINT       NOT NULL,
    CONSTRAINT tasks_status_ck CHECK (status IN ('TODO','IN_PROGRESS','DONE')),
    CONSTRAINT tasks_project_fk
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);

-- Helpful indexes
CREATE INDEX IF NOT EXISTS idx_tasks_project_id ON tasks(project_id);
CREATE INDEX IF NOT EXISTS idx_tasks_project_status ON tasks(project_id, status);