import { Ref, unref, watchEffect } from 'vue';
import { useTimeoutFn } from '/@/hooks/core/useTimeout';

export interface UseModalDragMoveContext {
  draggable: Ref<boolean>;
  destroyOnClose: Ref<boolean | undefined> | undefined;
  visible: Ref<boolean>;
}

export function useModalDragMove(context: UseModalDragMoveContext) {
  const getStyle = (dom: any, attr: any) => {
    return getComputedStyle(dom)[attr];
  };
  const drag = (wrap: any) => {
    if (!wrap) return;
    wrap.setAttribute('data-drag', unref(context.draggable));
    const dialogHeaderEl = wrap.querySelector('.ant-modal-header');
    const dragDom = wrap.querySelector('.ant-modal');

    if (!dialogHeaderEl || !dragDom || !unref(context.draggable)) return;

    dialogHeaderEl.style.cursor = 'move';

    dialogHeaderEl.onmousedown = (e: any) => {
      if (!e) return;
      // mouse pressed，Calculate the distance of the current element from the visible area
      const disX = e.clientX;
      const disY = e.clientY;
      const screenWidth = document.body.clientWidth; // bodycurrent width
      const screenHeight = document.documentElement.clientHeight; // Visible area height(should bebodyhigh，However, it cannot be obtained under certain circumstances)

      const dragDomWidth = dragDom.offsetWidth; // Dialog width
      const dragDomheight = dragDom.offsetHeight; // 对话框high

      const minDragDomLeft = dragDom.offsetLeft;

      const maxDragDomLeft = screenWidth - dragDom.offsetLeft - dragDomWidth;
      const minDragDomTop = dragDom.offsetTop;
      let maxDragDomTop = screenHeight - dragDom.offsetTop - dragDomheight;
      //update-begin-author:liusq---date:20230407--for: [issue/430]The pop-up page will automatically pop up.，Unable to move and display head--- 
      if(maxDragDomTop<0){
        maxDragDomTop = screenHeight - dragDom.offsetTop
      }
      //update-end-author:liusq---date:20230407--for: [issue/430]The pop-up page will automatically pop up.，Unable to move and display head--- 
      // The value band obtainedpx Regular match replacement
      const domLeft = getStyle(dragDom, 'left');
      const domTop = getStyle(dragDom, 'top');
      let styL = +domLeft;
      let styT = +domTop;

      // Pay attention toiemiddle The value obtained for the first time is the component’s own value.50% After moving, the value is assigned topx
      if (domLeft.includes('%')) {
        styL = +document.body.clientWidth * (+domLeft.replace(/%/g, '') / 100);
        styT = +document.body.clientHeight * (+domTop.replace(/%/g, '') / 100);
      } else {
        styL = +domLeft.replace(/px/g, '');
        styT = +domTop.replace(/px/g, '');
      }

      document.onmousemove = function (e) {
        // via event delegation，Calculate distance moved
        let left = e.clientX - disX;
        let top = e.clientY - disY;

        // Boundary processing
        if (-left > minDragDomLeft) {
          left = -minDragDomLeft;
        } else if (left > maxDragDomLeft) {
          left = maxDragDomLeft;
        }

        if (-top > minDragDomTop) {
          top = -minDragDomTop;
        } else if (top > maxDragDomTop) {
          top = maxDragDomTop;
        }

        // Move the current element
        dragDom.style.cssText += `;left:${left + styL}px;top:${top + styT}px;`;
      };

      document.onmouseup = () => {
        document.onmousemove = null;
        document.onmouseup = null;
      };
    };
  };

  const handleDrag = () => {
    const dragWraps = document.querySelectorAll('.ant-modal-wrap');
    for (const wrap of Array.from(dragWraps)) {
      if (!wrap) continue;
      const display = getStyle(wrap, 'display');
      const draggable = wrap.getAttribute('data-drag');
      if (display !== 'none') {
        // Drag position
        if (draggable === null || unref(context.destroyOnClose)) {
          drag(wrap);
        }
      }
    }
  };

  watchEffect(() => {
    if (!unref(context.visible) || !unref(context.draggable)) {
      return;
    }
    useTimeoutFn(() => {
      handleDrag();
    }, 30);
  });
}
