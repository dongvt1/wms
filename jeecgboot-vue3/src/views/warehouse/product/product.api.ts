import { defHttp } from '/@/utils/http/axios';
import { UploadFileParams } from '/#/axios';
import { AxiosProgressEvent } from 'axios';

// 接口前缀
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
 * 查询产品列表
 * @param params 查询参数
 */
export const productApi = {
  // 列表
  list: (params?: any) => defHttp.get<ProductListResult>({ url: `${API_PREFIX}/list`, params }),
  
  // 删除
  delete: (params: { id: string }) => defHttp.delete({ url: `${API_PREFIX}/delete`, params }),
  
  // 批量删除
  deleteBatch: (params: { ids: string }) => defHttp.delete({ url: `${API_PREFIX}/deleteBatch`, params }),
  
  // 根据ID查询
  getById: (params: { id: string }) => defHttp.get<ProductModel>({ url: `${API_PREFIX}/queryById`, params }),
  
  // 保存或更新
  save: (params: ProductModel) => defHttp.post({ url: `${API_PREFIX}/add`, params }),
  
  update: (params: ProductModel) => defHttp.put({ url: `${API_PREFIX}/edit`, params }),
  
  // 导出excel
  exportXls: (params?: any) => {
    const url = `${API_PREFIX}/exportXls`;
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
  
  // 导入excel
  importExcel: (params: UploadFileParams, onUploadProgress?: (progressEvent: AxiosProgressEvent) => void) =>
    defHttp.uploadFile<any>(
      {
        url: `${API_PREFIX}/importExcel`,
        onUploadProgress,
      },
      params
    ),
  
  // 上传产品图片
  uploadImage: (params: UploadFileParams, onUploadProgress?: (progressEvent: AxiosProgressEvent) => void) =>
    defHttp.uploadFile<any>(
      {
        url: `${API_PREFIX}/uploadImage`,
        onUploadProgress,
      },
      params
    ),
  
  // 获取产品历史记录
  getHistory: (params: { productId: string }) => 
    defHttp.get<ProductHistoryListResult>({ url: `${API_PREFIX}/history`, params }),
  
  // 搜索产品
  search: (params?: any) => defHttp.get<ProductListResult>({ url: `${API_PREFIX}/search`, params }),
};