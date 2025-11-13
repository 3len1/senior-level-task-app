import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

let client = null;
let subscriptions = {};

const isBrowser = typeof window !== 'undefined' && typeof document !== 'undefined';
const isTest = typeof process !== 'undefined' && process.env && process.env.JEST_WORKER_ID;

export function connectSocket({ onConnect, onDisconnect } = {}) {
  // In tests or non-browser environments, provide a no-op client to avoid TextEncoder/WebSocket issues
  if (!isBrowser || isTest) {
    client = { active: false, connected: false, subscribe: () => ({ unsubscribe() {} }) };
    onConnect && onConnect();
    return client;
  }

  if (client && client.active) return client;

  client = new Client({
    // Use SockJS so it works through the devServer proxy and with Spring's STOMP endpoint
    webSocketFactory: () => new SockJS('/stomp'),
    reconnectDelay: 5000, // auto-reconnect every 5s
    debug: () => {}, // silence logs
  });

  client.onConnect = () => {
    onConnect && onConnect();
  };

  client.onStompError = (frame) => {
    console.error('Broker reported error', frame.headers?.message, frame.body);
  };

  client.onWebSocketClose = (evt) => {
    onDisconnect && onDisconnect(evt);
  };

  client.activate();
  return client;
}

export function disconnectSocket() {
  if (client) {
    try { client.deactivate && client.deactivate(); } catch (_) {}
    client = null;
    subscriptions = {};
  }
}

export function subscribeProjectTasks(projectId, callback) {
  if (!client || !client.connected) return { unsubscribe() {} };
  const destination = `/topic/projects/${projectId}/tasks`;
  if (subscriptions[destination]) return subscriptions[destination];
  const sub = client.subscribe(destination, (msg) => {
    try {
      const body = JSON.parse(msg.body);
      // Normalize task events to DTO-like shape on the client
      if (body && body.id) {
        try {
          const { normalizeTask } = require('./api');
          callback(normalizeTask(body));
        } catch (_) {
          callback(body);
        }
      } else {
        callback(body);
      }
    } catch (e) {
      console.warn('Invalid WS payload', e);
    }
  });
  subscriptions[destination] = sub;
  return sub;
}

export function unsubscribe(destination) {
  if (subscriptions[destination]) {
    subscriptions[destination].unsubscribe();
    delete subscriptions[destination];
  }
}
