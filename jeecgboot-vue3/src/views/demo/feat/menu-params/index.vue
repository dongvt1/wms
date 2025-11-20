<template>
  <PageWrapper title="With parameter menu（routing）" content="Support multi-level parameters">
    Current parameters：{{ params }}
    <br />
    输入参数切换routing：
    <Input v-model:value="value" placeholder="It is recommended tourlStandard characters，After entering, click Switch" />
    <a-button type="primary" @click="handleClickGo">切换routing</a-button>
    <br />
    切换routing后
    <ul>
      <li>可刷新页面测试routing参数情况是否正常。</li>
      <li>Submenus can be expanded in the left menu，Click to test whether the parameters are carried normally。</li>
    </ul>
  </PageWrapper>
</template>
<script lang="ts">
  import { Input } from 'ant-design-vue';
  import { computed, defineComponent, ref, unref } from 'vue';
  import { useRouter } from 'vue-router';
  import { PageWrapper } from '/@/components/Page';

  export default defineComponent({
    name: 'TestMenu',
    components: { PageWrapper, Input },
    setup() {
      const { currentRoute, replace } = useRouter();
      const value = ref<string>('');

      const handleClickGo = () => {
        const { name } = unref(currentRoute);
        replace({ name: name!, params: { id: unref(value) } });
      };
      return {
        value,
        handleClickGo,
        params: computed(() => {
          return unref(currentRoute).params;
        }),
      };
    },
  });
</script>
