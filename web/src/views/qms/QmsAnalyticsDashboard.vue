<template>
  <div class="p-6" v-auth="'qms:analytics:view'">
    <!-- Filter Bar -->
    <div class="mb-6 bg-white p-4 rounded shadow-sm">
      <a-row :gutter="16" align="middle">
        <a-col :span="6">
          <a-range-picker
            v-model:value="dateRange"
            :placeholder="['Từ ngày', 'Đến ngày']"
            style="width: 100%"
            format="YYYY-MM-DD"
            valueFormat="YYYY-MM-DD"
          />
        </a-col>
        <a-col :span="4">
          <a-select
            v-model:value="filterType"
            placeholder="Loại kiểm tra"
            allowClear
            style="width: 100%"
          >
            <a-select-option value="iqc">IQC</a-select-option>
            <a-select-option value="pqc">PQC</a-select-option>
            <a-select-option value="fqc">FQC</a-select-option>
          </a-select>
        </a-col>
        <a-col :span="4">
          <a-input v-model:value="filterProductId" placeholder="Mã sản phẩm" allowClear />
        </a-col>
        <a-col :span="4">
          <a-input v-model:value="filterSupplierId" placeholder="Mã nhà cung cấp" allowClear />
        </a-col>
        <a-col :span="6">
          <a-space>
            <a-button type="primary" @click="loadDashboard">
              🔍 Lọc
            </a-button>
            <a-button @click="resetFilters">Xóa lọc</a-button>
            <a-button v-if="filterSupplierId" @click="viewSupplierReport">
              📋 Báo cáo NCC
            </a-button>
            <a-dropdown>
              <template #overlay>
                <a-menu @click="handleExport">
                  <a-menu-item key="excel">📊 Xuất Excel</a-menu-item>
                  <a-menu-item key="pdf">📄 Xuất PDF</a-menu-item>
                </a-menu>
              </template>
              <a-button>
                📥 Xuất báo cáo
                <DownOutlined />
              </a-button>
            </a-dropdown>
          </a-space>
        </a-col>
      </a-row>
    </div>

    <!-- Summary Cards -->
    <a-spin :spinning="loading">
      <a-row :gutter="16" class="mb-6">
        <a-col :span="6">
          <a-card size="small" class="summary-card">
            <a-statistic
              title="IQC — Đầu vào"
              :value="iqcPassRate"
              suffix="%"
              :value-style="{ color: rateColor(iqcPassRate) }"
            />
            <div class="text-gray-500 text-xs mt-1">
              Đạt: {{ dashboardData.iqcPassed || 0 }} / Tổng: {{ dashboardData.iqcTotal || 0 }} | Lỗi: {{ dashboardData.iqcFailed || 0 }}
            </div>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card size="small" class="summary-card">
            <a-statistic
              title="PQC — Sản xuất"
              :value="pqcPassRate"
              suffix="%"
              :value-style="{ color: rateColor(pqcPassRate) }"
            />
            <div class="text-gray-500 text-xs mt-1">
              Đạt: {{ dashboardData.pqcPassed || 0 }} / Tổng: {{ dashboardData.pqcTotal || 0 }} | Lỗi: {{ dashboardData.pqcFailed || 0 }}
            </div>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card size="small" class="summary-card">
            <a-statistic
              title="FQC — Thành phẩm"
              :value="fqcPassRate"
              suffix="%"
              :value-style="{ color: rateColor(fqcPassRate) }"
            />
            <div class="text-gray-500 text-xs mt-1">
              Đạt: {{ dashboardData.fqcPassed || 0 }} / Tổng: {{ dashboardData.fqcTotal || 0 }} | Lỗi: {{ dashboardData.fqcFailed || 0 }}
            </div>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card size="small" class="summary-card">
            <a-statistic
              title="NCR đang mở"
              :value="dashboardData.openNcrCount || 0"
              :value-style="{ color: (dashboardData.openNcrCount || 0) > 0 ? '#cf1322' : '#3f8600' }"
            />
            <div class="text-gray-500 text-xs mt-1">
              Báo cáo sự không phù hợp chưa đóng
            </div>
          </a-card>
        </a-col>
      </a-row>

      <!-- Charts Row -->
      <a-row :gutter="16">
        <!-- Line Chart: Quality Trend -->
        <a-col :span="14">
          <a-card title="📈 Xu hướng chất lượng" size="small" class="mb-4">
            <template #extra>
              <a-radio-group v-model:value="trendGroupBy" size="small" @change="loadTrend">
                <a-radio-button value="week">Tuần</a-radio-button>
                <a-radio-button value="month">Tháng</a-radio-button>
              </a-radio-group>
            </template>
            <div ref="trendChartRef" style="height: 320px; width: 100%"></div>
          </a-card>
        </a-col>

        <!-- Bar Chart: Pareto Analysis -->
        <a-col :span="10">
          <a-card title="📊 Pareto — Top 5 tiêu chí lỗi" size="small" class="mb-4">
            <div ref="paretoChartRef" style="height: 320px; width: 100%"></div>
          </a-card>
        </a-col>
      </a-row>
    </a-spin>
  </div>
