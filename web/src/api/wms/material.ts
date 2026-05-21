import { defHttp } from '/@/utils/http/axios';
import { PageResult, PageParams } from './types';

const BASE = '/wms/material';

export interface MaterialModel {
  id?: string;
  code: string;
  name: string;
  description?: string;
  unit?: string;
  price?: number;
  categoryId?: string;
  categoryName?: string;
  minStockLevel?: number;
  currentStock?: number;
  length?: number;
  width?: number;
  height?: number;
  weight?: number;
  status?: number;
  image?: string;
  createTime?: string;
  substitutes?: MaterialSubstituteModel[];
}

export interface MaterialSubstituteModel {
  id?: string;
  materialId?: string;
  substituteMaterialId?: string;
  substituteName?: string;
  substituteCode?: string;
  substituteUnit?: string;
  priority?: number;
  notes?: string;
}

export const wmsMaterialApi = {
  list: (params?: PageParams) => defHttp.get<PageResult<MaterialModel>>({ url: `${BASE}/list`, params }),
  listAll: () => defHttp.get<MaterialModel[]>({ url: `${BASE}/listAll` }),
  queryById: (id: string) => defHttp.get<MaterialModel>({ url: `${BASE}/queryById`, params: { id } }),
  add: (params: { material: MaterialModel; substitutes: MaterialSubstituteModel[] }) =>
    defHttp.post({ url: `${BASE}/add`, params }),
  edit: (params: { material: MaterialModel; substitutes: MaterialSubstituteModel[] }) =>
    defHttp.put({ url: `${BASE}/edit`, params }),
  delete: (params: { id: string }) => defHttp.delete({ url: `${BASE}/delete`, params }),
  deleteBatch: (params: { ids: string }) => defHttp.delete({ url: `${BASE}/deleteBatch`, params }),
  getSubstitutes: (materialId: string) =>
    defHttp.get<MaterialSubstituteModel[]>({ url: `${BASE}/getSubstitutes`, params: { materialId } }),
};
