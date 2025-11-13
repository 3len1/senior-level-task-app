import { call, put, takeLatest } from 'redux-saga/effects';
import { ProjectApi } from '../../services/api';
import { projectsRequested, projectsSucceeded, projectsFailed, createProjectRequested, createProjectSucceeded, createProjectFailed } from '../projectsSlice';
import { showSnackbar } from '../uiSlice';

function* fetchProjects() {
  try {
    const items = yield call(ProjectApi.list);
    yield put(projectsSucceeded(items));
  } catch (e) {
    const msg = e.response?.data?.message || e.message;
    yield put(projectsFailed(msg));
    yield put(showSnackbar({ severity: 'error', message: msg }));
  }
}

function* createProject(action) {
  try {
    const created = yield call(ProjectApi.create, action.payload);
    yield put(createProjectSucceeded(created));
    yield put(showSnackbar({ severity: 'success', message: 'Project created' }));
  } catch (e) {
    const msg = e.response?.data?.message || e.message;
    yield put(createProjectFailed(msg));
    yield put(showSnackbar({ severity: 'error', message: msg }));
  }
}

export default function* projectsSaga() {
  yield takeLatest(projectsRequested.type, fetchProjects);
  yield takeLatest(createProjectRequested.type, createProject);
}
