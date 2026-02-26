import { defHttp } from '/@/utils/http/axios';
import { UploadFileParams } from '/#/axios';

// interface prefix
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
 * Query warehouse area list
 * @param params query parameters
 */
export const warehouseAreaApi = {
  // list
  list: (params?: any) => defHttp.get<WarehouseAreaListResult>({ url: `${API_PREFIX}/list`, params }),
  
  // delete
  delete: (params: { id: string }) => defHttp.delete({ url: `${API_PREFIX}/delete`, params }),
  
  // 批量delete
  deleteBatch: (params: { ids: string }) => defHttp.delete({ url: `${API_PREFIX}/deleteBatch`, params }),
  
  // according toIDQuery
  getById: (params: { id: string }) => defHttp.get<WarehouseAreaModel>({ url: `${API_PREFIX}/queryById`, params }),
  
  // Save or update
  save: (params: WarehouseAreaModel) => defHttp.post({ url: `${API_PREFIX}/add`, params }),
  
  update: (params: WarehouseAreaModel) => defHttp.put({ url: `${API_PREFIX}/edit`, params }),
  
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
  importExcel: (params: UploadFileParams, onUploadProgress?: (progressEvent: ProgressEvent) => void) =>
    defHttp.uploadFile<any>(
      {
        url: `${API_PREFIX}/importExcel`,
        onUploadProgress,
      },
      params
    ),
};