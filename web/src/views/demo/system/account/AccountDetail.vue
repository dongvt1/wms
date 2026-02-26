<template>
  <PageWrapper
    :title="`user` + userId + `information`"
    content="这是usermaterialDetails页面。This page is only used to demonstrate the same route intabOpen multiple pages and display different data"
    contentBackground
    @back="goBack"
  >
    <template #extra>
      <a-button type="primary" danger> Disable account </a-button>
      <a-button type="primary"> Change password </a-button>
    </template>
    <template #footer>
      <a-tabs default-active-key="detail" v-model:activeKey="currentKey">
        <a-tab-pane key="detail" tab="usermaterial" />
        <a-tab-pane key="logs" tab="Operation log" />
      </a-tabs>
    </template>
    <div class="pt-4 m-4 desc-wrap">
      <template v-if="currentKey == 'detail'">
        <div v-for="i in 10" :key="i">这是user{{ userId }}materialTab</div>
      </template>
      <template v-if="currentKey == 'logs'">
        <div v-for="i in 10" :key="i">这是user{{ userId }}Operation logTab</div>
      </template>
    </div>
  </PageWrapper>
</template>

<script>
  import { defineComponent, ref } from 'vue';
  import { useRoute } from 'vue-router';
  import { PageWrapper } from '/@/components/Page';
  import { useGo } from '/@/hooks/web/usePage';
  import { useTabs } from '/@/hooks/web/useTabs';
  import { Tabs } from 'ant-design-vue';
  export default defineComponent({
    name: 'AccountDetail',
    components: { PageWrapper, ATabs: Tabs, ATabPane: Tabs.TabPane },
    setup() {
      const route = useRoute();
      const go = useGo();
      // 此处可以得到userID
      const userId = ref(route.params?.id);
      const currentKey = ref('detail');
      const { setTitle } = useTabs();
      // TODO
      // The code on this page is for demonstration only，should actually passuserId从接口获得user的相关material

      // set upTabtitle（Does not affect page title）
      setTitle('Details：user' + userId.value);

      // What to do when you click the return link on the left side of the page
      function goBack() {
        // The effect of this example is that when you click Return, you will always jump to the account list page.，You can return to the previous page during actual application
        go('/system/account');
      }
      return { userId, currentKey, goBack };
    },
  });
</script>

<style></style>
