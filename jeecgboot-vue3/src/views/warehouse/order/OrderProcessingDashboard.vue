<template>
  <div class="order-processing-dashboard">
    <Row :gutter="16">
      <!-- Statistics Cards -->
      <Col :span="6" v-for="(stat, index) in statistics" :key="index">
        <Card>
          <Statistic
            :title="stat.title"
            :value="stat.value"
            :prefix="stat.prefix"
            :suffix="stat.suffix"
            :value-style="{ color: stat.color }"
          />
        </Card>
      </Col>
    </Row>

    <Row :gutter="16" class="mt-4">
      <!-- Processing Actions -->
      <Col :span="12">
        <Card title="Processing Actions" :bordered="false">
          <Space direction="vertical" style="width: 100%">
            <Button type="primary" @click="handleAutoConfirm" :loading="autoConfirmLoading">
              <template #icon><ThunderboltOutlined /></template>
              Auto Confirm Orders
            </Button>
            <Button @click="handleProcessNotifications" :loading="processNotificationsLoading">
              <template #icon><MailOutlined /></template>
              Process Pending Notifications
            </Button>
            <Button @click="handleRefreshStatistics">
              <template #icon><ReloadOutlined /></template>
              Refresh Statistics
            </Button>
          </Space>
        </Card>
      </Col>

      <!-- Processing Status -->
      <Col :span="12">
        <Card title="Processing Status" :bordered="false">
          <div class="processing-status">
            <div class="status-item" v-for="(item, index) in processingStatus" :key="index">
              <div class="status-label">{{ item.label }}</div>
              <Progress 
                :percent="item.percent" 
                :status="item.status" 
                :stroke-color="item.color"
                :show-info="true"
              />
            </div>
          </div>
        </Card>
      </Col>
    </Row>

    <Row :gutter="16" class="mt-4">
      <!-- Recent Processing Logs -->
      <Col :span="24">
        <Card title="Recent Processing Logs" :bordered="false">
          <BasicTable @register="registerTable" />
        </Card>
      </Col>
    </Row>
  </div>
</template>

<script lang="ts" setup>
  import { ref, reactive, onMounted } from 'vue';
  import { Row, Col, Card, Statistic, Button, Space, Progress, message } from 'ant-design-vue';
  import { ThunderboltOutlined, MailOutlined, ReloadOutlined } from '@ant-design/icons-vue';
  import { BasicTable, useTable } from '/@/components/Table';
  import { orderApi } from './order.api';

  // Recordable type definition
  type Recordable<T = any> = Record<string, T>;

  const autoConfirmLoading = ref(false);
  const processNotificationsLoading = ref(false);
  const statistics = ref<Recordable[]>([]);
  const processingStatus = ref<Recordable[]>([]);

  const [registerTable, { reload: tableReload, setTableData }] = useTable({
    title: 'Recent Processing Logs',
    columns: [
      {
        title: 'Order ID',
        dataIndex: 'orderId',
        width: 120,
      },
      {
        title: 'Action',
        dataIndex: 'action',
        width: 120,
        customRender: ({ text }) => {
          const actionMap: Recordable<string> = {
            'CREATE': 'Create',
            'CONFIRM': 'Confirm',
            'CANCEL': 'Cancel',
            'SHIP': 'Ship',
            'COMPLETE': 'Complete',
            'STATUS_UPDATE': 'Status Update',
          };
          return actionMap[text] || text;
        },
      },
      {
        title: 'Status',
        dataIndex: 'status',
        width: 100,
        customRender: ({ text }) => {
          const color = text === 'SUCCESS' ? 'green' : text === 'FAILED' ? 'red' : 'orange';
          return `<a-tag color="${color}">${text}</a-tag>`;
        },
      },
      {
        title: 'Processing Time',
        dataIndex: 'processingTime',
        width: 120,
        customRender: ({ text }) => {
          return text ? `${text}ms` : '-';
        },
      },
      {
        title: 'Create Time',
        dataIndex: 'createTime',
        width: 150,
        customRender: ({ text }) => {
          return text ? new Date(text).toLocaleString() : '-';
        },
      },
    ],
    bordered: true,
    showIndexColumn: false,
    pagination: {
      pageSize: 10,
    },
    canResize: false,
    api: orderApi.getProcessingLogs,
    beforeFetch: (params) => {
      // This is a placeholder - in a real implementation, you would need
      // an API endpoint to get recent processing logs across all orders
      return params;
    },
  });

  onMounted(() => {
    loadStatistics();
  });

  async function loadStatistics() {
    try {
      const stats = await orderApi.getProcessingStatistics();
      if (stats.success) {
        updateStatistics(stats.result);
      }
    } catch (error) {
      console.error('Failed to load statistics:', error);
    }
  }

  function updateStatistics(data: Recordable) {
    statistics.value = [
      {
        title: 'Total Logs',
        value: data.totalLogs || 0,
        color: '#1890ff',
      },
      {
        title: 'Success Rate',
        value: data.totalLogs > 0 ? Math.round((data.successCount / data.totalLogs) * 100) : 0,
        suffix: '%',
        color: '#52c41a',
      },
      {
        title: 'Failed Count',
        value: data.failedCount || 0,
        color: '#ff4d4f',
      },
      {
        title: 'Avg Processing Time',
        value: Math.round(data.avgProcessingTime || 0),
        suffix: 'ms',
        color: '#722ed1',
      },
    ];

    const total = data.successCount + data.failedCount + data.pendingCount;
    processingStatus.value = [
      {
        label: 'Success',
        percent: total > 0 ? Math.round((data.successCount / total) * 100) : 0,
        status: 'success',
        color: '#52c41a',
      },
      {
        label: 'Failed',
        percent: total > 0 ? Math.round((data.failedCount / total) * 100) : 0,
        status: 'exception',
        color: '#ff4d4f',
      },
      {
        label: 'Pending',
        percent: total > 0 ? Math.round((data.pendingCount / total) * 100) : 0,
        status: 'active',
        color: '#faad14',
      },
    ];
  }

  async function handleAutoConfirm() {
    autoConfirmLoading.value = true;
    try {
      const result = await orderApi.autoConfirm();
      if (result.success) {
        message.success(`Auto confirm completed: ${result.result.confirmedCount} confirmed, ${result.result.failedCount} failed`);
        loadStatistics();
        tableReload();
      } else {
        message.error(result.message || 'Auto confirm failed');
      }
    } catch (error) {
      message.error('Auto confirm failed');
    } finally {
      autoConfirmLoading.value = false;
    }
  }

  async function handleProcessNotifications() {
    processNotificationsLoading.value = true;
    try {
      const result = await orderApi.processNotifications();
      if (result.success) {
        message.success(result.result);
        loadStatistics();
      } else {
        message.error(result.message || 'Process notifications failed');
      }
    } catch (error) {
      message.error('Process notifications failed');
    } finally {
      processNotificationsLoading.value = false;
    }
  }

  function handleRefreshStatistics() {
    loadStatistics();
    tableReload();
  }
</script>

<style scoped>
.order-processing-dashboard {
  padding: 16px;
}

.mt-4 {
  margin-top: 16px;
}

.processing-status {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.status-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.status-label {
  font-weight: 500;
  font-size: 14px;
}
</style>