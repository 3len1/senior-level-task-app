import React, { useEffect, useMemo } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { Box, Paper, Stack, Typography, Button, Table, TableHead, TableRow, TableCell, TableBody, Chip } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import { Link as RouterLink } from 'react-router-dom';
import { projectsRequested, selectProjects } from '../redux/projectsSlice';
import { tasksRequested } from '../redux/tasksSlice';

export default function TasksPage() {
  const dispatch = useDispatch();
  const { items: projects } = useSelector(selectProjects);
  const tasksState = useSelector((s) => s.tasks);

  useEffect(() => {
    if (!projects || projects.length === 0) {
      dispatch(projectsRequested());
    }
  }, [dispatch]);

  useEffect(() => {
    // When projects list is available, fetch tasks for each project
    if (projects && projects.length > 0) {
      projects.forEach((p) => dispatch(tasksRequested({ projectId: p.id })));
    }
  }, [dispatch, projects]);

  const combined = useMemo(() => {
    if (!projects || projects.length === 0) return [];
    const all = [];
    for (const p of projects) {
      const bucket = tasksState.byProject[p.id] || { items: [] };
      for (const t of bucket.items) {
        all.push({ ...t, project: p });
      }
    }
    return all;
  }, [projects, tasksState.byProject]);

  return (
    <Box>
      <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 2 }}>
        <Typography variant="h5">All Tasks</Typography>
        <Button variant="contained" startIcon={<AddIcon />} component={RouterLink} to="/tasks/new">
          New Task
        </Button>
      </Stack>
      <Paper>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Title</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Project</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {combined.map((t) => (
              <TableRow key={`${t.project.id}-${t.id}`} hover>
                <TableCell>
                  <Stack direction="row" spacing={1} alignItems="center">
                    <span>{t.title}</span>
                    {t.expired ? <Chip size="small" color="warning" label="Expired" /> : null}
                  </Stack>
                </TableCell>
                <TableCell>{t.status}</TableCell>
                <TableCell>
                  <Button size="small" component={RouterLink} to={`/projects/${t.project.id}/tasks`}>{t.project.name}</Button>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </Paper>
    </Box>
  );
}
