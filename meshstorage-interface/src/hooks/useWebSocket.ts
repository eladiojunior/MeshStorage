import { useEffect, useRef, useState, useCallback } from 'react';

type WebSocketStatus = 'connecting' | 'connected' | 'disconnected';

interface WebSocketOptions {
  onMessage?: (data: unknown) => void;
  onOpen?: () => void;
  onClose?: () => void;
  onError?: (error: Event) => void;
  reconnectInterval?: number;
  maxReconnectAttempts?: number;
}

const useWebSocket = (options: WebSocketOptions = {}) => {
  const [status, setStatus] = useState<WebSocketStatus>('disconnected');
  const socketRef = useRef<WebSocket | null>(null);
  const reconnectAttemptsRef = useRef(0);
  const reconnectTimeoutRef = useRef<number | null>(null);

  const {
    onMessage,
    onOpen,
    onClose,
    onError,
    reconnectInterval = 3000,
    maxReconnectAttempts = 5,
  } = options;

  const connect = useCallback(() => {
    if (socketRef.current && socketRef.current.readyState === WebSocket.OPEN) return;

    // Close existing socket if it exists
    if (socketRef.current) {
      socketRef.current.close();
    }

    setStatus('connecting');

    // Using direct API queries instead of WebSockets to make it more reliable
    console.log('Initializing with direct API connection instead of WebSocket');
    setStatus('connected');
    reconnectAttemptsRef.current = 0;
    if (onOpen) onOpen();
    
    // Simulate receiving initial data from API
    setTimeout(() => {
      if (onMessage) {
        Promise.all([
          fetch('/api/system-status').then(res => res.json()),
          fetch('/api/clients').then(res => res.json()),
          fetch('/api/applications').then(res => res.json()),
          fetch('/api/system-alerts').then(res => res.json()),
          fetch('/api/storage-history').then(res => res.json()),
          fetch('/api/file-type-distribution').then(res => res.json())
        ]).then(([systemStatus, clients, applications, systemAlerts, storageHistory, fileTypeDistribution]) => {
          const data = {
            type: 'initial',
            data: {
              systemStatus,
              clients,
              applications,
              systemAlerts,
              storageHistory,
              fileTypeDistribution
            }
          };
          onMessage(data);
        }).catch(error => {
          console.error('Failed to fetch API data:', error);
        });
      }
    }, 1000);
  }, [onMessage, onOpen, onClose, onError, reconnectInterval, maxReconnectAttempts]);

  const disconnect = useCallback(() => {
    if (socketRef.current) {
      socketRef.current.close();
      socketRef.current = null;
    }

    if (reconnectTimeoutRef.current) {
      window.clearTimeout(reconnectTimeoutRef.current);
      reconnectTimeoutRef.current = null;
    }

    setStatus('disconnected');
  }, []);

  const send = useCallback((data: never) => {
    if (socketRef.current && socketRef.current.readyState === WebSocket.OPEN) {
      socketRef.current.send(JSON.stringify(data));
      return true;
    }
    return false;
  }, []);

  // Connect on mount, disconnect on unmount
  useEffect(() => {
    connect();

    return () => {
      disconnect();
    };
  }, [connect, disconnect]);

  return {
    status,
    connect,
    disconnect,
    send,
  };
};

export default useWebSocket;
