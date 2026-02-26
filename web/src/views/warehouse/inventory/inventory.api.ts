import { defHttp } from '/@/utils/http/axios';
import { UploadFileParams } from '/#/axios';
import { AxiosProgressEvent } from 'axios';

// interface prefix
const API_PREFIX = '/warehouse/inventory';

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

export interface InventoryListResult {
  records: InventoryModel[];
  total: number;
  size: number;
  current: number;
  pages: number;
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

export interface InventoryTransactionListResult {
  records: InventoryTransactionModel[];
  total: number;
  size: number;
  current: number;
  pages: number;
}

export interface InventoryAdjustmentModel {
  id?: string;
  productId: string;
  productName?: string;
  productCode?: string;
  oldQuantity: number;
  newQuantity: number;
  adjustmentReason?: string;
  userId?: string;
  userName?: string;
  createdAt?: string;
}

export interface InventoryAdjustmentListResult {
  records: InventoryAdjustmentModel[];
  total: number;
  size: number;
  current: number;
  pages: number;
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

export interface InventoryAlertListResult {
  records: InventoryAlertModel[];
  total: number;
  size: number;
  current: number;
  pages: number;
}

export interface InventoryReportModel {
  productId: string;
  productName: string;
  productCode: string;
  quantity: number;
  unitPrice: number;
  totalValue: number;
  minStockThreshold: number;
  status: string;
}

export interface InventoryReportListResult {
  records: InventoryReportModel[];
  total: number;
  size: number;
  current: number;
  pages: number;
  summary: {
    totalProducts: number;
    totalQuantity: number;
    totalValue: number;
    lowStockProducts: number;
    outOfStockProducts: number;
  };
}

export interface InventoryAdjustmentParams {
  productId: string;
  newQuantity: number;
  adjustmentReason: string;
}

export interface InventoryUpdateParams {
  productId: string;
  minStockThreshold: number;
}

/**
 * Inventory managementAPI
 */
export const inventoryApi = {
  // Get inventory information for a single product
  getByProductId: (params: { productId: string }) => 
    defHttp.get<InventoryModel>({ url: `${API_PREFIX}/product/${params.productId}` }),
  
  // Get an inventory list of all products
  list: (params?: any) => 
    defHttp.get<InventoryListResult>({ url: `${API_PREFIX}/list`, params }),
  
  // Update inventory information
  update: (params: InventoryUpdateParams) => 
    defHttp.put({ url: `${API_PREFIX}/update`, params }),
  
  // Adjust inventory manually
  adjust: (params: InventoryAdjustmentParams) => 
    defHttp.post({ url: `${API_PREFIX}/adjust`, params }),
  
  // Get inventory transaction history
  getTransactions: (params?: any) => 
    defHttp.get<InventoryTransactionListResult>({ url: `${API_PREFIX}/transactions`, params }),
  
  // Get inventory adjustment history
  getAdjustments: (params?: any) => 
    defHttp.get<InventoryAdjustmentListResult>({ url: `${API_PREFIX}/adjustments`, params }),
  
  // Get inventory report
  getReport: (params?: any) => 
    defHttp.get<InventoryReportListResult>({ url: `${API_PREFIX}/report`, params }),
  
  // Export inventory report
  exportReport: (params?: any) => {
    const url = `${API_PREFIX}/export`;
    // usewindow.openPerform file download
    if (params) {
      const queryString = Object.keys(params)
        .map(key => `${key}=${encodeURIComponent(params[key])}`)
        .join('&');
      window.open(`${url}?${queryString}`);
    } else {
      window.open(url);
    }
  },
  
  // Export inventory data (Compatible with existing code)
  export: (params?: any) => {
    const url = `${API_PREFIX}/export`;
    // usewindow.openPerform file download
    if (params) {
      const queryString = Object.keys(params)
        .map(key => `${key}=${encodeURIComponent(params[key])}`)
        .join('&');
      window.open(`${url}?${queryString}`);
    } else {
      window.open(url);
    }
  },
  
  // Get inventory value report
  getValueReport: () =>
    defHttp.get<any>({ url: `${API_PREFIX}/value-report` }),
  
  // Get a list of low-stock products
  getLowStock: (params?: any) => 
    defHttp.get<InventoryListResult>({ url: `${API_PREFIX}/low-stock`, params }),
  
  // Get inventory alert list
  getAlerts: (params?: any) => 
    defHttp.get<InventoryAlertListResult>({ url: `${API_PREFIX}/alerts`, params }),
  
  // Resolve inventory alerts
  resolveAlert: (params: { alertId: string }) => 
    defHttp.put({ url: `${API_PREFIX}/alerts/resolve`, params }),
  
  // Ignore stock alerts
  dismissAlert: (params: { alertId: string }) => 
    defHttp.put({ url: `${API_PREFIX}/alerts/dismiss`, params }),
  
  // 批量Resolve inventory alerts
  resolveAlertsBatch: (params: { alertIds: string[] }) => 
    defHttp.put({ url: `${API_PREFIX}/alerts/resolve-batch`, params }),
  
  // 批量Ignore stock alerts
  dismissAlertsBatch: (params: { alertIds: string[] }) => 
    defHttp.put({ url: `${API_PREFIX}/alerts/dismiss-batch`, params }),
  
  // Get inventory statistics
  getStatistics: () => 
    defHttp.get<any>({ url: `${API_PREFIX}/statistics` }),
  
  // Get inventory trend data
  getTrends: (params: { productId: string; days?: number }) => 
    defHttp.get<any>({ url: `${API_PREFIX}/trends`, params }),
  
  // Search inventory
  search: (params?: any) => 
    defHttp.get<InventoryListResult>({ url: `${API_PREFIX}/search`, params }),
};

/**
 * Inventory alert configurationAPI
 */
export const inventoryAlertConfigApi = {
  // Get the alert configuration list
  list: (params?: any) => 
    defHttp.get<any>({ url: `${API_PREFIX}/alert-configs`, params }),
  
  // Get single product alert configuration
  getByProductId: (params: { productId: string }) => 
    defHttp.get<any>({ url: `${API_PREFIX}/alert-configs/product/${params.productId}` }),
  
  // Save or update alert configuration
  save: (params: any) => 
    defHttp.post({ url: `${API_PREFIX}/alert-configs/add`, params }),
  
  // Update alert configuration
  update: (params: any) => 
    defHttp.put({ url: `${API_PREFIX}/alert-configs/edit`, params }),
  
  // Delete alert configuration
  delete: (params: { id: string }) => 
    defHttp.delete({ url: `${API_PREFIX}/alert-configs/delete`, params }),
  
  // 批量Delete alert configuration
  deleteBatch: (params: { ids: string }) => 
    defHttp.delete({ url: `${API_PREFIX}/alert-configs/deleteBatch`, params }),
  
  // Get global alert configuration
  getGlobal: () => 
    defHttp.get<any>({ url: `${API_PREFIX}/alert-configs/global` }),
  
  // Update global alert configuration
  updateGlobal: (params: any) => 
    defHttp.put({ url: `${API_PREFIX}/alert-configs/global`, params }),
};