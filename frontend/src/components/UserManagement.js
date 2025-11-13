import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { Box, Paper, Typography, Stack, TextField, Button, Table, TableHead, TableRow, TableCell, TableBody } from '@mui/material';
import { selectUsers, usersRequested, createUserRequested } from '../redux/usersSlice';

export default function UserManagement() {
  const dispatch = useDispatch();
  const { items, loading } = useSelector(selectUsers);
  const [form, setForm] = useState({ username: '', password: '', role: 'ROLE_USER' });

  useEffect(() => { dispatch(usersRequested()); }, [dispatch]);

  const onChange = (e) => setForm((f) => ({ ...f, [e.target.name]: e.target.value }));
  const onSubmit = (e) => {
    e.preventDefault();
    if (!form.username || !form.password) return;
    dispatch(createUserRequested(form));
    setForm({ username: '', password: '', role: 'ROLE_USER' });
  };

  return (
    <Box>
      <Typography variant="h5" sx={{ mb: 2 }}>User Management</Typography>
      <Paper sx={{ p: 2, mb: 3 }}>
        <Stack component="form" spacing={2} direction={{ xs: 'column', sm: 'row' }} onSubmit={onSubmit}>
          <TextField name="username" label="Username" value={form.username} onChange={onChange} required />
          <TextField type="password" name="password" label="Password" value={form.password} onChange={onChange} required />
          <TextField name="role" label="Role" value={form.role} onChange={onChange} />
          <Button type="submit" variant="contained" disabled={loading}>Add User</Button>
        </Stack>
      </Paper>

      <Paper>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>ID</TableCell>
              <TableCell>Username</TableCell>
              <TableCell>Role</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {items.map((u) => (
              <TableRow key={u.id} hover>
                <TableCell>{u.id}</TableCell>
                <TableCell>{u.username}</TableCell>
                <TableCell>{u.role}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </Paper>
    </Box>
  );
}
