import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { Box, Paper, Typography, Stack, TextField, Button, Alert } from '@mui/material';
import { Link as RouterLink, useNavigate } from 'react-router-dom';
import { loginRequested, selectAuth } from '../redux/authSlice';

export default function LoginPage() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { loading, error, token } = useSelector(selectAuth);

  const [form, setForm] = useState({ username: '', password: '' });

  useEffect(() => {
    if (token) {
      navigate('/projects', { replace: true });
    }
  }, [token, navigate]);

  const onChange = (e) => setForm((f) => ({ ...f, [e.target.name]: e.target.value }));
  const onSubmit = (e) => {
    e.preventDefault();
    if (!form.username || !form.password) return;
    dispatch(loginRequested(form));
  };

  return (
    <Box sx={{ maxWidth: 480, mx: 'auto', mt: 4 }}>
      <Paper sx={{ p: 3 }}>
        <Typography variant="h5" sx={{ mb: 2 }}>Login</Typography>
        {error ? <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert> : null}
        <Stack component="form" spacing={2} onSubmit={onSubmit}>
          <TextField label="Username" name="username" value={form.username} onChange={onChange} required autoFocus />
          <TextField label="Password" type="password" name="password" value={form.password} onChange={onChange} required />
          <Button type="submit" variant="contained" disabled={loading}>Login</Button>
        </Stack>
        <Typography variant="body2" sx={{ mt: 2 }}>
          Don&apos;t have an account? <Button component={RouterLink} to="/register">Register</Button>
        </Typography>
      </Paper>
    </Box>
  );
}
