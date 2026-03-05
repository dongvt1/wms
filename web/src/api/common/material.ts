import { defHttp } from '/@/utils/http/axios';

const BASE_URL = '/common/material';

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

  /** Chiều dài (mm) */
  length?: number;
  /** Chiều rộng (mm) */
  width?: number;
  /** Chiều cao (mm) */
  height?: number;
  /** 1=active, 0=inactive */
  status?: number;
  image?: string;
  /** Cân nặng (kg) */
  weight?: number;
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

/**
 * Material API – trỏ vào /common/material (bảng material tách riêng)
 */
export const materialApi = {
  list: (params?: any) => defHttp.get({ url: `${BASE_URL}/list`, params }),
  listAll: () => defHttp.get<MaterialModel[]>({ url: `${BASE_URL}/listAll` }),
  queryById: (id: string) => defHttp.get<MaterialModel>({ url: `${BASE_URL}/queryById`, params: { id } }),
  add: (params: { material: MaterialModel; substitutes: MaterialSubstituteModel[] }) =>
    defHttp.post({ url: `${BASE_URL}/add`, params }),
  edit: (params: { material: MaterialModel; substitutes: MaterialSubstituteModel[] }) =>
    defHttp.put({ url: `${BASE_URL}/edit`, params }),
  delete: (params: { id: string }) => defHttp.delete({ url: `${BASE_URL}/delete`, params }),
  deleteBatch: (params: { ids: string }) => defHttp.delete({ url: `${BASE_URL}/deleteBatch`, params }),
  getSubstitutes: (materialId: string) =>
    defHttp.get<MaterialSubstituteModel[]>({ url: `${BASE_URL}/getSubstitutes`, params: { materialId } }),
};

/** Helper: lấy options cho Select dropdown trong BOM */
export async function getMaterialOptions() {
  try {
    const list: any = await materialApi.listAll();
    return (list || []).map((m: MaterialModel) => ({
      label: `${m.code} - ${m.name}`,
      value: m.id,
    }));
  } catch {
    return [];
  }
}
