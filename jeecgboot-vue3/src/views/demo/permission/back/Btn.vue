<template>
  <PageWrapper contentBackground title="Button permission control" contentClass="p-4">
    <CurrentPermissionMode />
    <p>
      currently ownedcodelist: <a> {{ permissionStore.getPermCodeList }} </a>
    </p>
    <Divider />
    <Alert class="mt-4" type="info" message="Please check the button changes after clicking it(You must be in background permission mode to test the functions displayed on this page)" show-icon />
    <Divider />
    <a-button type="primary" class="mr-2" @click="switchToken(2)" :disabled="!isBackPremissionMode"> Click the toggle button permissions(useridfor2) </a-button>
    <a-button type="primary" @click="switchToken(1)" :disabled="!isBackPremissionMode"> Click the toggle button permissions(useridfor1,default) </a-button>

    <template v-if="isBackPremissionMode">
      <Divider>Component method to determine permissions</Divider>
      <Authority :value="'1000'">
        <a-button type="primary" class="mx-4"> havecode ['1000']Permissions are visible </a-button>
      </Authority>

      <Authority :value="'2000'">
        <a-button color="success" class="mx-4"> havecode ['2000']Permissions are visible </a-button>
      </Authority>

      <Authority :value="['1000', '2000']">
        <a-button color="error" class="mx-4"> havecode ['1000','2000']角色Permissions are visible </a-button>
      </Authority>

      <Divider>Function method to determine permissions</Divider>
      <a-button v-if="hasPermission('1000')" type="primary" class="mx-4"> havecode ['1000']Permissions are visible </a-button>

      <a-button v-if="hasPermission('2000')" color="success" class="mx-4"> havecode ['2000']Permissions are visible </a-button>

      <a-button v-if="hasPermission(['1000', '2000'])" color="error" class="mx-4"> havecode ['1000','2000']角色Permissions are visible </a-button>

      <Divider>Command mode method to determine permissions(This method cannot dynamically modify permissions.)</Divider>
      <a-button v-auth="'1000'" type="primary" class="mx-4"> havecode ['1000']Permissions are visible </a-button>

      <a-button v-auth="'2000'" color="success" class="mx-4"> havecode ['2000']Permissions are visible </a-button>

      <a-button v-auth="['1000', '2000']" color="error" class="mx-4"> havecode ['1000','2000']角色Permissions are visible </a-button>
    </template>
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent, computed } from 'vue';
  import { Alert, Divider } from 'ant-design-vue';
  import CurrentPermissionMode from '../CurrentPermissionMode.vue';
  import { usePermission } from '/@/hooks/web/usePermission';
  import { Authority } from '/@/components/Authority';
  import { usePermissionStore } from '/@/store/modules/permission';
  import { PermissionModeEnum } from '/@/enums/appEnum';
  import { PageWrapper } from '/@/components/Page';
  import { useAppStore } from '/@/store/modules/app';
  import { useUserStore } from '/@/store/modules/user';

  export default defineComponent({
    components: { Alert, PageWrapper, CurrentPermissionMode, Divider, Authority },
    setup() {
      const { hasPermission } = usePermission();
      const permissionStore = usePermissionStore();
      const appStore = useAppStore();
      const userStore = useUserStore();

      const isBackPremissionMode = computed(() => appStore.getProjectConfig.permissionMode === PermissionModeEnum.BACK);

      async function switchToken(userId: number) {
        // 本函数切换user登录TokenThe parts are for demonstration only，If you switch identities during actual production, you should log in again.
        const token = 'fakeToken' + userId;
        userStore.setToken(token);

        // 重新获取user信息和菜单
        userStore.getUserInfoAction();
        permissionStore.changePermissionCode();
      }

      return {
        hasPermission,
        permissionStore,
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
