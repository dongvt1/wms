import { defHttp } from '/@/utils/http/axios';
import { UploadFileParams } from '/#/axios';
import { AxiosProgressEvent } from 'axios';
import { PageResult, PageParams } from './types';

const BASE = '/wms/customer';

export interface CustomerModel {
  id?: string;
  customerCode: string;
  customerName: string;
  contactPerson?: string;
  phone?: string;
  email?: string;
  address?: string;
  balance?: number;
  status?: number;
  createTime?: string;
  updateTime?: string;
}

export const wmsCustomerApi = {
  list: (params?: PageParams) => defHttp.get<PageResult<CustomerModel>>({ url: `${BASE}/list`, params }),
  queryById: (id: string) => defHttp.get<CustomerModel>({ url: `${BASE}/queryById`, params: { id } }),
  add: (params: CustomerModel) => defHttp.post({ url: `${BASE}/add`, params }),
  edit: (params: CustomerModel) => defHttp.put({ url: `${BASE}/edit`, params }),
  delete: (id: string) => defHttp.delete({ url: `${BASE}/delete`, params: { id } }),
  deleteBatch: (ids: string) => defHttp.delete({ url: `${BASE}/deleteBatch`, params: { ids } }),
  getByCode: (customerCode: string) => defHttp.get<CustomerModel>({ url: `${BASE}/getByCode`, params: { customerCode } }),
  getActive: () => defHttp.get<CustomerModel[]>({ url: `${BASE}/getActive` }),
  search: (keyword: string) => defHttp.get<PageResult<CustomerModel>>({ url: `${BASE}/search`, params: { keyword } }),
  getOrderHistory: (id: string) => defHttp.get({ url: `${BASE}/orderHistory`, params: { id } }),
  getBalance: (id: string) => defHttp.get({ url: `${BASE}/balance`, params: { id } }),
  updateBalance: (id: string, amount: number) => defHttp.post({ url: `${BASE}/updateBalance`, params: { id, amount } }),
  getStatistics: (id: string) => defHttp.get({ url: `${BASE}/statistics`, params: { id } }),
  exportXls: (params?: any) => defHttp.get({ url: `${BASE}/exportXls`, params, responseType: 'blob' }, { isTransformResponse: false }),
  importExcel: (params: UploadFileParams, onUploadProgress?: (e: AxiosProgressEvent) => void) =>
    defHttp.uploadFile<any>({ url: `${BASE}/importExcel`, onUploadProgress }, params),
};
