<!--popupcomponents-->
<template>
  <div class="JPopup components-input-demo-presuffix" v-if="avalid">
    <!--Input box-->
    <a-input @click="handleOpen" :value="innerShowText || showText" :placeholder="placeholder" readOnly v-bind="attrs">
      <template #prefix>
        <Icon icon="ant-design:cluster-outlined"></Icon>
      </template>
      <!-- update-begin-author:taoyan date:2022-5-31 for: VUEN-1157 popup After selecting，There are two clear icons；Clear this later one，只是把Input box中数据清除，Actual value is not cleared -->
      <!-- <template #suffix>
                <Icon icon="ant-design:close-circle-outlined" @click="handleEmpty" title="Clear" v-if="showText"></Icon>
            </template>-->
      <!-- update-begin-author:taoyan date:2022-5-31 for: VUEN-1157 popup After selecting，There are two clear icons；Clear this later one，只是把Input box中数据清除，Actual value is not cleared -->
    </a-input>
    <!-- update-begin--author:liaozhiyang---date:20240515---for：【QQYUN-9260】Required mode will affect the pop-up windowantdcomponents的样式 -->
    <a-form-item>
      <!--popupPop-up window-->
      <JPopupOnlReportModal
        @register="regModal"
        :code="code"
        :multi="multi"
        :sorter="sorter"
        :groupId="uniqGroupId"
        :param="param"
        :showAdvancedButton="showAdvancedButton"
        :getContainer="getContainer"
        :getFormValues="getFormValues"
        @ok="callBack"
      ></JPopupOnlReportModal>
    </a-form-item>
    <!-- update-end--author:liaozhiyang---date:20240515---for：【QQYUN-9260】Required mode will affect the pop-up windowantdcomponents的样式 -->
  </div>
</template>
<script lang="ts">
  import JPopupOnlReportModal from './modal/JPopupOnlReportModal.vue';
  import { defineComponent, ref, reactive, onMounted, watchEffect, watch, computed, unref } from 'vue';
  import { useModal } from '/@/components/Modal';
  import { propTypes } from '/@/utils/propTypes';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { useMessage } from '/@/hooks/web/useMessage';

  export default defineComponent({
    name: 'JPopup',
    components: {
      JPopupOnlReportModal,
    },
    inheritAttrs: false,
    props: {
      code: propTypes.string.def(''),
      value: propTypes.string.def(''),
      sorter: propTypes.string.def(''),
      width: propTypes.number.def(1200),
      placeholder: propTypes.string.def('Please select'),
      multi: propTypes.bool.def(false),
      param: propTypes.object.def({}),
      spliter: propTypes.string.def(','),
      groupId: propTypes.string.def(''),
      formElRef: propTypes.object,
      setFieldsValue: propTypes.func,
      getFormValues: propTypes.func,
      getContainer: propTypes.func,
      fieldConfig: {
        type: Array,
        default: () => [],
      },
      showAdvancedButton: propTypes.bool.def(true),
      // Is it in filter（search） used in
      inSearch: propTypes.bool.def(false),
    },
    emits: ['update:value', 'register', 'popUpChange', 'focus'],
    setup(props, { emit, refs }) {
      const { createMessage } = useMessage();
      const attrs = useAttrs();
      //popWhether to display
      const avalid = ref(true);
      const showText = ref('');
      const innerShowText = ref('')
      //registermodel
      const [regModal, { openModal }] = useModal();
      //form value
      let {code, fieldConfig } = props;
      // update-begin--author:liaozhiyang---date:20230811---for：【issues/675】Subtable fieldsPopupPop-up data is not updated
      //unique groupgroupId
      const uniqGroupId = computed(() => (props.groupId ? `${props.groupId}_${code}_${fieldConfig[0]['source']}_${fieldConfig[0]['target']}` : ''));
      // update-begin--author:liaozhiyang---date:20230811---for：【issues/675】Subtable fieldsPopupPop-up data is not updated
      /**
       * judgepopupAre the configuration items correct?
       */
      onMounted(() => {
        if (props.fieldConfig.length == 0) {
          createMessage.error('popupParameters are not configured correctly!');
          avalid.value = false;
        }
      });
      /**
       * monitorvaluenumerical value
       */
      watch(
        () => props.value,
        (val) => {
          showText.value = val && val.length > 0 ? val.split(props.spliter).join(',') : '';
        },
        { immediate: true }
      );

      /**
       * OpenpopPop-up box
       */
      function handleOpen() {
        emit('focus');
        // update-begin--author:liaozhiyang---date:20240528---for：【TV360X-317】After disablingJPopupandJPopupdic还可以点击出Pop-up window
        !attrs.value.disabled && openModal(true);
        // update-end--author:liaozhiyang---date:20240528---for：【TV360X-317】After disablingJPopupandJPopupdic还可以点击出Pop-up window
      }

      /**
       * TODO Clear
       */
      function handleEmpty() {
        showText.value = '';
      }

      /**
       * Pass value callback
       */
      function callBack(rows) {
        let { fieldConfig } = props;
        //matchpopupset callback value
        let values = {};
        let labels = []
        for (let item of fieldConfig) {
          let val = rows.map((row) => row[item.source]);
          // update-begin--author:liaozhiyang---date:20230831---for：【QQYUN-7535】The array has only one and isnumbertype，join会改变值的type为string
          val = val.length == 1 ? val[0] : val.join(',');
          // update-begin--author:liaozhiyang---date:20230831---for：【QQYUN-7535】The array has only one and isnumbertype，join会改变值的type为string
          item.target.split(',').forEach((target) => {
            values[target] = val;
          });

          if (props.inSearch) {
            // Process display values
            if (item.label) {
              let txt = rows.map((row) => row[item.label]);
              txt = txt.length == 1 ? txt[0] : txt.join(',');
              labels.push(txt);
            } else {
              labels.push(val);
            }
          }

        }
        innerShowText.value = labels.join(',');
        //Incoming form example method assignment
        props.formElRef && props.formElRef.setFieldsValue(values);
        //Pass in assignment method method assignment
        props.setFieldsValue && props.setFieldsValue(values);
        // update-begin--author:liaozhiyang---date:20230831---for：【issues/5288】popupPop-up box，Unable to populate selected data to itself
        // update-begin--author:liaozhiyang---date:20230811---for：【issues/5213】JPopupThrowchangeevent
        emit('popUpChange', values);
        // update-end--author:liaozhiyang---date:20230811---for：【issues/5213】JPopupThrowchangeevent
        // update-begin--author:liaozhiyang---date:20230831---for：【issues/5288】popupPop-up box，Unable to populate selected data to itself
      }

      return {
        showText,
        innerShowText,
        avalid,
        uniqGroupId,
        attrs,
        regModal,
        handleOpen,
        handleEmpty,
        callBack,
      };
    },
  });
</script>
<style lang="less" scoped>
  // update-begin--author:liaozhiyang---date:20240515---for：【QQYUN-9260】Required mode will affect the pop-up windowantdcomponents的样式
  .JPopup {
    > .ant-form-item {
      display: none;
    }
  }
  // update-end--author:liaozhiyang---date:20240515---for：【QQYUN-9260】Required mode will affect the pop-up windowantdcomponents的样式
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
