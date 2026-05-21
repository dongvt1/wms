/**
 * WMS API - Chuẩn hoá Public API
 * ================================
 *
 * ## Quy ước URL Prefix
 *
 * | Module          | Prefix cũ (không nhất quán)         | Prefix mới (chuẩn)      |
 * |-----------------|-------------------------------------|--------------------------|
 * | Product         | /warehouse/product, /common/product | /wms/product             |
 * | Category        | /warehouse/category                 | /wms/category            |
 * | Material        | /common/material                    | /wms/material            |
 * | BOM             | /common/bom                         | /wms/bom                 |
 * | Inventory       | /warehouse/inventory                | /wms/inventory           |
 * | Area            | /warehouse/area                     | /wms/area                |
 * | Shelf           | /warehouse/shelf                    | /wms/shelf               |
 * | Stock           | /warehouse/stock                    | /wms/stock               |
 * | Supplier        | /warehouse/supplier                 | /wms/supplier            |
 * | Customer        | /warehouse/customer                 | /wms/customer            |
 * | Order           | /warehouse/orders                   | /wms/order               |
 * | Work Order      | /warehouse/workOrder                | /wms/work-order          |
 * | Production Line | /warehouse/productionLine           | /wms/production-line     |
 * | QC Stage        | /qms/stage                          | /wms/qc/stage            |
 * | QC Session      | /qms/session                        | /wms/qc/session          |
 * | QC Checklist    | /qms/checklist                      | /wms/qc/checklist        |
 * | QC IQC          | /qms/iqc                            | /wms/qc/iqc              |
 * | QC PQC          | /qms/pqc                            | /wms/qc/pqc              |
 * | QC Review       | /qms/review                         | /wms/qc/review           |
 *
 * ## Quy ước CRUD Actions
 *
 * | Action          | HTTP Method | URL Pattern              |
 * |-----------------|-------------|--------------------------|
 * | Danh sách       | GET         | /{prefix}/list           |
 * | Chi tiết        | GET         | /{prefix}/queryById      |
 * | Thêm mới        | POST        | /{prefix}/add            |
 * | Cập nhật        | PUT         | /{prefix}/edit           |
 * | Xóa             | DELETE      | /{prefix}/delete         |
 * | Xóa hàng loạt   | DELETE      | /{prefix}/deleteBatch    |
 * | Export Excel    | GET         | /{prefix}/exportXls      |
 * | Import Excel    | POST        | /{prefix}/importExcel    |
 *
 * ## Quy ước Naming
 *
 * - API object: wms{Module}Api (e.g. wmsProductApi, wmsOrderApi)
 * - Model type: {Module}Model (e.g. ProductModel, OrderModel)
 * - Tất cả params đều typed
 * - Dùng PageResult<T> cho response phân trang
 * - Dùng PageParams cho request phân trang
 *
 * ## Cách sử dụng
 *
 * ```ts
 * import { wmsProductApi, wmsOrderApi } from '/@/api/wms';
 *
 * // Lấy danh sách sản phẩm
 * const result = await wmsProductApi.list({ pageNo: 1, pageSize: 10 });
 *
 * // Tạo đơn hàng
 * await wmsOrderApi.add({ customerId: '...', ... });
 * ```
 *
 * ## Migration Guide
 *
 * Các file API cũ ở `src/views/*/xxx.api.ts` và `src/api/warehouse/`, `src/api/common/`
 * sẽ dần được thay thế bằng import từ `/@/api/wms`.
 *
 * Bước 1: Import từ file mới
 * Bước 2: Đổi tên method nếu cần (e.g. save → add)
 * Bước 3: Backend cần map URL prefix mới (hoặc dùng URL rewrite)
 */

export * from './types';
export * from './product';
export * from './category';
export * from './material';
export * from './bom';
export * from './inventory';
export * from './area';
export * from './shelf';
export * from './stock';
export * from './supplier';
export * from './customer';
export * from './order';
export * from './workOrder';
export * from './productionLine';
export * from './qcStage';
export * from './qcSession';
export * from './qcChecklist';
export * from './qcInspection';
export * from './fqcInspection';
export * from './qcReview';
export * from './ncr';
export * from './qmsAttachment';
export * from './qmsAnalytics';
export * from './inspectionTemplate';
export * from './inspectionExecution';
export * from './templateAssignment';
export * from './approval';
export * from './report';
export * from './qmsErrorHandler';
