import React from 'react';
import { useEffect, useState } from 'react';
import { Box, Paper, Typography, List, ListItem, ListItemText, Chip, Stack } from '@mui/material';
import { connectSocket, subscribeAllTasks } from '../services/socket';

function formatWhen(ts) {
  try {
    return new Date(ts).toLocaleString();
  } catch (_) {
    return String(ts);
  }
}

export default function NotificationsPage() {
  const [items, setItems] = useState([]); // { id, type, title, projectId, when, extra }

  useEffect(() => {
    // Ensure socket is active; subscribe to global task events
    let sub = null;
    const client = connectSocket({
      onConnect: () => {
        sub = subscribeAllTasks((evt) => {
          setItems((prev) => {
            const now = Date.now();
            // Normalize into a display entry
            if (evt && evt.action === 'expired') {
              const entry = {
                key: `expired-${evt.projectId}-${evt.task}-${evt.deadline}-${now}`,
                type: 'expired',
                title: evt.task,
                projectId: evt.projectId,
                when: evt.deadline || now,
                extra: { deadline: evt.deadline },
              };
              return [entry, ...prev].slice(0, 100);
            }
            if (evt && evt.id) {
              // Global topic currently emits DTO on creation
              const entry = {
                key: `created-${evt.id}-${now}`,
                type: 'created',
                title: evt.title,
                projectId: evt.projectId ?? evt.project?.id ?? null,
                when: now,
                extra: { id: evt.id },
              };
              return [entry, ...prev].slice(0, 100);
            }
            // Fallback generic entry
            const entry = { key: `evt-${now}`, type: 'event', title: 'Task event', projectId: evt?.projectId ?? null, when: now, extra: evt };
            return [entry, ...prev].slice(0, 100);
          });
        });
      },
    });

    // If already connected (e.g., another page activated it), subscribe immediately
    if (client && client.connected && !sub) {
      sub = subscribeAllTasks((evt) => {
        setItems((prev) => {
          const now = Date.now();
          if (evt && evt.action === 'expired') {
            const entry = {
              key: `expired-${evt.projectId}-${evt.task}-${evt.deadline}-${now}`,
              type: 'expired',
              title: evt.task,
              projectId: evt.projectId,
              when: evt.deadline || now,
              extra: { deadline: evt.deadline },
            };
            return [entry, ...prev].slice(0, 100);
          }
          if (evt && evt.id) {
            const entry = {
              key: `created-${evt.id}-${now}`,
              type: 'created',
              title: evt.title,
              projectId: evt.projectId ?? evt.project?.id ?? null,
              when: now,
              extra: { id: evt.id },
            };
            return [entry, ...prev].slice(0, 100);
          }
          const entry = { key: `evt-${now}`, type: 'event', title: 'Task event', projectId: evt?.projectId ?? null, when: now, extra: evt };
          return [entry, ...prev].slice(0, 100);
        });
      });
    }

    return () => {
      try { sub && sub.unsubscribe && sub.unsubscribe(); } catch (_) {}
    };
  }, []);

  return (
    <Box>
      <Typography variant="h5" sx={{ mb: 2 }}>Notifications</Typography>
      <Paper sx={{ p: 2 }}>
        {items.length === 0 ? (
          <Typography variant="body1">
            Waiting for live WebSocket notifications...
          </Typography>
        ) : (
          <List dense>
            {items.map((n) => (
              <ListItem key={n.key} divider alignItems="flex-start">
                <Stack direction="row" spacing={1} alignItems="center" sx={{ mr: 2, minWidth: 180 }}>
                  <Chip size="small" color={n.type === 'expired' ? 'warning' : n.type === 'created' ? 'success' : 'default'} label={n.type} />
                  <Typography variant="caption" color="text.secondary">{formatWhen(n.when)}</Typography>
                </Stack>
                <ListItemText
                  primary={n.title || 'Task'}
                  secondary={
                    <span>
                      {n.projectId != null && (<><strong>Project:</strong> {n.projectId} · </>)}
                      {n.type === 'expired' && n.extra?.deadline && (
                        <>Deadline: {formatWhen(n.extra.deadline)}</>
                      )}
                      {n.type === 'created' && n.extra?.id && (
                        <>ID: {n.extra.id}</>
                      )}
                    </span>
                  }
                />
              </ListItem>
            ))}
          </List>
        )}
      </Paper>
    </Box>
  );
}
