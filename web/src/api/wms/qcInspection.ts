import { defHttp } from '/@/utils/http/axios';
import { PageResult, PageParams } from './types';

const BASE_IQC = '/wms/qc/iqc';
const BASE_PQC = '/wms/qc/pqc';

export interface QcInspectionModel {
  id?: string;
  inspectionCode?: string;
  inspectionType?: string; // 'IQC' | 'PQC'
  workOrderId?: string;
  productId?: string;
  productName?: string;
  supplierId?: string;
  supplierName?: string;
  checklistId?: string;
  inspector?: string;
  status?: string;
  result?: string;
  notes?: string;
  createTime?: string;
}

export const wmsIqcApi = {
  list: (params?: PageParams) => defHttp.get<PageResult<QcInspectionModel>>({ url: `${BASE_IQC}/list`, params }),
  queryById: (id: string) => defHttp.get<QcInspectionModel>({ url: `${BASE_IQC}/queryById`, params: { id } }),
  add: (params: any) => defHttp.post({ url: `${BASE_IQC}/add`, params }),
  edit: (params: any) => defHttp.put({ url: `${BASE_IQC}/edit`, params }),
  delete: (params: { id: string }) => defHttp.delete({ url: `${BASE_IQC}/delete`, params }),
  deleteBatch: (params: { ids: string }) => defHttp.delete({ url: `${BASE_IQC}/deleteBatch`, params }),
  getResults: (inspectionId: string) => defHttp.get({ url: `${BASE_IQC}/getResults`, params: { inspectionId } }),
  submitForApproval: (id: string) => defHttp.put({ url: `${BASE_IQC}/submit/${id}` }),
  approve: (id: string, status: string, notes?: string, operator?: string) =>
    defHttp.put({ url: `${BASE_IQC}/approve/${id}`, params: { status, notes, operator } }),
  statistics: () => defHttp.get({ url: `${BASE_IQC}/statistics` }),
};

export const wmsPqcApi = {
  list: (params?: PageParams) => defHttp.get<PageResult<QcInspectionModel>>({ url: `${BASE_PQC}/list`, params }),
  queryById: (id: string) => defHttp.get<QcInspectionModel>({ url: `${BASE_PQC}/queryById`, params: { id } }),
  add: (params: any) => defHttp.post({ url: `${BASE_PQC}/add`, params }),
  edit: (params: any) => defHttp.put({ url: `${BASE_PQC}/edit`, params }),
  delete: (params: { id: string }) => defHttp.delete({ url: `${BASE_PQC}/delete`, params }),
  deleteBatch: (params: { ids: string }) => defHttp.delete({ url: `${BASE_PQC}/deleteBatch`, params }),
  getResults: (inspectionId: string) => defHttp.get({ url: `${BASE_PQC}/getResults`, params: { inspectionId } }),
  submitForApproval: (id: string) => defHttp.put({ url: `${BASE_PQC}/submit/${id}` }),
  approve: (id: string, status: string, notes?: string, operator?: string) =>
    defHttp.put({ url: `${BASE_PQC}/approve/${id}`, params: { status, notes, operator } }),
  statistics: () => defHttp.get({ url: `${BASE_PQC}/statistics` }),
};
