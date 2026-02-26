<!--Character selection component-->
<template>
  <div class="JSelectRole">
    <JSelectBiz @handleOpen="handleOpen" :loading="loadingEcho" v-bind="attrs"></JSelectBiz>
    <!-- update-begin--author:liaozhiyang---date:20240515---for：【QQYUN-9260】Required mode will affect the pop-up windowantdComponent style -->
    <a-form-item>
      <RoleSelectModal @register="regModal" @getSelectResult="setValue" v-bind="getBindValue"></RoleSelectModal>
    </a-form-item>
    <!-- update-end--author:liaozhiyang---date:20240515---for：【QQYUN-9260】Required mode will affect the pop-up windowantdComponent style -->
  </div>
</template>
<script lang="ts">
  import RoleSelectModal from './modal/RoleSelectModal.vue';
  import JSelectBiz from './base/JSelectBiz.vue';
  import { defineComponent, ref, unref, reactive, watchEffect, watch, provide } from 'vue';
  import { useModal } from '/@/components/Modal';
  import { propTypes } from '/@/utils/propTypes';
  import { useRuleFormItem } from '/@/hooks/component/useFormItem';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { SelectValue } from 'ant-design-vue/es/select';

  export default defineComponent({
    name: 'JSelectRole',
    components: {
      RoleSelectModal,
      JSelectBiz,
    },
    inheritAttrs: false,
    props: {
      value: propTypes.oneOfType([propTypes.string, propTypes.array]),
      labelKey: {
        type: String,
        default: 'roleName',
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
      let selectValues = reactive<Recordable>({
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
        props.value && initValue();
        // When query conditions are reset，Clear interface display
        if (!props.value) {
          selectValues.value = [];
        }
      });

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
        // update-begin--author:liaozhiyang---date:20250318---for：【issues/7948】repairJselectRoleComponent does not support two-way binding
        emit('update:value', values);
        // update-end--author:liaozhiyang---date:20250318---for：【issues/7948】repairJselectRoleComponent does not support two-way binding
      }
      const getBindValue = Object.assign({}, unref(props), unref(attrs));
      return {
        state,
        attrs,
        getBindValue,
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
  .JSelectRole {
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
