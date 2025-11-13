import { createSlice } from '@reduxjs/toolkit';

function decodeJwtRole(token) {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.role || null;
  } catch (_) {
    return null;
  }
}

const initialToken = localStorage.getItem('jwt');
const initialUsername = localStorage.getItem('username') || '';
const initialRole = initialToken ? (localStorage.getItem('role') || decodeJwtRole(initialToken)) : null;
const initialUser = initialToken ? { username: initialUsername, role: initialRole } : null;

const authSlice = createSlice({
  name: 'auth',
  initialState: {
    token: initialToken || null,
    user: initialUser,
    loading: false,
    error: null,
  },
  reducers: {
    loginRequested(state) {
      state.loading = true;
      state.error = null;
    },
    loginSucceeded(state, action) {
      state.loading = false;
      state.token = action.payload.token;
      state.user = { username: action.payload.username, role: action.payload.role };
    },
    loginFailed(state, action) {
      state.loading = false;
      state.error = action.payload || 'Login failed';
    },
    logout(state) {
      state.token = null;
      state.user = null;
      state.error = null;
    },
    registerRequested(state) {
      state.loading = true;
      state.error = null;
    },
    registerSucceeded(state) {
      state.loading = false;
    },
    registerFailed(state, action) {
      state.loading = false;
      state.error = action.payload || 'Register failed';
    },
  },
});

export const {
  loginRequested,
  loginSucceeded,
  loginFailed,
  logout,
  registerRequested,
  registerSucceeded,
  registerFailed,
} = authSlice.actions;

export const selectAuth = (state) => state.auth;

export default authSlice.reducer;
