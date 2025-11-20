<!--user selects component-->
<template>
  <div>
    <JSelectBiz @handleOpen="handleOpen" :loading="loadingEcho" v-bind="attrs" @change="handleSelectChange"></JSelectBiz>
    <JSelectUserByDepartmentModal
      v-if="modalShow"
      :selectedUser="selectOptions"
      :modalTitle="modalTitle"
      :rowKey="rowKey"
      :labelKey="labelKey"
      :isRadioSelection="isRadioSelection"
      :params="params"
      @register="regModal"
      @change="setValue"
      @close="() => (modalShow = false)"
      v-bind="attrs"
    ></JSelectUserByDepartmentModal>
  </div>
</template>
<script lang="ts" setup>
  import { ref, reactive, watch, provide } from 'vue';
  import JSelectUserByDepartmentModal from './modal/JSelectUserByDepartmentModal.vue';
  import JSelectBiz from './base/JSelectBiz.vue';
  import { useModal } from '/@/components/Modal';
  import { propTypes } from '/@/utils/propTypes';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { SelectValue } from 'ant-design-vue/es/select';
  import { isArray, isString, isObject } from '/@/utils/is';
  import { getTableList as getTableListOrigin } from '/@/api/common/api';
  import { useMessage } from '/@/hooks/web/useMessage';

  defineOptions({ name: 'JSelectUserByDepartment' });
  const props = defineProps({
    value: propTypes.oneOfType([propTypes.string, propTypes.array]),
    modalTitle: {
      type: String,
      default: 'Department user selection',
    },
    rowKey: {
      type: String,
      default: 'username',
    },
    labelKey: {
      type: String,
      default: 'realname',
    },
    //query parameters
    params: {
      type: Object,
      default: () => {},
    },
    isRadioSelection: {
      type: Boolean,
      default: false,
    },
  });
  const emit = defineEmits(['options-change', 'change', 'update:value']);
  const { createMessage } = useMessage();
  //registermodel
  const [regModal, { openModal }] = useModal();
  // Whether to display a pop-up window
  const modalShow = ref(false);
  //drop-down box option value
  const selectOptions: any = ref<SelectValue>([]);
  //Drop down box selected value
  let selectValues: any = reactive<object>({
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

  const attrs: any = useAttrs();

  // Open pop-up window
  function handleOpen() {
    modalShow.value = true;
    setTimeout(() => {
      openModal(true, {
        isUpdate: false,
      });
    }, 0);
  }
  const handleSelectChange = (data) => {
    selectOptions.value = selectOptions.value.filter((item) => data.includes(item[props.rowKey]));
    setValue(selectOptions.value);
  };
  // Set the value of the drop-down box
  const setValue = (data) => {
    selectOptions.value = data.map((item) => {
      return {
        ...item,
        label: item[props.labelKey],
        value: item[props.rowKey],
      };
    });
    selectValues.value = data.map((item) => item[props.rowKey]);
    // renewvalue
    emit('update:value', selectValues.value);
    // triggerchangeevent（Not turning becausebasicFormThe string will be automatically converted into an array when submitted.）
    emit('change', selectValues.value);
    // triggeroptions-changeevent
    emit('options-change', selectOptions.value);
  };
  // translate
  const transform = () => {
    let value = props.value;
    let len;
    if (isArray(value) || isString(value)) {
      if (isArray(value)) {
        len = value.length;
        value = value.join(',');
      } else {
        len = value.split(',').length;
      }
      value = value.trim();
      if (value) {
        // ifvalueThe value is inselectedUserexist in，则不请求translate
        let isNotRequestTransform = false;
        isNotRequestTransform = value.split(',').every((value) => !!selectOptions.value.find((item) => item[props.rowKey] === value));
        if (isNotRequestTransform) {
          selectValues.value = value.split(',')
          return;
        }
        const params = { isMultiTranslate: true, pageSize: len, [props.rowKey]: value };
        if (isObject(attrs.params)) {
          Object.assign(params, attrs.params);
        }
        getTableListOrigin(params).then((result: any) => {
          const records = result.records ?? [];
          selectValues.value = records.map((item) => item[props.rowKey]);
          selectOptions.value = records.map((item) => {
            return {
              ...item,
              label: item[props.labelKey],
              value: item[props.rowKey],
            };
          });
        });
      }
    } else {
      selectValues.value = [];
    }
  };
  // monitorvaluechange
  watch(
    () => props.value,
    () => {
      transform();
    },
    { deep: true, immediate: true }
  );
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
