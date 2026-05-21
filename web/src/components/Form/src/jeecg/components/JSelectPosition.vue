<!--job selection component-->
<template>
  <div class="JSelectPosition">
    <JSelectBiz @handleOpen="handleOpen" :loading="loadingEcho" v-bind="attrs" @change="(changeValue) => $emit('update:value', changeValue)"></JSelectBiz>
    <!-- update-begin--author:liaozhiyang---date:20240515---for：【QQYUN-9260】Required mode will affect the pop-up windowantdComponent style -->
    <a-form-item>
      <PositionSelectModal @register="regModal" @getSelectResult="setValue" v-bind="getBindValue"></PositionSelectModal>
    </a-form-item>
    <!-- update-end--author:liaozhiyang---date:20240515---for：【QQYUN-9260】Required mode will affect the pop-up windowantdComponent style -->
  </div>
</template>
<script lang="ts">
  import PositionSelectModal from './modal/PositionSelectModal.vue';
  import JSelectBiz from './base/JSelectBiz.vue';
  import { defineComponent, ref, reactive, watchEffect, watch, provide, computed, unref } from 'vue';
  import { useModal } from '/@/components/Modal';
  import { propTypes } from '/@/utils/propTypes';
  import { useRuleFormItem } from '/@/hooks/component/useFormItem';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { SelectValue } from 'ant-design-vue/es/select';

  export default defineComponent({
    name: 'JSelectPosition',
    components: {
      PositionSelectModal,
      JSelectBiz,
    },
    inheritAttrs: false,
    props: {
      value: propTypes.oneOfType([propTypes.string, propTypes.array]),
      labelKey: {
        type: String,
        default: 'name',
      },
      rowKey: {
        type: String,
        default: 'id',
      },
      params: {
        type: Object,
        default: () => {},
      },
    },
    emits: ['options-change', 'change', 'update:value'],
    setup(props, { emit, refs }) {
      const emitData = ref<any[]>();
      //registermodel
      const [regModal, { openModal }] = useModal();
      //form value
      const [state] = useRuleFormItem(props, 'value', 'change', emitData);
      //drop-down box option value
      const selectOptions = ref<SelectValue>([]);
      //Drop down box selected value
      let selectValues = reactive<any>({
        value: [],
        change: false,
      });
      // Whether the echo data is being loaded
      const loadingEcho = ref<boolean>(false);
      //Issue selectOptions,xxxBizComponent receives
      provide('selectOptions', selectOptions);
      //Issue selectValues,xxxBizComponent receives
      provide('selectValues', selectValues);
      //Issue loadingEcho,xxxBizComponent receives
      provide('loadingEcho', loadingEcho);

      const tag = ref(false);
      const attrs = useAttrs();

      /**
       * Listening component value
       */
      // update-begin--author:liaozhiyang---date:20250423---for：【pull/8014】Cancel the data in the slot mode pop-up windowcheckboxselected state，Requires a second click to take effect。
      watch(
        () => props.value,
        () => {
          if (props.value) {
            initValue();
          } else {
            // update-begin--author:liaozhiyang---date:20250604---for：【issues/8233】resetFieldscannot be reset when
            if (selectValues.value?.length) {
              selectValues.value = [];
            }
            // update-end--author:liaozhiyang---date:20250604---for：【issues/8233】resetFieldscannot be reset when
          }
        },
        { deep: true, immediate: true }
      );
      // update-end--author:liaozhiyang---date:20250423---for：【pull/8014】Cancel the data in the slot mode pop-up windowcheckboxselected state，Requires a second click to take effect。

      /**
       * monitorselectValueschange
       */
      watch(selectValues, () => {
        if (selectValues) {
          state.value = selectValues.value;
        }
      });

      /**
       * Check-in pop-up box
       */
      function handleOpen() {
        tag.value = true;
        openModal(true, {
          isUpdate: false,
        });
      }

      /**
       * Convert string value to array
       */
      function initValue() {
        let value = props.value ? props.value : [];
        if (value && typeof value === 'string' && value != 'null' && value != 'undefined') {
          state.value = value.split(',');
          selectValues.value = value.split(',');
        }
      }

      /**
       * Set the value of the drop-down box
       */
      function setValue(options, values) {
        selectOptions.value = options;
        //emitData.value = values.join(",");
        state.value = values;
        selectValues.value = values;
        //update-begin-author:liusq date:20230517 for:Select job componentv-modelThe method binding value does not take effect
        emit('update:value', values.join(','));
        //update-begin-author:liusq date:20230517 for:Select job componentv-modelThe method binding value does not take effect

      }

      const getBindValue = Object.assign({}, unref(props), unref(attrs));
      return {
        state,
        getBindValue,
        attrs,
        selectOptions,
        selectValues,
        loadingEcho,
        tag,
        regModal,
        setValue,
        handleOpen,
      };
    },
  });
</script>
<style lang="less" scoped>
  // update-begin--author:liaozhiyang---date:20240515---for：【QQYUN-9260】Required mode will affect the pop-up windowantdComponent style
  .JSelectPosition {
    > .ant-form-item {
      display: none;
    }
  }
  // update-end--author:liaozhiyang---date:20240515---for：【QQYUN-9260】Required mode will affect the pop-up windowantdComponent style
  .j-select-row {
    @width: 82px;

    .left {
      width: calc(100% - @width - 8px);
    }

    .right {
      width: @width;
    }

    .full {
      width: 100%;
    }

    :deep(.ant-select-search__field) {
      display: none !important;
    }
  }
</style>
