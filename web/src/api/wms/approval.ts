import { defHttp } from '/@/utils/http/axios';
import { PageResult, PageParams } from './types';
import type { InspectionExecutionModel } from './inspectionExecution';

const BASE = '/api/qms/approval';

// --- DTOs ---

/** DTO for approve action */
export interface ApproveDTO {
  comment?: string;
}

/** DTO for reject action */
export interface RejectDTO {
  reason: string;
  stepId?: string;
}

/** DTO for re-inspect action */
export interface ReInspectDTO {
  stepId: string;
  reason: string;
}

// --- Models ---

/** Pending approval item (execution with pending_approval status) */
export type PendingApprovalModel = InspectionExecutionModel;

// --- Filter Params ---

export interface ApprovalListParams extends PageParams {
  stageType?: 'iqc' | 'pqc' | 'fqc';
  productId?: string;
  inspector?: string;
}

// --- API ---

export const wmsApprovalApi = {
  /** Danh sách phiên chờ phê duyệt */
  pending: (params?: ApprovalListParams) =>
    defHttp.get<PageResult<PendingApprovalModel>>({ url: `${BASE}/pending`, params }),

  /** Phê duyệt execution */
  approve: (executionId: string, params: ApproveDTO) =>
    defHttp.put({ url: `${BASE}/${executionId}/approve`, params }),

  /**
   * Từ chối execution (bắt buộc có reason).
   * - 422: validation error nếu thiếu reason
   */
  reject: (executionId: string, params: RejectDTO) =>
    defHttp.put({ url: `${BASE}/${executionId}/reject`, params }, { errorMessageMode: 'none' }),

  /**
   * Yêu cầu kiểm tra lại (chọn step + reason).
   * - 409: conflict nếu step đã approved
   */
  reInspect: (executionId: string, params: ReInspectDTO) =>
    defHttp.put({ url: `${BASE}/${executionId}/re-inspect`, params }, { errorMessageMode: 'none' }),
};
