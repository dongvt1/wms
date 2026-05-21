import type { ComputedRef } from 'vue';
import type { BasicTableProps } from '../types/table';
import { unref } from 'vue';
import { ROW_KEY } from '../const';
import { isString, isFunction } from '/@/utils/is';

interface Options {
  setSelectedRowKeys: (keys: string[]) => void;
  getSelectRowKeys: () => string[];
  clearSelectedRowKeys: () => void;
  emit: EmitType;
  getAutoCreateKey: ComputedRef<boolean | undefined>;
}

function getKey(record: Recordable, rowKey: string | ((record: Record<string, any>) => string) | undefined, autoCreateKey?: boolean) {
  if (!rowKey || autoCreateKey) {
    return record[ROW_KEY];
  }
  if (isString(rowKey)) {
    return record[rowKey];
  }
  if (isFunction(rowKey)) {
    return record[rowKey(record)];
  }
  return null;
}

export function useCustomRow(
  propsRef: ComputedRef<BasicTableProps>,
  { setSelectedRowKeys, getSelectRowKeys, getAutoCreateKey, clearSelectedRowKeys, emit }: Options
) {
  const customRow = (record: Recordable, index: number) => {
    return {
      onClick: (e: Event) => {
        e?.stopPropagation();
        function handleClick() {
          const { rowSelection, rowKey, clickToRowSelect } = unref(propsRef);
          if (!rowSelection || !clickToRowSelect) return;
          const keys = getSelectRowKeys();
          const key = getKey(record, rowKey, unref(getAutoCreateKey));
          if (!key) return;

          const isCheckbox = rowSelection.type === 'checkbox';
          if (isCheckbox) {
            // turn uptr
            const tr: HTMLElement = (e as MouseEvent).composedPath?.().find((dom: HTMLElement) => dom.tagName === 'TR') as HTMLElement;
            if (!tr) return;
            // turn upCheckbox，Check if it isdisabled
            const checkBox = tr.querySelector('input[type=checkbox]');
            if (!checkBox || checkBox.hasAttribute('disabled')) return;
            if (!keys.includes(key)) {
              setSelectedRowKeys([...keys, key]);
              return;
            }
            const keyIndex = keys.findIndex((item) => item === key);
            keys.splice(keyIndex, 1);
            setSelectedRowKeys(keys);
            return;
          }

          const isRadio = rowSelection.type === 'radio';
          if (isRadio) {
            // update-begin--author:liaozhiyang---date:20231016---for：【QQYUN-6794】tableList addedradioDisable function
            const rowSelection = propsRef.value.rowSelection;
            if (rowSelection.getCheckboxProps) {
              const result = rowSelection.getCheckboxProps(record);
              if (result.disabled) {
                return;
              }
            }
            // update-end--author:liaozhiyang---date:20231016---for：【QQYUN-6794】tableList addedradioDisable function
            if (!keys.includes(key)) {
              if (keys.length) {
                clearSelectedRowKeys();
              }
              setSelectedRowKeys([key]);
              return;
            } else {
              // update-begin--author:liaozhiyang---date:20240527---for：【TV360X-359】erpClick on the selected one in the main table to select the last one
              // Click on the selected，directreturnNot doing operations
              return;
              // update-end--author:liaozhiyang---date:20240527---for：【TV360X-359】erpClick on the selected one in the main table to select the last one
            }
            clearSelectedRowKeys();
          }
        }
        handleClick();
        emit('row-click', record, index, e);
      },
      onDblclick: (event: Event) => {
        emit('row-dbClick', record, index, event);
      },
      onContextmenu: (event: Event) => {
        emit('row-contextmenu', record, index, event);
      },
      onMouseenter: (event: Event) => {
        emit('row-mouseenter', record, index, event);
      },
      onMouseleave: (event: Event) => {
        emit('row-mouseleave', record, index, event);
      },
    };
  };

  return {
    customRow,
  };
}
