<template>
  <div class="p-4">
    <a-tabs v-model:activeKey="activeTab" @change="onTabChange">
      <!-- Tab 1: Lịch sử kiểm tra -->
      <a-tab-pane key="history" tab="📋 Lịch sử kiểm tra">
        <div class="mb-4 bg-white p-4 rounded shadow-sm">
          <a-row :gutter="16" align="middle">
            <a-col :span="4">
              <a-input
                v-model:value="historyFilters.productId"
                placeholder="Mã sản phẩm"
                allowClear
              />
            </a-col>
            <a-col :span="4">
              <a-select
                v-model:value="historyFilters.templateId"
                placeholder="Template"
                allowClear
                style="width: 100%"
                :options="templateOptions"
                show-search
                :filter-option="filterOption"
              />
            </a-col>
            <a-col :span="5">
              <a-range-picker
                v-model:value="historyDateRange"
                :placeholder="['Từ ngày', 'Đến ngày']"
                style="width: 100%"
                format="YYYY-MM-DD"
                valueFormat="YYYY-MM-DD"
              />
            </a-col>
            <a-col :span="3">
              <a-select
                v-model:value="historyFilters.overallResult"
                placeholder="Kết quả"
                allowClear
                style="width: 100%"
              >
                <a-select-option value="pass">PASS</a-select-option>
                <a-select-option value="fail">FAIL</a-select-option>
              </a-select>
            </a-col>
            <a-col :span="4">
              <a-input
                v-model:value="historyFilters.inspector"
                placeholder="Người kiểm tra"
                allowClear
              />
            </a-col>
            <a-col :span="4">
              <a-space>
                <a-button type="primary" @click="loadHistory">🔍 Tìm kiếm</a-button>
                <a-button @click="resetHistoryFilters">Xóa lọc</a-button>
              </a-space>
            </a-col>
          </a-row>
        </div>

        <a-table
          :columns="historyColumns"
          :data-source="historyData"
          :loading="historyLoading"
          :pagination="historyPagination"
          @change="onHistoryTableChange"
          bordered
          size="middle"
          row-key="id"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.dataIndex === 'stageType'">
              <a-tag :color="stageTypeColor(record.stageType)">
                {{ stageTypeLabel(record.stageType) }}
              </a-tag>
            </template>
            <template v-if="column.dataIndex === 'overallResult'">
              <a-tag :color="record.overallResult === 'pass' ? 'green' : 'red'">
                {{ record.overallResult === 'pass' ? 'PASS' : 'FAIL' }}
              </a-tag>
            </template>
            <template v-if="column.dataIndex === 'status'">
              <a-tag :color="statusColor(record.status)">
                {{ statusLabel(record.status) }}
              </a-tag>
            </template>
          </template>
        </a-table>
      </a-tab-pane>

      <!-- Tab 2: Thống kê -->
      <a-tab-pane key="statistics" tab="📊 Thống kê">
        <div class="mb-4 bg-white p-4 rounded shadow-sm">
          <a-row :gutter="16" align="middle">
            <a-col :span="6">
              <a-select
                v-model:value="statsTemplateId"
                placeholder="Chọn template *"
                style="width: 100%"
                :options="templateOptions"
                show-search
                :filter-option="filterOption"
              />
            </a-col>
            <a-col :span="6">
              <a-range-picker
                v-model:value="statsDateRange"
                :placeholder="['Từ ngày', 'Đến ngày']"
                style="width: 100%"
                format="YYYY-MM-DD"
                valueFormat="YYYY-MM-DD"
              />
            </a-col>
            <a-col :span="4">
              <a-button type="primary" @click="loadStatistics" :disabled="!statsTemplateId">
                📊 Xem thống kê
              </a-button>
            </a-col>
          </a-row>
        </div>

        <a-spin :spinning="statsLoading">
          <a-row :gutter="16" v-if="statisticsData">
            <!-- Donut chart -->
            <a-col :span="10">
              <a-card title="Tỷ lệ Pass/Fail" size="small" class="mb-4">
                <div ref="statsChartRef" style="height: 320px; width: 100%"></div>
                <div class="text-center mt-2 text-gray-500">
                  Tổng: {{ statisticsData.totalExecutions || 0 }} phiên kiểm tra
                </div>
              </a-card>
            </a-col>
            <!-- Field statistics table -->
            <a-col :span="14">
              <a-card title="Thống kê theo trường (Field)" size="small" class="mb-4">
                <a-table
                  :columns="fieldStatsColumns"
                  :data-source="statisticsData.fieldStatistics || []"
                  :pagination="false"
                  size="small"
                  bordered
                  row-key="fieldId"
                >
                  <template #bodyCell="{ column, record }">
                    <template v-if="column.dataIndex === 'passRate'">
                      <span :style="{ color: rateColor(record.passRate) }">
                        {{ record.passRate?.toFixed(1) }}%
                      </span>
                    </template>
                    <template v-if="column.dataIndex === 'failRate'">
                      <span :style="{ color: record.failRate > 10 ? '#cf1322' : '#595959' }">
                        {{ record.failRate?.toFixed(1) }}%
                      </span>
                    </template>
                  </template>
                </a-table>
              </a-card>
            </a-col>
          </a-row>
          <a-empty v-else description="Chọn template để xem thống kê" />
        </a-spin>
      </a-tab-pane>

      <!-- Tab 3: Pareto -->
      <a-tab-pane key="pareto" tab="📈 Pareto">
        <div class="mb-4 bg-white p-4 rounded shadow-sm">
          <a-row :gutter="16" align="middle">
            <a-col :span="6">
              <a-select
                v-model:value="paretoTemplateId"
                placeholder="Chọn template *"
                style="width: 100%"
                :options="templateOptions"
                show-search
                :filter-option="filterOption"
              />
            </a-col>
            <a-col :span="6">
              <a-range-picker
                v-model:value="paretoDateRange"
                :placeholder="['Từ ngày', 'Đến ngày']"
                style="width: 100%"
                format="YYYY-MM-DD"
                valueFormat="YYYY-MM-DD"
              />
            </a-col>
            <a-col :span="4">
              <a-button type="primary" @click="loadPareto" :disabled="!paretoTemplateId">
                📈 Phân tích Pareto
              </a-button>
            </a-col>
          </a-row>
        </div>

        <a-spin :spinning="paretoLoading">
          <a-card v-if="paretoData" title="Top 5 trường có tỷ lệ FAIL cao nhất" size="small">
            <div ref="paretoChartRef" style="height: 380px; width: 100%"></div>
          </a-card>
          <a-empty v-else description="Chọn template để xem biểu đồ Pareto" />
        </a-spin>
      </a-tab-pane>
    </a-tabs>

    <!-- Export buttons (floating) -->
    <div class="export-bar">
      <a-dropdown>
        <template #overlay>
          <a-menu @click="handleExport">
            <a-menu-item key="excel">📊 Xuất Excel</a-menu-item>
            <a-menu-item key="pdf">📄 Xuất PDF</a-menu-item>
          </a-menu>
        </template>
        <a-button type="primary">
          📥 Xuất báo cáo
          <DownOutlined />
        </a-button>
      </a-dropdown>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, Ref, reactive, onMounted, nextTick } from 'vue';
