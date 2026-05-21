import { defHttp } from '/@/utils/http/axios';
import { PageResult, PageParams } from './types';

const BASE = '/wms/inventory';

export interface InventoryModel {
  id?: string;
  productId: string;
  productName?: string;
  productCode?: string;
  quantity: number;
  reservedQuantity: number;
  availableQuantity: number;
  minStockThreshold: number;
  lastUpdated?: string;
  updatedBy?: string;
  createTime?: string;
  updateTime?: string;
}

export interface InventoryTransactionModel {
  id?: string;
  productId: string;
  productName?: string;
  productCode?: string;
  transactionType: string;
  quantity: number;
  referenceId?: string;
  reason?: string;
  userId?: string;
  userName?: string;
  createdAt?: string;
}

export interface InventoryAlertModel {
  id?: string;
  productId: string;
  productName?: string;
  productCode?: string;
  alertType: string;
  currentQuantity: number;
  thresholdValue: number;
  alertStatus: string;
  resolvedAt?: string;
  resolvedBy?: string;
  createdAt?: string;
}

export interface InventoryAdjustmentParams {
  productId: string;
  newQuantity: number;
  adjustmentReason: string;
}

export const wmsInventoryApi = {
  list: (params?: PageParams) => defHttp.get<PageResult<InventoryModel>>({ url: `${BASE}/list`, params }),
  getByProductId: (productId: string) => defHttp.get<InventoryModel>({ url: `${BASE}/product/${productId}` }),
  update: (params: { productId: string; minStockThreshold: number }) => defHttp.put({ url: `${BASE}/update`, params }),
  adjust: (params: InventoryAdjustmentParams) => defHttp.post({ url: `${BASE}/adjust`, params }),
  getTransactions: (params?: PageParams) => defHttp.get<PageResult<InventoryTransactionModel>>({ url: `${BASE}/transactions`, params }),
  getAdjustments: (params?: PageParams) => defHttp.get({ url: `${BASE}/adjustments`, params }),
  getReport: (params?: any) => defHttp.get({ url: `${BASE}/report`, params }),
  exportReport: (params?: any) => defHttp.get({ url: `${BASE}/export`, params, responseType: 'blob' }, { isTransformResponse: false }),
  getValueReport: () => defHttp.get({ url: `${BASE}/value-report` }),
  getLowStock: (params?: PageParams) => defHttp.get<PageResult<InventoryModel>>({ url: `${BASE}/low-stock`, params }),
  search: (params?: PageParams) => defHttp.get<PageResult<InventoryModel>>({ url: `${BASE}/search`, params }),
  getStatistics: () => defHttp.get({ url: `${BASE}/statistics` }),
  getTrends: (params: { productId: string; days?: number }) => defHttp.get({ url: `${BASE}/trends`, params }),
  // Alerts
  getAlerts: (params?: PageParams) => defHttp.get<PageResult<InventoryAlertModel>>({ url: `${BASE}/alerts`, params }),
  resolveAlert: (params: { alertId: string }) => defHttp.put({ url: `${BASE}/alerts/resolve`, params }),
  dismissAlert: (params: { alertId: string }) => defHttp.put({ url: `${BASE}/alerts/dismiss`, params }),
  resolveAlertsBatch: (params: { alertIds: string[] }) => defHttp.put({ url: `${BASE}/alerts/resolve-batch`, params }),
  dismissAlertsBatch: (params: { alertIds: string[] }) => defHttp.put({ url: `${BASE}/alerts/dismiss-batch`, params }),
  // Alert configs
  getAlertConfigs: (params?: any) => defHttp.get({ url: `${BASE}/alert-configs`, params }),
  saveAlertConfig: (params: any) => defHttp.post({ url: `${BASE}/alert-configs/add`, params }),
  updateAlertConfig: (params: any) => defHttp.put({ url: `${BASE}/alert-configs/edit`, params }),
  deleteAlertConfig: (params: { id: string }) => defHttp.delete({ url: `${BASE}/alert-configs/delete`, params }),
  getGlobalAlertConfig: () => defHttp.get({ url: `${BASE}/alert-configs/global` }),
  updateGlobalAlertConfig: (params: any) => defHttp.put({ url: `${BASE}/alert-configs/global`, params }),
};
