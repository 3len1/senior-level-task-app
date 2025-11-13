import React, { useEffect, useMemo, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate, useParams } from 'react-router-dom';
import { Box, Paper, Stack, Typography, TextField, Button, MenuItem } from '@mui/material';
import { createTaskRequested, updateTaskRequested, selectTasksByProject } from '../redux/tasksSlice';
import { projectsRequested, selectProjects } from '../redux/projectsSlice';

const STATUS_OPTIONS = ['TODO', 'IN_PROGRESS', 'DONE'];

export default function TaskForm() {
  const { projectId, taskId } = useParams();
  const pid = projectId ? Number(projectId) : null;
  const tid = taskId ? Number(taskId) : null;
  const dispatch = useDispatch();
  const navigate = useNavigate();

  // When editing within a project, we can read tasks from that project's bucket
  const projectBucket = useSelector((s) => (pid ? selectTasksByProject(s, pid) : { items: [] }));
  const { items: tasks } = projectBucket;

  const task = useMemo(() => tasks.find((t) => t.id === tid), [tasks, tid]);

  const { items: projects } = useSelector(selectProjects);

  const [form, setForm] = useState({ title: '', description: '', status: 'TODO', projectId: pid || '' });

  useEffect(() => {
    // Always ensure projects are loaded for the Project select when creating
    if (!projects || projects.length === 0) {
      dispatch(projectsRequested());
    }
  }, [dispatch]);

  useEffect(() => {
    if (task) {
      setForm({
        title: task.title || '',
        description: task.description || '',
        status: task.status || 'TODO',
        projectId: task.project?.id || pid || '',
      });
    }
  }, [task, pid]);

  const onChange = (e) => setForm((f) => ({ ...f, [e.target.name]: e.target.value }));

  const onSubmit = (e) => {
    e.preventDefault();
    const payload = {
      title: form.title,
      description: form.description,
      status: form.status,
    };

    const targetProjectId = pid || Number(form.projectId);

    if (tid) {
      dispatch(updateTaskRequested({ projectId: targetProjectId || task?.project?.id, taskId: tid, task: payload }));
      navigate(`/projects/${targetProjectId || task?.project?.id}/tasks`);
    } else if (targetProjectId) {
      dispatch(createTaskRequested({ projectId: targetProjectId, task: payload }));
      navigate(`/projects/${targetProjectId}/tasks`);
    }
  };

  const isCreating = !tid;

  return (
    <Box>
      <Typography variant="h5" sx={{ mb: 2 }}>{tid ? 'Edit Task' : 'New Task'}</Typography>
      <Paper sx={{ p: 2 }}>
        <Stack spacing={2} component="form" onSubmit={onSubmit}>
          {isCreating && (
            <TextField
              select
              required
              name="projectId"
              label="Project"
              value={form.projectId}
              onChange={onChange}
              helperText="Select the project this task belongs to"
            >
              {projects.map((p) => (
                <MenuItem key={p.id} value={p.id}>{p.name}</MenuItem>
              ))}
            </TextField>
          )}
          <TextField required name="title" label="Title" value={form.title} onChange={onChange} />
          <TextField multiline minRows={3} name="description" label="Description" value={form.description} onChange={onChange} />
          <TextField select name="status" label="Status" value={form.status} onChange={onChange}>
            {STATUS_OPTIONS.map((s) => (<MenuItem key={s} value={s}>{s}</MenuItem>))}
          </TextField>
          <Stack direction="row" spacing={2}>
            <Button type="submit" variant="contained">Save</Button>
            <Button variant="text" onClick={() => navigate(-1)}>Cancel</Button>
          </Stack>
        </Stack>
      </Paper>
    </Box>
  );
}