import { DownOutlined } from '@ant-design/icons-vue';
import { useMessage } from '/@/hooks/web/useMessage';
import { useECharts } from '/@/hooks/web/useECharts';
import {
  wmsReportApi,
  type InspectionHistoryModel,
  type InspectionStatisticsModel,
  type ParetoAnalysisModel,
} from '/@/api/wms/report';
import { wmsInspectionTemplateApi } from '/@/api/wms/inspectionTemplate';

const { createMessage } = useMessage();

// --- Tab state ---
const activeTab = ref<string>('history');

// --- Template options (shared across tabs) ---
const templateOptions = ref<{ label: string; value: string }[]>([]);

// --- History tab state ---
const historyLoading = ref(false);
const historyData = ref<InspectionHistoryModel[]>([]);
const historyDateRange = ref<[string, string] | null>(null);
const historyFilters = reactive({
  productId: undefined as string | undefined,
  templateId: undefined as string | undefined,
  overallResult: undefined as string | undefined,
  inspector: undefined as string | undefined,
});
const historyPagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `Tổng ${total} bản ghi`,
});

const historyColumns = [
  { title: 'Mã phiên', dataIndex: 'executionCode', width: 150 },
  { title: 'Template', dataIndex: 'templateName', width: 200, ellipsis: true },
  { title: 'Loại QC', dataIndex: 'stageType', width: 90, align: 'center' },
  { title: 'Người kiểm tra', dataIndex: 'inspector', width: 130 },
  { title: 'Ngày kiểm tra', dataIndex: 'inspectionDate', width: 120 },
  { title: 'Kết quả', dataIndex: 'overallResult', width: 90, align: 'center' },
  { title: 'Trạng thái', dataIndex: 'status', width: 120, align: 'center' },
  { title: 'Người duyệt', dataIndex: 'approvedBy', width: 130 },
  { title: 'Ngày tạo', dataIndex: 'createTime', width: 160 },
];

