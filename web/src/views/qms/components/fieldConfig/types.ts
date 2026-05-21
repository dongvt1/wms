/**
 * Type definitions for QMS Step Field configuration components
 */

/** Supported field types */
export type FieldType = 'text' | 'number' | 'boolean' | 'select' | 'measurement';

/** Number field configuration */
export interface NumberFieldConfigData {
  minValue?: number | null;
  maxValue?: number | null;
  decimalPlaces?: number | null;
}

/** Measurement field configuration */
export interface MeasurementFieldConfigData {
  nominalValue?: number | null;
  upperTolerance?: number | null;
  lowerTolerance?: number | null;
}

/** Select field configuration */
export interface SelectFieldConfigData {
  options: string[];
}

/** Boolean field configuration */
export interface BooleanFieldConfigData {
  trueLabel?: string;
  falseLabel?: string;
}

/** Text field configuration */
export interface TextFieldConfigData {
  maxLength?: number | null;
  multiline?: boolean;
  placeholder?: string;
}

/** Union type for all field configs */
export type FieldConfigData =
  | NumberFieldConfigData
  | MeasurementFieldConfigData
  | SelectFieldConfigData
  | BooleanFieldConfigData
  | TextFieldConfigData
  | Record<string, any>;

/** Step Field model used in the form */
export interface StepFieldModel {
  id?: string;
  fieldName: string;
  fieldCode: string;
  fieldType: FieldType;
  unit: string;
  isRequired: boolean;
  sortOrder: number;
  hint: string;
  defaultValue?: string;
  fieldConfig: FieldConfigData;
}

/** Default configs for each field type */
export function getDefaultConfig(fieldType: FieldType): FieldConfigData {
  switch (fieldType) {
    case 'number':
      return { minValue: null, maxValue: null, decimalPlaces: 2 };
    case 'measurement':
      return { nominalValue: null, upperTolerance: null, lowerTolerance: null };
    case 'select':
      return { options: [] };
    case 'boolean':
      return { trueLabel: 'Đạt', falseLabel: 'Không đạt' };
    case 'text':
      return { maxLength: null, multiline: false, placeholder: '' };
    default:
      return {};
  }
}

/** Field type options for select dropdown */
export const FIELD_TYPE_OPTIONS = [
  { label: 'Văn bản (Text)', value: 'text' },
  { label: 'Số (Number)', value: 'number' },
  { label: 'Đạt/Không đạt (Boolean)', value: 'boolean' },
  { label: 'Danh sách chọn (Select)', value: 'select' },
  { label: 'Đo lường (Measurement)', value: 'measurement' },
];
