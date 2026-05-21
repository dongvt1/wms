<template>
  <BasicModal
    v-bind="$attrs"
    title="Transaction Reports"
    :width="1000"
    @register="registerModal"
    :showOkBtn="false"
    :destroyOnClose="true"
  >
    <Tabs v-model="activeTab">
      <TabPane key="statistics" tab="Transaction Statistics">
        <Card title="Transaction Statistics" :bordered="false">
          <Row :gutter="16">
            <Col :span="6">
              <Statistic title="Total Transactions" :value="statistics.totalTransactions" />
            </Col>
            <Col :span="6">
              <Statistic title="Stock In" :value="statistics.totalInTransactions" :value-style="{ color: '#3f8600' }" />
            </Col>
            <Col :span="6">
              <Statistic title="Stock Out" :value="statistics.totalOutTransactions" :value-style="{ color: '#cf1322' }" />
            </Col>
            <Col :span="6">
              <Statistic title="Transfers" :value="statistics.totalTransferTransactions" :value-style="{ color: '#1890ff' }" />
            </Col>
          </Row>
          
          <Divider />
          
          <Row :gutter="16">
            <Col :span="12">
              <Card title="Transaction Types" size="small">
                <div v-for="(value, key) in statistics.transactionsByType" :key="key" class="mb-2">
                  <div class="flex justify-between">
                    <span>{{ getTransactionTypeName(String(key)) }}</span>
                    <Tag :color="getTransactionTypeColor(String(key))">{{ value }}</Tag>
                  </div>
                </div>
              </Card>
            </Col>
            <Col :span="12">
              <Card title="Date Range" size="small">
                <p><strong>From:</strong> {{ statistics.startDate }}</p>
                <p><strong>To:</strong> {{ statistics.endDate }}</p>
              </Card>
            </Col>
          </Row>
        </Card>
      </TabPane>
      
      <TabPane key="summary" tab="Transaction Summary">
        <Card title="Transaction Summary" :bordered="false">
          <BasicForm @register="registerDateForm" />
          <div class="mt-4">
            <a-button type="primary" @click="loadTransactionSummary" :loading="summaryLoading">
              Generate Report
            </a-button>
            <a-button class="ml-2" @click="exportSummary" :disabled="summaryData.length === 0">
              Export to Excel
            </a-button>
          </div>
          
          <BasicTable
            v-if="summaryData.length > 0"
            class="mt-4"
            :columns="summaryColumns"
            :dataSource="summaryData"
            :pagination="false"
            :bordered="true"
          />
        </Card>
      </TabPane>
    </Tabs>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, reactive } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form';
  import { BasicTable } from '/@/components/Table';
  import { Card, Row, Col, Statistic, Divider, Tabs, TabPane, Tag, Button } from 'ant-design-vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { stockTransactionApi } from './stockTransaction.api';
  import { stockTransactionColumns } from './stockTransaction.data';

  // Recordable type definition
  type Recordable<T = any> = Record<string, T>;

  const { createMessage } = useMessage();
  const activeTab = ref('statistics');
  const statistics = ref<Recordable>({});
  const summaryData = ref<Recordable[]>([]);
  const summaryLoading = ref(false);

  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    setModalProps({ confirmLoading: false });
    
    // Load default statistics for current month
    const endDate = new Date();
    const startDate = new Date();
    startDate.setMonth(startDate.getMonth() - 1);
    
    await loadTransactionStatistics(startDate, endDate);
  });

  const [registerDateForm, { setFieldsValue, getFieldsValue, validate }] = useForm({
    labelWidth: 120,
    schemas: [
      {
        field: 'dateRange',
        label: 'Date Range',
        component: 'RangePicker',
        required: true,
        componentProps: {
          valueFormat: 'YYYY-MM-DD',
          style: { width: '100%' },
        },
        colProps: { span: 12 },
      },
    ],
    showActionButtonGroup: false,
    baseColProps: { span: 24 },
  });

  const summaryColumns = reactive([
    ...stockTransactionColumns.filter(col => col.dataIndex !== 'action'),
  ]);

  async function loadTransactionStatistics(startDate: Date, endDate: Date) {
    try {
      const result = await stockTransactionApi.getStatistics({
        startDate: startDate.toISOString().split('T')[0],
        endDate: endDate.toISOString().split('T')[0],
      });
      statistics.value = result;
    } catch (error) {
      console.error('Failed to load transaction statistics:', error);
      createMessage.error('Failed to load transaction statistics');
    }
  }

  async function loadTransactionSummary() {
    try {
      summaryLoading.value = true;
      const values = await validate();
      
      if (values.dateRange && values.dateRange.length === 2) {
        const [startDate, endDate] = values.dateRange;
        const result = await stockTransactionApi.list({
          transactionDate_begin: startDate,
          transactionDate_end: endDate,
          pageSize: 1000, // Load all records for report
        });
        summaryData.value = result.records || [];
      }
    } catch (error) {
      console.error('Failed to load transaction summary:', error);
      createMessage.error('Failed to load transaction summary');
    } finally {
      summaryLoading.value = false;
    }
  }

  function exportSummary() {
    const values = getFieldsValue();
    if (values.dateRange && values.dateRange.length === 2) {
      const [startDate, endDate] = values.dateRange;
      stockTransactionApi.exportXls({
        transactionDate_begin: startDate,
        transactionDate_end: endDate,
      });
    }
  }

  function getTransactionTypeName(type: string) {
    switch (type) {
      case 'IN':
        return 'Stock In';
      case 'OUT':
        return 'Stock Out';
      case 'TRANSFER':
        return 'Transfer';
      default:
        return type;
    }
  }

  function getTransactionTypeColor(type: string) {
    switch (type) {
      case 'IN':
        return 'green';
      case 'OUT':
        return 'red';
      case 'TRANSFER':
        return 'blue';
      default:
        return 'default';
    }
  }
</script>

<style scoped>
  .mb-2 {
    margin-bottom: 8px;
  }
  
  .mt-4 {
    margin-top: 16px;
  }
  
  .ml-2 {
    margin-left: 8px;
  }
  
  .flex {
    display: flex;
  }
  
  .justify-between {
    justify-content: space-between;
  }
</style>