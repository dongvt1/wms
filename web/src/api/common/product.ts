import { defHttp } from '/@/utils/http/axios';
import { UploadFileParams } from '/#/axios';
import { AxiosProgressEvent } from 'axios';

const BASE_URL = '/common/product';

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
  /** Trạng thái: 0 = inactive, 1 = active */
  status: number;
  currentStock?: number;
  /** Loại: 'product' | 'material' | 'semi' */
  type?: string;
  createTime?: string;
  updateTime?: string;
}

export interface ProductListResult {
  records: ProductModel[];
  total: number;
  size: number;
  current: number;
  pages: number;
}

/**
 * Common Product API – trỏ vào /common/product
 * Dùng chung cho tất cả module: warehouse, planning, qms, ...
 */
export const productApi = {
  /** Danh sách có phân trang */
  list: (params?: any) => defHttp.get<ProductListResult>({ url: `${BASE_URL}/list`, params }),

  /** Thêm mới */
  add: (params: ProductModel) => defHttp.post({ url: `${BASE_URL}/add`, params }),

  /** Cập nhật */
  edit: (params: ProductModel) => defHttp.put({ url: `${BASE_URL}/edit`, params }),

  /** Xóa theo id */
  delete: (params: { id: string }) => defHttp.delete({ url: `${BASE_URL}/delete`, params }),

  /** Xóa hàng loạt */
  deleteBatch: (params: { ids: string }) => defHttp.delete({ url: `${BASE_URL}/deleteBatch`, params }),

  /** Lấy theo id */
  queryById: (params: { id: string }) => defHttp.get<ProductModel>({ url: `${BASE_URL}/queryById`, params }),

  /** Tìm kiếm theo từ khóa */
  search: (params: { keyword: string }) => defHttp.get<ProductListResult>({ url: `${BASE_URL}/search`, params }),

  /** Tất cả sản phẩm đang active */
  listActive: () => defHttp.get<ProductModel[]>({ url: `${BASE_URL}/listActive` }),

  /** Lấy theo loại: product / material / semi */
  listByType: (type: string) => defHttp.get<ProductModel[]>({ url: `${BASE_URL}/listByType`, params: { type } }),

  /** Lấy theo danh mục */
  listByCategory: (categoryId: string) =>
    defHttp.get<ProductModel[]>({ url: `${BASE_URL}/listByCategory`, params: { categoryId } }),

  /** Sản phẩm sắp hết hàng */
  getLowStock: () => defHttp.get<ProductModel[]>({ url: `${BASE_URL}/getLowStock` }),

  /** Upload ảnh sản phẩm */
  uploadImage: (
    params: UploadFileParams,
    onUploadProgress?: (progressEvent: AxiosProgressEvent) => void,
  ) =>
    defHttp.uploadFile<any>(
      { url: `${BASE_URL}/uploadImage`, onUploadProgress },
      params,
    ),

  /** Export Excel */
  exportXls: (params?: any) => {
    const url = `${BASE_URL}/exportXls`;
    if (params) {
      const qs = Object.keys(params)
        .map((k) => `${k}=${encodeURIComponent(params[k])}`)
        .join('&');
      window.open(`${url}?${qs}`);
    } else {
      window.open(url);
    }
  },

  /** Import Excel */
  importExcel: (
    params: UploadFileParams,
    onUploadProgress?: (progressEvent: AxiosProgressEvent) => void,
  ) =>
    defHttp.uploadFile<any>(
      { url: `${BASE_URL}/importExcel`, onUploadProgress },
      params,
    ),
};

/**
 * Helper: lấy danh sách options cho Select component
 * label = "code - name", value = id
 */
export async function getProductOptions(type?: string) {
  try {
    const list: any = type ? await productApi.listByType(type) : await productApi.listActive();
    return (list || []).map((p: ProductModel) => ({
      label: `${p.code} - ${p.name}`,
      value: p.id,
    }));
  } catch {
    return [];
  }
}

/**
 * Helper: lấy options chỉ cho NVL (type = 'material')
 */
export async function getMaterialOptions() {
  return getProductOptions('material');
}
