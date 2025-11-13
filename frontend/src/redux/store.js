import { configureStore } from '@reduxjs/toolkit';
import createSagaMiddleware from 'redux-saga';
import authReducer from './authSlice';
import projectsReducer from './projectsSlice';
import tasksReducer from './tasksSlice';
import usersReducer from './usersSlice';
import uiReducer from './uiSlice';
import rootSaga from './sagas/rootSaga';

const saga = createSagaMiddleware();

const store = configureStore({
  reducer: {
    auth: authReducer,
    projects: projectsReducer,
    tasks: tasksReducer,
    users: usersReducer,
    ui: uiReducer,
  },
  middleware: (getDefault) => getDefault({ thunk: false }).concat(saga),
});

saga.run(rootSaga);

export default store;
