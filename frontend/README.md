# Senior-Level Task App — Frontend (React + MUI)

Modern React frontend for the Task Management system. It provides JWT-authenticated task/project management with real-time updates via STOMP over WebSockets. Built with React 18, Material UI 6, Redux Toolkit + Redux-Saga, Axios, and a Webpack dev server with proxy to the Spring Boot backend.

## Features
- Projects and tasks CRUD
- JWT authentication (register/login)
- Role-aware UI flows (admin/moderator/user)
- Real-time task updates and deadline expiry notifications via WebSocket topics
- Responsive UI with Material UI
- State management with Redux Toolkit and Redux-Saga
- Jest + React Testing Library UI tests

## Prerequisites
- Node.js 18+ and npm
- Backend running on http://localhost:8080 (Spring Boot from `backend/taskmanager`)

## Getting Started
From repository root or `frontend/` folder:

```bash
cd frontend
npm install
npm start
```

- App dev server: http://localhost:3000
- The Webpack dev server proxies API requests to `http://localhost:8080`:
  - HTTP API: `/api/*` → `http://localhost:8080/*` (prefix `/api` is removed)
  - WebSocket: `/ws` → `ws://localhost:8080/ws`

If your backend is running on a different host/port, update `frontend/webpack.config.js` `devServer.proxy` section accordingly.

## Scripts
- `npm start` — Start Webpack dev server on port 3000 with proxy
- `npm run build` — Production build (outputs to `frontend/dist`)
- `npm test` — Run UI tests (Jest + RTL)

## Project Structure
```
frontend/
  public/
    index.html
  src/
    App.js
    index.js
    theme.js
    components/
      ProjectDashboard.js
      TaskList.js
      TaskForm.js
      UserManagement.js
      common/
        NotificationSnackbar.js
        ConfirmDialog.js
    redux/
      store.js
      authSlice.js
      projectsSlice.js
      tasksSlice.js
      usersSlice.js
      uiSlice.js
      sagas/
        rootSaga.js
        authSaga.js
        projectsSaga.js
        tasksSaga.js
        usersSaga.js
    services/
      api.js
      socket.js
  test/
    setupTests.js
    utils/
      renderWithProviders.js
    App.test.js
    ProjectDashboard.test.js
    TaskList.test.js
  package.json
  webpack.config.js
```

## API Endpoints Used
- Auth
  - `POST /login` → returns `{ token }`
  - `POST /register`
- Projects
  - `GET /projects`
  - `POST /projects`
- Tasks
  - `GET /projects/{projectId}/tasks`
  - `POST /projects/{projectId}/tasks`
  - `PUT /tasks/{taskId}`
  - `DELETE /tasks/{taskId}`

All HTTP requests are sent to `/api/...` at the frontend, which the dev server proxies to the backend root (the `/api` prefix is removed in the proxy).

## Authentication
- On successful login, a JWT is persisted in `localStorage` and automatically attached as `Authorization: Bearer <token>` via Axios interceptors (`src/services/api.js`).
- 401 responses trigger UI feedback; you may need to re-login if the token expires.

## WebSocket (Realtime)
- Connects to `ws://localhost:8080/ws` (proxied as `/ws` during dev)
- Subscribes to topic `/topic/projects/{projectId}/tasks`
- Backend broadcasts the following payload types:
  - Task upsert: a `Task` JSON object (create/update)
  - Task delete: `{ "deletedId": <taskId> }`
  - Deadline expired: `{ "action": "expired", "taskId": <id>, "projectId": <id>, "deadline": <ISO-8601> }`
- See `src/services/socket.js` and `src/components/TaskList.js` for subscription and handling logic.

## Testing
Run tests in watch mode:
```bash
npm test
```

Tests use Jest with jsdom and React Testing Library. Setup is in `test/setupTests.js`. Use `test/utils/renderWithProviders.js` to render components with Redux, Router, and MUI theme providers.

## Build for Production
```bash
npm run build
```
- Output: `frontend/dist`
- Serve the static files with any HTTP server or integrate into a production host as needed.

## Troubleshooting
- 401 Unauthorized: ensure the backend is running and `app.jwt.secret` is configured; re-login if needed.
- WebSocket not connecting: verify backend `/ws` is reachable and proxies allow WS upgrades.
- CORS issues in non-dev environments: configure allowed origins in the Spring `WebSocketConfig` and HTTP CORS.
- Port 3000 in use: run `npm start -- --port 3001` or adjust `devServer.port` in `webpack.config.js`.
