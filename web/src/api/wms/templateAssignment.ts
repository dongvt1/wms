import { defHttp } from '/@/utils/http/axios';
import { PageResult, PageParams } from './types';
import type { InspectionTemplateModel } from './inspectionTemplate';

const BASE = '/api/qms/template-assignment';

// --- Models ---

export interface TemplateAssignmentModel {
  id: string;
  templateId: string;
  templateName?: string;
  assignmentType: 'product' | 'product_group' | 'default';
  targetId?: string;
  targetName?: string;
  isActive: number;
  createBy?: string;
  createTime?: string;
}

// --- DTOs ---

export interface CreateAssignmentDTO {
  templateId: string;
  assignmentType: 'product' | 'product_group' | 'default';
  targetId?: string;
}

// --- Filter Params ---

export interface AssignmentListParams extends PageParams {
  templateId?: string;
  assignmentType?: 'product' | 'product_group' | 'default';
}

export interface ResolveParams {
  productId: string;
  stageType: 'iqc' | 'pqc' | 'fqc';
}

// --- API ---

export const wmsTemplateAssignmentApi = {
  /** Danh sách assignments (filter theo template) */
  list: (params?: AssignmentListParams) =>
    defHttp.get<PageResult<TemplateAssignmentModel>>({ url: `${BASE}/list`, params }),

  /**
   * Gán template cho sản phẩm/nhóm.
   * - 409: conflict nếu đã có assignment active cho cùng target + stage type
   */
  add: (params: CreateAssignmentDTO) =>
    defHttp.post<TemplateAssignmentModel>({ url: BASE, params }, { errorMessageMode: 'none' }),

  /** Gỡ assignment */
  delete: (id: string) =>
    defHttp.delete({ url: `${BASE}/${id}` }),

  /** Tìm template phù hợp cho sản phẩm + stage type (theo priority) */
  resolve: (params: ResolveParams) =>
    defHttp.get<InspectionTemplateModel>({ url: `${BASE}/resolve`, params }),
};
