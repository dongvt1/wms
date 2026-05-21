/**
 * adaptive width constructor
 *
 * @time 2022-4-8
 * @author sunjianlei
 */
import { ref } from 'vue';
import { useDebounceFn, tryOnUnmounted } from '@vueuse/core';
import { useEventListener } from '/@/hooks/event/useEventListener';

// key = jsoperator+number
const defWidthConfig: configType = {
  '<=565': '100%',
  '<=1366': '800px',
  '<=1600': '600px',
  '<=1920': '600px',
  '>1920': '500px',
};

type configType = Record<string, string | number>;

/**
 * adaptive width
 *
 * @param widthConfig width configuration，Can be referenced defWidthConfig Configuration
 * @param assign 是否合并默认Configuration
 * @param debounce Debounce milliseconds
 */
export function useAdaptiveWidth(widthConfig = defWidthConfig, assign = true, debounce = 50) {
  const widthConfigAssign = assign ? Object.assign({}, defWidthConfig, widthConfig) : widthConfig;
  const configKeys = Object.keys(widthConfigAssign);

  const adaptiveWidth = ref<string | number>();

  /**
   * Calculate width
   * @param innerWidth
   */
  function calcWidth(innerWidth) {
    let width;
    for (const key of configKeys) {
      try {
        // passjsOperation
        let flag = new Function(`return ${innerWidth} ${key}`)();
        if (flag) {
          width = widthConfigAssign[key];
          break;
        }
      } catch (e) {
        console.error(e);
      }
    }
    if (width) {
      adaptiveWidth.value = width;
    } else {
      console.warn('没有找到匹配的adaptive width');
    }
  }

  // Initial calculation
  calcWidth(window.innerWidth);

  // monitor resize event
  const { removeEvent } = useEventListener({
    el: window,
    name: 'resize',
    listener: useDebounceFn(() => calcWidth(window.innerWidth), debounce),
  });
  // 卸载组件时取消monitorevent
  tryOnUnmounted(() => removeEvent());

  return { adaptiveWidth };
}

/**
 * 抽屉adaptive width
 */
export function useDrawerAdaptiveWidth() {
  return useAdaptiveWidth(
    {
      '<=620': '100%',
      '<=1600': 600,
      '<=1920': 650,
      '>1920': 700,
    },
    false
  );
}
