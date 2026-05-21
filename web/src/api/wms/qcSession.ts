import { defHttp } from '/@/utils/http/axios';
import { PageResult, PageParams } from './types';

const BASE = '/wms/qc/session';

export interface QcSessionModel {
  id?: string;
  workOrderId?: string;
  stageId?: string;
  stageName?: string;
  inspector?: string;
  status?: string;
  result?: string;
  notes?: string;
  createTime?: string;
}

export const wmsQcSessionApi = {
  list: (params?: PageParams) => defHttp.get<PageResult<QcSessionModel>>({ url: `${BASE}/list`, params }),
  queryById: (id: string) => defHttp.get<QcSessionModel>({ url: `${BASE}/queryById`, params: { id } }),
  add: (data: any) => defHttp.post({ url: `${BASE}/add`, params: data }),
  edit: (data: any) => defHttp.put({ url: `${BASE}/edit`, params: data }),
  delete: (params: { id: string }) => defHttp.delete({ url: `${BASE}/delete`, params }),
  listByWorkOrder: (workOrderId: string) => defHttp.get({ url: `${BASE}/listByWorkOrder`, params: { workOrderId } }),
  getValues: (sessionId: string) => defHttp.get({ url: `${BASE}/getValues`, params: { sessionId } }),
  complete: (id: string) => defHttp.put({ url: `${BASE}/complete/${id}` }),
};
