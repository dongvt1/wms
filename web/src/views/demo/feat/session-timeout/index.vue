<template>
  <PageWrapper title="Login expiration example" content="用户Login expiration example，No more jumping to the login page，Directly generate a page to overwrite the current page，Convenient to maintain the user status before expiration！">
    <a-card title="Please click the button below to access the test interface" extra="The accessed interface will returnTokenexpired response">
      <a-card-grid style="width: 50%; text-align: center">
        <a-button type="primary" @click="test1">HttpStatus == 401</a-button>
      </a-card-grid>
      <a-card-grid style="width: 50%; text-align: center">
        <span></span>
        <a-button class="ml-4" type="primary" @click="test2">Response.code == 401</a-button>
      </a-card-grid>
    </a-card>
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { PageWrapper } from '/@/components/Page';
  import { useUserStore } from '/@/store/modules/user';

  import { sessionTimeoutApi, tokenExpiredApi } from '/@/api/demo/account';
  import { Card } from 'ant-design-vue';

  export default defineComponent({
    name: 'TestSessionTimeout',
    components: { ACardGrid: Card.Grid, ACard: Card, PageWrapper },
    setup() {
      const userStore = useUserStore();
      async function test1() {
        // The sample website production environment usesmockdata，Cannot returnHttpstatus code，
        // Therefore, directly change the state in the production environment to achieve the test effect.
        if (import.meta.env.PROD) {
          userStore.setToken(undefined);
          userStore.setSessionTimeout(true);
        } else {
          // thisapiwill returnstatus codefor401response
          await sessionTimeoutApi();
        }
      }

      async function test2() {
        // thisapiwill returncodefor401ofjsondata，Httpstatus codefor200
        try {
          await tokenExpiredApi();
        } catch (err) {
          console.log('Interface access error：', (err as Error).message || 'mistake');
        }
      }

      return { test1, test2 };
    },
  });
</script>
