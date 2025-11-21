import { defHttp } from '/@/utils/http/axios';
import { UploadFileParams } from '/#/axios';
import { AxiosProgressEvent } from 'axios';

// Order API
export const orderApi = {
  // Get order list
  list: (params) => defHttp.get({ url: '/warehouse/orders/list', params }),
  
  // Delete order
  delete: (id) => defHttp.delete({ url: `/warehouse/orders/delete?id=${id}` }),
  
  // Batch delete orders
  deleteBatch: (ids) => defHttp.delete({ url: `/warehouse/orders/deleteBatch?ids=${ids}` }),
  
  // Get order by ID
  queryById: (id) => defHttp.get({ url: `/warehouse/orders/queryById?id=${id}` }),
  
  // Add order
  add: (params) => defHttp.post({ url: '/warehouse/orders/add', params }),
  
  // Edit order
  edit: (params) => defHttp.put({ url: '/warehouse/orders/edit', params }),
  
  // Cancel order
  cancel: (orderId, reason) => defHttp.put({ 
    url: '/warehouse/orders/cancel', 
    params: { orderId, reason } 
  }),
  
  // Update order status
  updateStatus: (orderId, newStatus, reason) => defHttp.put({ 
    url: '/warehouse/orders/status', 
    params: { orderId, newStatus, reason } 
  }),
  
  // Get order report
  getReport: (params) => defHttp.get({ url: '/warehouse/orders/report', params }),
  
  // Search orders by code
  searchByCode: (orderCode) => defHttp.get({ url: `/warehouse/orders/search/code?orderCode=${orderCode}` }),
  
  // Search orders by customer name
  searchByCustomerName: (customerName) => defHttp.get({ url: `/warehouse/orders/search/customer?customerName=${customerName}` }),
  
  // Get order status history
  getStatusHistory: (orderId) => defHttp.get({ url: `/warehouse/orders/${orderId}/status-history` }),
  
  // Export Excel
  exportXls: (params?: any) => {
    const url = '/warehouse/orders/export';
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
  
  // Print order
  print: (orderId) => {
    const url = `/warehouse/orders/${orderId}/print`;
    window.open(url);
  },
  
  // Get order statistics
  getStatistics: () => defHttp.get({ url: '/warehouse/orders/statistics' }),
  
  // Generate order code
  generateCode: () => defHttp.get({ url: '/warehouse/orders/generate-code' }),
  
  // Calculate order amount
  calculateAmount: (orderItems) => defHttp.post({ url: '/warehouse/orders/calculate-amount', params: orderItems }),
  
  // Check inventory
  checkInventory: (orderItems) => defHttp.post({ url: '/warehouse/orders/check-inventory', params: orderItems }),
  
  // Confirm order
  confirm: (orderId) => defHttp.put({ url: `/warehouse/orders/${orderId}/confirm` }),
  
  // Ship order
  ship: (orderId) => defHttp.put({ url: `/warehouse/orders/${orderId}/ship` }),
  
  // Complete order
  complete: (orderId) => defHttp.put({ url: `/warehouse/orders/${orderId}/complete` }),
  
  // Search orders
  search: (params) => defHttp.get({ url: '/warehouse/orders/search', params }),
  
  // Batch process orders
  batchProcess: (params) => defHttp.post({ url: '/warehouse/orders/batch-process', params }),
  
  // Auto confirm orders
  autoConfirm: () => defHttp.post({ url: '/warehouse/orders/auto-confirm' }),
  
  // Generate stock out note
  generateStockOutNote: (orderId) => {
    const url = `/warehouse/orders/${orderId}/stock-out-note`;
    window.open(url);
  },
  
  // Get processing logs
  getProcessingLogs: (orderId) => defHttp.get({ url: `/warehouse/orders/${orderId}/processing-logs` }),
  
  // Resend notification
  resendNotification: (notificationId) => defHttp.post({
    url: '/warehouse/orders/resend-notification',
    params: { notificationId }
  }),
  
  // Process pending notifications
  processNotifications: () => defHttp.post({ url: '/warehouse/orders/process-notifications' }),
  
  // Get processing statistics
  getProcessingStatistics: () => defHttp.get({ url: '/warehouse/orders/processing-statistics' }),
  
  // Import Excel
  importExcel: (params: UploadFileParams, onUploadProgress?: (progressEvent: AxiosProgressEvent) => void) =>
    defHttp.uploadFile<any>(
      {
        url: '/warehouse/orders/importExcel',
        onUploadProgress,
      },
      params
    ),
};

// Order Item API
export const orderItemApi = {
  // Get order items by order ID
  getByOrderId: (orderId) => defHttp.get({ url: `/warehouse/orders/items/${orderId}` }),
  
  // Add order item
  add: (params) => defHttp.post({ url: '/warehouse/orders/items/add', params }),
  
  // Edit order item
  edit: (params) => defHttp.put({ url: '/warehouse/orders/items/edit', params }),
  
  // Delete order item
  delete: (id) => defHttp.delete({ url: `/warehouse/orders/items/delete?id=${id}` }),
  
  // Batch delete order items
  deleteBatch: (ids) => defHttp.delete({ url: `/warehouse/orders/items/deleteBatch?ids=${ids}` }),
};

// Order Status History API
export const orderStatusHistoryApi = {
  // Get status history by order ID
  getByOrderId: (orderId) => defHttp.get({ url: `/warehouse/orders/${orderId}/status-history` }),
  
  // Get status history by order code
  getByOrderCode: (orderCode) => defHttp.get({ url: `/warehouse/orders/status-history/code?orderCode=${orderCode}` }),
  
  // Get status change statistics
  getStatistics: () => defHttp.get({ url: '/warehouse/orders/status-history/statistics' }),
  
  // Get user operation statistics
  getUserStatistics: () => defHttp.get({ url: '/warehouse/orders/status-history/user-statistics' }),
};