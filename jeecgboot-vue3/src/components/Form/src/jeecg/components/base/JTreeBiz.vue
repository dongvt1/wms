<template>
  <div>
    <a-row class="j-select-row" type="flex" :gutter="8">
      <a-col class="left" :class="{ full: !showButton }">
        <a-select
          ref="select"
          v-model:value="selectValues.value"
          :mode="multiple"
          :open="false"
          :options="options"
          @change="handleChange"
          style="width: 100%"
        />
      </a-col>
      <a-col v-if="showButton" class="right">
        <a-button type="primary" @click="openModal">choose</a-button>
      </a-col>
    </a-row>
  </div>
</template>
<script lang="ts">
  import { defineComponent, ref, inject, reactive } from 'vue';
  import { propTypes } from '/@/utils/propTypes';
  import { useAttrs } from '/@/hooks/core/useAttrs';

  export default defineComponent({
    name: 'JSelectBiz',
    components: {},
    inheritAttrs: false,
    props: {
      showButton: propTypes.bool.def(true),
      // Whether to support multiple selection，default true
      multiple: {
        type: Boolean,
        default: 'multiple',
      },
    },
    emits: ['btnOk'],
    setup(props, { emit, refs }) {
      //Receive drop down box options
      const options = inject('selectOptions') || ref([]);
      //接收choose的值
      const selectValues = inject('selectValues') || ref({});
      const attrs = useAttrs();

      /**
       * Open popup
       */
      function openModal() {
        emit('btnOk');
      }

      /**
       * Drop-down box value change event
       */
      function handleChange(value) {
        selectValues.value = value;
        selectValues.change = true;
      }

      return {
        attrs,
        selectValues,
        options,
        handleChange,
        openModal,
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
