import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
});

// Attach JWT from localStorage if present
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('jwt');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export const AuthApi = {
  async login(username, password) {
    const { data } = await api.post('/login', { username, password });
    return data; // { token }
  },
  async register(username, password, role) {
    const { data } = await api.post('/register', { username, password, role });
    return data;
  },
};

export const ProjectApi = {
  async list() {
    const { data } = await api.get('/projects');
    return data;
  },
  async create(project) {
    const { data } = await api.post('/projects', project);
    return data;
  },
};

export function normalizeTask(raw) {
  if (!raw || typeof raw !== 'object') return raw;
  // If already DTO-like
  const projectId = raw.projectId ?? raw.project?.id ?? null;
  const assigneeId = raw.assigneeId ?? raw.assignee?.id ?? null;
  const assigneeUsername = raw.assigneeUsername ?? raw.assignee?.username ?? null;
  return {
    id: raw.id,
    title: raw.title,
    description: raw.description ?? '',
    status: raw.status,
    deadline: raw.deadline ?? null,
    projectId,
    assigneeId,
    assigneeUsername,
    // keep other flags if present (e.g., expired)
    expired: raw.expired ?? false,
  };
}

export const TaskApi = {
  async list(projectId) {
    const { data } = await api.get(`/projects/${projectId}/tasks`);
    return Array.isArray(data) ? data.map(normalizeTask) : [];
  },
  async create(projectId, task) {
    // Backend DTO expects flat fields; pass through
    const { data } = await api.post(`/projects/${projectId}/tasks`, task);
    return normalizeTask(data);
  },
  async update(taskId, task) {
    const { data } = await api.put(`/tasks/${taskId}`, task);
    return normalizeTask(data);
  },
  async remove(taskId) {
    await api.delete(`/tasks/${taskId}`);
    return { id: taskId };
  },
};

export const UserApi = {
  async list() {
    const { data } = await api.get('/users');
    return data;
  },
  async create(user) {
    const { data } = await api.post('/users', user);
    return data;
  },
};

export default api;
