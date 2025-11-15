import React from 'react';
import { AppBar, Toolbar, Button, Box } from '@mui/material';
import { Link as RouterLink, useLocation } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { selectAuth, logout } from '../redux/authSlice';
import NotificationSnackbar from './common/NotificationSnackbar';

export default function Header() {
  const { user, token } = useSelector((s) => s.auth);
  const role = user?.role;
  const dispatch = useDispatch();
  const location = useLocation();

  const isAdminOrMod = role === 'ROLE_ADMIN' || role === 'ROLE_MODERATOR';

  return (
    <>
      <AppBar position="static" color="primary">
        <Toolbar>
          <Box sx={{ flexGrow: 1, display: 'flex', gap: 1 }}>
            {token ? (
              <>
                <Button
                  color="inherit"
                  component={RouterLink}
                  to="/projects"
                  sx={{
                    color: 'inherit',
                    borderBottom: location.pathname.startsWith('/projects') ? '2px solid rgba(255,255,255,0.9)' : 'none',
                    borderRadius: 0
                  }}
                >
                  Projects
                </Button>
                <Button
                  color="inherit"
                  component={RouterLink}
                  to="/tasks"
                  sx={{
                    color: 'inherit',
                    borderBottom: location.pathname.startsWith('/tasks') ? '2px solid rgba(255,255,255,0.9)' : 'none',
                    borderRadius: 0
                  }}
                >
                  Tasks
                </Button>
                {isAdminOrMod && (
                  <Button
                    color="inherit"
                    component={RouterLink}
                    to="/users"
                    sx={{
                      color: 'inherit',
                      borderBottom: location.pathname.startsWith('/users') ? '2px solid rgba(255,255,255,0.9)' : 'none',
                      borderRadius: 0
                    }}
                  >
                    Users
                  </Button>
                )}
              </>
            ) : (
              <>
                <Button component={RouterLink} to="/login" color="inherit">Login</Button>
                <Button component={RouterLink} to="/register" color="inherit">Register</Button>
              </>
            )}
          </Box>
          {token && (
            <Button color="inherit" onClick={() => dispatch(logout())}>Logout</Button>
          )}
        </Toolbar>
      </AppBar>
      {/* Global snackbar for notifications */}
      <NotificationSnackbar />
    </>
  );
}
