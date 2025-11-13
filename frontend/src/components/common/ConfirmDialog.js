import React from 'react';
import { Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle, Button } from '@mui/material';

export default function ConfirmDialog({ open, title = 'Confirm', content, onCancel, onConfirm, confirmText = 'Confirm', cancelText = 'Cancel' }) {
  return (
    <Dialog open={open} onClose={onCancel} maxWidth="xs" fullWidth>
      <DialogTitle>{title}</DialogTitle>
      {content ? (
        <DialogContent>
          <DialogContentText>{content}</DialogContentText>
        </DialogContent>
      ) : null}
      <DialogActions>
        <Button onClick={onCancel}>{cancelText}</Button>
        <Button color="error" variant="contained" onClick={onConfirm}>{confirmText}</Button>
      </DialogActions>
    </Dialog>
  );
}
