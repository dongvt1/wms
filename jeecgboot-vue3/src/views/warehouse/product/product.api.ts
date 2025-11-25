import { defHttp } from '/@/utils/http/axios';
import { UploadFileParams } from '/#/axios';
import { AxiosProgressEvent } from 'axios';

// interface prefix
const API_PREFIX = '/warehouse/product';

export interface ProductModel {
  id?: string;
  code: string;
  name: string;
  description?: string;
  price: number;
  categoryId: string;
  categoryName?: string;
  minStockLevel: number;
  image?: string;
  status: number;
  currentStock?: number;
  createTime?: string;
  updateTime?: string;
}

export interface ProductListResult {
  records: ProductModel[];
  total: number;
  size: number;
  current: number;
  pages: number;
}

export interface ProductHistoryModel {
  id?: string;
  productId: string;
  action: string;
  oldData?: string;
  newData?: string;
  userId: string;
  userName?: string;
  createTime?: string;
}

export interface ProductHistoryListResult {
  records: ProductHistoryModel[];
  total: number;
  size: number;
  current: number;
  pages: number;
}

/**
 * Query product list
 * @param params query parameters
 */
export const productApi = {
  // list
  list: (params?: any) => defHttp.get<ProductListResult>({ url: `${API_PREFIX}/list`, params }),
  
  // delete
  delete: (params: { id: string }) => defHttp.delete({ url: `${API_PREFIX}/delete`, params }),
  
  // 批量delete
  deleteBatch: (params: { ids: string }) => defHttp.delete({ url: `${API_PREFIX}/deleteBatch`, params }),
  
  // according toIDQuery
  getById: (params: { id: string }) => defHttp.get<ProductModel>({ url: `${API_PREFIX}/queryById`, params }),
  
  // Save or update
  save: (params: ProductModel) => defHttp.post({ url: `${API_PREFIX}/add`, params }),
  
  update: (params: ProductModel) => defHttp.put({ url: `${API_PREFIX}/edit`, params }),
  
  // Exportexcel
  exportXls: (params?: any) => {
    const url = `${API_PREFIX}/exportXls`;
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
  
  // importexcel
  importExcel: (params: UploadFileParams, onUploadProgress?: (progressEvent: AxiosProgressEvent) => void) =>
    defHttp.uploadFile<any>(
      {
        url: `${API_PREFIX}/importExcel`,
        onUploadProgress,
      },
      params
    ),
  
  // Upload product images
  uploadImage: (params: UploadFileParams, onUploadProgress?: (progressEvent: AxiosProgressEvent) => void) =>
    defHttp.uploadFile<any>(
      {
        url: `${API_PREFIX}/uploadImage`,
        onUploadProgress,
      },
      params
    ),
  
  // Get product history
  getHistory: (params: { productId: string }) => 
    defHttp.get<ProductHistoryListResult>({ url: `${API_PREFIX}/history`, params }),
  
  // Search products
  search: (params?: any) => defHttp.get<ProductListResult>({ url: `${API_PREFIX}/search`, params }),
};