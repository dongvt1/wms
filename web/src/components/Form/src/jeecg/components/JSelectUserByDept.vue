<!--user selects component-->
<template>
  <div>
    <JSelectBiz @change="handleChange" @handleOpen="handleOpen" :loading="loadingEcho" v-bind="attrs"></JSelectBiz>
    <UserSelectByDepModal :rowKey="rowKey" @register="regModal" @getSelectResult="setValue" v-bind="getBindValue"></UserSelectByDepModal>
  </div>
</template>
<script lang="ts">
  import { unref } from 'vue';
  import UserSelectByDepModal from './modal/UserSelectByDepModal.vue';
  import JSelectBiz from './base/JSelectBiz.vue';
  import { defineComponent, ref, reactive, watchEffect, watch, provide } from 'vue';
  import { useModal } from '/@/components/Modal';
  import { propTypes } from '/@/utils/propTypes';
  import { useRuleFormItem } from '/@/hooks/component/useFormItem';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { SelectValue } from 'ant-design-vue/es/select';
  export default defineComponent({
    name: 'JSelectUserByDept',
    components: {
      UserSelectByDepModal,
      JSelectBiz,
    },
    inheritAttrs: false,
    props: {
      value: propTypes.oneOfType([propTypes.string, propTypes.array]),
      rowKey: {
        type: String,
        default: 'username',
      },
      labelKey: {
        type: String,
        default: 'realname',
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
      let selectValues = reactive<object>({
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
      watchEffect(() => {
        initValue();
      });

      /**
       * monitorselectValueschange
       */
      watch(selectValues, () => {
        if (selectValues) {
          // update-begin--author:liaozhiyang---date:20250616---for：【QQYUN-12869】Select user components by department，After selecting the user in the required state，After clicking reset，Verification information will appear
          if (props.value === undefined && selectValues.value?.length == 0) {
            return;
          }
          // update-end--author:liaozhiyang---date:20250616---for：【QQYUN-12869】Select user components by department，After selecting the user in the required state，After clicking reset，Verification information will appear
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
        } else {
          selectValues.value = value;
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
        emit('update:value', values);
        emit('options-change', options);
      }

      function handleChange(values) {
        emit('update:value', values);
      }
      const getBindValue = Object.assign({}, unref(props), unref(attrs));
      return {
        state,
        attrs,
        selectOptions,
        getBindValue,
        selectValues,
        loadingEcho,
        tag,
        regModal,
        setValue,
        handleOpen,
        handleChange,
      };
    },
  });
</script>
<style lang="less" scoped>
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
