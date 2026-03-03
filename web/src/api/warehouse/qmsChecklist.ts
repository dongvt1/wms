import { defHttp } from '/@/utils/http/axios';

const BASE = '/qms/checklist';

export const qmsChecklistApi = {
  list: (params?: any) => defHttp.get({ url: `${BASE}/list`, params }),
  add: (params: any) => defHttp.post({ url: `${BASE}/add`, params }),
  edit: (params: any) => defHttp.put({ url: `${BASE}/edit`, params }),
  delete: (params: { id: string }) => defHttp.delete({ url: `${BASE}/delete`, params }),
  deleteBatch: (params: { ids: string }) => defHttp.delete({ url: `${BASE}/deleteBatch`, params }),
  queryById: (id: string) => defHttp.get({ url: `${BASE}/queryById`, params: { id } }),
  getItems: (templateId: string) => defHttp.get({ url: `${BASE}/getItems`, params: { templateId } }),
  listActive: (inspectionType?: string) => defHttp.get({ url: `${BASE}/listActive`, params: { inspectionType } }),
};
