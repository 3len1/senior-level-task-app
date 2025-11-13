import { createSlice } from '@reduxjs/toolkit';

const projectsSlice = createSlice({
  name: 'projects',
  initialState: {
    items: [],
    loading: false,
    error: null,
  },
  reducers: {
    projectsRequested(state) {
      state.loading = true; state.error = null;
    },
    projectsSucceeded(state, action) {
      state.loading = false; state.items = action.payload;
    },
    projectsFailed(state, action) {
      state.loading = false; state.error = action.payload || 'Failed to load projects';
    },

    createProjectRequested(state) { state.loading = true; state.error = null; },
    createProjectSucceeded(state, action) { state.loading = false; state.items.push(action.payload); },
    createProjectFailed(state, action) { state.loading = false; state.error = action.payload || 'Failed to create project'; },
  }
});

export const {
  projectsRequested,
  projectsSucceeded,
  projectsFailed,
  createProjectRequested,
  createProjectSucceeded,
  createProjectFailed,
} = projectsSlice.actions;

export const selectProjects = (state) => state.projects;

export default projectsSlice.reducer;
