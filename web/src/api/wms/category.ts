import { defHttp } from '/@/utils/http/axios';
import { PageResult, PageParams } from './types';

const BASE = '/wms/category';

export interface CategoryModel {
  id?: string;
  name: string;
  description?: string;
  parentId?: string;
  parentName?: string;
  status: number;
  createTime?: string;
  updateTime?: string;
}

export const wmsCategoryApi = {
  list: (params?: PageParams) => defHttp.get<PageResult<CategoryModel>>({ url: `${BASE}/list`, params }),
  queryById: (params: { id: string }) => defHttp.get<CategoryModel>({ url: `${BASE}/queryById`, params }),
  add: (params: CategoryModel) => defHttp.post({ url: `${BASE}/add`, params }),
  edit: (params: CategoryModel) => defHttp.put({ url: `${BASE}/edit`, params }),
  delete: (params: { id: string }) => defHttp.delete({ url: `${BASE}/delete`, params }),
  deleteBatch: (params: { ids: string }) => defHttp.delete({ url: `${BASE}/deleteBatch`, params }),
  getTree: (params?: any) => defHttp.get<CategoryModel[]>({ url: `${BASE}/tree`, params }),
  exportXls: (params?: any) => defHttp.get({ url: `${BASE}/exportXls`, params, responseType: 'blob' }, { isTransformResponse: false }),
  importExcel: (params: any, onUploadProgress?: (e: any) => void) =>
    defHttp.uploadFile<any>({ url: `${BASE}/importExcel`, onUploadProgress }, params),
};
