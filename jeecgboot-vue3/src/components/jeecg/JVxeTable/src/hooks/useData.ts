import { ref, reactive, provide, resolveComponent } from 'vue';
import { useDesign } from '/@/hooks/web/useDesign';
import { JVxeDataProps, JVxeRefs, JVxeTableProps } from '../types';
import { VxeGridInstance } from 'vxe-table';
import { randomString } from '/@/utils/common/compUtils';

export function useData(props: JVxeTableProps): JVxeDataProps {
  const { prefixCls } = useDesign('j-vxe-table');
  provide('prefixCls', prefixCls);
  return {
    prefixCls: prefixCls,
    caseId: `j-vxe-${randomString(8)}`,
    vxeDataSource: ref([]),
    scroll: reactive({ top: 0, left: 0 }),
    scrolling: ref(false),
    defaultVxeProps: reactive({
      // update-begin--author:liaozhiyang---date:20240607---for：【TV360X-327】vxetablewarn
      // rowId: props.rowKey,
      rowConfig: {
        keyField: props.rowKey,
        // Highlighthoverof rows
        isHover: true,
      },
      // update-end--author:liaozhiyang---date:20240607---for：【TV360X-327】vxetablewarn

      // --- 【issues/209】Comes with ittooltipWill be misplaced，So replace it with the original onetitle ---
      // overflow hide and showtooltip
      showOverflow: "title",
      // 表头overflow hide and showtooltip
      showHeaderOverflow: "title",
      // --- 【issues/209】Comes with ittooltipWill be misplaced，So replace it with the original onetitle ---

      showFooterOverflow: true,
      // Editable configuration
      editConfig: {
        trigger: 'click',
        mode: 'cell',
        // update-begin--author:liaozhiyang---date:20231013---for：【QQYUN-5133】JVxeTable Line editing upgrade
        //activeMethod: () => !props.disabled,
        beforeEditMethod: () => !props.disabled,
        // update-end--author:liaozhiyang---date:20231013---for：【QQYUN-5133】JVxeTable Line editing upgrade
      },
      expandConfig: {
        iconClose: 'vxe-icon-arrow-right',
        iconOpen: 'vxe-icon-arrow-down',
        ...props.expandConfig,
      },
      // Virtual scrolling configuration，yaxis is greater thanxxEnable virtual scrolling when striping data
      scrollY: {
        gt: 30,
      },
      scrollX: {
        gt: 20,
        // Temporarily turn off left and right virtual scrolling
        enabled: false,
      },
      radioConfig: {
        // Keep checked
        reserve: true,
        highlight: true,
      },
      checkboxConfig: {
        // Keep checked
        reserve: true,
        highlight: true,
      },
      mouseConfig: { selected: false },
      keyboardConfig: {
        // Delete key function
        isDel: false,
        // Esckey to close the editing function
        isEsc: true,
        // Tab Key function
        isTab: true,
        // Press any key to enter editing（Except function keys）
        isEdit: true,
        // 方向Key function
        isArrow: true,
        // 回车Key function
        isEnter: true,
        // If the feature is supported，used for column.type=checkbox|radio，Enable the space bar to switch the check box or radio button status function
        isChecked: true,
      },
    }),
    selectedRows: ref<any[]>([]),
    selectedRowIds: ref<string[]>([]),
    disabledRowIds: [],
    statistics: reactive({
      has: false,
      sum: [],
      average: [],
    }),
    authsMap: ref(null),
    innerEditRules: {},
    innerLinkageConfig: new Map<string, any>(),
    reloadEffectRowKeysMap: reactive({}),
  };
}

export function useRefs(): JVxeRefs {
  return {
    gridRef: ref<VxeGridInstance>(),
    subPopoverRef: ref<any>(),
    detailsModalRef: ref<any>(),
  };
}

export function useResolveComponent(...t: any[]): any {
  // @ts-ignore
  return resolveComponent(...t);
}
