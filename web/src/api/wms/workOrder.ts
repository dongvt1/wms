import { defHttp } from '/@/utils/http/axios';
import { PageResult, PageParams } from './types';

const BASE = '/wms/work-order';

export interface WorkOrderModel {
  id?: string;
  workOrderCode?: string;
  productId?: string;
  productName?: string;
  bomId?: string;
  bomName?: string;
  productionLineId?: string;
  productionLineName?: string;
  plannedQuantity?: number;
  actualQuantity?: number;
  status?: string; // 'PLANNED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED'
  startDate?: string;
  endDate?: string;
  operator?: string;
  notes?: string;
  createTime?: string;
}

export const wmsWorkOrderApi = {
  list: (params?: PageParams) => defHttp.get<PageResult<WorkOrderModel>>({ url: `${BASE}/list`, params }),
  queryById: (id: string) => defHttp.get<WorkOrderModel>({ url: `${BASE}/queryById`, params: { id } }),
  add: (params: any) => defHttp.post({ url: `${BASE}/add`, params }),
  edit: (params: any) => defHttp.put({ url: `${BASE}/edit`, params }),
  delete: (params: { id: string }) => defHttp.delete({ url: `${BASE}/delete`, params }),
  deleteBatch: (params: { ids: string }) => defHttp.delete({ url: `${BASE}/deleteBatch`, params }),
  start: (id: string, operator?: string) => defHttp.put({ url: `${BASE}/start/${id}`, params: { operator } }),
  complete: (id: string, actualQuantity: number, operator?: string) =>
    defHttp.put({ url: `${BASE}/complete/${id}`, params: { actualQuantity, operator } }),
  cancel: (id: string, reason?: string, operator?: string) =>
    defHttp.put({ url: `${BASE}/cancel/${id}`, params: { reason, operator } }),
  getStages: (workOrderId: string) => defHttp.get({ url: `${BASE}/stages`, params: { workOrderId } }),
  updateStage: (params: { stageId: string; status: string; actualDurationHours?: number; notes?: string; operator?: string }) =>
    defHttp.put({ url: `${BASE}/stage/update`, params }),
  getStatistics: () => defHttp.get({ url: `${BASE}/statistics` }),
  getByStatus: (status: string) => defHttp.get({ url: `${BASE}/getByStatus`, params: { status } }),
};
