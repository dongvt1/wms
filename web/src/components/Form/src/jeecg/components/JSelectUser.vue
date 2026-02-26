<!--user selects component-->
<template>
  <div class="JselectUser">
    <JSelectBiz @change="handleSelectChange" @handleOpen="handleOpen" :loading="loadingEcho" v-bind="attrs"></JSelectBiz>
    <!-- update-begin--author:liaozhiyang---date:20240515---for：【QQYUN-9260】Required mode will affect the pop-up windowantdComponent style -->
    <a-form-item>
      <UserSelectModal
        :rowKey="rowKey"
        @register="regModal"
        @getSelectResult="setValue"
        v-bind="getBindValue"
        :excludeUserIdList="excludeUserIdList"
        @close="handleClose"
      />
    </a-form-item>
    <!-- update-end--author:liaozhiyang---date:20240515---for：【QQYUN-9260】Required mode will affect the pop-up windowantdComponent style -->
  </div>
</template>
<script lang="ts">
  import { unref } from 'vue';
  import UserSelectModal from './modal/UserSelectModal.vue';
  import JSelectBiz from './base/JSelectBiz.vue';
  import { defineComponent, ref, reactive, watchEffect, watch, provide } from 'vue';
  import { useModal } from '/@/components/Modal';
  import { propTypes } from '/@/utils/propTypes';
  import { useRuleFormItem } from '/@/hooks/component/useFormItem';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { SelectValue } from 'ant-design-vue/es/select';
  import { cloneDeep } from 'lodash-es';
  export default defineComponent({
    name: 'JSelectUser',
    components: {
      UserSelectModal,
      JSelectBiz,
    },
    inheritAttrs: false,
    props: {
      value: propTypes.oneOfType([propTypes.string, propTypes.array]),
      labelKey: {
        type: String,
        default: 'realname',
      },
      rowKey: {
        type: String,
        default: 'username',
      },
      params: {
        type: Object,
        default: () => {},
      },
      //update-begin---author:wangshuai ---date:20230703  for：【QQYUN-5685】5、Resigners can choose themselves------------
      //Exclude usersidcollection of
      excludeUserIdList:{
        type: Array,
        default: () => [],
      }
      //update-end---author:wangshuai ---date:20230703  for：【QQYUN-5685】5、Resigners can choose themselves------------
    },
    emits: ['options-change', 'change', 'update:value'],
    setup(props, { emit }) {
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
        change: false,
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
        // update-begin--author:liaozhiyang---date:20240611---for：【TV360X-576】Data selected，Select again to open the pop-up window and click Cancel.，Data cleared
        //update-begin-author:liusq---date:2024-06-03--for: [TV360X-840]User authorization，no choice，Click Cancel，Also echoes a selected user
        tempSave = [];
        //update-end-author:liusq---date:2024-06-03--for:[TV360X-840]User authorization，no choice，Click Cancel，Also echoes a selected user
        // update-end--author:liaozhiyang---date:20240611---for：【TV360X-576】Data selected，Select again to open the pop-up window and click Cancel.，Data cleared
        props.value && initValue();
        // When query conditions are reset The interface display is not cleared
        if (!props.value) {
          selectValues.value = [];
        }
      });

      /**
       * monitorselectValueschange
       */
      // watch(selectValues, () => {
      //   if (selectValues) {
      //     state.value = selectValues.value;
      //   }
      // });

      //update-begin---author:wangshuai ---date:20230703  for：【QQYUN-5685】5、Resigners can choose themselves------------
      const excludeUserIdList = ref<any>([]);
      
      /**
       * 需要monitor一下excludeUserIdList，otherwisemodalCan't get
       */ 
      watch(()=>props.excludeUserIdList,(data)=>{
        excludeUserIdList.value = data;
      },{ immediate: true })
      //update-end---author:wangshuai ---date:20230703  for：【QQYUN-5685】5、Resigners can choose themselves------------
      
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
      // update-begin--author:liaozhiyang---date:20240517---for：【QQYUN-9366】user selects component取消和关闭会把选择数据带入
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
        let result = typeof props.value == "string" ? values.join(',') : values;
        emit('update:value', result);
        emit('change', result);
      };
      // update-end--author:liaozhiyang---date:20240517---for：【QQYUN-9366】user selects component取消和关闭会把选择数据带入
      return {
        // state,
        attrs,
        selectOptions,
        getBindValue,
        selectValues,
        loadingEcho,
        tag,
        regModal,
        setValue,
        handleOpen,
        excludeUserIdList,
        handleClose,
        handleSelectChange,
      };
    },
  });
</script>
<style lang="less" scoped>
  // update-begin--author:liaozhiyang---date:20240515---for：【QQYUN-9260】Required mode will affect the pop-up windowantdComponent style
  .JselectUser {
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
