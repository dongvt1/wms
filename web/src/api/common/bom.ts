import { defHttp } from '/@/utils/http/axios';

const BASE_URL = '/common/bom';

export interface BomModel {
  id?: string;
  bomCode: string;
  bomName: string;
  productId?: string;
  productName?: string;
  outputQuantity?: number;
  unit?: string;
  version?: string;
  /** Trạng thái: 'active' | 'inactive' */
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
  /** Loại vật tư: 'raw_material' | 'sub_assembly' */
  itemType?: string;
  quantity?: number;
  unit?: string;
  purchaseLeadTimeDays?: number;
  wastageRate?: number;
  notes?: string;
  refDesignators?: string;
}

/**
 * Common BOM API – trỏ vào /common/bom
 * Dùng chung cho tất cả module: warehouse, planning, qms, ...
 */
export const bomApi = {
  /** Danh sách có phân trang */
  list: (params?: any) => defHttp.get({ url: `${BASE_URL}/list`, params }),

  /** Thêm BOM kèm NVL */
  add: (params: { bom: BomModel; items: BomItemModel[] }) =>
    defHttp.post({ url: `${BASE_URL}/add`, params }),

  /** Sửa BOM kèm NVL */
  edit: (params: { bom: BomModel; items: BomItemModel[] }) =>
    defHttp.put({ url: `${BASE_URL}/edit`, params }),

  /** Xóa BOM */
  delete: (params: { id: string }) => defHttp.delete({ url: `${BASE_URL}/delete`, params }),

  /** Xóa hàng loạt BOM */
  deleteBatch: (params: { ids: string }) => defHttp.delete({ url: `${BASE_URL}/deleteBatch`, params }),

  /** Lấy chi tiết BOM kèm NVL */
  queryById: (params: { id: string }) => defHttp.get({ url: `${BASE_URL}/queryById`, params }),

  /** Lấy danh sách NVL trong BOM */
  getItems: (bomId: string) => defHttp.get<BomItemModel[]>({ url: `${BASE_URL}/getItems`, params: { bomId } }),

  /** Lấy BOM theo thành phẩm */
  getByProductId: (productId: string) =>
    defHttp.get<BomModel[]>({ url: `${BASE_URL}/getByProductId`, params: { productId } }),

  /** Tất cả BOM đang active */
  listActive: () => defHttp.get<BomModel[]>({ url: `${BASE_URL}/listActive` }),

  /** Đặt BOM làm mặc định cho sản phẩm */
  setDefault: (params: { bomId: string; productId: string }) =>
    defHttp.post({ url: `${BASE_URL}/setDefault`, params }),

  /** Xem cấu trúc cây BOM */
  getTree: (bomId: string) => defHttp.get({ url: `${BASE_URL}/tree`, params: { bomId } }),

  /** Phẳng hoá BOM – tổng NVL gốc cần */
  flattenMaterials: (bomId: string, quantity = 1) =>
    defHttp.get({ url: `${BASE_URL}/flattenMaterials`, params: { bomId, quantity } }),

  /** Where-used: tìm tất cả BOM dùng NVL này */
  whereUsed: (materialId: string) =>
    defHttp.get({ url: `${BASE_URL}/whereUsed`, params: { materialId } }),
};

/**
 * Helper: lấy danh sách BOM options cho Select component
 * label = "bomCode - bomName", value = id
 */
export async function getBomOptions() {
  try {
    const list: any = await bomApi.listActive();
    return (list || []).map((b: BomModel) => ({
      label: `${b.bomCode} - ${b.bomName}`,
      value: b.id,
    }));
  } catch {
    return [];
  }
}
