import type { ValidationRule } from 'ant-design-vue/lib/form/Form';
import type { ComponentType } from './types/index';
import { useI18n } from '/@/hooks/web/useI18n';
import { dateUtil } from '/@/utils/dateUtil';
import { isNumber, isObject } from '/@/utils/is';

const { t } = useI18n();

/**
 * @description: generateplaceholder
 */
export function createPlaceholderMessage(component: ComponentType) {
  if (component.includes('Input') || component.includes('Complete')) {
    return t('common.inputText');
  }
  if (component.includes('Picker')) {
    return t('common.chooseText');
  }
  if (
    component.includes('Select') ||
    component.includes('Cascader') ||
    component.includes('Checkbox') ||
    component.includes('Radio') ||
    component.includes('Switch')
  ) {
    // return `Please select${label}`;
    return t('common.chooseText');
  }
  return '';
}

const DATE_TYPE = ['DatePicker', 'MonthPicker', 'WeekPicker', 'TimePicker'];

function genType() {
  return [...DATE_TYPE, 'RangePicker'];
}

export function setComponentRuleType(rule: ValidationRule, component: ComponentType, valueFormat: string) {
  //update-begin---author:wangshuai---date:2024-02-01---for:【QQYUN-8176】In edit form,When verification is required,If the component isApiSelect,When opening the edit page,Even if the field has a value,也会提示Please select---
  //https://github.com/vbenjs/vue-vben-admin/pull/3082 githubRepair the original text
  if (Reflect.has(rule, 'type')) {
    return;
  }
  //update-end---author:wangshuai---date:2024-02-01---for:【QQYUN-8176】In edit form,When verification is required,If the component isApiSelect,When opening the edit page,Even if the field has a value,也会提示Please select---
  if (['DatePicker', 'MonthPicker', 'WeekPicker', 'TimePicker'].includes(component)) {
    rule.type = valueFormat ? 'string' : 'object';
  } else if (['RangePicker', 'Upload', 'CheckboxGroup', 'TimePicker'].includes(component)) {
    rule.type = 'array';
  } else if (['InputNumber'].includes(component)) {
    rule.type = 'number';
  }
}

export function processDateValue(attr: Recordable, component: string) {
  const { valueFormat, value } = attr;
  if (valueFormat) {
    attr.value = isObject(value) ? dateUtil(value).format(valueFormat) : value;
  } else if (DATE_TYPE.includes(component) && value) {
    attr.value = dateUtil(attr.value);
  }
}

export function handleInputNumberValue(component?: ComponentType, val?: any) {
  if (!component) return val;
  if (['Input', 'InputPassword', 'InputSearch', 'InputTextArea'].includes(component)) {
    return val && isNumber(val) ? `${val}` : val;
  }
  return val;
}
/** 
*liaozhiyang
*2023-12-26
*The value transfer of some components requires converting the string type into a numeric type.
*/ 
export function handleInputStringValue(component?: ComponentType, val?: any) {
  if (!component) return val;
  // update-begin--author:liaozhiyang---date:20240517---for：【TV360X-13】InputNumberPrecise settings3Pass in decimal places''became0.00
  if (['InputNumber'].includes(component) && typeof val === 'string' && val != '') {
    return Number(val);
  }
  // update-end--author:liaozhiyang---date:20240517---for：【TV360X-13】InputNumberPrecise settings3Pass in decimal places''became0.00
  return val;
}

/**
 * time field
 */
export const dateItemType = genType();
