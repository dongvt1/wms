import { defHttp } from '/@/utils/http/axios';
import { UploadFileParams } from '/#/axios';
import { AxiosProgressEvent } from 'axios';
import { PageResult, PageParams } from './types';

const BASE = '/wms/order';

export interface OrderModel {
  id?: string;
  orderCode?: string;
  customerId?: string;
  customerName?: string;
  status?: string; // 'PENDING' | 'CONFIRMED' | 'SHIPPING' | 'COMPLETED' | 'CANCELLED'
  totalAmount?: number;
  notes?: string;
  cancelReason?: string;
  createTime?: string;
  updateTime?: string;
}

export interface OrderItemModel {
  id?: string;
  orderId?: string;
  productId?: string;
  productName?: string;
  productCode?: string;
  quantity?: number;
  unitPrice?: number;
  totalPrice?: number;
}

export const wmsOrderApi = {
  // CRUD
  list: (params?: PageParams) => defHttp.get<PageResult<OrderModel>>({ url: `${BASE}/list`, params }),
  queryById: (id: string) => defHttp.get<OrderModel>({ url: `${BASE}/queryById`, params: { id } }),
  add: (params: any) => defHttp.post({ url: `${BASE}/add`, params }),
  edit: (params: any) => defHttp.put({ url: `${BASE}/edit`, params }),
  delete: (id: string) => defHttp.delete({ url: `${BASE}/delete`, params: { id } }),
  deleteBatch: (ids: string) => defHttp.delete({ url: `${BASE}/deleteBatch`, params: { ids } }),
  // Workflow
  cancel: (orderId: string, reason?: string) => defHttp.put({ url: `${BASE}/cancel`, params: { orderId, reason } }),
  updateStatus: (orderId: string, newStatus: string, reason?: string) =>
    defHttp.put({ url: `${BASE}/status`, params: { orderId, newStatus, reason } }),
  confirm: (orderId: string) => defHttp.put({ url: `${BASE}/${orderId}/confirm` }),
  ship: (orderId: string) => defHttp.put({ url: `${BASE}/${orderId}/ship` }),
  complete: (orderId: string) => defHttp.put({ url: `${BASE}/${orderId}/complete` }),
  // Batch
  batchProcess: (params: any) => defHttp.post({ url: `${BASE}/batch-process`, params }),
  autoConfirm: () => defHttp.post({ url: `${BASE}/auto-confirm` }),
  // Search
  search: (params?: PageParams) => defHttp.get<PageResult<OrderModel>>({ url: `${BASE}/search`, params }),
  searchByCode: (orderCode: string) => defHttp.get({ url: `${BASE}/search/code`, params: { orderCode } }),
  searchByCustomerName: (customerName: string) => defHttp.get({ url: `${BASE}/search/customer`, params: { customerName } }),
  // Reports & Stats
  getReport: (params?: any) => defHttp.get({ url: `${BASE}/report`, params }),
  getStatistics: () => defHttp.get({ url: `${BASE}/statistics` }),
  getProcessingStatistics: () => defHttp.get({ url: `${BASE}/processing-statistics` }),
  // Utilities
  generateCode: () => defHttp.get<string>({ url: `${BASE}/generate-code` }),
  calculateAmount: (orderItems: any[]) => defHttp.post({ url: `${BASE}/calculate-amount`, params: orderItems }),
  checkInventory: (orderItems: any[]) => defHttp.post({ url: `${BASE}/check-inventory`, params: orderItems }),
  // History & Logs
  getStatusHistory: (orderId: string) => defHttp.get({ url: `${BASE}/${orderId}/status-history` }),
  getProcessingLogs: (orderId: string) => defHttp.get({ url: `${BASE}/${orderId}/processing-logs` }),
  // Notifications
  resendNotification: (notificationId: string) => defHttp.post({ url: `${BASE}/resend-notification`, params: { notificationId } }),
  processNotifications: () => defHttp.post({ url: `${BASE}/process-notifications` }),
  // Print & Export
  print: (orderId: string) => defHttp.get({ url: `${BASE}/${orderId}/print`, responseType: 'blob' }, { isTransformResponse: false }),
  generateStockOutNote: (orderId: string) => defHttp.get({ url: `${BASE}/${orderId}/stock-out-note`, responseType: 'blob' }, { isTransformResponse: false }),
  exportXls: (params?: any) => defHttp.get({ url: `${BASE}/exportXls`, params, responseType: 'blob' }, { isTransformResponse: false }),
  importExcel: (params: UploadFileParams, onUploadProgress?: (e: AxiosProgressEvent) => void) =>
    defHttp.uploadFile<any>({ url: `${BASE}/importExcel`, onUploadProgress }, params),
};

export const wmsOrderItemApi = {
  getByOrderId: (orderId: string) => defHttp.get<OrderItemModel[]>({ url: `${BASE}/items/${orderId}` }),
  add: (params: OrderItemModel) => defHttp.post({ url: `${BASE}/items/add`, params }),
  edit: (params: OrderItemModel) => defHttp.put({ url: `${BASE}/items/edit`, params }),
  delete: (id: string) => defHttp.delete({ url: `${BASE}/items/delete`, params: { id } }),
  deleteBatch: (ids: string) => defHttp.delete({ url: `${BASE}/items/deleteBatch`, params: { ids } }),
};
