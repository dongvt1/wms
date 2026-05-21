import { defHttp } from '/@/utils/http/axios';
import { PageResult, PageParams } from './types';

const BASE_FQC = '/wms/qc/fqc';

export interface FqcInspectionModel {
  id?: string;
  inspectionCode?: string;
  outboundOrderId?: string;
  productId?: string;
  productName?: string;
  customerId?: string;
  customerName?: string;
  templateId?: string;
  quantityInspected?: number;
  quantityPassed?: number;
  quantityFailed?: number;
  inspector?: string;
  inspectionDate?: string;
  status?: string;
  notes?: string;
  createTime?: string;
}

export const wmsFqcApi = {
  list: (params?: PageParams) => defHttp.get<PageResult<FqcInspectionModel>>({ url: `${BASE_FQC}/list`, params }),
  queryById: (id: string) => defHttp.get<FqcInspectionModel>({ url: `${BASE_FQC}/queryById`, params: { id } }),
  add: (params: any) => defHttp.post({ url: `${BASE_FQC}/add`, params }),
  edit: (params: any) => defHttp.put({ url: `${BASE_FQC}/edit`, params }),
  delete: (params: { id: string }) => defHttp.delete({ url: `${BASE_FQC}/delete`, params }),
  deleteBatch: (params: { ids: string }) => defHttp.delete({ url: `${BASE_FQC}/deleteBatch`, params }),
  getResults: (inspectionId: string) => defHttp.get({ url: `${BASE_FQC}/getResults`, params: { inspectionId } }),
  submitForApproval: (id: string) => defHttp.put({ url: `${BASE_FQC}/submit/${id}` }),
  approve: (id: string, status: string, notes?: string, operator?: string) =>
    defHttp.put({ url: `${BASE_FQC}/approve/${id}`, params: { status, notes, operator } }),
  statistics: () => defHttp.get({ url: `${BASE_FQC}/statistics` }),
  checkOutbound: (orderId: string) => defHttp.get({ url: `${BASE_FQC}/checkOutbound/${orderId}` }),
};
