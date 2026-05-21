import { defHttp } from '/@/utils/http/axios';

const BASE = '/qms/analytics';

// --- Types ---

export interface SupplierReportModel {
  supplierId?: string;
  supplierName?: string;
  iqcTotal?: number;
  iqcPassed?: number;
  iqcPassRate?: number;
  ncrCount?: number;
  ranking?: number;
  totalSuppliers?: number;
}

export interface InspectionTypeSummary {
  type: string;
  total: number;
  passed: number;
  failed: number;
  passRate: number;
}

export interface DashboardResponse {
  inspectionSummaries: InspectionTypeSummary[];
  openNcrCount: number;
}

export interface TrendDataPoint {
  period: string;
  passed: number;
  failed: number;
  total: number;
  failRate: number;
}

export interface ParetoItem {
  criterionName: string;
  failureCount: number;
  failureRate: number;
}

// --- API ---

export const qmsAnalyticsApi = {
  /** Dashboard tổng hợp */
  dashboard: (params?: any) =>
    defHttp.get<DashboardResponse>({ url: `${BASE}/dashboard`, params }),

  /** Xu hướng chất lượng theo thời gian */
  trend: (params: { startDate?: string; endDate?: string; groupBy?: string }) =>
    defHttp.get<TrendDataPoint[]>({ url: `${BASE}/trend`, params }),

  /** Báo cáo chất lượng nhà cung cấp */
  supplier: (supplierId: string) =>
    defHttp.get<SupplierReportModel>({ url: `${BASE}/supplier/${supplierId}` }),

  /** Pareto analysis - top 5 tiêu chí lỗi */
  pareto: (params?: any) =>
    defHttp.get<ParetoItem[]>({ url: `${BASE}/pareto`, params }),

  /** Xuất báo cáo */
  export: (params: { format: string; [key: string]: any }) =>
    defHttp.get<any>({ url: `${BASE}/export`, params, responseType: 'blob' }),
};

/** Alias for backward compatibility */
export const wmsAnalyticsApi = qmsAnalyticsApi;
