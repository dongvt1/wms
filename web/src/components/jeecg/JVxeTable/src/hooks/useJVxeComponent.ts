import { computed, nextTick, ref, unref, watch } from 'vue';
import { propTypes } from '/@/utils/propTypes';
import { useDesign } from '/@/hooks/web/useDesign';
import { getEnhanced, replaceProps } from '../utils/enhancedUtils';
import { vModel } from '/@/components/jeecg/JVxeTable/utils';
import { JVxeRenderType } from '../types/JVxeTypes';
import { isBoolean, isFunction, isObject, isPromise } from '/@/utils/is';
import { JVxeComponent } from '../types/JVxeComponent';
import { filterDictText } from '/@/utils/dict/JDictSelectUtil';
import { getAreaTextByCode } from "@/components/Form/src/utils/Area";

export function useJVxeCompProps() {
  return {
    // Component type
    type: propTypes.string,
    // Rendering type
    renderType: propTypes.string.def('default'),
    // Rendering parameters
    params: propTypes.object,
    // Rendering customization options
    renderOptions: propTypes.object,
  };
}

export function useJVxeComponent(props: JVxeComponent.Props) {
  const value = computed(() => {
    // update-begin--author:liaozhiyang---date:20240430---for：【QQYUN-9125】oracleDatabase date type fields will have hours, minutes and seconds by default.
    const val = props.params.row[props.params.column.property];
    if (props.type === 'date' && typeof val === 'string') {
      return val.split(' ').shift();
    } else {
      return val;
    }
    // update-end--author:liaozhiyang---date:20240430---for：【QQYUN-9125】oracleDatabase date type fields will have hours, minutes and seconds by default.
  });
  const innerValue = ref(value.value);
  const row = computed(() => props.params.row);
  const rows = computed(() => props.params.data);
  const column = computed(() => props.params.column);
  // User configured original column
  const originColumn = computed(() => column.value.params);
  const rowIndex = computed(() => props.params._rowIndex);
  const columnIndex = computed(() => props.params._columnIndex);
  // Table data length
  const fullDataLength = computed(() => props.params.$table.internalData.tableFullData.length);
  // Is it scrolling?
  const scrolling = computed(() => !!props.renderOptions.scrolling);
  // When there isformatterhour，priority useformatter
  const innerLabel = computed(() => {
    if(typeof column.value?.formatter === 'function'){
      return column.value.formatter({
        cellValue: innerValue.value,
        row: row.value,
        column: column.value,
      });
    }
    return innerValue.value
  });
  const cellProps = computed(() => {
    let renderOptions = props.renderOptions;
    let col = originColumn.value;

    let cellProps = {};

    // Input placeholder
    cellProps['placeholder'] = replaceProps(col, col.placeholder);

    // parseprops
    if (isObject(col.props)) {
      Object.keys(col.props).forEach((key) => {
        cellProps[key] = replaceProps(col, col.props[key]);
      });
    }

    // Determine whether it is a disabled column
    cellProps['disabled'] = isBoolean(col['disabled']) ? col['disabled'] : cellProps['disabled'];
    // Determine whether a row is disabled
    if (renderOptions.isDisabledRow(row.value, rowIndex.value)) {
      cellProps['disabled'] = true;
    }
    // update-begin--author:liaozhiyang---date:20240528---for：【TV360X-291】Uncheck the sync database to disable the sorting function
    if (col.props && col.props.isDisabledCell) {
      if (col.props.isDisabledCell({ row: row.value, rowIndex: rowIndex.value, column: col, columnIndex: columnIndex.value })) {
        cellProps['disabled'] = true;
      }
    }
    // update-end--author:liaozhiyang---date:20240528---for：【TV360X-291】Uncheck the sync database to disable the sorting function
    // Determine whether to disable all components
    if (renderOptions.disabled === true) {
      cellProps['disabled'] = true;
      // update-begin--author:liaozhiyang---date:20240607---for：【TV360X-1068】行编辑整体禁用hour上传按钮不显示
      cellProps['disabledTable'] = true;
      // update-end--author:liaozhiyang---date:20240607---for：【TV360X-1068】行编辑整体禁用hour上传按钮不显示
    }
    //update-begin-author:taoyan date:2022-5-25 for: VUEN-1111 one-to-many subtable Department selection Should not cascade
    if (col.checkStrictly === true) {
      cellProps['checkStrictly'] = true;
    }
    //update-end-author:taoyan date:2022-5-25 for: VUEN-1111 one-to-many subtable Department selection Should not cascade

    //update-begin-author:taoyan date:2022-5-27 for: User component Control single-select and multi-select new parameter configurations
    if (col.isRadioSelection === true) {
      cellProps['isRadioSelection'] = true;
    } else if (col.isRadioSelection === false) {
      cellProps['isRadioSelection'] = false;
    }
    //update-end-author:taoyan date:2022-5-27 for: User component Control single-select and multi-select new parameter configurations

    return cellProps;
  });

  const listeners = computed(() => {
    let listeners = Object.assign({}, props.renderOptions.listeners || {});
    // defaultchangeevent
    if (!listeners.change) {
      listeners.change = async (event) => {
        vModel(event.value, row, column);
        await nextTick();
        // deal with change event相关逻辑（For example, check）
        props.params.$table.updateStatus(props.params);
      };
    }
    return listeners;
  });
  const context = {
    innerLabel,
    innerValue,
    row,
    rows,
    rowIndex,
    column,
    columnIndex,
    originColumn,
    fullDataLength,
    cellProps,
    scrolling,
    handleChangeCommon,
    handleBlurCommon,
  };
  const ctx = { props, context };

  // Get component enhancements
  let enhanced = getEnhanced(props.type);

  watch(
    value,
    (newValue) => {
      // -update-begin--author:liaozhiyang---date:20241210---for：【issues/7497】After hiding a column，Dictionary does not translate，Normal after recovery
      // TODO First fix and solve the problem like this，We’ll look at the root cause later
      enhanced = getEnhanced(props.type);
      // -update-end--author:liaozhiyang---date:20241210---for：【issues/7497】After hiding a column，Dictionary does not translate，After recovery
      // Validate value format
      let getValue = enhanced.getValue(newValue, ctx);
      if (newValue !== getValue) {
        // Value format is incorrect，reassign
        newValue = getValue;
        vModel(newValue, row, column);
      }
      innerValue.value = enhanced.setValue(newValue, ctx);
      // update-begin--author:liaozhiyang---date:20240509---for：【QQYUN-9205】one to many(jVxetablecomponentsdate)Years of support，years，Annual，year week
      if (props.type === 'date' && props.renderType === JVxeRenderType.spaner && enhanced.translate.enabled === true) {
        if (isFunction(enhanced.translate.handler)) {
          innerValue.value = enhanced.translate.handler(newValue, ctx);
        }
        return;
      }
      // update-end--author:liaozhiyang---date:20240509---for：【QQYUN-9205】one to many(jVxetablecomponentsdate)Years of support，years，Annual，year week

      //update-begin---author:wangshuai---date:2024-09-18---for:【issues/7203】自动生成one to many表单代码中，Provincial and city echo issues---
      if (props.type === 'pca' && props.renderType === JVxeRenderType.spaner) {
        innerValue.value = getAreaTextByCode(newValue);
        return;
      }
      //update-end---author:wangshuai---date:2024-09-18---for:【issues/7203】自动生成one to many表单代码中，Provincial and city echo issues---

      // Determine whether translation is enabled
      if (props.renderType === JVxeRenderType.spaner && enhanced.translate.enabled === true) {
        if (isFunction(enhanced.translate.handler)) {
          let res = enhanced.translate.handler(newValue, ctx);
          // Asynchronous translation，Can solve the problem of slow dictionary query
          if (isPromise(res)) {
            res.then((v) => (innerValue.value = v));
          } else {
            innerValue.value = res;
          }
        }
      }
    },
    { immediate: true }
  );

  /** 通用deal with change event */
  function handleChangeCommon($value, force = false) {
    const newValue = enhanced.getValue($value, ctx);
    const oldValue = value.value;
    // update-begin--author:liaozhiyang---date:20230718---for：【issues-5025】JVueTable的event @valueChangeRepeated triggering problem
    const execute = force ? true : newValue !== oldValue;
    if (execute) {
      trigger('change', { value: newValue });
      // triggervalueChangeevent
      parentTrigger('valueChange', {
        type: props.type,
        value: newValue,
        oldValue: oldValue,
        col: originColumn.value,
        rowIndex: rowIndex.value,
        columnIndex: columnIndex.value,
      });
    }
    // update-end--author:liaozhiyang---date:20230718---for：【issues-5025】JVueTable的event @valueChangeRepeated triggering problem
  }

  /** 通用deal with blur event */
  function handleBlurCommon($value) {
    // update-begin--author:liaozhiyang---date:20230817---for：【issues/636】JVxeTableplusblurevent
    const newValue = enhanced.getValue($value, ctx);
    const oldValue = value.value;
    //trigger('blur', { value });
    // triggerblurevent
    parentTrigger('blur', {
      type: props.type,
      value: newValue,
      oldValue: oldValue,
      col: originColumn.value,
      rowIndex: rowIndex.value,
      columnIndex: columnIndex.value,
    });
    // update-end--author:liaozhiyang---date:20230817---for：【issues/636】JVxeTableplusblurevent
  }

  /**
   * 如果event存在的话，就trigger
   * @param name event名
   * @param event event参数
   * @param args Other incidental parameters
   */
  function trigger(name, event?, args: any[] = []) {
    let listener = listeners.value[name];
    if (isFunction(listener)) {
      if (isObject(event)) {
        event = packageEvent(name, event);
      }
      listener(event, ...args);
    }
  }

  function parentTrigger(name, event, args: any[] = []) {
    args.unshift(packageEvent(name, event));
    trigger('trigger', name, args);
  }

  function packageEvent(name, event: any = {}) {
    event.row = row.value;
    event.column = column.value;
    // onlineEnhanced parameter compatibility
    event.column['key'] = column.value['property'];
    // event.cellTarget = this
    if (!event.type) {
      event.type = name;
    }
    if (!event.cellType) {
      event.cellType = props.type;
    }
    // Whether to verify the form，default为true
    if (isBoolean(event.validate)) {
      event.validate = true;
    }
    return event;
  }

  /**
   * Anti-style conflict class name generator
   * @param scope
   */
  function useCellDesign(scope: string) {
    return useDesign(`vxe-cell-${scope}`);
  }

  return {
    ...context,
    enhanced,
    trigger,
    useCellDesign,
  };
}

/**
 * 获取componentsdefault增强
 */
export function useDefaultEnhanced(): JVxeComponent.EnhancedPartial {
  return {
    installOptions: {
      autofocus: '',
    },
    interceptor: {
      'event.clearActived': () => true,
      'event.clearActived.className': () => true,
    },
    switches: {
      editRender: true,
      visible: false,
    },
    aopEvents: {
      editActived() {},
      editClosed() {},
      activeMethod: () => true,
    },
    translate: {
      enabled: false,
      handler(value, ctx) {
        // default翻译方法
        if (ctx) {
          return filterDictText(unref(ctx.context.column).params.options, value);
        } else {
          return value;
        }
      },
    },
    getValue: (value) => value,
    setValue: (value) => value,
    createValue: (defaultValue) => defaultValue,
  } as JVxeComponent.Enhanced;
}