</template>

<script lang="ts" setup>
import { ref, Ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { DownOutlined } from '@ant-design/icons-vue';
import { useMessage } from '/@/hooks/web/useMessage';
import { useECharts } from '/@/hooks/web/useECharts';

const router = useRouter();
import {
  qmsAnalyticsApi,
  type DashboardSummaryModel,
  type TrendDataModel,
  type ParetoItemModel,
} from '/@/api/wms/qmsAnalytics';
import dayjs from 'dayjs';

const { createMessage } = useMessage();
const router = useRouter();

// --- Filter state ---
const dateRange = ref<[string, string] | null>(null);
const filterType = ref<string | undefined>(undefined);
const filterProductId = ref('');
const filterSupplierId = ref('');
const trendGroupBy = ref<'week' | 'month'>('week');

// --- Data state ---
const loading = ref(false);
const dashboardData = ref<DashboardSummaryModel>({});
const trendData = ref<TrendDataModel[]>([]);
const paretoData = ref<ParetoItemModel[]>([]);

// --- Charts ---
const trendChartRef = ref<HTMLDivElement | null>(null);
const paretoChartRef = ref<HTMLDivElement | null>(null);
const { setOptions: setTrendOptions } = useECharts(trendChartRef as Ref<HTMLDivElement>);
const { setOptions: setParetoOptions } = useECharts(paretoChartRef as Ref<HTMLDivElement>);

// --- Computed pass rates ---
const iqcPassRate = computed(() => {
  const total = dashboardData.value.iqcTotal || 0;
  if (total === 0) return 0;
  return Math.round(((dashboardData.value.iqcPassed || 0) / total) * 100);
});

const pqcPassRate = computed(() => {
  const total = dashboardData.value.pqcTotal || 0;
  if (total === 0) return 0;
  return Math.round(((dashboardData.value.pqcPassed || 0) / total) * 100);
});

const fqcPassRate = computed(() => {
  const total = dashboardData.value.fqcTotal || 0;
  if (total === 0) return 0;
  return Math.round(((dashboardData.value.fqcPassed || 0) / total) * 100);
});

function rateColor(rate: number): string {
  if (rate >= 90) return '#3f8600';
  if (rate >= 70) return '#faad14';
  return '#cf1322';
}

// --- Filter helpers ---
function getFilterParams() {
  const params: Record<string, any> = {};
  if (dateRange.value && dateRange.value[0]) {
    params.startDate = dateRange.value[0];
    params.endDate = dateRange.value[1];
  }
  if (filterProductId.value) params.productId = filterProductId.value;
  if (filterSupplierId.value) params.supplierId = filterSupplierId.value;
  return params;
}

function resetFilters() {
  dateRange.value = null;
  filterType.value = undefined;
  filterProductId.value = '';
  filterSupplierId.value = '';
  loadDashboard();
}

function viewSupplierReport() {
  if (filterSupplierId.value) {
    router.push({ path: '/qms/supplier-report', query: { supplierId: filterSupplierId.value } });
  }
}

// --- Data loading ---
async function loadDashboard() {
  loading.value = true;
  try {
    const params = getFilterParams();
    const [summaryRes] = await Promise.all([
      qmsAnalyticsApi.dashboard(params),
      loadTrend(),
      loadPareto(),
    ]);
    dashboardData.value = summaryRes || {};
  } catch (e) {
    createMessage.error('Không thể tải dữ liệu dashboard');
  } finally {
    loading.value = false;
  }
}

async function loadTrend() {
  try {
    const params = getFilterParams();
    const startDate = params.startDate || dayjs().subtract(3, 'month').format('YYYY-MM-DD');
    const endDate = params.endDate || dayjs().format('YYYY-MM-DD');

    const res = await qmsAnalyticsApi.trend({
      startDate,
      endDate,
      groupBy: trendGroupBy.value,
    });
    trendData.value = res || [];
    renderTrendChart();
  } catch (e) {
    console.error('Failed to load trend data', e);
  }
}

async function loadPareto() {
  try {
    const params = getFilterParams();
    const res = await qmsAnalyticsApi.pareto(params);
    paretoData.value = res || [];
    renderParetoChart();
  } catch (e) {
    console.error('Failed to load pareto data', e);
  }
}

// --- Chart rendering ---
function renderTrendChart() {
  const periods = trendData.value.map((d) => d.period || '');
  const passedSeries = trendData.value.map((d) => d.passed || 0);
  const failedSeries = trendData.value.map((d) => d.failed || 0);
  const failRateSeries = trendData.value.map((d) => d.failRate || 0);

  setTrendOptions({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' },
    },
    legend: {
      data: ['Đạt', 'Không đạt', 'Tỷ lệ lỗi (%)'],
      bottom: 0,
    },
    grid: { left: '3%', right: '4%', bottom: '12%', containLabel: true },
    xAxis: {
      type: 'category',
      data: periods,
      axisLabel: { rotate: 30 },
    },
    yAxis: [
      { type: 'value', name: 'Số lượng' },
      { type: 'value', name: 'Tỷ lệ (%)', max: 100, axisLabel: { formatter: '{value}%' } },
    ],
    series: [
      {
        name: 'Đạt',
        type: 'line',
        data: passedSeries,
        smooth: true,
        itemStyle: { color: '#52c41a' },
      },
      {
        name: 'Không đạt',
        type: 'line',
        data: failedSeries,
        smooth: true,
        itemStyle: { color: '#ff4d4f' },
      },
      {
        name: 'Tỷ lệ lỗi (%)',
        type: 'line',
        yAxisIndex: 1,
        data: failRateSeries,
        smooth: true,
        lineStyle: { type: 'dashed' },
        itemStyle: { color: '#faad14' },
      },
    ],
  });
}

