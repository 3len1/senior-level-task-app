import { call, put, takeLatest } from 'redux-saga/effects';
import { UserApi } from '../../services/api';
import { usersRequested, usersSucceeded, usersFailed, createUserRequested, createUserSucceeded, createUserFailed } from '../usersSlice';
import { showSnackbar } from '../uiSlice';

function* fetchUsers() {
  try {
    const items = yield call(UserApi.list);
    yield put(usersSucceeded(items));
  } catch (e) {
    const msg = e.response?.data?.message || e.message;
    yield put(usersFailed(msg));
    yield put(showSnackbar({ severity: 'error', message: msg }));
  }
}

function* createUser(action) {
  try {
    const created = yield call(UserApi.create, action.payload);
    yield put(createUserSucceeded(created));
    yield put(showSnackbar({ severity: 'success', message: 'User created' }));
  } catch (e) {
    const msg = e.response?.data?.message || e.message;
    yield put(createUserFailed(msg));
    yield put(showSnackbar({ severity: 'error', message: msg }));
  }
}

export default function* usersSaga() {
  yield takeLatest(usersRequested.type, fetchUsers);
  yield takeLatest(createUserRequested.type, createUser);
}
