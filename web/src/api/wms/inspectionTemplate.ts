import { defHttp } from '/@/utils/http/axios';
import { PageResult, PageParams } from './types';

const BASE = '/api/qms/inspection-template';

// --- Field Config Types ---

export interface NumberFieldConfig {
  minValue?: number;
  maxValue?: number;
  decimalPlaces?: number;
}

export interface MeasurementFieldConfig {
  nominalValue?: number;
  upperTolerance?: number;
  lowerTolerance?: number;
}

export interface SelectFieldConfig {
  options: string[];
}

export interface BooleanFieldConfig {
  trueLabel?: string;
  falseLabel?: string;
}

export interface TextFieldConfig {
  maxLength?: number;
  multiline?: boolean;
}

export type FieldConfig =
  | NumberFieldConfig
  | MeasurementFieldConfig
  | SelectFieldConfig
  | BooleanFieldConfig
  | TextFieldConfig;

export type FieldType = 'text' | 'number' | 'boolean' | 'select' | 'measurement';

// --- Step Field ---

export interface StepFieldDTO {
  id?: string;
  fieldName: string;
  fieldCode?: string;
  fieldType: FieldType;
  unit?: string;
  defaultValue?: string;
  isRequired?: boolean;
  sortOrder?: number;
  fieldConfig?: FieldConfig;
  hint?: string;
}

export interface StepFieldModel {
  id: string;
  stepId: string;
  fieldName: string;
  fieldCode?: string;
  fieldType: FieldType;
  unit?: string;
  defaultValue?: string;
  isRequired: boolean;
  sortOrder: number;
  fieldConfig?: FieldConfig;
  hint?: string;
}

// --- Inspection Step ---

export interface InspectionStepDTO {
  id?: string;
  stepName: string;
  description?: string;
  sortOrder?: number;
  isMandatory?: boolean;
  requiresApproval?: boolean;
  fields?: StepFieldDTO[];
}

export interface InspectionStepModel {
  id: string;
  templateId: string;
  stepName: string;
  description?: string;
  sortOrder: number;
  isMandatory: boolean;
  requiresApproval: boolean;
  fields: StepFieldModel[];
}

// --- Inspection Template ---

export interface InspectionTemplateDTO {
  templateName: string;
  description?: string;
  stageType: 'iqc' | 'pqc' | 'fqc';
  version?: string;
  notes?: string;
  steps?: InspectionStepDTO[];
}

export interface InspectionTemplateModel {
  id: string;
  templateCode: string;
  templateName: string;
  description?: string;
  stageType: 'iqc' | 'pqc' | 'fqc';
  version: string;
  status: 'draft' | 'active' | 'obsolete';
  notes?: string;
  stepCount?: number;
  createBy?: string;
  createTime?: string;
  updateBy?: string;
  updateTime?: string;
  steps?: InspectionStepModel[];
}

// --- Filter Params ---

export interface TemplateListParams extends PageParams {
  stageType?: 'iqc' | 'pqc' | 'fqc';
  status?: 'draft' | 'active' | 'obsolete';
  search?: string;
}

// --- API ---

export const wmsInspectionTemplateApi = {
  /** Danh sách template có phân trang + filter */
  list: (params?: TemplateListParams) =>
    defHttp.get<PageResult<InspectionTemplateModel>>({ url: `${BASE}/list`, params }),

  /** Chi tiết template kèm steps + fields */
  queryById: (id: string) =>
    defHttp.get<InspectionTemplateModel>({ url: `${BASE}/${id}` }),

  /** Tạo template mới (kèm steps + fields) */
  add: (params: InspectionTemplateDTO) =>
    defHttp.post<InspectionTemplateModel>({ url: BASE, params }),

  /** Cập nhật template (kèm steps + fields) */
  edit: (id: string, params: InspectionTemplateDTO) =>
    defHttp.put<InspectionTemplateModel>({ url: `${BASE}/${id}`, params }),

  /** Xóa template (kiểm tra referential integrity) */
  delete: (id: string) =>
    defHttp.delete({ url: `${BASE}/${id}` }),

  /**
   * Kích hoạt template.
   * - 422: validation errors (inline display)
   * - 409: conflict with existing active template (modal confirm)
   */
  activate: (id: string) =>
    defHttp.put({ url: `${BASE}/${id}/activate` }, { errorMessageMode: 'none' }),

  /** Nhân bản template (deep clone) */
  clone: (id: string) =>
    defHttp.post<InspectionTemplateModel>({ url: `${BASE}/${id}/clone` }),

  /** Preview template data (render dạng form kiểm tra) */
  preview: (id: string) =>
    defHttp.get<InspectionTemplateModel>({ url: `${BASE}/${id}/preview` }),
};
