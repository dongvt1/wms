<template>
  <div class="m-5 result-error">
    <Result status="error" title="Submission failed" sub-title="Please check and modify the following information，Submit again。">
      <template #extra>
        <a-button key="console" type="primary"> Return to modification </a-button>
      </template>
    </Result>
    <div class="result-error__content">
      <div class="result-error__content-title"> The content you submitted contains the following errors： </div>
      <div class="mb-4">
        <CloseCircleOutlined class="mr-2 result-error__content-icon" />
        Your account has been frozen
        <a class="ml-4">Thaw immediately ></a>
      </div>
      <div class="mb-4">
        <CloseCircleOutlined class="mr-2 result-error__content-icon" />
        Your account is not yet eligible to apply
        <a class="ml-4">Thaw immediately ></a>
      </div>
    </div>
  </div>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { Result } from 'ant-design-vue';
  import { CloseCircleOutlined } from '@ant-design/icons-vue';
  export default defineComponent({
    components: {
      Result,
      CloseCircleOutlined,
    },
  });
</script>
<style lang="less" scoped>
  .result-error {
    padding: 48px 32px;
    background-color: @component-background;

    &__content {
      padding: 24px 40px;
      background-color: @background-color-light;

      &-title {
        margin-bottom: 16px;
        font-size: 16px;
        font-weight: 500;
      }

      &-icon {
        color: #ff4d4f;
      }
    }
  }
</style>
