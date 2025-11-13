import { call, put, takeLatest } from 'redux-saga/effects';
import { AuthApi } from '../../services/api';
import { loginRequested, loginSucceeded, loginFailed, registerRequested, registerSucceeded, registerFailed, logout } from '../authSlice';

function decodeJwtRole(token) {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.role || null;
  } catch (e) {
    return null;
  }
}

function* handleLogin(action) {
  try {
    const { username, password } = action.payload;
    const { token } = yield call(AuthApi.login, username, password);
    const role = decodeJwtRole(token);
    localStorage.setItem('jwt', token);
    localStorage.setItem('username', username);
    if (role) localStorage.setItem('role', role);
    yield put(loginSucceeded({ token, username, role }));
  } catch (e) {
    yield put(loginFailed(e.response?.data?.message || e.message));
  }
}

function* handleRegister(action) {
  try {
    const { username, password, role } = action.payload;
    yield call(AuthApi.register, username, password, role);
    yield put(registerSucceeded());
  } catch (e) {
    yield put(registerFailed(e.response?.data?.message || e.message));
  }
}

function* handleLogout() {
  localStorage.removeItem('jwt');
  localStorage.removeItem('username');
  localStorage.removeItem('role');
}

export default function* authSaga() {
  yield takeLatest(loginRequested.type, handleLogin);
  yield takeLatest(registerRequested.type, handleRegister);
  yield takeLatest(logout.type, handleLogout);
}
