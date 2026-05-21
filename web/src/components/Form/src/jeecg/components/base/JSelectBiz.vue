<template>
  <div>
    <div v-if="isDetailsMode">
      <p class="detailStr" :title="detailStr">{{ detailStr }}</p>
    </div>
    <a-row v-else class="j-select-row" type="flex" :gutter="8">
      <a-col class="left" :class="{ full: !showButton }">
        <!-- Show loading effect -->
        <a-input v-if="loading" readOnly placeholder="loading…">
          <template #prefix>
            <LoadingOutlined />
          </template>
        </a-input>
        <a-select
          v-else
          ref="select"
          v-model:value="selectValues.value"
          :placeholder="placeholder"
          :mode="multiple"
          :open="false"
          :disabled="disabled"
          :options="options"
          :maxTagCount="maxTagCount"
          @change="handleChange"
          style="width: 100%"
          @click="!disabled && openModal(false)"
          v-bind="attrs"
        >
          <template v-if="isCustomRenderTag" #tagRender="{ label, value, option}">
            <a-tag class="ant-select-selection-item">
              <span class="ant-select-selection-item-content" style="font-size: 14px;max-width: 300px" :title="tagRender(label, value, option)">{{ tagRender(label, value, option) }}</span>
              <span class="ant-select-selection-item-remove">
                <Icon icon="ant-design:close-outlined" size="12" @click="handleRemoveClick(value)"></Icon>
              </span>
            </a-tag>
          </template>
        </a-select>
      </a-col>
      <a-col v-if="showButton" class="right">
        <a-button v-if="buttonIcon" :preIcon="buttonIcon" type="primary" @click="openModal(true)" :disabled="disabled">
          {{ buttonText }}
        </a-button>
        <a-button v-else type="primary" @click="openModal(true)" :disabled="disabled">
          {{ buttonText }}
        </a-button>
      </a-col>
    </a-row>
  </div>
</template>
<script lang="ts">
  import { defineComponent, ref, inject, reactive, watch } from 'vue';
  import { propTypes } from '/@/utils/propTypes';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { LoadingOutlined } from '@ant-design/icons-vue';
  import { getDepartPathNameByOrgCode } from "@/utils/common/compUtils";

  export default defineComponent({
    name: 'JSelectBiz',
    components: { LoadingOutlined },
    inheritAttrs: false,
    props: {
      showButton: propTypes.bool.def(true),
      buttonText: propTypes.string.def('choose'),
      disabled: propTypes.bool.def(false),
      placeholder: {
        type: String,
        default: '请choose',
      },
      // Whether to support multiple selection，default true
      multiple: {
        type: String,
        default: 'multiple',
      },
      // Is loading
      loading: propTypes.bool.def(false),
      // How many are displayed at most? tag
      maxTagCount: propTypes.number,
      // buttonIcon
      buttonIcon: propTypes.string.def(''),
      // 【TV360X-1002】Is it in detail mode?
      isDetailsMode: propTypes.bool.def(false),
      //update-begin---author:wangshuai---date:2025-09-06---for: Whether to customize rendering during multi-selectiontagtext，Do not render if empty，Radio selection is not supported---
      //Whether to customize renderingtag
      isCustomRenderTag: propTypes.bool.def(false),
      rowKey: propTypes.string.def('id'),
      //update-end---author:wangshuai---date:2025-09-06---for:Whether to customize rendering during multi-selectiontagtext，Do not render if empty，Radio selection is not supported---
    },
    emits: ['handleOpen', 'change'],
    setup(props, { emit, refs }) {
      //Receive drop down box options
      const options = inject('selectOptions') || ref([]);
      //接收choose的值
      const selectValues = inject('selectValues') || ref({});
      const attrs = useAttrs();
      const detailStr = ref('');

      //Storage department name
      const departNamePath = ref<Record<string, string>>({});
      
      /**
       * Open popup
       */
      function openModal(isButton) {
        if (props.showButton && isButton) {
          emit('handleOpen');
        }
        if (!props.showButton && !isButton) {
          emit('handleOpen');
        }
      }

      /**
       * Drop-down box value change event
       */
      function handleChange(value) {
        selectValues.value = value;
        selectValues.change = true;
        emit('change', value);
      }
      
      /**
       * Multiple choicetagcustom rendering
       *
       * @param label
       * @param value
       * @param isEllipsis Whether to omit
       */
      function tagRender(label, value, isEllipsis) {
        if (departNamePath.value[value]) {
           //Do you need to omit the display?
           if(!isEllipsis){
             return departNamePath.value[value];
           } else {
             let departName = departNamePath.value[value];
             //Exceed20After interception20bit characters，Add an ellipsis before
             if(departName && departName.length >= 20){
               const name:any = departName.substring(departName.length - 20);
               return '...' + name;
             } else {
               return departName;
             }
           }
        }
        //judgerowKeyIs itorgCode
        if(props?.rowKey && props?.rowKey === 'orgCode'){
          getDepartPathNameByOrgCode(value, label, '').then((data) => {
            departNamePath.value[value] = data;
          });
        } else {
          //Otherwise followiddeal with
          getDepartPathNameByOrgCode('', label, value).then((data) => {
            departNamePath.value[value] = data;
          });
        }
      }

      /**
       * tagdelete
       * 
       * @param value
       */
      function handleRemoveClick(value) {
        if(selectValues?.value){
          let values = selectValues?.value.filter(item => item !== value);
          handleChange(values);
        }
      }
      // -update-begin--author:liaozhiyang---date:20240617---for：【TV360X-1002】The row editing user component and department component display mode of the details page is optimized.
      watch(
        [selectValues, options],
        () => {
          if (props.isDetailsMode) {
            if (Array.isArray(selectValues.value) && Array.isArray(options.value)) {
              const result = options.value.map((item) => item.label);
              detailStr.value = result.join(',');
            }
          }
        },
        { immediate: true }
      );
      // -update-end--author:liaozhiyang---date:20240617---for：【TV360X-1002】The row editing user component and department component display mode of the details page is optimized.

      return {
        attrs,
        selectValues,
        options,
        handleChange,
        openModal,
        detailStr,
        tagRender,
        handleRemoveClick,
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
  .detailStr {
    margin: 0;
    max-width: 100%;
    overflow: hidden;
    text-overflow: ellipsis;
  }
</style>
