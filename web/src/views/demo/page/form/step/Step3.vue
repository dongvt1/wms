<template>
  <div class="step3">
    <a-result status="success" title="Operation successful" sub-title="Expected to arrive within two hours">
      <template #extra>
        <a-button type="primary" @click="redo"> Transfer another amount </a-button>
        <a-button> View bill </a-button>
      </template>
    </a-result>
    <div class="desc-wrap">
      <a-descriptions :column="1" class="mt-5">
        <a-descriptions-item label="payment account"> ant-design@alipay.com </a-descriptions-item>
        <a-descriptions-item label="Collection account"> test@example.com </a-descriptions-item>
        <a-descriptions-item label="Payee name"> Jeecg </a-descriptions-item>
        <a-descriptions-item label="Transfer amount"> 500Yuan </a-descriptions-item>
      </a-descriptions>
    </div>
  </div>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { Result, Descriptions } from 'ant-design-vue';
  export default defineComponent({
    components: {
      [Result.name]: Result,
      [Descriptions.name]: Descriptions,
      [Descriptions.Item.name]: Descriptions.Item,
    },
    emits: ['redo'],
    setup(_, { emit }) {
      return {
        redo: () => {
          emit('redo');
        },
      };
    },
  });
</script>
<style lang="less" scoped>
  .step3 {
    width: 600px;
    margin: 0 auto;
  }

  .desc-wrap {
    padding: 24px 40px;
    margin-top: 24px;
    background-color: @background-color-light;
  }
</style>
