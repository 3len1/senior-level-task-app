import { all, fork } from 'redux-saga/effects';
import authSaga from './authSaga';
import projectsSaga from './projectsSaga';
import tasksSaga from './tasksSaga';
import usersSaga from './usersSaga';

export default function* rootSaga() {
  yield all([
    fork(authSaga),
    fork(projectsSaga),
    fork(tasksSaga),
    fork(usersSaga),
  ]);
}
