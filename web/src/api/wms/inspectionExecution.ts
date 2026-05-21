import { defHttp } from '/@/utils/http/axios';
import { PageResult, PageParams } from './types';

const BASE = '/api/qms/inspection-execution';

// --- DTOs ---

/** Field value submitted for a step */
export interface FieldValueDTO {
  fieldId: string;
  value: string | null;
}

/** Create execution params */
export interface CreateExecutionDTO {
  productId: string;
  stageType: 'iqc' | 'pqc' | 'fqc';
  workOrderId?: string;
  productionStageId?: string;
}

// --- Response Models ---

/** Field evaluation result from backend */
export interface FieldResultVO {
  fieldId: string;
  value: string | null;
  result: 'pass' | 'fail' | 'na' | null;
  message?: string;
}

/** Step submit response */
export interface StepSubmitResultVO {
  stepResult: 'pass' | 'fail' | 'pending';
  fieldResults: FieldResultVO[];
}

/** Field value model in execution detail */
export interface FieldValueModel {
  id: string;
  stepResultId: string;
  fieldId: string;
  fieldName: string;
  fieldType: string;
  fieldConfig: Record<string, any> | null;
  isRequired: boolean;
  actualValue: string | null;
  result: 'pass' | 'fail' | 'na' | null;
  evalMessage: string | null;
  unit?: string;
  sortOrder?: number;
}

/** Step result model in execution detail */
export interface StepResultModel {
  id: string;
  executionId: string;
  stepId: string;
  stepName: string;
  sortOrder: number;
  isMandatory: boolean;
  result: 'pass' | 'fail' | 'pending' | null;
  status: 'pending' | 'completed' | 'approved' | 'rejected' | 're_inspect';
  completedTime: string | null;
  notes: string | null;
  fields: FieldValueModel[];
}

/** Inspection execution detail model */
export interface InspectionExecutionModel {
  id: string;
  executionCode: string;
  templateId: string;
  templateName?: string;
  templateSnapshot?: Record<string, any>;
  productId: string;
  stageType: string;
  workOrderId?: string;
  productionStageId?: string;
  inspector?: string;
  inspectionDate?: string;
  overallResult?: 'pass' | 'fail' | null;
  status: 'draft' | 'in_progress' | 'pending_approval' | 'approved' | 'rejected';
  approvedBy?: string;
  approvedTime?: string;
  notes?: string;
  createBy?: string;
  createTime?: string;
  updateBy?: string;
  updateTime?: string;
  steps: StepResultModel[];
}

// --- Filter Params ---

export interface ExecutionListParams extends PageParams {
  productId?: string;
  stageType?: 'iqc' | 'pqc' | 'fqc';
  status?: string;
  workOrderId?: string;
  inspector?: string;
  startDate?: string;
  endDate?: string;
}

// --- API ---

export const wmsInspectionExecutionApi = {
  /** Danh sách phiên kiểm tra có phân trang + filter */
  list: (params?: ExecutionListParams) =>
    defHttp.get<PageResult<InspectionExecutionModel>>({ url: `${BASE}/list`, params }),

  /** Chi tiết phiên kiểm tra kèm steps + field values */
  queryById: (id: string) =>
    defHttp.get<InspectionExecutionModel>({ url: `${BASE}/${id}` }),

  /** Tạo phiên kiểm tra mới (auto-resolve template) */
  add: (params: CreateExecutionDTO) =>
    defHttp.post<InspectionExecutionModel>({ url: BASE, params }),

  /** Lưu nháp - saves field values without evaluation */
  saveDraft: (id: string, params: { values: FieldValueDTO[] }) =>
    defHttp.put({ url: `${BASE}/${id}/save-draft`, params }),

  /**
   * Lưu giá trị cho một bước + evaluate.
   * - 422: validation errors (missing required fields)
   */
  submitStepValues: (id: string, stepId: string, params: { values: FieldValueDTO[] }) =>
    defHttp.put<StepSubmitResultVO>(
      { url: `${BASE}/${id}/step/${stepId}/values`, params },
      { errorMessageMode: 'none' }
    ),

  /**
   * Submit toàn bộ execution để phê duyệt.
   * - 422: validation errors (mandatory steps incomplete)
   */
  submit: (id: string) =>
    defHttp.put({ url: `${BASE}/${id}/submit` }, { errorMessageMode: 'none' }),
};
