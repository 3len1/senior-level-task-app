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

export const TaskApi = {
  async list(projectId) {
    const { data } = await api.get(`/projects/${projectId}/tasks`);
    return data;
  },
  async create(projectId, task) {
    const { data } = await api.post(`/projects/${projectId}/tasks`, task);
    return data;
  },
  async update(taskId, task) {
    const { data } = await api.put(`/tasks/${taskId}`, task);
    return data;
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
