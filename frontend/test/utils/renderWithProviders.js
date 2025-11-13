import React from 'react';
import { Provider } from 'react-redux';
import { ThemeProvider, CssBaseline } from '@mui/material';
import { configureStore } from '@reduxjs/toolkit';
import theme from '../../src/theme';
import authReducer from '../../src/redux/authSlice';
import projectsReducer from '../../src/redux/projectsSlice';
import tasksReducer from '../../src/redux/tasksSlice';
import usersReducer from '../../src/redux/usersSlice';
import uiReducer from '../../src/redux/uiSlice';

export default function renderWithProviders(ui, { preloadedState } = {}) {
  const store = configureStore({
    reducer: { auth: authReducer, projects: projectsReducer, tasks: tasksReducer, users: usersReducer, ui: uiReducer },
    preloadedState,
  });
  const Wrapper = ({ children }) => (
    <Provider store={store}>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        {children}
      </ThemeProvider>
    </Provider>
  );
  return { store, Wrapper };
}
