import { createSlice } from '@reduxjs/toolkit';

const usersSlice = createSlice({
  name: 'users',
  initialState: {
    items: [],
    loading: false,
    error: null,
  },
  reducers: {
    usersRequested(state) { state.loading = true; state.error = null; },
    usersSucceeded(state, action) { state.loading = false; state.items = action.payload; },
    usersFailed(state, action) { state.loading = false; state.error = action.payload || 'Failed to load users'; },

    createUserRequested(state) { state.loading = true; state.error = null; },
    createUserSucceeded(state, action) { state.loading = false; state.items.push(action.payload); },
    createUserFailed(state, action) { state.loading = false; state.error = action.payload || 'Failed to create user'; },
  }
});

export const {
  usersRequested,
  usersSucceeded,
  usersFailed,
  createUserRequested,
  createUserSucceeded,
  createUserFailed,
} = usersSlice.actions;

export const selectUsers = (state) => state.users;

export default usersSlice.reducer;
