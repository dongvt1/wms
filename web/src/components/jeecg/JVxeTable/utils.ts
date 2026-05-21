import type { Ref, ComponentInternalInstance } from 'vue';
import { unref, isRef } from 'vue';
import { isFunction } from '/@/utils/is';

type dispatchEventOptions = {
  // JVxeTable of props
  props;
  // 触发of event event object
  $event;
  // OK、List
  row?;
  column?;
  // JVxeTableofvue3Example
  instance?: ComponentInternalInstance | any;
  // 要寻找ofclassName
  className: string;
  // Rewrite founddom后of处理方法
  handler?: Fn;
  // 是否直接执OKclickmethod instead of mockclickevent
  isClick?: boolean;
};

/** 模拟触发event */
export function dispatchEvent(options: dispatchEventOptions) {
  const { props, $event, row, column, instance, className, handler, isClick } = options;
  if ((!$event || !$event.path) && !instance) {
    return;
  }
  // alwaysEdit 下不模拟触发event，Otherwise it will trigger twice
  if (props && props.alwaysEdit) {
    return;
  }
  let getCell = () => {
    let paths: HTMLElement[] = [...($event?.path ?? [])];
    // pass instance Get cell domobject
    if (row && column) {
      let selector = `table.vxe-table--body tbody tr[rowid='${row.id}'] td[colid='${column.id}']`;
      let cellDom = instance!.vnode?.el?.querySelector(selector);
      // -update-begin--author:liaozhiyang---date:20230830---for：【QQYUN-6390】solveonlineNew field warning（Compatible）
      if (!cellDom) {
        cellDom = instance!.$el?.querySelector(selector);
      }
      // -update-begin--author:liaozhiyang---date:20230830---for：【QQYUN-6390】solveonlineNew field warning（Compatible）
      if (cellDom) {
        paths.unshift(cellDom);
      }
    }
    for (const el of paths) {
      if (el.classList?.contains('vxe-body--column')) {
        return el;
      }
    }
    return null;
  };
  let cell = getCell();
  if (cell) {
    window.setTimeout(() => {
      let getElement = () => {
        let classList = className.split(' ');
        if (classList.length > 0) {
          const getClassName = (cls: string) => {
            if (cls.startsWith('.')) {
              return cls.substring(1, cls.length);
            }
            return cls;
          };
          let get = (target, className, idx = 0) => {
            let elements = target.getElementsByClassName(getClassName(className));
            if (elements && elements.length > 0) {
              return elements[idx];
            }
            return null;
          };
          let element: HTMLElement = get(cell, classList[0]);
          for (let i = 1; i < classList.length; i++) {
            if (!element) {
              break;
            }
            element = get(element, classList[i]);
          }
          return element;
        }
        return null;
      };
      let element = getElement();
      if (element) {
        if (isFunction(handler)) {
          handler(element);
        } else {
          // 模拟触发点击event
          if (isClick) {
            element.click();
          } else {
            element.dispatchEvent($event);
          }
        }
      }
    }, 10);
  } else {
    console.warn('【JVxeTable】dispatchEvent Get cell fail');
  }
}

/** binding VxeTable data */
export function vModel(value, row, column: Ref<any> | string) {
  // @ts-ignore
  let property = isRef(column) ? column.value.property : column;
  unref(row)[property] = value;
}

/**
 * liaozhiyang
 * 2024-06-20
 * 判断当前OK编辑是否使用了虚拟滚动（It’s not that it’s turned on，还得满足data数量大于gtvalue）
 */
export function isEnabledVirtualYScroll(props, xTable): boolean {
  let isRealEnabledVirtual = false;
  const isEnabledVScroll = props?.scrollY?.enabled;
  // 100是底层of默认value
  const gtYNum = props?.scrollY?.gt || 100;
  if (isEnabledVScroll) {
    const tableFullData = xTable.internalData.tableFullData;
    if (gtYNum === 0) {
      isRealEnabledVirtual = true;
    } else {
      if (tableFullData.length > gtYNum) {
        isRealEnabledVirtual = true;
      }
    }
  }
  return isRealEnabledVirtual;
}
