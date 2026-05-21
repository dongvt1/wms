import { defHttp } from '/@/utils/http/axios';
import { PageResult, PageParams } from './types';

const BASE = '/wms/qc/review';

export interface QcReviewModel {
  id?: string;
  workOrderId?: string;
  reviewer?: string;
  approver?: string;
  overallResult?: string;
  status?: string;
  notes?: string;
  createTime?: string;
}

export const wmsQcReviewApi = {
  list: (params?: PageParams) => defHttp.get<PageResult<QcReviewModel>>({ url: `${BASE}/list`, params }),
  queryById: (id: string) => defHttp.get<QcReviewModel>({ url: `${BASE}/queryById`, params: { id } }),
  byWorkOrder: (workOrderId: string) => defHttp.get({ url: `${BASE}/byWorkOrder`, params: { workOrderId } }),
  submit: (id: string, reviewer?: string) => defHttp.put({ url: `${BASE}/submit/${id}`, params: { reviewer } }),
  approve: (id: string, approver?: string, overallResult?: string, notes?: string) =>
    defHttp.put({ url: `${BASE}/approve/${id}`, params: { approver, overallResult, notes } }),
  reject: (id: string, approver?: string, reason?: string) =>
    defHttp.put({ url: `${BASE}/reject/${id}`, params: { approver, reason } }),
  syncStats: (id: string) => defHttp.put({ url: `${BASE}/syncStats/${id}` }),
  /** Get auto-calculated suggested overall result */
  suggest: (id: string) => defHttp.get<{ result: string }>({ url: `/qms/review/suggest/${id}` }),
  /** Override the overall result with a reason (Quản_lý_QC only) */
  override: (id: string, params: { result: string; reason: string; operator?: string }) =>
    defHttp.put({ url: `/qms/review/override/${id}`, params }),
};
