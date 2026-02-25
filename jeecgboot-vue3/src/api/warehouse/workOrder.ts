import { defHttp } from '/@/utils/http/axios';

const BASE_URL = '/warehouse/workOrder';

export const workOrderApi = {
  list: (params?: any) => defHttp.get({ url: `${BASE_URL}/list`, params }),
  add: (params: any) => defHttp.post({ url: `${BASE_URL}/add`, params }),
  edit: (params: any) => defHttp.put({ url: `${BASE_URL}/edit`, params }),
  delete: (params: { id: string }) => defHttp.delete({ url: `${BASE_URL}/delete`, params }),
  deleteBatch: (params: { ids: string }) => defHttp.delete({ url: `${BASE_URL}/deleteBatch`, params }),
  queryById: (id: string) => defHttp.get({ url: `${BASE_URL}/queryById`, params: { id } }),
  start: (id: string, operator?: string) =>
    defHttp.put({ url: `${BASE_URL}/start/${id}`, params: { operator } }),
  complete: (id: string, actualQuantity: number, operator?: string) =>
    defHttp.put({ url: `${BASE_URL}/complete/${id}`, params: { actualQuantity, operator } }),
  cancel: (id: string, reason?: string, operator?: string) =>
    defHttp.put({ url: `${BASE_URL}/cancel/${id}`, params: { reason, operator } }),
  updateStage: (params: {
    stageId: string;
    status: string;
    actualDurationHours?: number;
    notes?: string;
    operator?: string;
  }) => defHttp.put({ url: `${BASE_URL}/stage/update`, params }),
  getStages: (workOrderId: string) =>
    defHttp.get({ url: `${BASE_URL}/stages`, params: { workOrderId } }),
  getStatistics: () => defHttp.get({ url: `${BASE_URL}/statistics` }),
  getByStatus: (status: string) => defHttp.get({ url: `${BASE_URL}/getByStatus`, params: { status } }),
};
