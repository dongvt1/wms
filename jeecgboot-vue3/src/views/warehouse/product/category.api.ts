import { defHttp } from '/@/utils/http/axios';

// 接口前缀
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
 * 查询产品分类列表
 * @param params 查询参数
 */
export const categoryApi = {
  // 列表
  list: (params?: any) => defHttp.get<CategoryListResult>({ url: `${API_PREFIX}/list`, params }),
  
  // 删除
  delete: (params: { id: string }) => defHttp.delete({ url: `${API_PREFIX}/delete`, params }),
  
  // 批量删除
  deleteBatch: (params: { ids: string }) => defHttp.delete({ url: `${API_PREFIX}/deleteBatch`, params }),
  
  // 根据ID查询
  getById: (params: { id: string }) => defHttp.get<CategoryModel>({ url: `${API_PREFIX}/queryById`, params }),
  
  // 保存或更新
  save: (params: CategoryModel) => defHttp.post({ url: `${API_PREFIX}/add`, params }),
  
  update: (params: CategoryModel) => defHttp.put({ url: `${API_PREFIX}/edit`, params }),
  
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
  importExcel: (params: any, onUploadProgress?: (progressEvent: any) => void) =>
    defHttp.uploadFile<any>(
      {
        url: `${API_PREFIX}/importExcel`,
        onUploadProgress,
      },
      params
    ),
  
  // 获取分类树形结构
  getTree: (params?: any) => defHttp.get<CategoryModel[]>({ url: `${API_PREFIX}/tree`, params }),
};