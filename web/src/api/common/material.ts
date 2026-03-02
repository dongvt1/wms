/**
 * Material (Nguyên vật liệu) API – Common Module
 *
 * Material TRONG HỆ THỐNG NÀY là Product với type = 'material'.
 * File này cung cấp alias semantic rõ ràng để các module (planning, qms)
 * import theo đúng nghĩa của dữ liệu mà mình đang làm việc với.
 */

import { productApi, getProductOptions, type ProductModel } from './product';

/** MaterialModel = ProductModel với type = 'material' */
export type MaterialModel = ProductModel;

/** materialApi = productApi (cùng bảng, cùng endpoint) */
export const materialApi = productApi;

/**
 * Lấy danh sách options chỉ cho NVL (type = 'material')
 * label = "code - name", value = id
 */
export async function getMaterialOptions() {
  return getProductOptions('material');
}

/**
 * Lấy danh sách tất cả NVL + thành phẩm (dùng khi BOM cần chọn cả 2 loại)
 */
export async function getAllProductMaterialOptions() {
  return getProductOptions(); // không lọc type → lấy tất cả active
}

export { productApi as productForBomApi };
