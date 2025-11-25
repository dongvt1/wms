import type { NamePath, RuleObject, ValidateOptions } from 'ant-design-vue/lib/form/interface';
import type { VNode, ComputedRef } from 'vue';
import type { ButtonProps as AntdButtonProps } from '/@/components/Button';
import type { FormItem } from './formItem';
import type { ColEx, ComponentType } from './index';
import type { TableActionType } from '/@/components/Table/src/types/table';
import type { CSSProperties } from 'vue';
import type { RowProps } from 'ant-design-vue/lib/grid/Row';

export type FieldMapToTime = [string, [string, string], string?][];
export type FieldMapToNumber = [string, [string, string]][];

export type Rule = RuleObject & {
  trigger?: 'blur' | 'change' | ['change', 'blur'];
};

export interface RenderCallbackParams {
  schema: FormSchema;
  values: Recordable;
  model: Recordable;
  field: string;
}

export interface ButtonProps extends AntdButtonProps {
  text?: string;
}

export interface FormActionType {
  submit: () => Promise<void>;
  setFieldsValue: <T>(values: T) => Promise<void>;
  resetFields: () => Promise<void>;
  getFieldsValue: () => Recordable;
  clearValidate: (name?: string | string[]) => Promise<void>;
  updateSchema: (data: Partial<FormSchema> | Partial<FormSchema>[]) => Promise<void>;
  resetSchema: (data: Partial<FormSchema> | Partial<FormSchema>[]) => Promise<void>;
  setProps: (formProps: Partial<FormProps>) => Promise<void>;
  getProps: ComputedRef<Partial<FormProps>>;
  getSchemaByField: (field: string) => Nullable<FormSchema>;
  removeSchemaByFiled: (field: string | string[]) => Promise<void>;
  appendSchemaByField: (schema: FormSchema, prefixField: string | undefined, first?: boolean | undefined) => Promise<void>;
  validateFields: (nameList?: NamePath[], options?: ValidateOptions) => Promise<any>;
  validate: (nameList?: NamePath[]) => Promise<any>;
  scrollToField: (name: NamePath, options?: ScrollOptions) => Promise<void>;
  getSchemaComponentProps: (schema: FormSchema) => Recordable
}

export type RegisterFn = (formInstance: FormActionType) => void;

export type UseFormReturnType = [RegisterFn, FormActionType];

export interface FormProps {
  layout?: 'vertical' | 'inline' | 'horizontal';
  // Form value
  model?: Recordable;
  // The width of all items in the entire form
  labelWidth?: number | string;
  //alignment
  labelAlign?: 'left' | 'right';
  //Row configuration for the entire form
  rowProps?: RowProps;
  // Submit form on reset
  submitOnReset?: boolean;
  // Col configuration for the entire form
  labelCol?: Partial<ColEx> | null;
  // Col configuration for the entire form
  wrapperCol?: Partial<ColEx> | null;

  // General row style
  baseRowStyle?: CSSProperties;

  // General col configuration
  baseColProps?: Partial<ColEx>;

  // Form configuration rules
  schemas?: FormSchema[];
  // Function values used to merge into dynamic control form items
  mergeDynamicData?: Recordable;
  // Compact mode for search forms
  compact?: boolean;
  // Blank line span
  emptySpan?: number | Partial<ColEx>;
  // Internal component size of the form
  size?: 'default' | 'small' | 'large';
  // Whether to disable
  disabled?: boolean;
  // Time interval fields are mapped into multiple
  fieldMapToTime?: FieldMapToTime;
  // number interval fields are mapped into multiple
  fieldMapToNumber?: FieldMapToNumber;
  // Placeholder is set automatically
  autoSetPlaceHolder?: boolean;
  // Auto submit on press enter on input
  autoSubmitOnEnter?: boolean;
  // Check whether the information is added to the label
  rulesMessageJoinLabel?: boolean;
  // Whether to display the expand collapse button
  showAdvancedButton?: boolean;
  // Whether to focus on the first input box, only works when the first form item is input
  autoFocusFirstItem?: boolean;
  // 【jeecg】if showAdvancedButton for true，Default collapse when the specified number of columns is exceeded，默认for3
  autoAdvancedCol?: number;
  // if showAdvancedButton for true，Rows exceeding the specified number of rows are collapsed by default
  // update-begin--author:liaozhiyang---date:202401009---for：【issues/7261】Query items above the tableautoAdvancedLineConfiguration has no effect（deleteautoAdvancedLine）
  // autoAdvancedLine?: number;
  // update-end--author:liaozhiyang---date:202401009---for：【issues/7261】Query items above the tableautoAdvancedLineConfiguration has no effect（deleteautoAdvancedLine）
  // Number of rows to keep displayed when folded
  alwaysShowLines?: number;
  // Whether to show the operation button
  showActionButtonGroup?: boolean;

  // Reset button configuration
  resetButtonOptions?: Partial<ButtonProps>;

