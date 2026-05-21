import { defHttp } from '/@/utils/http/axios';
import { UploadFileParams } from '/#/axios';
import { AxiosProgressEvent } from 'axios';
import { PageResult, PageParams } from './types';

const BASE = '/wms/stock';

export interface StockTransactionModel {
  id?: string;
  transactionCode?: string;
  transactionType: string; // 'IN' | 'OUT' | 'TRANSFER'
  status?: string;
  supplierId?: string;
  supplierName?: string;
  fromWarehouse?: string;
  toWarehouse?: string;
  reason?: string;
  totalQuantity?: number;
  approvedBy?: string;
  cancelReason?: string;
  createTime?: string;
  updateTime?: string;
}

export interface StockTransactionItemModel {
  id?: string;
  transactionId?: string;
  productId?: string;
  productName?: string;
  productCode?: string;
  quantity?: number;
  unit?: string;
  notes?: string;
}

export const wmsStockApi = {
  // Transactions
  list: (params?: PageParams) => defHttp.get<PageResult<StockTransactionModel>>({ url: `${BASE}/transactions`, params }),
  queryById: (id: string) => defHttp.get<StockTransactionModel>({ url: `${BASE}/transactions/${id}` }),
  delete: (id: string) => defHttp.delete({ url: `${BASE}/transactions/${id}` }),
  deleteBatch: (ids: string) => defHttp.delete({ url: `${BASE}/transactions/batch`, params: { ids } }),
  createStockIn: (params: any) => defHttp.post({ url: `${BASE}/stock-in`, params }),
  createStockOut: (params: any) => defHttp.post({ url: `${BASE}/stock-out`, params }),
  createTransfer: (params: any) => defHttp.post({ url: `${BASE}/transfer`, params }),
  approve: (id: string, approvedBy?: string) => defHttp.put({ url: `${BASE}/transactions/${id}/approve`, params: { approvedBy } }),
  cancel: (id: string, cancelReason?: string) => defHttp.put({ url: `${BASE}/transactions/${id}/cancel`, params: { cancelReason } }),
  getStatistics: (params?: any) => defHttp.get({ url: `${BASE}/transactions/statistics`, params }),
  print: (id: string) => defHttp.get({ url: `${BASE}/transactions/${id}/print` }),
  exportXls: (params?: any) => defHttp.get({ url: `${BASE}/transactions/exportXls`, params, responseType: 'blob' }, { isTransformResponse: false }),
  importExcel: (params: UploadFileParams, onUploadProgress?: (e: AxiosProgressEvent) => void) =>
    defHttp.uploadFile<any>({ url: `${BASE}/transactions/importExcel`, onUploadProgress }, params),
  // Items
  getItems: (transactionId: string) => defHttp.get<StockTransactionItemModel[]>({ url: `${BASE}/items`, params: { transactionId } }),
  deleteItem: (id: string) => defHttp.delete({ url: `${BASE}/items/${id}` }),
};
