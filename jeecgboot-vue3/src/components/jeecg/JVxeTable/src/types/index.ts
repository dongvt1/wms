import type { Component, Ref, ComputedRef, ExtractPropTypes } from 'vue';
import type { VxeColumnProps } from 'vxe-table/types/column';
import type { JVxeComponent } from './JVxeComponent';
import type { VxeGridInstance, VxeTablePropTypes } from 'vxe-table';
import { JVxeTypes } from './JVxeTypes';
import { vxeProps } from '../vxe.data';
import { useMethods } from '../hooks/useMethods';
import { getJVxeAuths } from '../utils/authUtils';

export type JVxeTableProps = Partial<ExtractPropTypes<ReturnType<typeof vxeProps>>>;
export type JVxeTableMethods = ReturnType<typeof useMethods>['methods'];

export type JVxeVueComponent = {
  enhanced?: JVxeComponent.EnhancedPartial;
} & Component;

type statisticsTypes = 'sum' | 'average';

export type JVxeColumn = IJVxeColumn & Recordable;

/**
 * JVxe Column configuration items
 */
export interface IJVxeColumn extends VxeColumnProps {
  type?: any;
  // row unique identifier
  key: string;
  // Tips for form expected values，Can be used${...}Variable replacement text
  placeholder?: string;
  // default value
  defaultValue?: any;
  // Whether to disable the current column，defaultfalse
  disabled?: boolean;
  // Verification rules TODO Type to be defined
  validateRules?: any;
  // Linked fields at the next levelkey
  linkageKey?: string;
  // Customize other properties passed in to the component
  props?: Recordable;
  allowClear?: boolean; // allow clearing
  // 【inputNumber】Is it a statistical column?，only inputNumber To set statistical columns。Statistics column：sum Sum；average average value
  statistics?: boolean | [statisticsTypes, statisticsTypes?];
  // 【select】
  dictCode?: string; // dictionary code
  options?: { title?: string; label?: string; text?: string; value: any; disabled?: boolean }[]; // Drop down list of options
  allowInput?: boolean; // Allow input
  allowSearch?: boolean; // Allow search
  // 【slot】
  slotName?: string; // Slot name
  // 【checkbox】
  customValue?: [any, any]; // custom value
  defaultChecked?: boolean; // default选中
  // 【upload】 upload
  btnText?: string; // Upload button text
  token?: boolean; // whether to pass token
  responseName?: string; // Return value name
  action?: string; // Upload address
  allowRemove?: boolean; // Whether to allow deletion
  allowDownload?: boolean; // Whether to allow downloading
  // 【下拉dictionary搜索】
  dict?: string; // dictionary表配置信息：Database table name,Show field name,Store field name
  async?: boolean; // Whether to synchronize mode
  tipsContent?: string;
  // 【popup】
  popupCode?: string;
  field?: string;
  orgFields?: string;
  destFields?: string;
}

export interface JVxeRefs {
  gridRef: Ref<VxeGridInstance | undefined>;
  subPopoverRef: Ref<any>;
  detailsModalRef: Ref<any>;
}

export interface JVxeDataProps {
  prefixCls: string;
  // vxe ExampleID
  caseId: string;
  // vxe final columns
  vxeColumns?: ComputedRef;
  // vxe final dataSource
  vxeDataSource: Ref<Recordable[]>;
  // Record scroll bar position
  scroll: { top: number; left: number };
  // Whether it is currently scrolling
  scrolling: Ref<boolean>;
  // vxe default配置
  defaultVxeProps: object;
  // Bind the left selection box
  selectedRows: Ref<any[]>;
  // Bind the left selection box已选择的id
  selectedRowIds: Ref<string[]>;
  disabledRowIds: string[];
  // Statistics column配置
  statistics: {
    has: boolean;
    sum: string[];
    average: string[];
  };
  // All authorization information related to the current form
  authsMap: Ref<Nullable<ReturnType<typeof getJVxeAuths>>>;
  // built-in EditRules
  innerEditRules: Recordable<VxeTablePropTypes.EditRules[]>;
  // Linked drop-down options（Used to isolate different dropdown options）
  // Internal linkage configuration，map
  innerLinkageConfig: Map<string, any>;
  // Rows with data refresh effect turned on
  reloadEffectRowKeysMap: Recordable;
}

export interface JVxeLinkageConfig {
  // The first level of linkage key
  key: string;
  // How to get data
  requestData: (parent: string) => Promise<any>;
}

export { JVxeTypes };
