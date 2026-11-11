export default {
  install(app) {
    let socket;
    let isConnecting = false;
    let isManuallyClosed = false;
    const eventCallbacks = new Map();
    let messageListenerAdded = false;

    const socketApi = {
      connect() {
        if (socket && socket.readyState === WebSocket.OPEN) {
          console.log('WebSocket уже подключён, пропускаю connect()');
          return;
        }

        if (isConnecting) {
          console.log('Уже идёт подключение, жду завершения');
          return;
        }

        const serverUrl = `ws://${window.location.host}/api/v1/streaming/ws`;

        console.log('Connecting to:', serverUrl);

        isConnecting = true;
        isManuallyClosed = false;
        messageListenerAdded = false;

        if (socket && socket.readyState !== WebSocket.CLOSED) {
          console.log('Закрываю старое соединение перед новым');
          socket.onclose = null;
          socket.close();
        }

        socket = new WebSocket(serverUrl);

        socket.onopen = () => {
          console.log('WebSocket connected...');
          isConnecting = false;
        };

        socket.onmessage = (event) => {
          let serverMessage = { type: 'none' };
          const msg = event.data;

          console.log('RAW WS MESSAGE:', msg);

          try {
            serverMessage = JSON.parse(msg);
            console.log('PARSED WS MESSAGE:', serverMessage);
          } catch (e) {
            console.log('WS parse error:', e);
          }
        };

        socket.onerror = (e) => {
          console.log('WebSocket error:', e);
        };

        socket.onclose = (e) => {
          console.log(
            `[close] Соединение закрыто, код=${e.code}, reason="${e.reason}"`
          );

          isConnecting = false;

          if (isManuallyClosed) {
            console.log('Закрыто намеренно, не переподключаюсь');
            return;
          }

          console.log('Переподключение через 5 сек...');

          setTimeout(() => {
            if (!isManuallyClosed) this.connect();
          }, 5000);
        };
      },

      isConnected() {
        return socket?.readyState === WebSocket.OPEN;
      },

      sendMessage(payload) {
        if (!payload?.data) {
          return Promise.reject(new Error('Invalid payload'));
        }

        if (!payload.data.authorId) {
          const myId =
            app.config.globalProperties.$store?.state?.profile?.info?.info
              ?.id;

          if (myId) {
            payload.data.authorId = myId;
          }
        }

        if (!payload.data.clientId) {
  payload.data.clientId =
    (typeof crypto !== 'undefined' && crypto.randomUUID)
      ? crypto.randomUUID()
      : `${Date.now()}-${Math.random().toString(16).slice(2)}`;
}

        if (!payload.data.readStatus) {
          payload.data.readStatus = 'SENT';
        }

        const message = JSON.stringify(payload);

        console.log('SEND WS MESSAGE:', message);

        if (!socket) {
          console.error('Socket not initialized');
          return Promise.reject(new Error('Socket not initialized'));
        }

        if (socket.readyState !== WebSocket.OPEN) {
          console.error('Socket not open (readyState:', socket.readyState, ')');
          return Promise.reject(new Error('Socket not open'));
        }

        try {
          socket.send(message);
          return Promise.resolve();
        } catch (error) {
          console.error('Error sending message:', error);
          return Promise.reject(error);
        }
      },

        subscribe(eventType, callback) {
        console.log('SUBSCRIBE TO EVENT:', eventType);

        if (!socket) {
          console.warn('Socket not ready for subscribe');
          return;
        }
         if (!eventCallbacks.has(eventType)) {
          eventCallbacks.set(eventType, []);
        }
        eventCallbacks.get(eventType).push(callback);

        if (!messageListenerAdded) {
          messageListenerAdded = true;
          socket.addEventListener('message', (event) => {
            let serverMessage = null;

            console.log('RAW WS MESSAGE IN SUBSCRIBE:', event.data);

          try {
            serverMessage = JSON.parse(event.data);
            console.log('PARSED WS MESSAGE IN SUBSCRIBE:', serverMessage);
          } catch (e) {
            console.log('Parse error:', e);
          }

          eventCallbacks.forEach((callbacks, type) => {
              if (serverMessage.type === type) {
                callbacks.forEach(cb => cb(serverMessage));
              }
     });
          });
        }
      },
      unsubscribe(eventType, callback) {
        if (eventCallbacks.has(eventType)) {
          const callbacks = eventCallbacks.get(eventType);
          const index = callbacks.indexOf(callback);
          if (index > -1) {
            callbacks.splice(index, 1);
            console.log(`Unsubscribed from ${eventType}`);
          }
        }
      },
    };

    app.config.globalProperties.$socket = socketApi;
  },
};