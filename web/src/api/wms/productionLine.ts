import { defHttp } from '/@/utils/http/axios';
import { PageResult, PageParams } from './types';

const BASE = '/wms/production-line';

export interface ProductionLineModel {
  id?: string;
  lineCode?: string;
  lineName?: string;
  description?: string;
  capacity?: number;
  status?: string; // 'active' | 'inactive' | 'maintenance'
  createTime?: string;
}

export const wmsProductionLineApi = {
  list: (params?: PageParams) => defHttp.get<PageResult<ProductionLineModel>>({ url: `${BASE}/list`, params }),
  queryById: (params: { id: string }) => defHttp.get<ProductionLineModel>({ url: `${BASE}/queryById`, params }),
  add: (params: any) => defHttp.post({ url: `${BASE}/add`, params }),
  edit: (params: any) => defHttp.put({ url: `${BASE}/edit`, params }),
  delete: (params: { id: string }) => defHttp.delete({ url: `${BASE}/delete`, params }),
  deleteBatch: (params: { ids: string }) => defHttp.delete({ url: `${BASE}/deleteBatch`, params }),
  listAll: () => defHttp.get<ProductionLineModel[]>({ url: `${BASE}/listAll` }),
  getByStatus: (status: string) => defHttp.get<ProductionLineModel[]>({ url: `${BASE}/getByStatus`, params: { status } }),
};
