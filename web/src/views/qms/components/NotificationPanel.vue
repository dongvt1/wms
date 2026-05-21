<template>
  <div class="qms-notification-panel">
    <div class="panel-header">
      <span class="panel-title">Thông báo QMS</span>
      <a-button type="link" size="small" :disabled="!hasUnread" @click="handleMarkAllRead">
        Đánh dấu tất cả đã đọc
      </a-button>
    </div>

    <a-spin :spinning="loading">
      <a-list
        class="notification-list"
        :data-source="notifications"
        :locale="{ emptyText: 'Không có thông báo' }"
      >
        <template #renderItem="{ item }">
          <a-list-item
            :class="['notification-item', { 'notification-item--unread': !item.isRead }]"
            @click="handleClickNotification(item)"
          >
            <a-list-item-meta>
              <template #title>
                <div class="notification-title">
                  <a-badge v-if="!item.isRead" status="processing" />
                  <span>{{ item.title }}</span>
                </div>
              </template>
              <template #description>
                <div class="notification-content">{{ item.content }}</div>
                <div class="notification-time">{{ item.createTime }}</div>
              </template>
              <template #avatar>
                <a-avatar :style="getAvatarStyle(item.entityType)" size="small">
                  {{ getEntityLabel(item.entityType) }}
                </a-avatar>
              </template>
            </a-list-item-meta>
            <template #actions>
              <a-button
                v-if="!item.isRead"
                type="link"
                size="small"
                @click.stop="handleMarkRead(item)"
              >
                Đã đọc
              </a-button>
            </template>
          </a-list-item>
        </template>
      </a-list>
    </a-spin>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useMessage } from '/@/hooks/web/useMessage';
import { qmsNotificationApi, QmsNotificationModel } from '/@/api/wms/qmsNotification';

const emit = defineEmits<{
  (e: 'close'): void;
  (e: 'countChange'): void;
}>();

const router = useRouter();
const { createMessage } = useMessage();

const notifications = ref<QmsNotificationModel[]>([]);
const loading = ref(false);

const hasUnread = computed(() => notifications.value.some((n) => !n.isRead));

onMounted(() => {
  loadNotifications();
});

async function loadNotifications() {
  loading.value = true;
  try {
    const res = await qmsNotificationApi.list({ pageNo: 1, pageSize: 20 });
    notifications.value = res?.records || [];
  } catch (e) {
    console.error('Failed to load QMS notifications', e);
    notifications.value = [];
  } finally {
    loading.value = false;
  }
}

async function handleMarkRead(item: QmsNotificationModel) {
  if (!item.id) return;
  try {
    await qmsNotificationApi.markRead(item.id);
    item.isRead = 1;
    emit('countChange');
  } catch (e) {
    createMessage.error('Đánh dấu đã đọc thất bại');
  }
}

async function handleMarkAllRead() {
  try {
    await qmsNotificationApi.markAllRead();
    notifications.value.forEach((n) => (n.isRead = 1));
    emit('countChange');
    createMessage.success('Đã đánh dấu tất cả đã đọc');
  } catch (e) {
    createMessage.error('Thao tác thất bại');
  }
}

function handleClickNotification(item: QmsNotificationModel) {
  // Mark as read if unread
  if (!item.isRead) {
    handleMarkRead(item);
  }

  // Navigate to the related entity
  if (item.entityType && item.entityId) {
    const route = getEntityRoute(item.entityType);
    if (route) {
      router.push(route);
      emit('close');
    }
  }
}

function getEntityRoute(entityType: string): string | null {
  const routeMap: Record<string, string> = {
    iqc: '/qms/iqc',
    pqc: '/qms/pqc',
    fqc: '/qms/fqc',
    ncr: '/qms/ncr',
    review: '/qms/review',
  };
  return routeMap[entityType] || null;
}

function getEntityLabel(entityType?: string): string {
  const labelMap: Record<string, string> = {
    iqc: 'IQC',
    pqc: 'PQC',
    fqc: 'FQC',
    ncr: 'NCR',
    review: 'RV',
  };
  return labelMap[entityType || ''] || 'QMS';
}

function getAvatarStyle(entityType?: string): Record<string, string> {
  const colorMap: Record<string, string> = {
    iqc: '#1890ff',
    pqc: '#52c41a',
    fqc: '#722ed1',
    ncr: '#f5222d',
    review: '#fa8c16',
  };
  return {
    backgroundColor: colorMap[entityType || ''] || '#999',
    fontSize: '10px',
  };
}

defineExpose({ loadNotifications });
</script>

<style scoped>
.qms-notification-panel {
  width: 340px;
  max-height: 420px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px 8px;
  border-bottom: 1px solid #f0f0f0;
}

.panel-title {
  font-weight: 600;
  font-size: 14px;
}

.notification-list {
  max-height: 360px;
  overflow-y: auto;
}

.notification-item {
  cursor: pointer;
  transition: background-color 0.2s;
}

.notification-item:hover {
  background-color: #f5f5f5;
}

.notification-item--unread {
  background-color: #e6f7ff;
}

.notification-item--unread:hover {
  background-color: #bae7ff;
}

.notification-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
}

.notification-content {
  font-size: 12px;
  color: #666;
  margin-top: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 220px;
}

.notification-time {
  font-size: 11px;
  color: #999;
  margin-top: 4px;
}
</style>
