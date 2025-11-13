import { createSlice } from '@reduxjs/toolkit';

const uiSlice = createSlice({
  name: 'ui',
  initialState: {
    snackbar: null,
  },
  reducers: {
    showSnackbar(state, action) {
      state.snackbar = action.payload; // { message, severity }
    },
    clearSnackbar(state) {
      state.snackbar = null;
    },
  },
});

export const { showSnackbar, clearSnackbar } = uiSlice.actions;
export const selectSnackbar = (state) => state.ui.snackbar;
export default uiSlice.reducer;
