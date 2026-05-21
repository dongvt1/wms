import { defineComponent, h, ref, watch } from 'vue';
import { randomString } from '/@/utils/common/compUtils';
import '../style/reload-effect.less';

// Modify data effects
export default defineComponent({
  props: {
    vNode: null,
    // Whether to enable special effects
    effect: Boolean,
  },
  emits: ['effectBegin', 'effectEnd'],
  setup(props, { emit }) {
    // vNode: null,
    const innerEffect = ref(props.effect);
    // Cope with multiple special effects at the same time
    const effectIdx = ref(0);
    const effectList = ref<any[]>([]);

    watch(
      () => props.effect,
      () => (innerEffect.value = props.effect)
    );
    watch(
      () => props.vNode,
      (_vNode, old) => {
        if (props.effect && old != null) {
          let topLayer = renderSpan(old, 'top');
          effectList.value.push(topLayer);
        }
      },
      { deep: true, immediate: true }
    );

    // Conditionally rendered content span
    function renderVNode() {
      if (props.vNode == null) {
        return null;
      }
      let bottom = renderSpan(props.vNode, 'bottom');
      // Effects enabled，and have old data，Just render the top layer of special effects
      if (innerEffect.value && effectList.value.length > 0) {
        emit('effectBegin');
        // 1.4s Turn off special effects in the future
        window.setTimeout(() => {
          let item = effectList.value[effectIdx.value];
          if (item && item.elm) {
            // After the special effects，Show first display set to none，Instead of directly deleting the element，
            // The purpose is to prevent the page from re-rendering，Cause animation to reset
            item.elm.style.display = 'none';
          }
          // When all level animations have ended，Then delete all elements
          if (++effectIdx.value === effectList.value.length) {
            innerEffect.value = false;
            effectIdx.value = 0;
            effectList.value = [];
            emit('effectEnd');
          }
        }, 1400);
        return [effectList.value, bottom];
      } else {
        return bottom;
      }
    }

    // render content span
    function renderSpan(vNode, layer) {
      let options = {
        key: layer + effectIdx.value + randomString(6),
        class: ['j-vxe-reload-effect-span', `layer-${layer}`],
        style: {},
        // update-begin--author:liaozhiyang---date:20240424---for：【issues/1175】solvevxetablemousehoveraftertitleDisplay error
        title: vNode,
        // update-end--author:liaozhiyang---date:20240424---for：【issues/1175】solvevxetablemousehoveraftertitleDisplay error

      };
      if (layer === 'top') {
        // The latest rendering is below
        options.style['z-index'] = 9999 - effectIdx.value;
      }
      return h('span', options, [vNode]);
    }

    return () =>
      h(
        'div',
        {
          class: ['j-vxe-reload-effect-box'],
        },
        [renderVNode()]
      );
  },
});
