import { defHttp } from '/@/utils/http/axios';

const BASE = '/warehouse/qms/session';

export const qcSessionApi = {
  list: (params?: any) => defHttp.get({ url: `${BASE}/list`, params }),
  listByWorkOrder: (workOrderId: string) =>
    defHttp.get({ url: `${BASE}/listByWorkOrder`, params: { workOrderId } }),
  add: (data: any) => defHttp.post({ url: `${BASE}/add`, params: data }),
  edit: (data: any) => defHttp.put({ url: `${BASE}/edit`, params: data }),
  delete: (params: { id: string }) => defHttp.delete({ url: `${BASE}/delete`, params }),
  queryById: (id: string) => defHttp.get({ url: `${BASE}/queryById`, params: { id } }),
  getValues: (sessionId: string) => defHttp.get({ url: `${BASE}/getValues`, params: { sessionId } }),
  complete: (id: string) => defHttp.put({ url: `${BASE}/complete/${id}` }),
};
