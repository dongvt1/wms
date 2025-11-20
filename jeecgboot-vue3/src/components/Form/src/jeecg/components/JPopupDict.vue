<!--popupcomponents-->
<template>
  <div class="JPopupDict components-input-demo-presuffix">
    <!--Input box-->
    <a-select v-model:value="showText" v-bind="attrs" :mode="multi ? 'multiple' : ''" @click="handleOpen" readOnly :loading="loading">
      <a-select-option v-for="item in options" :value="item.value">{{ item.text }}</a-select-option>
    </a-select>
    <!-- update-begin--author:liaozhiyang---date:20240515---for：【QQYUN-9260】Required mode will affect the pop-up windowantdcomponents的样式 -->
    <a-form-item>
      <!--popupPop-up window-->
      <JPopupOnlReportModal
        @register="regModal"
        :code="code"
        :multi="multi"
        :selected="selected"
        :rowkey="valueFiled"
        :sorter="sorter"
        :groupId="''"
        :param="param"
        :getFormValues="getFormValues"
        :getContainer="getContainer"
        :showAdvancedButton="showAdvancedButton"
        @ok="callBack"
      />
    </a-form-item>
    <!-- update-end--author:liaozhiyang---date:20240515---for：【QQYUN-9260】Required mode will affect the pop-up windowantdcomponents的样式 -->
  </div>
</template>
<script lang="ts">
  import JPopupOnlReportModal from './modal/JPopupOnlReportModal.vue';
  import { defineComponent, ref, nextTick, watch, reactive, unref } from 'vue';
  import { useModal } from '/@/components/Modal';
  import { propTypes } from '/@/utils/propTypes';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { defHttp } from '/@/utils/http/axios';
  import { useMessage } from '/@/hooks/web/useMessage';
  //define requesturlinformation
  const configUrl = reactive({
    getColumns: '/online/cgreport/api/getRpColumns/',
    getData: '/online/cgreport/api/getData/',
  });

  export default defineComponent({
    name: 'JPopupDict',
    components: {
      JPopupOnlReportModal,
    },
    inheritAttrs: false,
    props: {
      /**
       * Example：demo,name,id
       * demo: onlineReport coding
       * name: onlineReport fields，Displayed by the userlabel
       * id: onlineReport fields，for storagekey
       */
      dictCode: propTypes.string.def(''),
      value: propTypes.string.def(''),
      sorter: propTypes.string.def(''),
      multi: propTypes.bool.def(false),
      param: propTypes.object.def({}),
      spliter: propTypes.string.def(','),
      getFormValues: propTypes.func,
      getContainer: propTypes.func,
      showAdvancedButton: propTypes.bool.def(true),
    },
    emits: ['update:value', 'register', 'change'],
    setup(props, { emit }) {
      const { createMessage } = useMessage();
      const attrs = useAttrs();
      const showText = ref<any>(props.multi ? [] : '');
      const options = ref<any>([]);
      const cgRpConfigId = ref('');
      const loading = ref(false);
      const code = props.dictCode.split(',')[0];
      const labelFiled = props.dictCode.split(',')[1];
      const valueFiled = props.dictCode.split(',')[2];
      const selected = ref([]);
      if (!code || !valueFiled || !labelFiled) {
        createMessage.error('popupDictParameters are not configured correctly!');
      }
      //registermodel
      const [regModal, { openModal }] = useModal();

      /**
       * OpenpopPop-up box
       */
      function handleOpen() {
        // update-begin--author:liaozhiyang---date:20240528---for：【TV360X-317】After disablingJPopupandJPopupdic还可以点击出Pop-up window
        !attrs.value.disabled && openModal(true);
        // update-end--author:liaozhiyang---date:20240528---for：【TV360X-317】After disablingJPopupandJPopupdic还可以点击出Pop-up window
      }
      /**
       * monitorvaluenumerical value
       */
      watch(
        () => props.value,
        (val) => {
          const callBack = () => {
            if (props.multi) {
              showText.value = val && val.length > 0 ? val.split(props.spliter) : [];
            } else {
              showText.value = val ?? '';
            }
          };
          if (props.value || props.defaultValue) {
            if (cgRpConfigId.value) {
              loadData({ callBack });
            } else {
              loadColumnsInfo({ callBack });
            }
          } else {
            callBack();
          }
        },
        { immediate: true }
      );
      watch(
        () => showText.value,
        (val) => {
          let result;
          if (props.multi) {
            result = val.join(',');
          } else {
            result = val;
          }
          nextTick(() => {
            emit('change', result);
            emit('update:value', result);
          });
        }
      );
      /**
       * 加载列information
       */
      function loadColumnsInfo({ callBack }) {
        loading.value = true;
        let url = `${configUrl.getColumns}${code}`;
        defHttp
          .get({ url }, { isTransformResponse: false, successMessageMode: 'none' })
          .then((res) => {
            if (res.success) {
              cgRpConfigId.value = res.result.cgRpConfigId;
              loadData({ callBack });
            }
          })
          .catch((err) => {
            loading.value = false;
            callBack?.();
          });
      }
      function loadData({ callBack }) {
        loading.value = true;
        let url = `${configUrl.getData}${unref(cgRpConfigId)}`;
        defHttp
          .get(
            { url, params: { ['force_' + valueFiled]: props.value || props.defaultValue } },
            { isTransformResponse: false, successMessageMode: 'none' }
          )
          .then((res) => {
            let data = res.result;
            if (data.records?.length) {
              options.value = data.records.map((item) => {
                return { value: item[valueFiled], text: item[labelFiled] };
              });
              selected.value = data.records;
            }
          })
          .finally(() => {
            loading.value = false;
            callBack?.();
          });
      }
      /**
       * Pass value callback
       */
      function callBack(rows) {
        const dataOptions: any = [];
        const dataValue: any = [];
        let result;
        rows.forEach((item) => {
          dataOptions.push({ value: item[valueFiled], text: item[labelFiled] });
          dataValue.push(item[valueFiled]);
        });
        options.value = dataOptions;
        if (props.multi) {
          showText.value = dataValue;
          result = dataValue.join(props.spliter);
        } else {
          showText.value = dataValue[0];
          result = dataValue[0];
        }
        nextTick(() => {
          emit('change', result);
          emit('update:value', result);
        });
      }

      return {
        showText,
        attrs,
        regModal,
        handleOpen,
        callBack,
        code,
        options,
        loading,
        selected,
        valueFiled,
      };
    },
  });
</script>
<style lang="less" scoped>
  // update-begin--author:liaozhiyang---date:20240515---for：【QQYUN-9260】Required mode will affect the pop-up windowantdcomponents的样式
  .JPopupDict {
    > .ant-form-item {
      display: none;
    }
  }
  // update-end--author:liaozhiyang---date:20240515---for：【QQYUN-9260】Required mode will affect the pop-up windowantdcomponents的样式
  .components-input-demo-presuffix {
    :deep(.ant-select-dropdown) {
      display: none !important;
    }
  }
  .components-input-demo-presuffix .anticon-close-circle {
    cursor: pointer;
    color: #ccc;
    transition: color 0.3s;
    font-size: 12px;
  }

  .components-input-demo-presuffix .anticon-close-circle:hover {
    color: #f5222d;
  }

  .components-input-demo-presuffix .anticon-close-circle:active {
    color: #666;
  }
</style>
