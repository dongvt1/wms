import { defHttp } from '/@/utils/http/axios';
import { UploadFileParams } from '/#/axios';
import { AxiosProgressEvent } from 'axios';

// 接口前缀
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
 * 库存管理API
 */
export const inventoryApi = {
  // 获取单个产品库存信息
  getByProductId: (params: { productId: string }) => 
    defHttp.get<InventoryModel>({ url: `${API_PREFIX}/product/${params.productId}` }),
  
  // 获取所有产品库存列表
  list: (params?: any) => 
    defHttp.get<InventoryListResult>({ url: `${API_PREFIX}/list`, params }),
  
  // 更新库存信息
  update: (params: InventoryUpdateParams) => 
    defHttp.put({ url: `${API_PREFIX}/update`, params }),
  
  // 手动调整库存
  adjust: (params: InventoryAdjustmentParams) => 
    defHttp.post({ url: `${API_PREFIX}/adjust`, params }),
  
  // 获取库存交易历史
  getTransactions: (params?: any) => 
    defHttp.get<InventoryTransactionListResult>({ url: `${API_PREFIX}/transactions`, params }),
  
  // 获取库存调整历史
  getAdjustments: (params?: any) => 
    defHttp.get<InventoryAdjustmentListResult>({ url: `${API_PREFIX}/adjustments`, params }),
  
  // 获取库存报告
  getReport: (params?: any) => 
    defHttp.get<InventoryReportListResult>({ url: `${API_PREFIX}/report`, params }),
  
  // 导出库存报告
  exportReport: (params?: any) => {
    const url = `${API_PREFIX}/export`;
    // 使用window.open进行文件下载
    if (params) {
      const queryString = Object.keys(params)
        .map(key => `${key}=${encodeURIComponent(params[key])}`)
        .join('&');
      window.open(`${url}?${queryString}`);
    } else {
      window.open(url);
    }
  },
  
  // 导出库存数据 (兼容现有代码)
  export: (params?: any) => {
    const url = `${API_PREFIX}/export`;
    // 使用window.open进行文件下载
    if (params) {
      const queryString = Object.keys(params)
        .map(key => `${key}=${encodeURIComponent(params[key])}`)
        .join('&');
      window.open(`${url}?${queryString}`);
    } else {
      window.open(url);
    }
  },
  
  // 获取库存价值报告
  getValueReport: () =>
    defHttp.get<any>({ url: `${API_PREFIX}/value-report` }),
  
  // 获取低库存产品列表
  getLowStock: (params?: any) => 
    defHttp.get<InventoryListResult>({ url: `${API_PREFIX}/low-stock`, params }),
  
  // 获取库存预警列表
  getAlerts: (params?: any) => 
    defHttp.get<InventoryAlertListResult>({ url: `${API_PREFIX}/alerts`, params }),
  
  // 解决库存预警
  resolveAlert: (params: { alertId: string }) => 
    defHttp.put({ url: `${API_PREFIX}/alerts/resolve`, params }),
  
  // 忽略库存预警
  dismissAlert: (params: { alertId: string }) => 
    defHttp.put({ url: `${API_PREFIX}/alerts/dismiss`, params }),
  
  // 批量解决库存预警
  resolveAlertsBatch: (params: { alertIds: string[] }) => 
    defHttp.put({ url: `${API_PREFIX}/alerts/resolve-batch`, params }),
  
  // 批量忽略库存预警
  dismissAlertsBatch: (params: { alertIds: string[] }) => 
    defHttp.put({ url: `${API_PREFIX}/alerts/dismiss-batch`, params }),
  
  // 获取库存统计信息
  getStatistics: () => 
    defHttp.get<any>({ url: `${API_PREFIX}/statistics` }),
  
  // 获取库存趋势数据
  getTrends: (params: { productId: string; days?: number }) => 
    defHttp.get<any>({ url: `${API_PREFIX}/trends`, params }),
  
  // 搜索库存
  search: (params?: any) => 
    defHttp.get<InventoryListResult>({ url: `${API_PREFIX}/search`, params }),
};

/**
 * 库存预警配置API
 */
export const inventoryAlertConfigApi = {
  // 获取预警配置列表
  list: (params?: any) => 
    defHttp.get<any>({ url: `${API_PREFIX}/alert-configs`, params }),
  
  // 获取单个产品预警配置
  getByProductId: (params: { productId: string }) => 
    defHttp.get<any>({ url: `${API_PREFIX}/alert-configs/product/${params.productId}` }),
  
  // 保存或更新预警配置
  save: (params: any) => 
    defHttp.post({ url: `${API_PREFIX}/alert-configs/add`, params }),
  
  // 更新预警配置
  update: (params: any) => 
    defHttp.put({ url: `${API_PREFIX}/alert-configs/edit`, params }),
  
  // 删除预警配置
  delete: (params: { id: string }) => 
    defHttp.delete({ url: `${API_PREFIX}/alert-configs/delete`, params }),
  
  // 批量删除预警配置
  deleteBatch: (params: { ids: string }) => 
    defHttp.delete({ url: `${API_PREFIX}/alert-configs/deleteBatch`, params }),
  
  // 获取全局预警配置
  getGlobal: () => 
    defHttp.get<any>({ url: `${API_PREFIX}/alert-configs/global` }),
  
  // 更新全局预警配置
  updateGlobal: (params: any) => 
    defHttp.put({ url: `${API_PREFIX}/alert-configs/global`, params }),
};