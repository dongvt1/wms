/*
 * JVxeTable Keyboard operation
 */
import type { VxeTablePropTypes } from 'vxe-table';
import type { JVxeTableProps } from '../types';
import { computed } from 'vue';

/**
 * JVxeTable Keyboard operation
 *
 * @param props
 */
export function useKeyboardEdit(props: JVxeTableProps) {
  // 是否开启了Keyboard operation
  const enabledKeyboard = computed(() => props.keyboardEdit ?? false);
  // rewrite keyboardConfig
  const keyboardConfig: VxeTablePropTypes.KeyboardConfig = {
    editMethod({ row, column, $table }) {
      // rewrite默认的覆盖式，Change to append
      $table.setActiveCell(row, column);
      return true;
    },
  };
  // Keyboard operation配置
  const keyboardEditConfig = computed(() => {
    return {
      mouseConfig: {
        selected: enabledKeyboard.value,
      },
      keyboardConfig,
    };
  });

  return {
    keyboardEditConfig,
  };
}
