# Senior-Level Task App

A full‑stack task management application with a Spring Boot backend and a React (Webpack) frontend. Users can register/login (JWT), create projects and tasks, and see task updates in real time via WebSockets (STOMP over SockJS).

## Table of contents
- Project overview
- Prerequisites
- Setup instructions
  - Backend (Spring Boot)
  - Frontend (React)
  - Optional: Start Postgres via Docker Compose
- Real-time collaboration (how it works)
- Useful URLs
- Troubleshooting

---

## Project overview
- Backend: Spring Boot app under `backend/taskmanager`
  - REST API, JWT authentication, roles (`ROLE_USER`, `ROLE_ADMIN`)
  - PostgreSQL database
  - Swagger/OpenAPI available for easy testing
  - WebSocket endpoint for realtime updates
- Frontend: React app under `frontend`
  - Redux Toolkit + Redux-Saga
  - MUI (Material UI) components
  - Webpack dev server with proxy to the backend
- Realtime: STOMP over SockJS at `/ws` with topic `/topic/projects/{projectId}/tasks` for task events

---

## Prerequisites
- Java 17+
- Node.js 18+ and npm (or pnpm/yarn)
- Docker Desktop (optional, used to run PostgreSQL via Docker Compose)

---

## Setup instructions

### 1) Backend (Spring Boot)
From the project root:
```powershell
cd backend/taskmanager
```

Configure JWT secret (recommended in dev to avoid startup issues). You can set it in `application.yml` or via env var:
```yaml
# backend/taskmanager/src/main/resources/application.yml
app:
  jwt:
    secret: "please-change-me-to-a-long-random-string"
    expiration-ms: 86400000
```
Or via environment variable (PowerShell example):
```powershell
$env:APP_JWT_SECRET="please-change-me-to-a-long-random-string"
```

Run the backend on port 8080:
- Windows:
  ```powershell
  mvnw.cmd spring-boot:run
  ```
- macOS/Linux:
  ```bash
  ./mvnw spring-boot:run
  ```

Run backend tests/build:
```powershell
# tests
mvnw.cmd test
# build jar
mvnw.cmd clean package
```
Jar output will be under `backend/taskmanager/target/`.

Database connection (default dev settings) is configured for local Postgres at `jdbc:postgresql://localhost:5432/taskdb` with user `taskuser` and password `taskpass`. Use the Compose setup (below) to start a matching Postgres quickly.

### 2) Frontend (React + Webpack)
From the project root:
```powershell
cd frontend
npm install
npm start
```
The dev server runs at http://localhost:3000 and proxies API and WebSocket traffic to the backend on http://localhost:8080.

- Proxy routes (configured in `frontend/webpack.config.js`):
  - HTTP API: `/api` → `http://localhost:8080` (with `pathRewrite` removing `/api`)
  - WebSocket: `/ws` → `http://localhost:8080`

Build for production:
```powershell
npm run build
```

### 3) Optional: Start Postgres via Docker Compose
A Compose file for backend services (Postgres) is provided at `backend/taskmanager/compose.yaml`.

From the project root:
```powershell
docker compose -f backend/taskmanager/compose.yaml up -d
```
Stop services:
```powershell
docker compose -f backend/taskmanager/compose.yaml down
```
Notes:
- This Compose file starts the database only (Postgres). The Spring Boot app runs locally on port 8080.
- Named volume `taskmanager_pgdata` is declared as external for reuse across runs.

---

## Real-time collaboration (how it works)
- Transport: STOMP over SockJS.
- Server endpoint: WebSocket handshake at `/ws` (Spring Boot). After connecting, clients subscribe to topics.
- Topic for task updates: `/topic/projects/{projectId}/tasks`
  - When a task is created/updated/deleted in project `P`, the backend publishes a message to `/topic/projects/P/tasks`.
  - All connected clients subscribed to that topic receive the update immediately.

### Frontend client
The frontend uses `@stomp/stompjs` with `sockjs-client` and connects like this (simplified):
```js
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

const client = new Client({
  webSocketFactory: () => new SockJS('/ws'), // proxied by webpack-dev-server
  reconnectDelay: 5000,
});

client.onConnect = () => {
  client.subscribe('/topic/projects/1/tasks', (msg) => {
    const event = JSON.parse(msg.body);
    // update UI state with the event
  });
};

client.activate();
```

### Backend publisher
On task changes (create/update/delete), the backend sends messages to `/topic/projects/{projectId}/tasks`. Clients subscribed to the project’s topic see the updates with no page refresh.

---

## Useful URLs
- Backend API base: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs
- WebSocket endpoint: `ws://localhost:8080/ws` (through the dev server: `http://localhost:3000/ws` → proxy to backend)
- Frontend (dev): http://localhost:3000

---

## Troubleshooting
- Docker container name conflict (`/postgres-db` already in use):
  - Ensure you use the provided compose file and project naming. If needed, remove stray containers:
    ```powershell
    docker rm -f postgres-db
    ```
- Volume warning (existing volume from different project):
  - The compose file declares `pgdata` as external with a fixed name `taskmanager_pgdata` to enable reuse.
- Webpack dev server proxy errors:
  - Ensure backend is running on `http://localhost:8080`.
  - The proxy is defined as an array in `webpack.config.js` (required for webpack-dev-server v5).
- WebSocket `ECONNRESET` during startup:
  - Occurs if the backend isn’t up yet. Start the backend first, then the frontend.
- JWT errors:
  - Ensure `app.jwt.secret` is at least 32 characters long.

---

## License
For assessment purposes. 