// --- Statistics tab state ---
const statsLoading = ref(false);
const statsTemplateId = ref<string | undefined>(undefined);
const statsDateRange = ref<[string, string] | null>(null);
const statisticsData = ref<InspectionStatisticsModel | null>(null);

const statsChartRef = ref<HTMLDivElement | null>(null);
const { setOptions: setStatsOptions } = useECharts(statsChartRef as Ref<HTMLDivElement>);

const fieldStatsColumns = [
  { title: 'Tên trường', dataIndex: 'fieldName', width: 180, ellipsis: true },
  { title: 'Kiểu', dataIndex: 'fieldType', width: 100 },
  { title: 'Tổng đánh giá', dataIndex: 'totalEvaluations', width: 110, align: 'center' },
  { title: 'Pass', dataIndex: 'passCount', width: 70, align: 'center' },
  { title: 'Fail', dataIndex: 'failCount', width: 70, align: 'center' },
  { title: 'Tỷ lệ Pass', dataIndex: 'passRate', width: 100, align: 'center' },
  { title: 'Tỷ lệ Fail', dataIndex: 'failRate', width: 100, align: 'center' },
];

// --- Pareto tab state ---
const paretoLoading = ref(false);
const paretoTemplateId = ref<string | undefined>(undefined);
const paretoDateRange = ref<[string, string] | null>(null);
const paretoData = ref<ParetoAnalysisModel | null>(null);

const paretoChartRef = ref<HTMLDivElement | null>(null);
const { setOptions: setParetoOptions } = useECharts(paretoChartRef as Ref<HTMLDivElement>);

// --- Helpers ---

function stageTypeColor(type: string) {
  const map: Record<string, string> = { iqc: 'blue', pqc: 'orange', fqc: 'green' };
  return map[type] || 'default';
}

function stageTypeLabel(type: string) {
  const map: Record<string, string> = { iqc: 'IQC', pqc: 'PQC', fqc: 'FQC' };
  return map[type] || type?.toUpperCase();
}

function statusColor(status: string) {
  const map: Record<string, string> = {
    draft: 'default',
    in_progress: 'processing',
    pending_approval: 'warning',
    approved: 'success',
    rejected: 'error',
  };
  return map[status] || 'default';
}

function statusLabel(status: string) {
  const map: Record<string, string> = {
    draft: 'Nháp',
    in_progress: 'Đang thực hiện',
    pending_approval: 'Chờ duyệt',
    approved: 'Đã duyệt',
    rejected: 'Từ chối',
  };
  return map[status] || status;
}

