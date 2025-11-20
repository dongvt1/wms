<!--Department selection component-->
<template>
  <div class="JSelectDept">
    <JSelectBiz  @change="handleSelectChange" @handleOpen="handleOpen" :loading="loadingEcho" v-bind="attrs" :isCustomRenderTag="isCustomRenderTag" :rowKey="getBindValue?.rowKey"/>
    <!-- update-begin--author:liaozhiyang---date:20240515---for：【QQYUN-9260】Required mode will affect the pop-up windowantdComponent style -->
    <a-form-item>
      <DeptSelectModal @register="regModal" @getSelectResult="setValue" v-bind="getBindValue" :multiple="multiple" @close="handleClose"/>
    </a-form-item>
    <!-- update-end--author:liaozhiyang---date:20240515---for：【QQYUN-9260】Required mode will affect the pop-up windowantdComponent style -->
  </div>
</template>
<script lang="ts">
  import DeptSelectModal from './modal/DeptSelectModal.vue';
  import JSelectBiz from './base/JSelectBiz.vue';
  import { defineComponent, ref, reactive, watchEffect, watch, provide, unref, toRaw } from 'vue';
  import { useModal } from '/@/components/Modal';
  import { propTypes } from '/@/utils/propTypes';
  import { useRuleFormItem } from '/@/hooks/component/useFormItem';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { SelectValue } from 'ant-design-vue/es/select';
  import { cloneDeep } from 'lodash-es';

  export default defineComponent({
    name: 'JSelectDept',
    components: {
      DeptSelectModal,
      JSelectBiz,
    },
    inheritAttrs: false,
    props: {
      value: propTypes.oneOfType([propTypes.string, propTypes.array]),
      // Whether to allow multiple selections，default true
      multiple: propTypes.bool.def(true),
      // custom renderingtag
      isCustomRenderTag: propTypes.bool.def(true),
    },
    emits: ['options-change', 'change', 'select', 'update:value'],
    setup(props, { emit, refs }) {
      const emitData = ref<any[]>();
      //registermodel
      const [regModal, { openModal }] = useModal();
      //form value
      // const [state] = useRuleFormItem(props, 'value', 'change', emitData);
      //drop-down box option value
      const selectOptions = ref<SelectValue>([]);
      //Drop down box selected value
      let selectValues = reactive<Recordable>({
        value: [],
      });
      let tempSave: any = [];

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
        // update-begin--author:liaozhiyang---date:20240611---for：【TV360X-576】Data selected，Select again to open the pop-up window and click Cancel.，Data cleared（synchronousJSelectDeptChange the law）
        //update-begin-author:liusq---date:2024-06-03--for: [TV360X-840]User authorization，no choice，Click Cancel，Also echoes a selected user
        tempSave = [];
        //update-end-author:liusq---date:2024-06-03--for:[TV360X-840]User authorization，no choice，Click Cancel，Also echoes a selected user
        // update-end--author:liaozhiyang---date:20240611---for：【TV360X-576】Data selected，Select again to open the pop-up window and click Cancel.，Data cleared（synchronousJSelectDeptChange the law）
        props.value && initValue();
      });

      //update-begin-author:liusq---date:20220609--for: In order to solve the pop-up windowformInitialization assignment problem ---
      watch(
        () => props.value,
        () => {
          initValue();
        }
      );
      //update-end-author:liusq---date:20220609--for: In order to solve the pop-up windowformInitialization assignment problem ---
      /**
       * monitorselectValueschange
       */
      // update-begin--author:liaozhiyang---date:20240527---for：【TV360X-414】部门设置了default值，Query reset becomes empty(synchronousJSelectUser组件Change the law)
      // watch(selectValues, () => {
      //   if (selectValues) {
      //     state.value = selectValues.value;
      //   }
      // });
      // update-end--author:liaozhiyang---date:20240527---for：【TV360X-414】部门设置了default值，Query reset becomes empty(synchronousJSelectUser组件Change the law)
      /**
       * monitorselectOptionschange
       */
      watch(selectOptions, () => {
        if (selectOptions) {
          emit('select', toRaw(unref(selectOptions)), toRaw(unref(selectValues)));
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
        if (value && typeof value === 'string') {
          // state.value = value.split(',');
          selectValues.value = value.split(',');
          tempSave = value.split(',');
        } else {
          // 【VUEN-857】Compatible array（Line editing usage issues）
          selectValues.value = value;
          tempSave = cloneDeep(value);
        }
      }

      /**
       * Set the value of the drop-down box
       */
      function setValue(options, values) {
        selectOptions.value = options;
        //emitData.value = values.join(",");
        // state.value = values;
        selectValues.value = values;
        send(values);
      }
      const getBindValue = Object.assign({}, unref(props), unref(attrs));

       // update-begin--author:liaozhiyang---date:20240527---for：【TV360X-414】部门设置了default值，Query reset becomes empty(synchronousJSelectUser组件Change the law)
      const handleClose = () => {
        if (tempSave.length) {
          selectValues.value = cloneDeep(tempSave);
        } else {
          send(tempSave);
        }
      };
      const handleSelectChange = (values) => {
        tempSave = cloneDeep(values);
        send(tempSave);
      };
      const send = (values) => {
        let result = typeof props.value == 'string' ? values.join(',') : values;
        emit('update:value', result);
        emit('change', result);
        // update-begin--author:liaozhiyang---date:20240627---for：【TV360X-1648】User editing interface“Department”and“Responsible department”Linkage error（synchronous之前丢的代码）
        if (!values || values.length == 0) {
          emit('select', null, null);
        }
        // update-end--author:liaozhiyang---date:20240627---for：【TV360X-1648】User editing interface“Department”and“Responsible department”Linkage error（synchronous之前丢的代码）
      };
      // update-end--author:liaozhiyang---date:20240527---for：【TV360X-414】部门设置了default值，Query reset becomes empty(synchronousJSelectUser组件Change the law)
      
      return {
        // state,
        attrs,
        selectOptions,
        selectValues,
        loadingEcho,
        getBindValue,
        tag,
        regModal,
        setValue,
        handleOpen,
        handleClose,
        handleSelectChange,
      };
    },
  });
</script>
<style lang="less" scoped>
  // update-begin--author:liaozhiyang---date:20240515---for：【QQYUN-9260】Required mode will affect the pop-up windowantdComponent style
  .JSelectDept {
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
