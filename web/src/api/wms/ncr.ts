import { defHttp } from '/@/utils/http/axios';
import { PageResult, PageParams } from './types';

const BASE_NCR = '/wms/qc/ncr';

export interface NcrModel {
  id?: string;
  ncrCode?: string;
  sourceType?: string; // 'iqc' | 'pqc' | 'fqc' | 'other'
  sourceId?: string;
  productId?: string;
  productName?: string;
  supplierId?: string;
  supplierName?: string;
  description?: string;
  severity?: string; // 'critical' | 'major' | 'minor'
  quantityDefective?: number;
  proposedAction?: string; // 'return' | 'repair' | 'scrap' | 'accept_conditional'
  correctiveAction?: string;
  status?: string; // 'open' | 'investigating' | 'action_taken' | 'verified' | 'closed'
  assignedTo?: string;
  closedBy?: string;
  closedDate?: string;
  notes?: string;
  createBy?: string;
  createTime?: string;
  updateBy?: string;
  updateTime?: string;
}

export const wmsNcrApi = {
  list: (params?: PageParams) => defHttp.get<PageResult<NcrModel>>({ url: `${BASE_NCR}/list`, params }),
  queryById: (id: string) => defHttp.get<NcrModel>({ url: `${BASE_NCR}/queryById`, params: { id } }),
  add: (params: any) => defHttp.post({ url: `${BASE_NCR}/add`, params }),
  edit: (params: any) => defHttp.put({ url: `${BASE_NCR}/edit`, params }),
  delete: (params: { id: string }) => defHttp.delete({ url: `${BASE_NCR}/delete`, params }),
  transition: (id: string, targetStatus: string, notes?: string) =>
    defHttp.put({ url: `${BASE_NCR}/transition/${id}`, params: { targetStatus, notes } }),
  close: (id: string, confirmationNotes: string) =>
    defHttp.put({ url: `${BASE_NCR}/close/${id}`, params: { confirmationNotes } }),
  statistics: () => defHttp.get({ url: `${BASE_NCR}/statistics` }),
  bySupplier: (supplierId: string) => defHttp.get<NcrModel[]>({ url: `${BASE_NCR}/bySupplier/${supplierId}` }),
};
