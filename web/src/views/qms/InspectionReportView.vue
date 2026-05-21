<template>
  <div class="p-4 inspection-report">
    <a-tabs v-model:activeKey="activeTab">
      <!-- Tab 1: Lịch sử kiểm tra -->
      <a-tab-pane key="history" tab="Lịch sử kiểm tra">
        <BasicTable @register="registerHistoryTable">
          <template #toolbar>
            <a-button @click="handleExport('excel')">
              <FileExcelOutlined /> Xuất Excel
            </a-button>
            <a-button @click="handleExport('pdf')">
              <FilePdfOutlined /> Xuất PDF
            </a-button>
          </template>
          <template #stageType="{ record }">
            <a-tag :color="stageTypeColor(record.stageType)">
              {{ stageTypeLabel(record.stageType) }}
            </a-tag>
          </template>
          <template #overallResult="{ record }">
            <a-tag v-if="record.overallResult === 'pass'" color="green">PASS</a-tag>
            <a-tag v-else-if="record.overallResult === 'fail'" color="red">FAIL</a-tag>
            <a-tag v-else color="default">--</a-tag>
          </template>
        </BasicTable>
      </a-tab-pane>

      <!-- Tab 2: Thống kê -->
      <a-tab-pane key="statistics" tab="Thống kê chất lượng">
        <a-row :gutter="16" class="mb-4">
          <a-col :span="6">
            <a-card>
              <a-statistic title="Tổng phiên kiểm tra" :value="stats.total" />
            </a-card>
          </a-col>
          <a-col :span="6">
            <a-card>
              <a-statistic
                title="Tỷ lệ PASS"
                :value="stats.passRate"
                suffix="%"
                :value-style="{ color: '#3f8600' }"
              />
            </a-card>
          </a-col>
          <a-col :span="6">
            <a-card>
              <a-statistic
                title="Tỷ lệ FAIL"
                :value="stats.failRate"
                suffix="%"
                :value-style="{ color: '#cf1322' }"
              />
            </a-card>
          </a-col>
          <a-col :span="6">
            <a-card>
              <a-statistic title="Chờ phê duyệt" :value="stats.pendingCount" />
            </a-card>
          </a-col>
        </a-row>

        <a-card title="Biểu đồ thống kê" class="mb-4">
          <div ref="chartRef" class="chart-container"></div>
        </a-card>
      </a-tab-pane>

      <!-- Tab 3: Pareto -->
      <a-tab-pane key="pareto" tab="Phân tích Pareto">
        <a-card title="Top 5 trường có tỷ lệ FAIL cao nhất">
          <div ref="paretoChartRef" class="chart-container"></div>
        </a-card>
      </a-tab-pane>
    </a-tabs>
  </div>
</template>

<script lang="ts" name="inspection-report-view" setup>
  import { ref, reactive, onMounted } from 'vue';
  import { FileExcelOutlined, FilePdfOutlined } from '@ant-design/icons-vue';
  import { BasicTable, useTable } from '/@/components/Table';
  import { useMessage } from '/@/hooks/web/useMessage';

  const { createMessage } = useMessage();

  const activeTab = ref('history');
  const chartRef = ref<HTMLElement | null>(null);
  const paretoChartRef = ref<HTMLElement | null>(null);

  const stats = reactive({
    total: 0,
    passRate: 0,
    failRate: 0,
    pendingCount: 0,
  });

  // --- History Table ---
  const [registerHistoryTable] = useTable({
    title: 'Lịch sử kiểm tra',
    api: async (params: any) => {
      // TODO: Connect to report API when available
      return { items: [], total: 0 };
    },
    columns: [
      { title: 'Mã phiên', dataIndex: 'executionCode', width: 150 },
      { title: 'Template', dataIndex: 'templateName', width: 200 },
      { title: 'Sản phẩm', dataIndex: 'productName', width: 180 },
      { title: 'Loại QC', dataIndex: 'stageType', slots: { customRender: 'stageType' }, width: 100 },
      { title: 'Người kiểm tra', dataIndex: 'inspector', width: 140 },
      { title: 'Ngày kiểm tra', dataIndex: 'inspectionDate', width: 130 },
      { title: 'Kết quả', dataIndex: 'overallResult', slots: { customRender: 'overallResult' }, width: 100 },
      { title: 'Người phê duyệt', dataIndex: 'approvedBy', width: 140 },
      { title: 'Ngày phê duyệt', dataIndex: 'approvedTime', width: 160 },
    ],
    formConfig: {
      labelWidth: 100,
      schemas: [
        {
          field: 'stageType',
          label: 'Loại QC',
          component: 'Select',
          colProps: { span: 6 },
          componentProps: {
            options: [
              { label: 'IQC', value: 'iqc' },
              { label: 'PQC', value: 'pqc' },
              { label: 'FQC', value: 'fqc' },
            ],
            allowClear: true,
            placeholder: 'Tất cả',
          },
        },
        {
          field: 'overallResult',
          label: 'Kết quả',
          component: 'Select',
          colProps: { span: 6 },
          componentProps: {
            options: [
              { label: 'PASS', value: 'pass' },
              { label: 'FAIL', value: 'fail' },
            ],
            allowClear: true,
            placeholder: 'Tất cả',
          },
        },
        {
          field: 'dateRange',
          label: 'Thời gian',
          component: 'RangePicker',
          colProps: { span: 8 },
        },
      ],
      autoSubmitOnEnter: true,
    },
    useSearchForm: true,
    showTableSetting: true,
    bordered: true,
    showIndexColumn: true,
  });

  // --- Helpers ---
  function stageTypeColor(type: string) {
    const map: Record<string, string> = { iqc: 'blue', pqc: 'orange', fqc: 'green' };
    return map[type] || 'default';
  }

  function stageTypeLabel(type: string) {
    const map: Record<string, string> = { iqc: 'IQC', pqc: 'PQC', fqc: 'FQC' };
    return map[type] || type?.toUpperCase();
  }

  function handleExport(format: 'excel' | 'pdf') {
    createMessage.info(`Đang xuất báo cáo ${format.toUpperCase()}...`);
    // TODO: Connect to report export API
  }

  // --- Init ---
  onMounted(async () => {
    // TODO: Load statistics from API
  });
</script>

<style scoped>
  .inspection-report {
    max-width: 1400px;
    margin: 0 auto;
  }

  .chart-container {
    width: 100%;
    height: 400px;
  }
</style>