function rateColor(rate: number): string {
  if (rate >= 90) return '#3f8600';
  if (rate >= 70) return '#faad14';
  return '#cf1322';
}

function filterOption(input: string, option: any) {
  return (option?.label ?? '').toLowerCase().includes(input.toLowerCase());
}

// --- Load template options ---
async function loadTemplateOptions() {
  try {
    const res = await wmsInspectionTemplateApi.list({ pageNo: 1, pageSize: 200 });
    templateOptions.value = (res?.records || []).map((t) => ({
      label: `${t.templateCode} - ${t.templateName}`,
      value: t.id!,
    }));
  } catch (e) {
    console.error('Failed to load template options', e);
  }
}

// --- History tab ---
async function loadHistory() {
  historyLoading.value = true;
  try {
    const params: Record<string, any> = {
      pageNo: historyPagination.current,
      pageSize: historyPagination.pageSize,
    };
    if (historyFilters.productId) params.productId = historyFilters.productId;
    if (historyFilters.templateId) params.templateId = historyFilters.templateId;
    if (historyFilters.overallResult) params.overallResult = historyFilters.overallResult;
    if (historyFilters.inspector) params.inspector = historyFilters.inspector;
    if (historyDateRange.value && historyDateRange.value[0]) {
      params.startDate = historyDateRange.value[0];
      params.endDate = historyDateRange.value[1];
    }

    const res = await wmsReportApi.history(params);
    historyData.value = res?.records || [];
    historyPagination.total = res?.total || 0;
  } catch (e) {
    createMessage.error('Không thể tải lịch sử kiểm tra');
  } finally {
    historyLoading.value = false;
  }
}

function resetHistoryFilters() {
  historyFilters.productId = undefined;
  historyFilters.templateId = undefined;
  historyFilters.overallResult = undefined;
  historyFilters.inspector = undefined;
  historyDateRange.value = null;
  historyPagination.current = 1;
  loadHistory();
}

function onHistoryTableChange(pagination: any) {
  historyPagination.current = pagination.current;
  historyPagination.pageSize = pagination.pageSize;
  loadHistory();
}

// --- Statistics tab ---
async function loadStatistics() {
  if (!statsTemplateId.value) {
    createMessage.warning('Vui lòng chọn template');
    return;
  }
  statsLoading.value = true;
  try {
    const params: Record<string, any> = { templateId: statsTemplateId.value };
    if (statsDateRange.value && statsDateRange.value[0]) {
      params.startDate = statsDateRange.value[0];
      params.endDate = statsDateRange.value[1];
    }
    const res = await wmsReportApi.statistics(params);
    statisticsData.value = res;
    await nextTick();
    renderStatsChart();
  } catch (e) {
    createMessage.error('Không thể tải thống kê');
  } finally {
    statsLoading.value = false;
  }
}

function renderStatsChart() {
  if (!statisticsData.value) return;
  const { passCount = 0, failCount = 0 } = statisticsData.value;

  setStatsOptions({
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)',
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      data: ['PASS', 'FAIL'],
    },
    series: [
      {
        name: 'Kết quả',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 6,
          borderColor: '#fff',
          borderWidth: 2,
        },
        label: {
          show: true,
          formatter: '{b}\n{d}%',
          fontSize: 13,
        },
        emphasis: {
          label: { show: true, fontSize: 16, fontWeight: 'bold' },
        },
        data: [
          { value: passCount, name: 'PASS', itemStyle: { color: '#52c41a' } },
          { value: failCount, name: 'FAIL', itemStyle: { color: '#ff4d4f' } },
        ],
      },
    ],
  });
}

