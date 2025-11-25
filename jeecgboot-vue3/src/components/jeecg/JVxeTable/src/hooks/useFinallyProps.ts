import { unref, computed, ref, watch, nextTick } from 'vue';
import { merge, debounce } from 'lodash-es';
import { isArray } from '/@/utils/is';
import { useAttrs } from '/@/hooks/core/useAttrs';
import { useKeyboardEdit } from '../hooks/useKeyboardEdit';
import { JVxeDataProps, JVxeTableMethods, JVxeTableProps } from '../types';

export function useFinallyProps(props: JVxeTableProps, data: JVxeDataProps, methods: JVxeTableMethods) {
  const attrs = useAttrs();
  // vxe Keyboard operation configuration
  const { keyboardEditConfig } = useKeyboardEdit(props);
  // vxe final editRules
  const vxeEditRules = computed(() => merge({}, props.editRules, data.innerEditRules));
  // vxe final events
  const vxeEvents = computed(() => {
    let listeners = { ...unref(attrs) };
    let events = {
      onScroll: methods.handleVxeScroll,
      onCellClick: methods.handleCellClick,
      onEditClosed: methods.handleEditClosed,
      onEditActived: methods.handleEditActived,
      onRadioChange: methods.handleVxeRadioChange,
      onCheckboxAll: methods.handleVxeCheckboxAll,
      onCheckboxChange: methods.handleVxeCheckboxChange,
      // update-begin--author:liaozhiyang---date:20240321---for：【QQYUN-8566】JVXETableUnable to remember column settings
      onCustom: methods.handleCustom,
      // update-begin--author:liaozhiyang---date:20240321---for：【QQYUN-8566】JVXETableUnable to remember column settings
    };
    // events passed by the user，Perform merge operation
    Object.keys(listeners).forEach((key) => {
      let listen = listeners[key];
      if (events.hasOwnProperty(key)) {
        if (isArray(listen)) {
          listen.push(events[key]);
        } else {
          listen = [events[key], listen];
        }
      }
      events[key] = listen;
    });
    return events;
  });

  // vxe final props
  const vxePropsMerge = computed(() => {
    // update-begin--author:liaozhiyang---date:20240417---for:【QQYUN-8785】onlineform column positionidNo restrictions，Drag other columns toidcolumn above，Error when synchronizing database
    let rowClass = {};
    if (props.dragSort) {
      rowClass = {
        rowClassName: (params) => {
          let { row } = params;
          const find = props.notAllowDrag?.find((item:any) => {
            const {key, value} = item;
            return row[key] == value;
          });
          // Business comes inrowClassName
          const popsRowClassName = props.rowClassName ?? '';
          let outClass = '';
          if(typeof popsRowClassName==='string'){
            popsRowClassName && (outClass = popsRowClassName);
          }else if(typeof popsRowClassName==='function'){
            outClass = popsRowClassName(params)
          }
          return find ? `not-allow-drag ${outClass}` : `allow-drag ${outClass}`;
        },
      };
    }
    // update-end--author:liaozhiyang---date:20240417---for:【QQYUN-8785】onlineform column positionidNo restrictions，Drag other columns toidcolumn above，Error when synchronizing database
    return merge(
      {},
      data.defaultVxeProps,
      {
        showFooter: data.statistics.has,
      },
      unref(attrs),
      {
        ref: 'gridRef',
        size: props.size,
        loading: false,
        disabled: props.disabled,
        // columns: unref(data.vxeColumns),
        editRules: unref(vxeEditRules),
        height: props.height === 'auto' ? null : props.height,
        maxHeight: props.maxHeight,
        // update-begin--author:liaozhiyang---date:20231013---for：【QQYUN-5133】JVxeTable Line editing upgrade
        scrollY: props.scrollY,
        scrollX: props.scrollX,
        // update-end--author:liaozhiyang---date:20231013---for：【QQYUN-5133】JVxeTable Line editing upgrade
        border: props.bordered,
        footerMethod: methods.handleFooterMethod,
        // Expand row configuration
        expandConfig: {
          toggleMethod: methods.handleExpandToggleMethod,
        },
        // Editable configuration
        editConfig: {
          // update-begin--author:liaozhiyang---date:20231013---for：【QQYUN-5133】JVxeTable Line editing upgrade
          //activeMethod: methods.handleActiveMethod,
          beforeEditMethod: methods.handleActiveMethod,
          // update-end--author:liaozhiyang---date:20231013---for：【QQYUN-5133】JVxeTable Line editing upgrade
        },
        radioConfig: {
          checkMethod: methods.handleCheckMethod,
        },
        checkboxConfig: {
          checkMethod: methods.handleCheckMethod,
        },
        ...rowClass
        // rowClassName:(params)=>{
        //   const { row } = params;
        //   return row.dbFieldName=='id'?"not-allow-drag":"allow-drag"
        // }
      },
      unref(vxeEvents),
      unref(keyboardEditConfig)
    );
  });

  // update-begin--author:sunjianlei---date:20250804---for:【issues/8593】Fixed an issue where content is not refreshed after column changes
  const vxeColumnsRef = ref(data.vxeColumns!.value || [])
  const watchColumnsDebounce = debounce(async () => {
    vxeColumnsRef.value = []
    await nextTick()
    vxeColumnsRef.value = data.vxeColumns!.value
  }, 50)
  watch(data.vxeColumns!, watchColumnsDebounce)
  // update-end----author:sunjianlei---date:20250804---for:【issues/8593】Fixed an issue where content is not refreshed after column changes

  const vxeProps = computed(() => {
    return {
      ...unref(vxePropsMerge),
      // 【issue/8695】extracted separately columns，Prevent performance issues
      columns: unref(vxeColumnsRef),
    }
  });

  return {
    vxeProps,
    prefixCls: data.prefixCls,
  };
}
