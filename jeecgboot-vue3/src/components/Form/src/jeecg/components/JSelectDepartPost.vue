<!--Department selection component-->
<template>
  <div class="JSelectDepartPost">
    <JSelectBiz @change="handleSelectChange" @handleOpen="handleOpen" :loading="loadingEcho" v-bind="attrs" :isCustomRenderTag="isCustomRenderTag" :rowKey="getBindValue?.rowKey"/>
    <a-form-item>
      <DeptSelectModal @register="regModal" @getSelectResult="setValue" modalTitle="Department position selection" v-bind="getBindValue" :multiple="multiple" @close="handleClose" />
    </a-form-item>
  </div>
</template>
<script lang="ts">
  import DeptSelectModal from './modal/DeptSelectModal.vue';
  import JSelectBiz from './base/JSelectBiz.vue';
  import { defineComponent, ref, reactive, watchEffect, watch, provide, unref, toRaw } from 'vue';
  import { useModal } from '/@/components/Modal';
  import { propTypes } from '/@/utils/propTypes';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { SelectValue } from 'ant-design-vue/es/select';
  import { cloneDeep } from 'lodash-es';

  export default defineComponent({
    name: 'JSelectDepartPost',
    components: {
      DeptSelectModal,
      JSelectBiz,
    },
    inheritAttrs: false,
    props: {
      value: propTypes.oneOfType([propTypes.string, propTypes.array]),
      // Whether to allow multiple selections，default true
      multiple: propTypes.bool.def(true),
      //Whether to select only positions
      izOnlySelectDepartPost: propTypes.bool.def(true),
      // custom renderingtag
      isCustomRenderTag: propTypes.bool.def(true),
    },
    emits: ['options-change', 'change', 'select', 'update:value'],
    setup(props, { emit, refs }) {
      const emitData = ref<any[]>();
      //registermodel
      const [regModal, { openModal }] = useModal();
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
        tempSave = [];
        props.value && initValue();
      });

      watch(
        () => props.value,
        () => {
          initValue();
        }
      );

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
          selectValues.value = value.split(',');
          tempSave = value.split(',');
        } else {
          selectValues.value = value;
          tempSave = cloneDeep(value);
        }
      }

      /**
       * Set the value of the drop-down box
       */
      function setValue(options, values) {
        selectOptions.value = options;
        selectValues.value = values;
        send(values);
      }
      const getBindValue = Object.assign({}, unref(props), unref(attrs));

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
        if (!values || values.length == 0) {
          emit('select', null, null);
        }
      };

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
  .JSelectDepartPost {
    > .ant-form-item {
      display: none;
    }
  }
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