// --- Pareto tab ---
async function loadPareto() {
  if (!paretoTemplateId.value) {
    createMessage.warning('Vui lòng chọn template');
    return;
  }
  paretoLoading.value = true;
  try {
    const params: Record<string, any> = { templateId: paretoTemplateId.value };
    if (paretoDateRange.value && paretoDateRange.value[0]) {
      params.startDate = paretoDateRange.value[0];
      params.endDate = paretoDateRange.value[1];
    }
    const res = await wmsReportApi.pareto(params);
    paretoData.value = res;
    await nextTick();
    renderParetoChart();
  } catch (e) {
    createMessage.error('Không thể tải dữ liệu Pareto');
  } finally {
    paretoLoading.value = false;
  }
}

function renderParetoChart() {
  if (!paretoData.value || !paretoData.value.items?.length) return;

  const items = paretoData.value.items;
  const names = items.map((d) => d.fieldName || '');
  const failCounts = items.map((d) => d.failCount || 0);
  const cumulativeRates = items.map((d) => d.cumulativeRate || 0);

  setParetoOptions({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' },
    },
    legend: {
      data: ['Số lần Fail', 'Tỷ lệ tích lũy (%)'],
      bottom: 0,
    },
    grid: { left: '3%', right: '6%', bottom: '12%', top: '10%', containLabel: true },
    xAxis: {
      type: 'category',
      data: names,
      axisLabel: { rotate: 15, interval: 0 },
    },
    yAxis: [
      { type: 'value', name: 'Số lần Fail' },
      { type: 'value', name: 'Tích lũy (%)', max: 100, axisLabel: { formatter: '{value}%' } },
    ],
    series: [
      {
        name: 'Số lần Fail',
        type: 'bar',
        data: failCounts,
        barMaxWidth: 60,
        itemStyle: {
          color: function (params: any) {
            const colors = ['#ff4d4f', '#ff7a45', '#ffa940', '#ffc53d', '#ffec3d'];
            return colors[params.dataIndex] || '#ffc53d';
          },
        },
        label: { show: true, position: 'top' },
      },
      {
        name: 'Tỷ lệ tích lũy (%)',
        type: 'line',
        yAxisIndex: 1,
        data: cumulativeRates,
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: { width: 2 },
        itemStyle: { color: '#722ed1' },
        label: {
          show: true,
          formatter: '{c}%',
          position: 'top',
        },
      },
    ],
  });
}

// --- Export ---
async function handleExport({ key }: { key: string }) {
  try {
    const params: Record<string, any> = { format: key };
    // Use the current tab's template filter if available
    if (activeTab.value === 'statistics' && statsTemplateId.value) {
      params.templateId = statsTemplateId.value;
      if (statsDateRange.value && statsDateRange.value[0]) {
        params.startDate = statsDateRange.value[0];
        params.endDate = statsDateRange.value[1];
      }
    } else if (activeTab.value === 'pareto' && paretoTemplateId.value) {
      params.templateId = paretoTemplateId.value;
      if (paretoDateRange.value && paretoDateRange.value[0]) {
        params.startDate = paretoDateRange.value[0];
        params.endDate = paretoDateRange.value[1];
      }
    } else {
      // History tab - use history filters
      if (historyFilters.templateId) params.templateId = historyFilters.templateId;
      if (historyDateRange.value && historyDateRange.value[0]) {
        params.startDate = historyDateRange.value[0];
        params.endDate = historyDateRange.value[1];
      }
    }

    const res = await wmsReportApi.export(params as any);
    // Handle blob response
    const blob = new Blob([res], {
      type: key === 'excel'
        ? 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
        : 'application/pdf',
    });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `qms-inspection-report.${key === 'excel' ? 'xlsx' : 'pdf'}`;
    link.click();
    window.URL.revokeObjectURL(url);
    createMessage.success('Xuất báo cáo thành công');
  } catch (e) {
    createMessage.error('Không thể xuất báo cáo');
  }
}

// --- Tab change handler ---
function onTabChange(key: string) {
  activeTab.value = key;
}

// --- Init ---
onMounted(() => {
  loadTemplateOptions();
  loadHistory();
});
</script>

<style scoped>
.export-bar {
  position: fixed;
  bottom: 24px;
  right: 24px;
  z-index: 100;
}
</style>
