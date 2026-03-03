import { defHttp } from '/@/utils/http/axios';

const BASE = '/qms/iqc';

export const iqcApi = {
  list: (params?: any) => defHttp.get({ url: `${BASE}/list`, params }),
  add: (params: any) => defHttp.post({ url: `${BASE}/add`, params }),
  edit: (params: any) => defHttp.put({ url: `${BASE}/edit`, params }),
  delete: (params: { id: string }) => defHttp.delete({ url: `${BASE}/delete`, params }),
  deleteBatch: (params: { ids: string }) => defHttp.delete({ url: `${BASE}/deleteBatch`, params }),
  queryById: (id: string) => defHttp.get({ url: `${BASE}/queryById`, params: { id } }),
  getResults: (inspectionId: string) => defHttp.get({ url: `${BASE}/getResults`, params: { inspectionId } }),
  submitForApproval: (id: string) => defHttp.put({ url: `${BASE}/submit/${id}` }),
  approve: (id: string, status: string, notes?: string, operator?: string) =>
    defHttp.put({ url: `${BASE}/approve/${id}`, params: { status, notes, operator } }),
  statistics: () => defHttp.get({ url: `${BASE}/statistics` }),
};
