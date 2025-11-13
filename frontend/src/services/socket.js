import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

let client = null;
let subscriptions = {};

export function connectSocket({ onConnect, onDisconnect } = {}) {
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
    try { client.deactivate(); } catch (_) {}
    client = null;
    subscriptions = {};
  }
}

export function subscribeProjectTasks(projectId, callback) {
  if (!client || !client.connected) return null;
  const destination = `/topic/projects/${projectId}/tasks`;
  if (subscriptions[destination]) return subscriptions[destination];
  const sub = client.subscribe(destination, (msg) => {
    try {
      const body = JSON.parse(msg.body);
      callback(body);
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
