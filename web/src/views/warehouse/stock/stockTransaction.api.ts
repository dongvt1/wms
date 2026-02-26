import { defHttp } from '/@/utils/http/axios';
import { UploadFileParams } from '/#/axios';
import { AxiosProgressEvent } from 'axios';

// Stock Transaction API
export const stockTransactionApi = {
  // Get stock transaction list
  list: (params) => defHttp.get({ url: '/warehouse/stock/transactions', params }),
  
  // Delete stock transaction
  delete: (id) => defHttp.delete({ url: `/warehouse/stock/transactions/${id}` }),
  
  // Batch delete stock transactions
  deleteBatch: (ids) => defHttp.delete({ url: `/warehouse/stock/transactions/batch?ids=${ids}` }),
  
  // Get stock transaction by ID
  queryById: (id) => defHttp.get({ url: `/warehouse/stock/transactions/${id}` }),
  
  // Create stock in transaction
  createStockIn: (params) => defHttp.post({ url: '/warehouse/stock/stock-in', params }),
  
  // Create stock out transaction
  createStockOut: (params) => defHttp.post({ url: '/warehouse/stock/stock-out', params }),
  
  // Create stock transfer transaction
  createStockTransfer: (params) => defHttp.post({ url: '/warehouse/stock/transfer', params }),
  
  // Approve transaction
  approve: (id, approvedBy) => defHttp.put({ url: `/warehouse/stock/transactions/${id}/approve`, params: { approvedBy } }),
  
  // Cancel transaction
  cancel: (id, cancelReason) => defHttp.put({ url: `/warehouse/stock/transactions/${id}/cancel`, params: { cancelReason } }),
  
  // Get transaction statistics
  getStatistics: (params) => defHttp.get({ url: '/warehouse/stock/transactions/statistics', params }),
  
  // Print transaction
  print: (id) => defHttp.get({ url: `/warehouse/stock/transactions/${id}/print` }),
  
  // Export Excel
  exportXls: (params?: any) => {
    const url = '/warehouse/stock/transactions/exportXls';
    // Use window.open for file download
    if (params) {
      const queryString = Object.keys(params)
        .map(key => `${key}=${encodeURIComponent(params[key])}`)
        .join('&');
      window.open(`${url}?${queryString}`);
    } else {
      window.open(url);
    }
  },
  
  // Import Excel
  importExcel: (params: UploadFileParams, onUploadProgress?: (progressEvent: AxiosProgressEvent) => void) =>
    defHttp.uploadFile<any>(
      {
        url: '/warehouse/stock/transactions/importExcel',
        onUploadProgress,
      },
      params
    ),
};

// Stock Transaction Item API
export const stockTransactionItemApi = {
  // Get stock transaction item list
  list: (params) => defHttp.get({ url: '/warehouse/stock/items', params }),
  
  // Delete stock transaction item
  delete: (id) => defHttp.delete({ url: `/warehouse/stock/items/${id}` }),
  
  // Batch delete stock transaction items
  deleteBatch: (ids) => defHttp.delete({ url: `/warehouse/stock/items/batch?ids=${ids}` }),
  
  // Get stock transaction item by ID
  queryById: (id) => defHttp.get({ url: `/warehouse/stock/items/${id}` }),
  
  // Get items by transaction ID
  getItemsByTransactionId: (transactionId) => defHttp.get({ url: `/warehouse/stock/items?transactionId=${transactionId}` }),
  
  // Export Excel
  exportXls: (params?: any) => {
    const url = '/warehouse/stock/items/exportXls';
    // Use window.open for file download
    if (params) {
      const queryString = Object.keys(params)
        .map(key => `${key}=${encodeURIComponent(params[key])}`)
        .join('&');
      window.open(`${url}?${queryString}`);
    } else {
      window.open(url);
    }
  },
  
  // Import Excel
  importExcel: (params: UploadFileParams, onUploadProgress?: (progressEvent: AxiosProgressEvent) => void) =>
    defHttp.uploadFile<any>(
      {
        url: '/warehouse/stock/items/importExcel',
        onUploadProgress,
      },
      params
    ),
};

// Supplier API
export const supplierApi = {
  // Get supplier list
  list: (params) => defHttp.get({ url: '/warehouse/supplier/list', params }),
  
  // Add supplier
  add: (params) => defHttp.post({ url: '/warehouse/supplier/add', params }),
  
  // Edit supplier
  edit: (params) => defHttp.put({ url: '/warehouse/supplier/edit', params }),
  
  // Delete supplier
  delete: (id) => defHttp.delete({ url: `/warehouse/supplier/delete?id=${id}` }),
  
  // Batch delete suppliers
  deleteBatch: (ids) => defHttp.delete({ url: `/warehouse/supplier/deleteBatch?ids=${ids}` }),
  
  // Get supplier by ID
  queryById: (id) => defHttp.get({ url: `/warehouse/supplier/queryById?id=${id}` }),
  
  // Get supplier by code
  getByCode: (supplierCode) => defHttp.get({ url: `/warehouse/supplier/getByCode?supplierCode=${supplierCode}` }),
  
  // Get active suppliers
  getActive: () => defHttp.get({ url: '/warehouse/supplier/getActive' }),
  
  // Export Excel
  exportXls: (params?: any) => {
    const url = '/warehouse/supplier/exportXls';
    // Use window.open for file download
    if (params) {
      const queryString = Object.keys(params)
        .map(key => `${key}=${encodeURIComponent(params[key])}`)
        .join('&');
      window.open(`${url}?${queryString}`);
    } else {
      window.open(url);
    }
  },
  
  // Import Excel
  importExcel: (params: UploadFileParams, onUploadProgress?: (progressEvent: AxiosProgressEvent) => void) =>
    defHttp.uploadFile<any>(
      {
        url: '/warehouse/supplier/importExcel',
        onUploadProgress,
      },
      params
    ),
};