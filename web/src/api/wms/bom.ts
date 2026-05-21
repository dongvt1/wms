import { defHttp } from '/@/utils/http/axios';
import { PageParams } from './types';

const BASE = '/wms/bom';

export interface BomModel {
  id?: string;
  bomCode: string;
  bomName: string;
  productId?: string;
  productName?: string;
  outputQuantity?: number;
  unit?: string;
  version?: string;
  status?: string;
  isDefault?: boolean;
  notes?: string;
  createTime?: string;
}

export interface BomItemModel {
  id?: string;
  bomId?: string;
  materialId?: string;
  materialName?: string;
  materialCode?: string;
  childBomId?: string;
  itemType?: string;
  quantity?: number;
  unit?: string;
  purchaseLeadTimeDays?: number;
  wastageRate?: number;
  notes?: string;
  refDesignators?: string;
}

export const wmsBomApi = {
  list: (params?: PageParams) => defHttp.get({ url: `${BASE}/list`, params }),
  queryById: (params: { id: string }) => defHttp.get({ url: `${BASE}/queryById`, params }),
  add: (params: { bom: BomModel; items: BomItemModel[] }) => defHttp.post({ url: `${BASE}/add`, params }),
  edit: (params: { bom: BomModel; items: BomItemModel[] }) => defHttp.put({ url: `${BASE}/edit`, params }),
  delete: (params: { id: string }) => defHttp.delete({ url: `${BASE}/delete`, params }),
  deleteBatch: (params: { ids: string }) => defHttp.delete({ url: `${BASE}/deleteBatch`, params }),
  getItems: (bomId: string) => defHttp.get<BomItemModel[]>({ url: `${BASE}/getItems`, params: { bomId } }),
  getByProductId: (productId: string) => defHttp.get<BomModel[]>({ url: `${BASE}/getByProductId`, params: { productId } }),
  listActive: () => defHttp.get<BomModel[]>({ url: `${BASE}/listActive` }),
  setDefault: (params: { bomId: string; productId: string }) => defHttp.post({ url: `${BASE}/setDefault`, params }),
  getTree: (bomId: string) => defHttp.get({ url: `${BASE}/tree`, params: { bomId } }),
  flattenMaterials: (bomId: string, quantity = 1) =>
    defHttp.get({ url: `${BASE}/flattenMaterials`, params: { bomId, quantity } }),
  whereUsed: (materialId: string) => defHttp.get({ url: `${BASE}/whereUsed`, params: { materialId } }),
};
