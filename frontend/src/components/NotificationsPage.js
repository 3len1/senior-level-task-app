import React from 'react';
import { Box, Paper, Typography } from '@mui/material';

export default function NotificationsPage() {
  return (
    <Box>
      <Typography variant="h5" sx={{ mb: 2 }}>Notifications</Typography>
      <Paper sx={{ p: 2 }}>
        <Typography variant="body1">
          Live WebSocket notifications will appear here in a future step. For now, real-time events show as snackbars in the bottom-left.
        </Typography>
      </Paper>
    </Box>
  );
}
