import { defHttp } from '/@/utils/http/axios';
import { UploadFileParams } from '/#/axios';
import { AxiosProgressEvent } from 'axios';
import { PageResult, PageParams } from './types';

const BASE = '/wms/supplier';

export interface SupplierModel {
  id?: string;
  supplierCode: string;
  supplierName: string;
  contactPerson?: string;
  phone?: string;
  email?: string;
  address?: string;
  status?: number;
  createTime?: string;
  updateTime?: string;
}

export const wmsSupplierApi = {
  list: (params?: PageParams) => defHttp.get<PageResult<SupplierModel>>({ url: `${BASE}/list`, params }),
  queryById: (id: string) => defHttp.get<SupplierModel>({ url: `${BASE}/queryById`, params: { id } }),
  add: (params: SupplierModel) => defHttp.post({ url: `${BASE}/add`, params }),
  edit: (params: SupplierModel) => defHttp.put({ url: `${BASE}/edit`, params }),
  delete: (id: string) => defHttp.delete({ url: `${BASE}/delete`, params: { id } }),
  deleteBatch: (ids: string) => defHttp.delete({ url: `${BASE}/deleteBatch`, params: { ids } }),
  getByCode: (supplierCode: string) => defHttp.get<SupplierModel>({ url: `${BASE}/getByCode`, params: { supplierCode } }),
  getActive: () => defHttp.get<SupplierModel[]>({ url: `${BASE}/getActive` }),
  exportXls: (params?: any) => defHttp.get({ url: `${BASE}/exportXls`, params, responseType: 'blob' }, { isTransformResponse: false }),
  importExcel: (params: UploadFileParams, onUploadProgress?: (e: AxiosProgressEvent) => void) =>
    defHttp.uploadFile<any>({ url: `${BASE}/importExcel`, onUploadProgress }, params),
};
