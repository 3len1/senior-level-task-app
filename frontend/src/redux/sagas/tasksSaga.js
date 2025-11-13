import { call, put, takeLatest } from 'redux-saga/effects';
import { TaskApi } from '../../services/api';
import {
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
} from '../tasksSlice';
import { showSnackbar } from '../uiSlice';

function* fetchTasks(action) {
  const { projectId } = action.payload;
  try {
    const items = yield call(TaskApi.list, projectId);
    yield put(tasksSucceeded({ projectId, items }));
  } catch (e) {
    const msg = e.response?.data?.message || e.message;
    yield put(tasksFailed({ projectId, error: msg }));
    yield put(showSnackbar({ severity: 'error', message: msg }));
  }
}

function* createTask(action) {
  const { projectId, task } = action.payload;
  try {
    const created = yield call(TaskApi.create, projectId, task);
    yield put(createTaskSucceeded({ projectId, task: created }));
    yield put(showSnackbar({ severity: 'success', message: 'Task created' }));
  } catch (e) {
    const msg = e.response?.data?.message || e.message;
    yield put(createTaskFailed(msg));
    yield put(showSnackbar({ severity: 'error', message: msg }));
  }
}

function* updateTask(action) {
  const { projectId, taskId, task } = action.payload;
  try {
    const saved = yield call(TaskApi.update, taskId, task);
    yield put(updateTaskSucceeded({ projectId, task: saved }));
    yield put(showSnackbar({ severity: 'success', message: 'Task updated' }));
  } catch (e) {
    const msg = e.response?.data?.message || e.message;
    yield put(updateTaskFailed(msg));
    yield put(showSnackbar({ severity: 'error', message: msg }));
  }
}

function* deleteTask(action) {
  const { projectId, taskId } = action.payload;
  try {
    yield call(TaskApi.remove, taskId);
    yield put(deleteTaskSucceeded({ projectId, taskId }));
    yield put(showSnackbar({ severity: 'success', message: 'Task deleted' }));
  } catch (e) {
    const msg = e.response?.data?.message || e.message;
    yield put(deleteTaskFailed(msg));
    yield put(showSnackbar({ severity: 'error', message: msg }));
  }
}

export default function* tasksSaga() {
  yield takeLatest(tasksRequested.type, fetchTasks);
  yield takeLatest(createTaskRequested.type, createTask);
  yield takeLatest(updateTaskRequested.type, updateTask);
  yield takeLatest(deleteTaskRequested.type, deleteTask);
}
