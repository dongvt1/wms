import { defHttp } from '/@/utils/http/axios';
import { UploadFileParams } from '/#/axios';

// 接口前缀
const API_PREFIX = '/warehouse/area';

export interface WarehouseAreaModel {
  id?: string;
  areaCode: string;
  areaName: string;
  description?: string;
  status: number;
  capacity: number;
  usedCapacity: number;
  createTime?: string;
  updateTime?: string;
}

export interface WarehouseAreaListResult {
  records: WarehouseAreaModel[];
  total: number;
  size: number;
  current: number;
  pages: number;
}

/**
 * 查询仓库区域列表
 * @param params 查询参数
 */
export const warehouseAreaApi = {
  // 列表
  list: (params?: any) => defHttp.get<WarehouseAreaListResult>({ url: `${API_PREFIX}/list`, params }),
  
  // 删除
  delete: (params: { id: string }) => defHttp.delete({ url: `${API_PREFIX}/delete`, params }),
  
  // 批量删除
  deleteBatch: (params: { ids: string }) => defHttp.delete({ url: `${API_PREFIX}/deleteBatch`, params }),
  
  // 根据ID查询
  getById: (params: { id: string }) => defHttp.get<WarehouseAreaModel>({ url: `${API_PREFIX}/queryById`, params }),
  
  // 保存或更新
  save: (params: WarehouseAreaModel) => defHttp.post({ url: `${API_PREFIX}/add`, params }),
  
  update: (params: WarehouseAreaModel) => defHttp.put({ url: `${API_PREFIX}/edit`, params }),
  
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
  importExcel: (params: UploadFileParams, onUploadProgress?: (progressEvent: ProgressEvent) => void) =>
    defHttp.uploadFile<any>(
      {
        url: `${API_PREFIX}/importExcel`,
        onUploadProgress,
      },
      params
    ),
};