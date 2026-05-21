import { defHttp } from '/@/utils/http/axios';
import { UploadFileParams } from '/#/axios';
import { AxiosProgressEvent } from 'axios';
import { PageResult, PageParams } from './types';

const BASE = '/wms/product';

export interface ProductModel {
  id?: string;
  code: string;
  name: string;
  description?: string;
  price?: number;
  categoryId?: string;
  categoryName?: string;
  minStockLevel?: number;
  image?: string;
  status: number;
  currentStock?: number;
  /** Loại: 'product' | 'material' | 'semi' */
  type?: string;
  createTime?: string;
  updateTime?: string;
}

export const wmsProductApi = {
  list: (params?: PageParams) => defHttp.get<PageResult<ProductModel>>({ url: `${BASE}/list`, params }),
  queryById: (params: { id: string }) => defHttp.get<ProductModel>({ url: `${BASE}/queryById`, params }),
  add: (params: ProductModel) => defHttp.post({ url: `${BASE}/add`, params }),
  edit: (params: ProductModel) => defHttp.put({ url: `${BASE}/edit`, params }),
  delete: (params: { id: string }) => defHttp.delete({ url: `${BASE}/delete`, params }),
  deleteBatch: (params: { ids: string }) => defHttp.delete({ url: `${BASE}/deleteBatch`, params }),
  search: (params: { keyword: string }) => defHttp.get<PageResult<ProductModel>>({ url: `${BASE}/search`, params }),
  listActive: () => defHttp.get<ProductModel[]>({ url: `${BASE}/listActive` }),
  listByType: (type: string) => defHttp.get<ProductModel[]>({ url: `${BASE}/listByType`, params: { type } }),
  listByCategory: (categoryId: string) => defHttp.get<ProductModel[]>({ url: `${BASE}/listByCategory`, params: { categoryId } }),
  getLowStock: () => defHttp.get<ProductModel[]>({ url: `${BASE}/getLowStock` }),
  getHistory: (params: { productId: string }) => defHttp.get({ url: `${BASE}/history`, params }),
  uploadImage: (params: UploadFileParams, onUploadProgress?: (e: AxiosProgressEvent) => void) =>
    defHttp.uploadFile<any>({ url: `${BASE}/uploadImage`, onUploadProgress }, params),
  exportXls: (params?: any) => defHttp.get({ url: `${BASE}/exportXls`, params, responseType: 'blob' }, { isTransformResponse: false }),
  importExcel: (params: UploadFileParams, onUploadProgress?: (e: AxiosProgressEvent) => void) =>
    defHttp.uploadFile<any>({ url: `${BASE}/importExcel`, onUploadProgress }, params),
};
