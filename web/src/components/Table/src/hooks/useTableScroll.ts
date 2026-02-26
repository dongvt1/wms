import type { BasicTableProps, TableRowSelection, BasicColumn } from '../types/table';
import type { Ref, ComputedRef, Slots } from 'vue';
import { computed, unref, ref, nextTick, watch } from 'vue';
import { getViewportOffset } from '/@/utils/domUtils';
import { isBoolean } from '/@/utils/is';
import { useWindowSizeFn } from '/@/hooks/event/useWindowSizeFn';
import { useModalContext } from '/@/components/Modal';
import { onMountedOrActivated } from '/@/hooks/core/onMountedOrActivated';
import { useDebounceFn } from '@vueuse/core';
import componentSetting from '/@/settings/componentSetting';

export function useTableScroll(
  propsRef: ComputedRef<BasicTableProps>,
  tableElRef: Ref<ComponentRef>,
  columnsRef: ComputedRef<BasicColumn[]>,
  rowSelectionRef: ComputedRef<TableRowSelection<any> | null>,
  getDataSourceRef: ComputedRef<Recordable[]>,
  slots: Slots,
  getPaginationInfo: ComputedRef<any>
) {
  const tableHeightRef: Ref<Nullable<number>> = ref(null);

  const modalFn = useModalContext();

  // Greater than animation time 280
  const debounceRedoHeight = useDebounceFn(redoHeight, 100);

  const getCanResize = computed(() => {
    const { canResize, scroll } = unref(propsRef);
    return canResize && !(scroll || {}).y;
  });

  watch(
    () => [unref(getCanResize), unref(getDataSourceRef)?.length],
    () => {
      debounceRedoHeight();
    },
    {
      flush: 'post',
    }
  );

  function redoHeight() {
    nextTick(() => {
      calcTableHeight();
    });
  }

  function setHeight(heigh: number) {
    tableHeightRef.value = heigh;
    //  Solve the problem of modal adaptive height calculation when the form is placed in the modal
    modalFn?.redoModalHeight?.();
  }

  // No need to repeat queries
  let paginationEl: HTMLElement | null;
  let footerEl: HTMLElement | null;
  let bodyEl: HTMLElement | null;

  async function calcTableHeight() {
    const { resizeHeightOffset, pagination, maxHeight, minHeight } = unref(propsRef);
    const tableData = unref(getDataSourceRef);

    const table = unref(tableElRef);
    if (!table) return;

    const tableEl: Element = table.$el;
    if (!tableEl) return;

    if (!bodyEl) {
      //update-begin-author:taoyan date:2023-2-11 for: issues/355 front end-jeecgboot-vue3 3.4.4Version,BasicTableHigh adaptive function failure,set upBasicTablecomponentsmaxHeightInvalid; The reason has been found,Please see details
      bodyEl = tableEl.querySelector('.ant-table-tbody');
      //update-end-author:taoyan date:2023-2-11 for: issues/355 front end-jeecgboot-vue3 3.4.4Version,BasicTableHigh adaptive function failure,set upBasicTablecomponentsmaxHeightInvalid; The reason has been found,Please see details
      if (!bodyEl) return;
    }

    const hasScrollBarY = bodyEl.scrollHeight > bodyEl.clientHeight;
    const hasScrollBarX = bodyEl.scrollWidth > bodyEl.clientWidth;

    if (hasScrollBarY) {
      tableEl.classList.contains('hide-scrollbar-y') && tableEl.classList.remove('hide-scrollbar-y');
    } else {
      !tableEl.classList.contains('hide-scrollbar-y') && tableEl.classList.add('hide-scrollbar-y');
    }

    if (hasScrollBarX) {
      tableEl.classList.contains('hide-scrollbar-x') && tableEl.classList.remove('hide-scrollbar-x');
    } else {
      !tableEl.classList.contains('hide-scrollbar-x') && tableEl.classList.add('hide-scrollbar-x');
    }

    bodyEl!.style.height = 'unset';

    if (!unref(getCanResize) || ( !tableData || tableData.length === 0)) return;

    await nextTick();
    //Add a delay to get the correct bottomIncludeBody paginationHeight footerHeight headerHeight

    const headEl = tableEl.querySelector('.ant-table-thead');

    if (!headEl) return;

    // Table height from bottom
    const { bottomIncludeBody } = getViewportOffset(headEl);
    // Table height from bottom height-custom offset

    const paddingHeight = 32;
    // Pager height
    let paginationHeight = 2;
    if (!isBoolean(pagination)) {
      paginationEl = tableEl.querySelector('.ant-pagination') as HTMLElement;
      if (paginationEl) {
        const offsetHeight = paginationEl.offsetHeight;
        paginationHeight += offsetHeight || 0;
      } else {
        // TODO First fix 24
        paginationHeight += 24;
      }
    } else {
      paginationHeight = -8;
    }

    let footerHeight = 0;
    // update-begin--author:liaozhiyang---date:20240424---for：【issues/1137】BasicTableAdaptive height calculation does not subtract tail height
    footerEl = tableEl.querySelector('.ant-table-footer');
    if (footerEl) {
      const offsetHeight = footerEl.offsetHeight;
      footerHeight = offsetHeight || 0;
    }
    // update-end--author:liaozhiyang---date:20240424---for：【issues/1137】BasicTableAdaptive height calculation does not subtract tail height

    let headerHeight = 0;
    if (headEl) {
      headerHeight = (headEl as HTMLElement).offsetHeight;
    }

    let height = bottomIncludeBody - (resizeHeightOffset || 0) - paddingHeight - paginationHeight - footerHeight - headerHeight;
    // update-begin--author:liaozhiyang---date:20240603---for【TV360X-861】The list query area cannot be scrolled up.
    // 10+6(outer marginpadding:10 + inner layerpadding-bottom:6)
    height -= 16;
    // update-end--author:liaozhiyang---date:20240603---for：【TV360X-861】The list query area cannot be scrolled up.
    
    height = (height < minHeight! ? (minHeight as number) : height) ?? height;
    height = (height > maxHeight! ? (maxHeight as number) : height) ?? height;
    setHeight(height);

    bodyEl!.style.height = `${height}px`;
    // update-begin--author:liaozhiyang---date:20240609---for【issues/8374】Pagination always appears at the bottom
    nextTick(() => {
      if (maxHeight === undefined) {
        if (unref(getPaginationInfo) && unref(getDataSourceRef).length) {
          const pageSize = unref(getPaginationInfo)?.pageSize;
          const current = unref(getPaginationInfo)?.current;
          const total = unref(getPaginationInfo)?.total;
          const tableBody = tableEl.querySelector('.ant-table-body') as HTMLElement;
          const tr = tableEl.querySelector('.ant-table-tbody')?.children ?? [];
          const lastrEl = tr[tr.length - 1] as HTMLElement;
          const trHeight = lastrEl.offsetHeight;
          const dataHeight = trHeight * pageSize;
          if (tableBody && lastrEl) {
            // update-begin--author:liaozhiyang---date:20250702---for：【issues/8532】onlineThe data on the first page of button permissions in permission management cannot be seen
            // tableWhether to hide（hiddentableCan't suck bottom）
            const isTableBodyHide = tableBody.offsetHeight == 0 && tableBody.offsetWidth == 0;
            if (isTableBodyHide) {
              return;
            }
            // update-end--author:liaozhiyang---date:20250702---for：【issues/8532】onlineThe data on the first page of button permissions in permission management cannot be seen
            if (current === 1 && pageSize > unref(getDataSourceRef).length && total <= pageSize) {
              tableBody.style.height = `${height}px`;
            } else {
              tableBody.style.height = `${dataHeight < height ? dataHeight : height}px`;
            }
          }
        }
      }
    });
    // update-end--author:liaozhiyang---date:20240609---for【issues/8374】Pagination always appears at the bottom
  }
  useWindowSizeFn(calcTableHeight, 280);
  onMountedOrActivated(() => {
    calcTableHeight();
    nextTick(() => {
      debounceRedoHeight();
    });
  });

  const getScrollX = computed(() => {
    let width = 0;
    // update-begin--author:liaozhiyang---date:20230922---for：【QQYUN-6391】When there are too many fields in the online form list,Column headers and data are not aligned
    // if (unref(rowSelectionRef)) {
    //   width += 60;
    // }
    // update-end--author:liaozhiyang---date:20230922---for：【QQYUN-6391】When there are too many fields in the online form list,Column headers and data are not aligned
    // update-begin--author:liaozhiyang---date:20230925---for：【issues/5411】BasicTable ConfigurationmaxColumnWidth Not effective
    const { maxColumnWidth } = unref(propsRef);
    // TODO props ?? 0;
    const NORMAL_WIDTH = maxColumnWidth ?? 150;
    // update-end--author:liaozhiyang---date:20230925---for：【issues/5411】BasicTable ConfigurationmaxColumnWidth Not effective
    // date-begin--author:liaozhiyang---date:20250716---for：【QQYUN-13122】Only displayed when there are dozens of fields2fields，The remaining fields areifShow:falseThere will be scroll bars
    const columns = unref(columnsRef).filter((item) => !(item.defaultHidden == true || item.ifShow == false))
    // date-end--author:liaozhiyang---date:20250716---for：【QQYUN-13122】Only displayed when there are dozens of fields2fields，The remaining fields areifShow:falseThere will be scroll bars
    columns.forEach((item) => {
      width += Number.parseInt(item.width as string) || 0;
    });
    const unsetWidthColumns = columns.filter((item) => !Reflect.has(item, 'width'));

    const len = unsetWidthColumns.length;
    if (len !== 0) {
      width += len * NORMAL_WIDTH;
    }
    // update-begin--author:liaozhiyang---date:202401009---for：【TV360X-116】The table is misaligned when there are many embedded style fields
    if (slots.expandedRowRender) {
      width += propsRef.value.expandColumnWidth;
    }
    // update-end--author:liaozhiyang---date:202401009---for：【TV360X-116】The table is misaligned when there are many embedded style fields
    const table = unref(tableElRef);
    const tableWidth = table?.$el?.offsetWidth ?? 0;
    return tableWidth > width ? '100%' : width;
  });

  const getScrollRef = computed(() => {
    const tableHeight = unref(tableHeightRef);
    const { canResize, scroll } = unref(propsRef);
    const { table } = componentSetting;
    return {
      x: unref(getScrollX),
      y: canResize ? tableHeight : null,
      // update-begin--author:liaozhiyang---date:20240424---for：【issues/1188】BasicTableplusscrollToFirstRowOnChangetype definition
      scrollToFirstRowOnChange: table.scrollToFirstRowOnChange,
      // update-end--author:liaozhiyang---date:20240424---for：【issues/1188】BasicTableplusscrollToFirstRowOnChangetype definition
      ...scroll,
    };
  });

  return { getScrollRef, redoHeight };
}
