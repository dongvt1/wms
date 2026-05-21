// noinspection JSUnusedGlobalSymbols

import { unref } from 'vue';
import { useWebSocket, WebSocketResult } from '@vueuse/core';
import { getToken } from '/@/utils/auth';

let result: WebSocketResult<any>;
const listeners = new Map();

/**
 * turn on WebSocket Link，Only needs to be executed once globally
 * @param url
 */
export function connectWebSocket(url: string) {
  //update-begin-author:taoyan date:2022-4-24 for: v2.4.6 of websocket Server，There are performance and security issues。 #3278
  const token = (getToken() || '') as string;
  result = useWebSocket(url, {
    // Automatically reconnect (Encountered errors and repeated connections at most10Second-rate)
    autoReconnect: {
      retries : 10,
      delay : 5000
    },
    // Heartbeat detection
    heartbeat: {
      message: "ping",
      interval: 55000
    },
    protocols: [token],
    // update-begin--author:liaozhiyang---date:20240726---for：[issues/6662] Demo systemsocketGeneral break，Change the way of writing
    onConnected: function (ws) {
      console.log('[WebSocket] Connection successful', ws);
    },
    onDisconnected: function (ws, event) {
      console.log('[WebSocket] Lost connection：', ws, event);
    },
    onError: function (ws, event) {
      console.log('[WebSocket] Connection error occurred: ', ws, event);
    },
    onMessage: function (_ws, e) {
      console.debug('[WebSocket] -----receive messages-------', e.data);
      try {
        //update-begin---author:wangshuai---date:2024-05-07---for:【issues/1161】front endwebsocketMonitoring does not work due to heartbeat---
        if (e.data === 'ping') {
          return;
        }
        //update-end---author:wangshuai---date:2024-05-07---for:【issues/1161】front endwebsocketMonitoring does not work due to heartbeat---
        const data = JSON.parse(e.data);
        for (const callback of listeners.keys()) {
          try {
            callback(data);
          } catch (err) {
            console.error(err);
          }
        }
      } catch (err) {
        console.error('[WebSocket] dataParsing failed：', err);
      }
    },
    // update-end--author:liaozhiyang---date:20240726---for：[issues/6662] Demo systemsocketGeneral break，Change the way of writing
  });
  // update-begin--author:liaozhiyang---date:20240726---for：[issues/6662] Demo systemsocketGeneral break，Change the way of writing
  //update-end-author:taoyan date:2022-4-24 for: v2.4.6 of websocket Server，There are performance and security issues。 #3278
  // if (result) {
  //   result.open = onOpen;
  //   result.close = onClose;

  //   const ws = unref(result.ws);
  //   if(ws!=null){
  //     ws.onerror = onError;
  //     ws.onmessage = onMessage;
  //     //update-begin---author:wangshuai---date:2024-04-30---for:【issues/1217】After sending the test message，The bell number does not change---
  //     ws.onopen = onOpen;
  //     ws.onclose = onClose;
  //     //update-end---author:wangshuai---date:2024-04-30---for:【issues/1217】After sending the test message，The bell number does not change---
  //   }
  // }
  // update-end--author:liaozhiyang---date:20240726---for：[issues/6662] Demo systemsocketGeneral break，Change the way of writing
}

function onOpen() {
  console.log('[WebSocket] Connection successful');
}

function onClose(e) {
  console.log('[WebSocket] Lost connection：', e);
}

function onError(e) {
  console.log('[WebSocket] Connection error occurred: ', e);
}

function onMessage(e) {
  console.debug('[WebSocket] -----receive messages-------', e.data);
  try {
    //update-begin---author:wangshuai---date:2024-05-07---for:【issues/1161】front endwebsocketMonitoring does not work due to heartbeat---
    if(e==='ping'){
      return;
    }
    //update-end---author:wangshuai---date:2024-05-07---for:【issues/1161】front endwebsocketMonitoring does not work due to heartbeat---
    const data = JSON.parse(e.data);
    for (const callback of listeners.keys()) {
      try {
        callback(data);
      } catch (err) {
        console.error(err);
      }
    }
  } catch (err) {
    console.error('[WebSocket] dataParsing failed：', err);
  }
}


/**
 * Add to WebSocket Message listening
 * @param callback
 */
export function onWebSocket(callback: (data: object) => any) {
  if (!listeners.has(callback)) {
    if (typeof callback === 'function') {
      listeners.set(callback, null);
    } else {
      console.debug('[WebSocket] Add to WebSocket Message listening失败：传入of参数不是一个方法');
    }
  }
}

/**
 * Lift WebSocket Message listening
 *
 * @param callback
 */
export function offWebSocket(callback: (data: object) => any) {
  listeners.delete(callback);
}

export function useMyWebSocket() {
  return result;
}
