import React from 'react';
import { Snackbar, Alert } from '@mui/material';
import { useDispatch, useSelector } from 'react-redux';
import { clearSnackbar, selectSnackbar } from '../../redux/uiSlice';

export default function NotificationSnackbar() {
  const dispatch = useDispatch();
  const snack = useSelector(selectSnackbar);

  const handleClose = () => dispatch(clearSnackbar());

  return (
    <Snackbar
      open={!!snack}
      autoHideDuration={4000}
      onClose={handleClose}
      anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
    >
      <Alert onClose={handleClose} severity={snack?.severity || 'info'} variant="filled" sx={{ width: '100%' }}>
        {snack?.message}
      </Alert>
    </Snackbar>
  );
}
