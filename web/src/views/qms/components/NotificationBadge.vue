<template>
  <a-popover
    v-model:open="popoverVisible"
    trigger="click"
    placement="bottomRight"
    overlay-class-name="qms-notification-popover"
  >
    <template #content>
      <NotificationPanel
        ref="panelRef"
        @close="popoverVisible = false"
        @count-change="fetchUnreadCount"
      />
    </template>
    <span class="qms-notification-badge" title="Thông báo QMS">
      <a-badge :count="unreadCount" :overflow-count="99" :offset="[-2, 2]">
        <BellOutlined class="badge-icon" />
      </a-badge>
    </span>
  </a-popover>
</template>

<script lang="ts" setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue';
import { BellOutlined } from '@ant-design/icons-vue';
import { qmsNotificationApi } from '/@/api/wms/qmsNotification';
import NotificationPanel from './NotificationPanel.vue';

const POLL_INTERVAL = 30_000; // 30 seconds

const unreadCount = ref(0);
const popoverVisible = ref(false);
const panelRef = ref<InstanceType<typeof NotificationPanel> | null>(null);
let pollTimer: ReturnType<typeof setInterval> | null = null;

onMounted(() => {
  fetchUnreadCount();
  startPolling();
});

onBeforeUnmount(() => {
  stopPolling();
});

// Reload panel data when popover opens
watch(popoverVisible, (visible) => {
  if (visible) {
    panelRef.value?.loadNotifications();
  }
});

async function fetchUnreadCount() {
  try {
    const count = await qmsNotificationApi.unreadCount();
    unreadCount.value = typeof count === 'number' ? count : 0;
  } catch (e) {
    console.error('Failed to fetch QMS unread count', e);
  }
}

function startPolling() {
  pollTimer = setInterval(fetchUnreadCount, POLL_INTERVAL);
}

function stopPolling() {
  if (pollTimer) {
    clearInterval(pollTimer);
    pollTimer = null;
  }
}
</script>

<style scoped>
.qms-notification-badge {
  display: inline-flex;
  align-items: center;
  cursor: pointer;
  padding: 0 8px;
}

.badge-icon {
  font-size: 18px;
  color: #555;
}

.badge-icon:hover {
  color: #1890ff;
}
</style>

<style>
.qms-notification-popover .ant-popover-inner-content {
  padding: 0;
}
</style>
