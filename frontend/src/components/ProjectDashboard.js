import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { Box, Grid, Paper, Typography, TextField, Button, Stack, Card, CardContent, CardActions, FormControl, InputLabel, Select, MenuItem, Link as MUILink } from '@mui/material';
import { Link as RouterLink } from 'react-router-dom';
import { projectsRequested, createProjectRequested, selectProjects } from '../redux/projectsSlice';
import { loginRequested, registerRequested, selectAuth } from '../redux/authSlice';

export default function ProjectDashboard() {
  const dispatch = useDispatch();
  const { items, loading } = useSelector(selectProjects);
  const { user, loading: authLoading } = useSelector(selectAuth);

  const [projectName, setProjectName] = useState('');
  const [projectDesc, setProjectDesc] = useState('');

  const [loginForm, setLoginForm] = useState({ username: '', password: '' });
  const [registerForm, setRegisterForm] = useState({ username: '', password: '', role: 'ROLE_USER' });

  useEffect(() => { dispatch(projectsRequested()); }, [dispatch]);

  const onCreateProject = (e) => {
    e.preventDefault();
    if (!projectName.trim()) return;
    dispatch(createProjectRequested({ name: projectName, description: projectDesc }));
    setProjectName(''); setProjectDesc('');
  };

  const onLogin = (e) => { e.preventDefault(); dispatch(loginRequested(loginForm)); };
  const onRegister = (e) => { e.preventDefault(); dispatch(registerRequested(registerForm)); };

  return (
    <Box>
      <Grid container spacing={3} alignItems="flex-start">
        <Grid item xs={12} md={8}>
          <Typography variant="h5" sx={{ mb: 2 }}>Projects</Typography>
          <Grid container spacing={2}>
            {items.map((p) => (
              <Grid key={p.id} item xs={12} sm={6} md={4}>
                <Card>
                  <CardContent>
                    <Typography variant="h6">{p.name}</Typography>
                    <Typography color="text.secondary" sx={{ minHeight: 40 }}>{p.description}</Typography>
                  </CardContent>
                  <CardActions>
                    <Button component={RouterLink} to={`/projects/${p.id}/tasks`} size="small">Open</Button>
                  </CardActions>
                </Card>
              </Grid>
            ))}
          </Grid>
        </Grid>
        <Grid item xs={12} md={4}>
          <Paper sx={{ p: 2, mb: 3 }}>
            <Typography variant="h6" sx={{ mb: 1 }}>Create Project</Typography>
            <Stack component="form" spacing={2} onSubmit={onCreateProject}>
              <TextField label="Name" value={projectName} onChange={(e) => setProjectName(e.target.value)} required />
              <TextField label="Description" value={projectDesc} onChange={(e) => setProjectDesc(e.target.value)} />
              <Button type="submit" variant="contained" disabled={loading}>Create</Button>
            </Stack>
          </Paper>

          {!user && (
            <>
              <Paper sx={{ p: 2, mb: 3 }}>
                <Typography variant="h6" sx={{ mb: 1 }}>Login</Typography>
                <Stack component="form" spacing={2} onSubmit={onLogin}>
                  <TextField label="Username" value={loginForm.username} onChange={(e) => setLoginForm({ ...loginForm, username: e.target.value })} required />
                  <TextField type="password" label="Password" value={loginForm.password} onChange={(e) => setLoginForm({ ...loginForm, password: e.target.value })} required />
                  <Button type="submit" variant="contained" disabled={authLoading}>Login</Button>
                </Stack>
                <Typography variant="body2" sx={{ mt: 2 }}>
                  Don&apos;t have an account?{' '}
                  <MUILink href="#register" color="primary" underline="hover">Register</MUILink>
                </Typography>
              </Paper>
              <Paper id="register" sx={{ p: 2 }}>
                <Typography variant="h6" sx={{ mb: 1 }}>Register</Typography>
                <Stack component="form" spacing={2} onSubmit={onRegister}>
                  <TextField label="Username" value={registerForm.username} onChange={(e) => setRegisterForm({ ...registerForm, username: e.target.value })} required />
                  <TextField type="password" label="Password" value={registerForm.password} onChange={(e) => setRegisterForm({ ...registerForm, password: e.target.value })} required />
                  <FormControl fullWidth>
                    <InputLabel id="role-label">Role</InputLabel>
                    <Select
                      labelId="role-label"
                      label="Role"
                      value={registerForm.role}
                      onChange={(e) => setRegisterForm({ ...registerForm, role: e.target.value })}
                    >
                      <MenuItem value="ROLE_USER">ROLE_USER</MenuItem>
                      <MenuItem value="ROLE_ADMIN">ROLE_ADMIN</MenuItem>
                    </Select>
                  </FormControl>
                  <Button type="submit" variant="outlined" disabled={authLoading}>Register</Button>
                </Stack>
              </Paper>
            </>
          )}
        </Grid>
      </Grid>
    </Box>
  );
}
