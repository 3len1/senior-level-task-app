import { createSlice } from '@reduxjs/toolkit';

const tasksSlice = createSlice({
  name: 'tasks',
  initialState: {
    byProject: {}, // projectId -> { items: [], loading, error }
    lastEvent: null, // for notifications
  },
  reducers: {
    tasksRequested(state, action) {
      const { projectId } = action.payload;
      state.byProject[projectId] = state.byProject[projectId] || { items: [], loading: false, error: null };
      state.byProject[projectId].loading = true;
      state.byProject[projectId].error = null;
    },
    tasksSucceeded(state, action) {
      const { projectId, items } = action.payload;
      state.byProject[projectId] = state.byProject[projectId] || { items: [], loading: false, error: null };
      state.byProject[projectId].loading = false;
      state.byProject[projectId].items = items;
    },
    tasksFailed(state, action) {
      const { projectId, error } = action.payload;
      state.byProject[projectId] = state.byProject[projectId] || { items: [], loading: false, error: null };
      state.byProject[projectId].loading = false;
      state.byProject[projectId].error = error || 'Failed to load tasks';
    },

    createTaskRequested(state, action) {
      // noop for loading handled in UI if needed
    },
    createTaskSucceeded(state, action) {
      const { projectId, task } = action.payload;
      const bucket = state.byProject[projectId];
      if (bucket) bucket.items.push(task);
      state.lastEvent = { type: 'task_created', task };
    },
    createTaskFailed(state) {},

    updateTaskRequested(state) {},
    updateTaskSucceeded(state, action) {
      const { task } = action.payload;
      const projectId = task.project?.id || action.payload.projectId;
      const bucket = state.byProject[projectId];
      if (bucket) {
        const idx = bucket.items.findIndex((t) => t.id === task.id);
        if (idx >= 0) bucket.items[idx] = task; else bucket.items.push(task);
      }
      state.lastEvent = { type: 'task_updated', task };
    },
    updateTaskFailed(state) {},

    deleteTaskRequested(state) {},
    deleteTaskSucceeded(state, action) {
      const { projectId, taskId } = action.payload;
      const bucket = state.byProject[projectId];
      if (bucket) bucket.items = bucket.items.filter((t) => t.id !== taskId);
      state.lastEvent = { type: 'task_deleted', taskId };
    },
    deleteTaskFailed(state) {},

    // Real-time events via websocket
    wsTaskEventReceived(state, action) {
      const evt = action.payload; // could be full task or {deletedId} or {action: 'expired'}
      const { projectId } = evt.projectId ? evt : { projectId: action.meta?.projectId };
      const bucket = projectId ? (state.byProject[projectId] || null) : null;
      if ('deletedId' in evt) {
        if (bucket) bucket.items = bucket.items.filter((t) => t.id !== evt.deletedId);
        state.lastEvent = { type: 'task_deleted', taskId: evt.deletedId };
      } else if (evt.action === 'expired') {
        state.lastEvent = { type: 'task_expired', taskId: evt.taskId };
        // mark expired in list if present
        if (bucket && bucket.items) {
          const idx = bucket.items.findIndex((t) => t.id === evt.taskId);
          if (idx >= 0) {
            bucket.items[idx] = { ...bucket.items[idx], expired: true };
          }
        }
      } else if (evt.id) {
        const t = evt;
        const pid = t.project?.id || projectId;
        const b = pid ? (state.byProject[pid] || (state.byProject[pid] = { items: [], loading: false, error: null })) : null;
        if (b) {
          const idx = b.items.findIndex((x) => x.id === t.id);
          if (idx >= 0) b.items[idx] = t; else b.items.push(t);
        }
        state.lastEvent = { type: 'task_upserted', task: t };
      }
    },
    clearLastEvent(state) { state.lastEvent = null; },
  }
});

export const {
  tasksRequested,
  tasksSucceeded,
  tasksFailed,
  createTaskRequested,
  createTaskSucceeded,
  createTaskFailed,
  updateTaskRequested,
  updateTaskSucceeded,
  updateTaskFailed,
  deleteTaskRequested,
  deleteTaskSucceeded,
  deleteTaskFailed,
  wsTaskEventReceived,
  clearLastEvent,
} = tasksSlice.actions;

export const selectTasksByProject = (state, projectId) => state.tasks.byProject[projectId] || { items: [], loading: false, error: null };
export const selectTasksLastEvent = (state) => state.tasks.lastEvent;

export default tasksSlice.reducer;