  // Confirm button configuration
  submitButtonOptions?: Partial<ButtonProps>;

  // Operation column configuration
  actionColOptions?: Partial<ColEx>;

  // Show reset button
  showResetButton?: boolean;
  // Show confirmation button
  showSubmitButton?: boolean;

  resetFunc?: () => Promise<void>;
  submitFunc?: () => Promise<void>;
  transformDateFunc?: (date: any) => string;
  colon?: boolean;
}
export interface FormSchema {
  // Field name
  field: string;
  // Event name triggered by internal value change, default change
  changeEvent?: string;
  // Variable name bound to v-model Default value
  valueField?: string;
  // Label name
  // update-begin--author:liaozhiyang---date:20240724---for：【issues/6908】When switching between multiple languages ​​without refreshing，BasicColumnandFormSchemaThe values ​​inside cannot be switched normally
  label: string | VNode | Fn;
  // update-end--author:liaozhiyang---date:20240724---for：【issues/6908】When switching between multiple languages ​​without refreshing，BasicColumnandFormSchemaThe values ​​inside cannot be switched normally
  // Auxiliary text
  subLabel?: string;
  // Help text on the right side of the text
  helpMessage?: string | string[] | ((renderCallbackParams: RenderCallbackParams) => string | string[]);
  // BaseHelp component props
  helpComponentProps?: Partial<HelpComponentProps>;
  // Label width, if it is passed, the labelCol and WrapperCol configured by itemProps will be invalid
  labelWidth?: string | number;
  // Disable the adjustment of labelWidth with global settings of formModel, and manually set labelCol and wrapperCol by yourself
  disabledLabelWidth?: boolean;
  // render component
  component: ComponentType;
  // Component parameters
  componentProps?:
    | ((opt: { schema: FormSchema; tableAction: TableActionType; formActionType: FormActionType; formModel: Recordable }) => Recordable)
    | object;
  // Required
  required?: boolean | ((renderCallbackParams: RenderCallbackParams) => boolean);

  suffix?: string | number | VueNode | ((values: RenderCallbackParams) => string | number | VueNode);
  // 【QQYUN-12876】Is it compact? suffix（When the component width is not full，Can be placed immediately to the right of the component）
  suffixCompact?: boolean;

  // Validation rules
  rules?: Rule[];
  // Check whether the information is added to the label
  rulesMessageJoinLabel?: boolean;

  // Reference formModelItem
  itemProps?: Partial<FormItem> | ((renderCallbackParams: RenderCallbackParams) => Partial<FormItem>);

  // col configuration outside formModelItem
  colProps?: Partial<ColEx>;

  // default value
  defaultValue?: any;
  isAdvanced?: boolean;

  // Matching details components
  span?: number;

  ifShow?: boolean | ((renderCallbackParams: RenderCallbackParams) => boolean);

  show?: boolean | ((renderCallbackParams: RenderCallbackParams) => boolean);

  // Render the content in the form-item tag
  render?: (renderCallbackParams: RenderCallbackParams) => VNode | VNode[] | string;

  // Rendering col content requires outer wrapper form-item
  renderColContent?: (renderCallbackParams: RenderCallbackParams) => VNode | VNode[] | string;

  renderComponentContent?: ((renderCallbackParams: RenderCallbackParams) => any) | VNode | VNode[] | string;

  // Custom slot, in from-item
  slot?: string;

  // Custom slot, similar to renderColContent
  colSlot?: string;

  dynamicDisabled?: boolean | ((renderCallbackParams: RenderCallbackParams) => boolean);

  dynamicRules?: (renderCallbackParams: RenderCallbackParams) => Rule[];
  // update-begin--author:liaozhiyang---date:20240308---for：【QQYUN-8377】formSchema propsSupport dynamic modification
  // Setup componentpropsofkey
  dynamicPropskey?: string;
  dynamicPropsVal?: ((renderCallbackParams: RenderCallbackParams) => any);
  // update-end--author:liaozhiyang---date:20240308---for：【QQYUN-8377】formSchema propsSupport dynamic modification

  // 这个属性自定义of 用于自定义of业务 比如在表单打开of时候修改表单of禁用状态，But it cannot be rewrittencomponentProps，因for他of内容太多了，So usedynamicDisabledandbussaccomplish
  buss?: any;
  
  //labelword count control（labelwidth）
  labelLength?: number;
  // update-begin--author:liaozhiyang---date:20240529---for【TV360X-460】basicFormsupportv-authinstruction(Access control visible and hidden)
  auth?: string;
  // update-end--author:liaozhiyang---date:20240529---for【TV360X-460】basicFormsupportv-authinstruction(Access control visible and hidden)
}
export interface HelpComponentProps {
  maxWidth: string;
  // Whether to display the serial number
  showIndex: boolean;
  // Text list
  text: any;
  // colour
  color: string;
  // font size
  fontSize: string;
  icon: string;
  absolute: boolean;
  // Positioning
  position: any;
}
