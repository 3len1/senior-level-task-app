import React, { useEffect, useMemo, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate, useParams, Link } from 'react-router-dom';
import { Box, Paper, Typography, Stack, Button, IconButton, Chip, Tooltip, Table, TableHead, TableRow, TableCell, TableBody } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import { selectTasksByProject, tasksRequested, deleteTaskRequested, wsTaskEventReceived } from '../redux/tasksSlice';
import { connectSocket, subscribeProjectTasks, disconnectSocket } from '../services/socket';
import ConfirmDialog from './common/ConfirmDialog';
import { showSnackbar } from '../redux/uiSlice';

export default function TaskList() {
  const { projectId } = useParams();
  const pid = Number(projectId);
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { items, loading, error } = useSelector((s) => selectTasksByProject(s, pid));

  const [deleteTarget, setDeleteTarget] = useState(null);

  useEffect(() => {
    if (!pid) return;
    dispatch(tasksRequested({ projectId: pid }));
  }, [dispatch, pid]);

  useEffect(() => {
    let sub;
    connectSocket({ onConnect: () => {
      sub = subscribeProjectTasks(pid, (payload) => {
        dispatch(wsTaskEventReceived(payload));
        if (payload.action === 'expired') {
          dispatch(showSnackbar({ severity: 'warning', message: `A task expired in project ${payload.projectId}` }));
        } else if (payload.deletedId) {
          dispatch(showSnackbar({ severity: 'info', message: `Task ${payload.deletedId} deleted` }));
        } else if (payload.id) {
          dispatch(showSnackbar({ severity: 'info', message: `Task updated: ${payload.title}` }));
        }
      });
    }});
    return () => {
      try { sub && sub.unsubscribe(); } catch (_) {}
    };
  }, [dispatch, pid]);

  const onConfirmDelete = () => {
    if (deleteTarget) {
      dispatch(deleteTaskRequested({ projectId: pid, taskId: deleteTarget.id }));
      setDeleteTarget(null);
    }
  };

  return (
    <Box>
      <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 2 }}>
        <Typography variant="h5">Tasks</Typography>
        <Button variant="contained" startIcon={<AddIcon />} component={Link} to={`/projects/${pid}/tasks/new`}>
          New Task
        </Button>
      </Stack>
      <Paper>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Title</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Deadline</TableCell>
              <TableCell align="right">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {items.map((t) => (
              <TableRow key={t.id} hover>
                <TableCell>
                  <Stack direction="row" spacing={1} alignItems="center">
                    <span>{t.title}</span>
                    {t.expired ? <Chip size="small" color="warning" label="Expired" /> : null}
                  </Stack>
                </TableCell>
                <TableCell>{t.status}</TableCell>
                <TableCell>{t.deadline ? new Date(t.deadline).toLocaleString() : '-'}</TableCell>
                <TableCell align="right">
                  <Tooltip title="Edit" disablePortal>
                    <IconButton color="primary" size="small" component={Link} to={`/tasks/${t.id}/edit`}>
                      <EditIcon fontSize="small" />
                    </IconButton>
                  </Tooltip>
                  <Tooltip title="Delete">
                    <IconButton color="error" size="small" onClick={() => setDeleteTarget(t)}>
                      <DeleteIcon fontSize="small" />
                    </IconButton>
                  </Tooltip>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </Paper>

      <ConfirmDialog
        open={!!deleteTarget}
        title="Delete Task"
        content={`Are you sure you want to delete task "${deleteTarget?.title}"?`}
        onCancel={() => setDeleteTarget(null)}
        onConfirm={onConfirmDelete}
        confirmText="Delete"
      />
    </Box>
  );
}
