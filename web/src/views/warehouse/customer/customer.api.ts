import { defHttp } from '/@/utils/http/axios';
import { UploadFileParams } from '/#/axios';
import { AxiosProgressEvent } from 'axios';

// Customer API
export const customerApi = {
  // Get customer list
  list: (params) => defHttp.get({ url: '/warehouse/customer/list', params }),
  
  // Delete customer
  delete: (id) => defHttp.delete({ url: `/warehouse/customer/delete?id=${id}` }),
  
  // Batch delete customers
  deleteBatch: (ids) => defHttp.delete({ url: `/warehouse/customer/deleteBatch?ids=${ids}` }),
  
  // Get customer by ID
  queryById: (id) => defHttp.get({ url: `/warehouse/customer/queryById?id=${id}` }),
  
  // Add customer
  add: (params) => defHttp.post({ url: '/warehouse/customer/add', params }),
  
  // Edit customer
  edit: (params) => defHttp.put({ url: '/warehouse/customer/edit', params }),
  
  // Get customer by code
  getByCode: (customerCode) => defHttp.get({ url: `/warehouse/customer/getByCode?customerCode=${customerCode}` }),
  
  // Get active customers
  getActive: () => defHttp.get({ url: '/warehouse/customer/getActive' }),
  
  // Search customers
  search: (keyword) => defHttp.get({ url: `/warehouse/customer/search?keyword=${keyword}` }),
  
  // Get customer order history
  getOrderHistory: (id) => defHttp.get({ url: `/warehouse/customer/orderHistory?id=${id}` }),
  
  // Get customer balance
  getBalance: (id) => defHttp.get({ url: `/warehouse/customer/balance?id=${id}` }),
  
  // Update customer balance
  updateBalance: (id, amount) => defHttp.post({ url: `/warehouse/customer/updateBalance?id=${id}&amount=${amount}` }),
  
  // Get customer statistics
  getStatistics: (id) => defHttp.get({ url: `/warehouse/customer/statistics?id=${id}` }),
  
  // Export Excel
  exportXls: (params?: any) => {
    const url = '/warehouse/customer/exportXls';
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
        url: '/warehouse/customer/importExcel',
        onUploadProgress,
      },
      params
    ),
};

// Customer Balance API
export const customerBalanceApi = {
  // Get customer balance by customer ID
  getByCustomerId: (customerId) => defHttp.get({ url: `/warehouse/customer/balance?id=${customerId}` }),
  
  // Update customer balance
  updateBalance: (customerId, amount, updatedBy) => 
    defHttp.post({ 
      url: '/warehouse/customer/updateBalance', 
      params: { id: customerId, amount, updatedBy } 
    }),
};