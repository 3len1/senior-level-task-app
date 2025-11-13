import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { Box, Paper, Typography, Stack, TextField, Button, Alert, FormControl, InputLabel, Select, MenuItem } from '@mui/material';
import { Link as RouterLink, useNavigate } from 'react-router-dom';
import { registerRequested, selectAuth } from '../redux/authSlice';

export default function RegisterPage() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { loading, error, token } = useSelector(selectAuth);

  const [form, setForm] = useState({ username: '', password: '', role: 'ROLE_USER' });

  useEffect(() => {
    if (token) {
      // If already logged in, go to projects
      navigate('/projects', { replace: true });
    }
  }, [token, navigate]);

  const onChange = (e) => setForm((f) => ({ ...f, [e.target.name]: e.target.value }));
  const onSubmit = async (e) => {
    e.preventDefault();
    if (!form.username || !form.password || !form.role) return;
    dispatch(registerRequested(form));
  };

  return (
    <Box sx={{ maxWidth: 520, mx: 'auto', mt: 4 }}>
      <Paper sx={{ p: 3 }}>
        <Typography variant="h5" sx={{ mb: 2 }}>Register</Typography>
        {error ? <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert> : null}
        <Stack component="form" spacing={2} onSubmit={onSubmit}>
          <TextField label="Username" name="username" value={form.username} onChange={onChange} required autoFocus />
          <TextField label="Password" type="password" name="password" value={form.password} onChange={onChange} required />
          <FormControl fullWidth>
            <InputLabel id="role-label">Role</InputLabel>
            <Select
              labelId="role-label"
              label="Role"
              name="role"
              value={form.role}
              onChange={onChange}
            >
              <MenuItem value="ROLE_USER">User</MenuItem>
              <MenuItem value="ROLE_MODERATOR">Moderator</MenuItem>
              <MenuItem value="ROLE_ADMIN">Admin</MenuItem>
            </Select>
          </FormControl>
          <Button type="submit" variant="contained" disabled={loading}>Register</Button>
        </Stack>
        <Typography variant="body2" sx={{ mt: 2 }}>
          Already have an account? <Button component={RouterLink} to="/login">Login</Button>
        </Typography>
      </Paper>
    </Box>
  );
}