function renderParetoChart() {
  const names = paretoData.value.map((d) => d.criterionName || '');
  const counts = paretoData.value.map((d) => d.failureCount || 0);

  setParetoOptions({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
    },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: names,
      axisLabel: {
        rotate: 20,
        interval: 0,
      },
    },
    yAxis: {
      type: 'value',
      name: 'Số lần lỗi',
    },
    series: [
      {
        type: 'bar',
        data: counts,
        barMaxWidth: 50,
        itemStyle: {
          color: function (params: any) {
            const colors = ['#ff4d4f', '#ff7a45', '#ffa940', '#ffc53d', '#ffec3d'];
            return colors[params.dataIndex] || '#ffc53d';
          },
        },
        label: {
          show: true,
          position: 'top',
        },
      },
    ],
  });
}

// --- Export ---
async function handleExport({ key }: { key: string }) {
  try {
    const params = { ...getFilterParams(), format: key as 'excel' | 'pdf' };
    const res = await qmsAnalyticsApi.export(params);
    const blob = new Blob([res.data], {
      type: key === 'excel'
        ? 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
        : 'application/pdf',
    });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `qms-report.${key === 'excel' ? 'xlsx' : 'pdf'}`;
    link.click();
    window.URL.revokeObjectURL(url);
    createMessage.success('Xuất báo cáo thành công');
  } catch (e) {
    createMessage.error('Không thể xuất báo cáo');
  }
}

// --- Navigate to supplier report ---
function viewSupplierReport() {
  if (filterSupplierId.value) {
    router.push({ path: '/qms/supplier-report', query: { supplierId: filterSupplierId.value } });
  }
}

// --- Init ---
onMounted(() => {
  dateRange.value = [
    dayjs().subtract(3, 'month').format('YYYY-MM-DD'),
    dayjs().format('YYYY-MM-DD'),
  ];
  loadDashboard();
});
</script>

<style scoped>
.summary-card {
  border-radius: 8px;
  transition: all 0.2s;
}
.summary-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}
</style>
