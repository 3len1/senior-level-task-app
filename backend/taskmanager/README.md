# Taskmanager Backend — Run Guide

This README covers how to run the Spring Boot backend, how to use Docker Compose from the project root, and how to control Spring Boot’s Docker Compose integration to avoid conflicts (like containers already running).

## Quick start (without Docker auto-start)
- From the project root:
  ```powershell
  cd backend/taskmanager
  mvnw.cmd spring-boot:run
  ```
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs
- Actuator Health: http://localhost:8080/actuator/health
- Actuator Info: http://localhost:8080/actuator/info
- Actuator Metrics (list): http://localhost:8080/actuator/metrics

If your app needs Postgres/other services, either run them manually (Docker or native) or use the module compose below (`backend/taskmanager/compose.yaml`).

## Run services with Docker Compose (module compose)
There is a Compose file for backend services at `backend/taskmanager/compose.yaml` (starts Postgres).

- From the project root:
  ```powershell
  docker compose -f backend/taskmanager/compose.yaml up -d
  ```
- Or from the module directory:
  ```powershell
  cd backend/taskmanager
  docker compose -f compose.yaml up -d
  ```
- Stop:
  ```powershell
  docker compose -f backend/taskmanager/compose.yaml down
  ```

Tip: Set a JWT secret for the app (PowerShell example):
```powershell
$env:APP_JWT_SECRET = "please-change-me-to-a-long-random-string"
```

## Spring Boot Docker Compose integration (optional)
Spring Boot 3.x can auto-run `docker compose` when the app starts. In this repo, the Compose file for backend services lives at `backend/taskmanager/compose.yaml` (Postgres only). Common pitfalls:
- Spring looks in the wrong directory for `compose.yaml` (wrong relative path).
- Containers already exist with the same name (`postgres-db`), or volumes belong to a different Compose project.

### Control the integration via properties
Add these to `src/main/resources/application.properties` (or set as program args/env vars):

- Disable auto compose (recommended default in dev):
  ```properties
  spring.docker.compose.enabled=false
  ```
  Program arg: `--spring.docker.compose.enabled=false`
  Env: `SPRING_DOCKER_COMPOSE_ENABLED=false`

- When you DO want Spring to auto-start using the module compose file (`backend/taskmanager/compose.yaml`):
  ```yaml
  spring:
    docker:
      compose:
        # Path resolved relative to this file's location (src/main/resources)
        file: ../../../../../backend/taskmanager/compose.yaml
        project-name: taskmanager
        lifecycle-management: start_only
  ```
  Note: The relative path above starts from `backend/taskmanager/src/main/resources` and navigates back to the repo root, then down to `backend/taskmanager/compose.yaml`. On Windows, forward slashes are fine.

- Avoid recreating already existing/running containers:
  ```properties
  spring.docker.compose.start.command=up
  spring.docker.compose.start.arguments=--no-recreate --no-build -d
  ```
  This corresponds to `docker compose up --no-recreate --no-build -d`.

- Keep containers running on app shutdown (optional):
  ```properties
  spring.docker.compose.stop.command=none
  ```
  Alternative values: `stop`, `down` (default behavior is to stop/down depending on version—use `none` to leave them running).

### Use a dedicated profile
A good team workflow is to disable by default and enable only when requested:

- `application.properties` (default):
  ```properties
  spring.docker.compose.enabled=false
  ```
- `application-compose.properties`:
  ```properties
  spring.docker.compose.enabled=true
  spring.docker.compose.file=compose.yaml
  spring.docker.compose.start.command=up
  spring.docker.compose.start.arguments=--no-recreate --no-build -d
  ```
- Run with profile:
  ```powershell
  mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=compose
  # or program arg: --spring.profiles.active=compose
  ```

## Resolving common Docker errors
- Conflict: container name already in use (e.g., `/postgres-db`)
  - Remove the existing container:
    ```powershell
    docker rm -f postgres-db
    ```
  - Or avoid hard-coding `container_name` in `compose.yaml` so Compose can namespace it per project automatically.
  - Or run with a project name to avoid collisions:
    ```powershell
    docker compose -p senior-level-task-app up -d
    ```

- Warning about an existing volume created for a different project:
  - Declare it as external in `compose.yaml` if you intend to reuse it:
    ```yaml
    volumes:
      taskmanager_pgdata:
        external: true
    ```
  - Or remove/rename the volume:
    ```powershell
    docker volume rm taskmanager_pgdata
    ```

## Environment configuration
Set JWT secret in `application.yml` or via env var:
```yaml
app:
  jwt:
    secret: please-change-me-to-a-long-random-string
    expiration-ms: 86400000
```

PowerShell (temporary for the session):
```powershell
$env:APP_JWT_SECRET = "please-change-me-to-a-long-random-string"
```

## Run tests / build
```powershell
mvnw.cmd test
mvnw.cmd clean package
```

Jar output is in `backend/taskmanager/target/`.
