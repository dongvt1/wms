import type { FieldMapToTime, FormSchema } from './types/form';
import type { CSSProperties, PropType } from 'vue';
import type { ColEx } from './types';
import type { TableActionType } from '/@/components/Table';
import type { ButtonProps } from 'ant-design-vue/es/button/buttonTypes';
import type { RowProps } from 'ant-design-vue/lib/grid/Row';
import dayjs from "dayjs";
import { propTypes } from '/@/utils/propTypes';
import componentSetting from '/@/settings/componentSetting';

const { form } = componentSetting;
export const basicProps = {
  model: {
    type: Object as PropType<Recordable>,
    default: {},
  },
  // label width  fixed width
  labelWidth: {
    type: [Number, String] as PropType<number | string>,
    default: 0,
  },
  fieldMapToTime: {
    type: Array as PropType<FieldMapToTime>,
    default: () => [],
  },
  fieldMapToNumber: {
    type: Array as PropType<FieldMapToTime>,
    default: () => [],
  },
  compact: propTypes.bool,
  // Form configuration rules
  schemas: {
    type: [Array] as PropType<FormSchema[]>,
    default: () => [],
  },
  mergeDynamicData: {
    type: Object as PropType<Recordable>,
    default: null,
  },
  baseRowStyle: {
    type: Object as PropType<CSSProperties>,
  },
  baseColProps: {
    type: Object as PropType<Partial<ColEx>>,
  },
  autoSetPlaceHolder: propTypes.bool.def(true),
  // existINPUTWhen clicking Enter on the component，Whether to automatically submit
  autoSubmitOnEnter: propTypes.bool.def(false),
  submitOnReset: propTypes.bool,
  size: propTypes.oneOf(['default', 'small', 'large']).def('default'),
  // Disable form
  disabled: propTypes.bool,
  emptySpan: {
    type: [Number, Object] as PropType<number>,
    default: 0,
  },
  // Whether to display the collapse expand button
  showAdvancedButton: propTypes.bool,
  // conversion time
  transformDateFunc: {
    type: Function as PropType<Fn>,
    default: (date: any) => {
      // Determine whether it isdayjsExample
      return dayjs.isDayjs(date) ? date?.format('YYYY-MM-DD HH:mm:ss') : date;
    },
  },
  rulesMessageJoinLabel: propTypes.bool.def(true),
  // 【jeecg】Exceed3Column auto-collapse
  autoAdvancedCol: propTypes.number.def(3),
  // update-begin--author:liaozhiyang---date:202401009---for：【issues/7261】Query items above the tableautoAdvancedLineConfiguration has no effect（deleteautoAdvancedLine）
  // Exceed3Rows automatically fold
  // autoAdvancedLine: propTypes.number.def(3),
  // update-end--author:liaozhiyang---date:202401009---for：【issues/7261】Query items above the tableautoAdvancedLineConfiguration has no effect（deleteautoAdvancedLine）
  // Number of rows not affected by folding
  alwaysShowLines: propTypes.number.def(1),

  // Whether to display operation buttons
  showActionButtonGroup: propTypes.bool.def(true),
  // Operation columnColConfiguration
  actionColOptions: Object as PropType<Partial<ColEx>>,
  // Show reset button
  showResetButton: propTypes.bool.def(true),
  // Whether to focus on the first input box，只exist第一个表单项为inputfunction when
  autoFocusFirstItem: propTypes.bool,
  // 重置按钮Configuration
  resetButtonOptions: Object as PropType<Partial<ButtonProps>>,

  // Show confirmation button
  showSubmitButton: propTypes.bool.def(true),
  // 确认按钮Configuration
  submitButtonOptions: Object as PropType<Partial<ButtonProps>>,

  // Custom reset function
  resetFunc: Function as PropType<() => Promise<void>>,
  submitFunc: Function as PropType<() => Promise<void>>,

  // The following is the defaultprops
  hideRequiredMark: propTypes.bool,

  labelCol: {
    type: Object as PropType<Partial<ColEx>>,
    default: form.labelCol,
  },

  layout: propTypes.oneOf(['horizontal', 'vertical', 'inline']).def('horizontal'),
  tableAction: {
    type: Object as PropType<TableActionType>,
  },

  wrapperCol: {
    type: Object as PropType<Partial<ColEx>>,
    default: form.wrapperCol,
  },

  colon: propTypes.bool.def(form.colon),

  labelAlign: propTypes.string,

  rowProps: Object as PropType<RowProps>,
  
  // When the form is a query condition Automatically query when the form changes，No need to click the query button
  autoSearch: propTypes.bool.def(false),
};
