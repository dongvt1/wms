import { defHttp } from '/@/utils/http/axios';
import { UploadFileParams } from '/#/axios';
import { AxiosProgressEvent } from 'axios';
import { PageResult, PageParams } from './types';

const BASE = '/wms/shelf';

export interface WarehouseShelfModel {
  id?: string;
  shelfCode: string;
  shelfName: string;
  areaId: string;
  areaName?: string;
  description?: string;
  status: number;
  capacity: number;
  usedCapacity: number;
  createTime?: string;
  updateTime?: string;
}

export const wmsShelfApi = {
  list: (params?: PageParams) => defHttp.get<PageResult<WarehouseShelfModel>>({ url: `${BASE}/list`, params }),
  queryById: (params: { id: string }) => defHttp.get<WarehouseShelfModel>({ url: `${BASE}/queryById`, params }),
  add: (params: WarehouseShelfModel) => defHttp.post({ url: `${BASE}/add`, params }),
  edit: (params: WarehouseShelfModel) => defHttp.put({ url: `${BASE}/edit`, params }),
  delete: (params: { id: string }) => defHttp.delete({ url: `${BASE}/delete`, params }),
  deleteBatch: (params: { ids: string }) => defHttp.delete({ url: `${BASE}/deleteBatch`, params }),
  exportXls: (params?: any) => defHttp.get({ url: `${BASE}/exportXls`, params, responseType: 'blob' }, { isTransformResponse: false }),
  importExcel: (params: UploadFileParams, onUploadProgress?: (e: AxiosProgressEvent) => void) =>
    defHttp.uploadFile<any>({ url: `${BASE}/importExcel`, onUploadProgress }, params),
};
