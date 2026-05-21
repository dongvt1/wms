<template>
  <PageWrapper
    title="Background permission example"
    contentBackground
    contentClass="p-4"
    content="at presentmocktwo sets of data， idfor1 and 2 The specific return menu can be found inmock/sys/menu.tsView within"
  >
    <CurrentPermissionMode />

    <Alert class="mt-4" type="info" message="Please check the left menu changes after clicking" show-icon />

    <div class="mt-4">
      Permission switching(请先切换权限模式for后台权限模式):
      <a-button-group>
        <a-button @click="switchToken(1)" :disabled="!isBackPremissionMode"> Get useridfor1menu </a-button>
        <a-button @click="switchToken(2)" :disabled="!isBackPremissionMode"> Get useridfor2menu </a-button>
      </a-button-group>
    </div>
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent, computed } from 'vue';
  import CurrentPermissionMode from '../CurrentPermissionMode.vue';
  import { RoleEnum } from '/@/enums/roleEnum';
  import { usePermission } from '/@/hooks/web/usePermission';
  import { useUserStore } from '/@/store/modules/user';
  import { PageWrapper } from '/@/components/Page';
  import { PermissionModeEnum } from '/@/enums/appEnum';
  import { useAppStore } from '/@/store/modules/app';
  import { Alert } from 'ant-design-vue';
  export default defineComponent({
    components: { Alert, CurrentPermissionMode, PageWrapper },
    setup() {
      const { refreshMenu } = usePermission();
      const userStore = useUserStore();
      const appStore = useAppStore();

      const isBackPremissionMode = computed(() => appStore.getProjectConfig.permissionMode === PermissionModeEnum.BACK);

      async function switchToken(userId: number) {
        // This function switches user loginTokenThe parts are for demonstration only，If you switch identities during actual production, you should log in again.
        const token = 'fakeToken' + userId;
        userStore.setToken(token);

        // 重新Get user信息and菜单
        userStore.getUserInfoAction();
        refreshMenu();
      }

      return {
        RoleEnum,
        refreshMenu,
        switchToken,
        isBackPremissionMode,
      };
    },
  });
</script>
<style lang="less" scoped>
  .demo {
    background-color: @component-background;
  }
</style>
