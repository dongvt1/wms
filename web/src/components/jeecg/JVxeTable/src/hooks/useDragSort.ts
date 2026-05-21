import { onMounted, onUnmounted, nextTick } from 'vue';
import { JVxeTableMethods, JVxeTableProps } from '/@/components/jeecg/JVxeTable/src/types';
import Sortable from 'sortablejs';
import { isEnabledVirtualYScroll } from '/@/components/jeecg/JVxeTable/utils';

export function useDragSort(props: JVxeTableProps, methods: JVxeTableMethods) {
  if (props.dragSort) {
    let sortable2: Sortable;
    let initTime: any;

    onMounted(() => {
      // Bind the drag event after loading is complete
      initTime = setTimeout(createSortable, 300);
    });

    onUnmounted(() => {
      clearTimeout(initTime);
      if (sortable2) {
        sortable2.destroy();
      }
    });

    function createSortable() {
      let xTable = methods.getXTable();
      // let dom = xTable.$el.querySelector('.vxe-table--fixed-wrapper .vxe-table--body tbody')
      // let dom = xTable.$el.querySelector('.body--wrapper>.vxe-table--body tbody');
      let dom = xTable.$el.querySelector('.vxe-table--body-inner-wrapper > .vxe-table--body tbody');
      if (!dom) {
        console.warn('[JVxeTable] Drag sort initialization failed，may bevxe-tableVersion incompatibility caused by upgrade。');
        return;
      }
      let startChildren = [];
      sortable2 = Sortable.create(dom as HTMLElement, {
        handle: '.drag-btn',
        // update-begin--author:liaozhiyang---date:20240417---for:【QQYUN-8785】onlineform column positionidNo restrictions，Drag other columns toidcolumn above，Error when synchronizing database
        filter: '.not-allow-drag',
        draggable: ".allow-drag",
        // update-end--author:liaozhiyang---date:20240417---for:【QQYUN-8785】onlineform column positionidNo restrictions，Drag other columns toidcolumn above，Error when synchronizing database
        direction: 'vertical',
        animation: 300,
        onStart(e) {
          let from = e.from;
          // @ts-ignore
          startChildren = [...from.children];
        },
        onEnd(e: any) {
          // -update-begin--author:liaozhiyang---date:20240619---for：【TV360X-585】Virtual scrolling does not work when dragging fields
          const isRealEnabledVirtual = isEnabledVirtualYScroll(props, xTable);
          let newIndex;
          let oldIndex;
          // Scroll sorting needs to distinguish whether the current line editor has activated virtual scrolling(Ground floorloadDataThe method handles whether virtual scrolling is actually turned on is different, so it needs to be distinguished.)
          if (isRealEnabledVirtual) {
            // e.cloneThe element is the actual dragged element(Virtual scrolling will not change)
            const dragNode = e.clone;
            const dragRowInfo = xTable.getRowNode(dragNode);
            // e.itemThe element is a draggable element only when there is no virtual scrolling.(If virtual scrolling occurs, it will change)
            const itemNode = e.item;
            const itemRowInfo = xTable.getRowNode(itemNode);
            // e.newIndexIs the index of the element in the current visual area(Not the actual index of the data)、e.oldIndex Is the index of the element in the visual area when dragging(Not the actual index of the data)
            if (dragRowInfo!.rowid === itemRowInfo!.rowid) {
              // e.cloneande.itemThe same description shows that the dragged element is inDOMmiddle，Not given by virtual scrollingremoveLose。
              if (e.newIndex === e.oldIndex) {
                // New and old at this timeindexIf the same, it can be considered that there is no dragging
                return;
              }
            } else {
            }
            // real at this timeDOMThe elements are sorted(通过drag元素的前后元素确定drag元素在真实数据middle是往前还是往后拖)
            oldIndex = dragRowInfo!.index;
            const len = e.from.childNodes.length;
            let referenceIndex;
            let referenceNode;
            if (e.newIndex + 1 < len) {
              // dragDOMafter exchange，There are elements behind（The reference is the following element）
              referenceNode = e.from.childNodes[e.newIndex + 1];
              referenceIndex = xTable.getRowNode(referenceNode)!.index;
              if (oldIndex > referenceIndex) {
                newIndex = referenceIndex;
              } else {
                newIndex = referenceIndex - 1;
              }
            } else {
              // dragDOMafter exchange，There are no elements behind（The reference is the previous element）
              referenceNode = e.from.childNodes[e.newIndex - 1];
              referenceIndex = xTable.getRowNode(referenceNode)!.index;
              newIndex = referenceIndex;
            }
          } else {
            oldIndex = e.oldIndex;
            newIndex = e.newIndex;
            if (oldIndex === newIndex) {
              return;
            }
            const from = e.from;
            const element = startChildren[oldIndex];
            let target = null;
            if (oldIndex > newIndex) {
              // move up
              if (oldIndex + 1 < startChildren.length) {
                target = startChildren[oldIndex + 1];
              }
            } else {
              // move down
              target = startChildren[oldIndex + 1];
            }
            from.removeChild(element);
            from.insertBefore(element, target);
          }
          // -update-end--author:liaozhiyang---date:20240620---for：【TV360X-585】Virtual scrolling does not work when dragging fields
          nextTick(() => {
            methods.doSort(oldIndex, newIndex);
            methods.trigger('dragged', { oldIndex: oldIndex, newIndex: newIndex });
          });
        },
      });
    }
  }
}
