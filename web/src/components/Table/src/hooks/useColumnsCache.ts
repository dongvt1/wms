import { computed, nextTick, unref, watchEffect } from 'vue';
import { router } from '/@/router';
import { useRoute } from 'vue-router';
import { createLocalStorage } from '/@/utils/cache';
import { useTableContext } from './useTableContext';
import { useMessage } from '/@/hooks/web/useMessage';

/**
 * List configuration cache
 */
export function useColumnsCache(opt, setColumns, handleColumnFixed) {
  let isInit = false;
  const table = useTableContext();
  const $ls = createLocalStorage();
  const { createMessage: $message } = useMessage();
  const route = useRoute();
  // List configuration cachekey
  const cacheKey = computed(() => {
    // update-begin--author:liaozhiyang---date:20240226---for：【QQYUN-8367】onlineReport configuration column display save，Affects other pagestableShow and hide fields（Hot update of the development environment will have this problem，No problem in production environment）
    const path = route.path;
    let key = path.replace(/[\/\\]/g, '_');
    // update-end--author:liaozhiyang---date:20240226---for：【QQYUN-8367】onlineReport configuration column display save，Affects other pagestableShow and hide fields（Hot update of the development environment will have this problem，No problem in production environment）
    let cacheKey = table.getBindValues.value.tableSetting?.cacheKey;
    if (cacheKey) {
      key += ':' + cacheKey;
    }
    return 'columnCache:' + key;
  });

  watchEffect(() => {
    const columns = table.getColumns();
    if (columns.length) {
      init();
    }
  });

  async function init() {
    if (isInit) {
      return;
    }
    isInit = true;
    let columnCache = $ls.get(cacheKey.value);
    if (columnCache && columnCache.checkedList) {
      const { checkedList, sortedList, sortableOrder, checkIndex } = columnCache;
      await nextTick();
      // checkboxsort cache
      opt.sortableOrder.value = sortableOrder;
      // checkboxselected cache
      opt.state.checkedList = checkedList;
      // tableColumnsort cache
      opt.plainSortOptions.value.sort((prev, next) => {
        return sortedList.indexOf(prev.value) - sortedList.indexOf(next.value);
      });
      // ReordertableColumn
      checkedList.sort((prev, next) => sortedList.indexOf(prev) - sortedList.indexOf(next));
      // Whether to display the line number column
      if (checkIndex) {
        table.setProps({ showIndexColumn: true });
      }
      setColumns(checkedList);
      // Set fixed columns
      setColumnFixed(columnCache);
    }
  }

  /** Set fixed columns */
  async function setColumnFixed(columnCache) {
    const { fixedColumns } = columnCache;
    const columns = opt.plainOptions.value;
    for (const column of columns) {
      let fixedCol = fixedColumns.find((fc) => fc.key === (column.key || column.dataIndex));
      if (fixedCol) {
        await nextTick();
        handleColumnFixed(column, fixedCol.fixed);
      }
    }
  }

  // Determine column fixed status
  const fixedReg = /^(true|left|right)$/;

  /** Get the fixed column */
  function getFixedColumns() {
    let fixedColumns: any[] = [];
    const columns = opt.plainOptions.value;
    for (const column of columns) {
      if (fixedReg.test((column.fixed ?? '').toString())) {
        fixedColumns.push({
          key: column.key || column.dataIndex,
          fixed: column.fixed === true ? 'left' : column.fixed,
        });
      }
    }
    return fixedColumns;
  }

  /** Save column configuration */
  function saveSetting() {
    const { checkedList } = opt.state;
    // update-begin--author:liaozhiyang---date:20240611---for：【TV360X-105】Column display setting issue[After resetting, the order saved is still the last one.]
    let sortedList = [];
    if (opt.restAfterOptions.value) {
      sortedList = opt.restAfterOptions.value.map((item) => item.value);
    } else {
      sortedList = unref(opt.plainSortOptions).map((item) => item.value);
    }
    // update-end--author:liaozhiyang---date:20240611---for：【TV360X-105】Column display setting issue[After resetting, the order saved is still the last one.]
    $ls.set(cacheKey.value, {
      // saved columns
      checkedList,
      // sorted column
      sortedList,
      // Whether to display the line number column
      checkIndex: unref(opt.checkIndex),
      // checkboxOriginal sorting
      sortableOrder: unref(opt.sortableOrder),
      // fixed column
      fixedColumns: getFixedColumns(),
    });
    $message.success('Saved successfully');
    // Close directly after saving
    opt.popoverVisible.value = false;
  }

  /** reset（delete）column configuration */
  async function resetSetting() {
    // resetfixed column
    await resetFixedColumn();
    $ls.remove(cacheKey.value);
    $message.success('reset成功');
  }

  async function resetFixedColumn() {
    const columns = opt.plainOptions.value;
    for (const column of columns) {
      column.fixed;
      if (fixedReg.test((column.fixed ?? '').toString())) {
        await nextTick();
        handleColumnFixed(column, null);
      }
    }
  }

  return {
    saveSetting,
    resetSetting,
    getCache: () => $ls.get(cacheKey.value),
  };
}
