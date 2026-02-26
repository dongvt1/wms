import { defHttp } from '/@/utils/http/axios';

// interface prefix
const API_PREFIX = '/warehouse/category';

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

export interface CategoryListResult {
  records: CategoryModel[];
  total: number;
  size: number;
  current: number;
  pages: number;
}

/**
 * Query product classification list
 * @param params query parameters
 */
export const categoryApi = {
  // list
  list: (params?: any) => defHttp.get<CategoryListResult>({ url: `${API_PREFIX}/list`, params }),
  
  // delete
  delete: (params: { id: string }) => defHttp.delete({ url: `${API_PREFIX}/delete`, params }),
  
  // 批量delete
  deleteBatch: (params: { ids: string }) => defHttp.delete({ url: `${API_PREFIX}/deleteBatch`, params }),
  
  // according toIDQuery
  getById: (params: { id: string }) => defHttp.get<CategoryModel>({ url: `${API_PREFIX}/queryById`, params }),
  
  // Save or update
  save: (params: CategoryModel) => defHttp.post({ url: `${API_PREFIX}/add`, params }),
  
  update: (params: CategoryModel) => defHttp.put({ url: `${API_PREFIX}/edit`, params }),
  
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
  importExcel: (params: any, onUploadProgress?: (progressEvent: any) => void) =>
    defHttp.uploadFile<any>(
      {
        url: `${API_PREFIX}/importExcel`,
        onUploadProgress,
      },
      params
    ),
  
  // Get the classification tree structure
  getTree: (params?: any) => defHttp.get<CategoryModel[]>({ url: `${API_PREFIX}/tree`, params }),
};