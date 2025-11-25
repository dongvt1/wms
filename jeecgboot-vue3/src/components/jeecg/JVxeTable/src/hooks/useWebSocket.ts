import { watch, onUnmounted } from 'vue';
import { buildUUID } from '/@/utils/uuid';
import { useGlobSetting } from '/@/hooks/setting';
import { useUserStore } from '/@/store/modules/user';
import { JVxeDataProps, JVxeTableMethods, JVxeTableProps } from '../types';
import { isArray } from '/@/utils/is';
import { getToken } from '/@/utils/auth';

// vxe socket
const vs = {
  // Page unique id，Used to identify the same user，different pageswebsocket
  pageId: buildUUID(),
  // webSocket object
  ws: null,
  // some constants
  constants: {
    // Message type
    TYPE: 'type',
    // message data
    DATA: 'data',
    // Message type：Heartbeat detection
    TYPE_HB: 'heart_beat',
    // Message type：renewvxe tabledata
    TYPE_UVT: 'update_vxe_table',
  },
  // Heartbeat detection
  heartCheck: {
    // Interval time，How often to send heartbeat messages
    interval: 10000,
    // Heartbeat message timeout，How long does it take for heartbeat messages to be reconnected after no reply?
    timeout: 6000,
    timeoutTimer: -1,
    clear() {
      clearTimeout(this.timeoutTimer);
      return this;
    },
    start() {
      vs.sendMessage(vs.constants.TYPE_HB, '');
      // If it has not been reset after a certain period of time，It means that the backend is actively disconnected.
      this.timeoutTimer = window.setTimeout(() => {
        vs.reconnect();
      }, this.timeout);
      return this;
    },
    // Heartbeat message return
    back() {
      this.clear();
      window.setTimeout(() => this.start(), this.interval);
    },
  },

  /** initialization WebSocket */
  initialWebSocket() {
    if (this.ws === null) {
      const userId = useUserStore().getUserInfo?.id;
      const domainURL = useGlobSetting().uploadUrl!;
      const domain = domainURL.replace('https://', 'wss://').replace('http://', 'ws://');
      const url = `${domain}/vxeSocket/${userId}/${this.pageId}`;
      //update-begin-author:taoyan date:2022-4-24 for: v2.4.6 of websocket Server，There are performance and security issues。 #3278
      let token = (getToken() || '') as string;
      this.ws = new WebSocket(url, [token]);
      //update-end-author:taoyan date:2022-4-24 for: v2.4.6 of websocket Server，There are performance and security issues。 #3278
      this.ws.onopen = this.on.open.bind(this);
      this.ws.onerror = this.on.error.bind(this);
      this.ws.onmessage = this.on.message.bind(this);
      this.ws.onclose = this.on.close.bind(this);
    }
  },

  // Send message
  sendMessage(type, message) {
    try {
      let ws = this.ws;
      if (ws != null && ws.readyState === ws.OPEN) {
        ws.send(
          JSON.stringify({
            type: type,
            data: message,
          })
        );
      }
    } catch (err: any) {
      console.warn('【JVxeWebSocket】Send message失败：(' + err.code + ')');
    }
  },

  /** Bind globalVXEsheet */
  tableMap: new Map(),
  /** Add binding */
  addBind(map, key, value: VmArgs) {
    let binds = map.get(key);
    if (isArray(binds)) {
      binds.push(value);
    } else {
      map.set(key, [value]);
    }
  },
  /** Remove binding */
  removeBind(map, key, value: VmArgs) {
    let binds = map.get(key);
    if (isArray(binds)) {
      for (let i = 0; i < binds.length; i++) {
        let bind = binds[i];
        if (bind === value) {
          binds.splice(i, 1);
          break;
        }
      }
      if (binds.length === 0) {
        map.delete(key);
      }
    } else {
      map.delete(key);
    }
  },
  // 呼叫绑定of表单
  callBind(map, key, callback) {
    let binds = map.get(key);
    if (isArray(binds)) {
      binds.forEach(callback);
    }
  },

  lockReconnect: false,
  /** Try to reconnect */
  reconnect() {
    if (this.lockReconnect) return;
    this.lockReconnect = true;
    setTimeout(() => {
      if (this.ws && this.ws.close) {
        this.ws.close();
      }
      this.ws = null;
      console.info('【JVxeWebSocket】Try to reconnect...');
      this.initialWebSocket();
      this.lockReconnect = false;
    }, 5000);
  },

  on: {
    open() {
      console.info('【JVxeWebSocket】Connection successful');
      this.heartCheck.start();
    },
    error(e) {
      console.warn('【JVxeWebSocket】Connection error occurred:', e);
      this.reconnect();
    },
    message(e) {
      // Parse the message
      let json;
      try {
        json = JSON.parse(e.data);
      } catch (e: any) {
        console.warn('【JVxeWebSocket】收到无法解析of消息:', e.data);
        return;
      }
      let type = json[this.constants.TYPE];
      let data = json[this.constants.DATA];
      switch (type) {
        // Heartbeat detection
        case this.constants.TYPE_HB:
          this.heartCheck.back();
          break;
        // renewformdata
        case this.constants.TYPE_UVT:
          this.callBind(this.tableMap, data.socketKey, (args) => this.onVM.onUpdateTable(args, ...data.args));
          break;
        default:
          console.warn('【JVxeWebSocket】收到不识别ofMessage type:' + type);
          break;
      }
    },
    close(e) {
      console.info('【JVxeWebSocket】connection closed:', e);
      this.reconnect();
    },
  },

  onVM: {
    /** 收到renewsheetof消息 */
    onUpdateTable({ props, data, methods }: VmArgs, row, caseId) {
      if (data.caseId !== caseId) {
        const tableRow = methods.getIfRowById(row.id).row;
        // 局部保renewdata
        if (tableRow) {
          if (props.reloadEffect) {
            data.reloadEffectRowKeysMap[row.id] = true;
          }
          Object.assign(tableRow, row, { id: tableRow.id });
          methods.getXTable().reloadRow(tableRow);
        }
      }
    },
  },
} as {
  ws: Nullable<WebSocket>;
} & Recordable;

type VmArgs = {
  props: JVxeTableProps;
  data: JVxeDataProps;
  methods: JVxeTableMethods;
};

export function useWebSocket(props: JVxeTableProps, data: JVxeDataProps, methods) {
  const args: VmArgs = { props, data, methods };
  watch(
    () => props.socketReload,
    (socketReload: boolean) => {
      if (socketReload) {
        vs.initialWebSocket();
        vs.addBind(vs.tableMap, props.socketKey, args);
      } else {
        vs.removeBind(vs.tableMap, props.socketKey, args);
      }
    },
    { immediate: true }
  );

  /** sendsocket消息renew行 */
  function socketSendUpdateRow(row) {
    vs.sendMessage(vs.constants.TYPE_UVT, {
      socketKey: props.socketKey,
      args: [row, data.caseId],
    });
  }

  onUnmounted(() => {
    vs.removeBind(vs.tableMap, props.socketKey, args);
  });

  return {
    socketSendUpdateRow,
  };
}
