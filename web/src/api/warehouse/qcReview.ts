import { defHttp } from '/@/utils/http/axios';

const BASE = '/warehouse/qms/review';

export const qcReviewApi = {
  list: (params?: any) => defHttp.get({ url: `${BASE}/list`, params }),
  byWorkOrder: (workOrderId: string) =>
    defHttp.get({ url: `${BASE}/byWorkOrder`, params: { workOrderId } }),
  queryById: (id: string) => defHttp.get({ url: `${BASE}/queryById`, params: { id } }),
  submit: (id: string, reviewer?: string) =>
    defHttp.put({ url: `${BASE}/submit/${id}`, params: { reviewer } }),
  approve: (id: string, approver?: string, overallResult?: string, notes?: string) =>
    defHttp.put({ url: `${BASE}/approve/${id}`, params: { approver, overallResult, notes } }),
  reject: (id: string, approver?: string, reason?: string) =>
    defHttp.put({ url: `${BASE}/reject/${id}`, params: { approver, reason } }),
  syncStats: (id: string) => defHttp.put({ url: `${BASE}/syncStats/${id}` }),
};
