import { defHttp } from '/@/utils/http/axios';
import { PageResult, PageParams } from './types';

const BASE = '/api/qms/report';

// --- Models ---

/** Inspection history record */
export interface InspectionHistoryModel {
  id: string;
  executionCode: string;
  templateId: string;
  templateName?: string;
  productId: string;
  productName?: string;
  stageType: 'iqc' | 'pqc' | 'fqc';
  inspector?: string;
  inspectionDate?: string;
  overallResult?: 'pass' | 'fail';
  status: string;
  approvedBy?: string;
  approvedTime?: string;
  createTime?: string;
}

/** Field-level statistics */
export interface FieldStatisticsModel {
  fieldId: string;
  fieldName: string;
  fieldType: string;
  totalEvaluations: number;
  passCount: number;
  failCount: number;
  passRate: number;
  failRate: number;
}

/** Template-level statistics */
export interface InspectionStatisticsModel {
  templateId: string;
  templateName: string;
  totalExecutions: number;
  passCount: number;
  failCount: number;
  passRate: number;
  failRate: number;
  fieldStatistics?: FieldStatisticsModel[];
}

/** Pareto analysis item */
export interface ParetoItemModel {
  rank: number;
  fieldId: string;
  fieldName: string;
  fieldType: string;
  totalEvaluations: number;
  failCount: number;
  failRate: number;
  cumulativeRate: number;
}

/** Pareto analysis result */
export interface ParetoAnalysisModel {
  templateId: string;
  templateName: string;
  items: ParetoItemModel[];
}

// --- Filter Params ---

export interface HistoryFilterParams extends PageParams {
  productId?: string;
  templateId?: string;
  startDate?: string;
  endDate?: string;
  overallResult?: 'pass' | 'fail';
  inspector?: string;
  stageType?: 'iqc' | 'pqc' | 'fqc';
}

export interface StatisticsParams {
  templateId: string;
  startDate?: string;
  endDate?: string;
}

export interface ExportParams {
  templateId?: string;
  format: 'pdf' | 'excel';
  startDate?: string;
  endDate?: string;
  productId?: string;
}

// --- API ---

export const wmsReportApi = {
  /** Lịch sử kiểm tra (phân trang + filter) */
  history: (params?: HistoryFilterParams) =>
    defHttp.get<PageResult<InspectionHistoryModel>>({ url: `${BASE}/history`, params }),

  /** Thống kê pass/fail theo template */
  statistics: (params: StatisticsParams) =>
    defHttp.get<InspectionStatisticsModel>({ url: `${BASE}/statistics`, params }),

  /** Pareto analysis - top 5 fields có tỷ lệ fail cao nhất */
  pareto: (params: StatisticsParams) =>
    defHttp.get<ParetoAnalysisModel>({ url: `${BASE}/pareto`, params }),

  /** Xuất báo cáo (PDF/Excel) - returns blob */
  export: (params: ExportParams) =>
    defHttp.get<Blob>(
      { url: `${BASE}/export`, params, responseType: 'blob' },
      { isTransformResponse: false, isReturnNativeResponse: true }
    ),
};
