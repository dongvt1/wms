<template>
  <PageWrapper
    title="Front-end permission button example"
    contentBackground
    contentClass="p-4"
    content="Because the user information interface will be requested when refreshing，Role information will be reset based on the interface，So after refreshing, the interface will return to its original state.，if not needed，Can comment src/layout/default/indexObtain user information interface within"
  >
    <CurrentPermissionMode />

    <p>
      current role: <a> {{ userStore.getRoleList }} </a>
    </p>
    <Alert class="mt-4" type="info" message="Please check the button changes after clicking it" show-icon />

    <div class="mt-4">
      Permission switching(Please switch the permission mode to front-end role permission mode first.):
      <a-button-group>
        <a-button @click="changeRole(RoleEnum.SUPER)" :type="isSuper ? 'primary' : 'default'">
          {{ RoleEnum.SUPER }}
        </a-button>
        <a-button @click="changeRole(RoleEnum.TEST)" :type="isTest ? 'primary' : 'default'">
          {{ RoleEnum.TEST }}
        </a-button>
      </a-button-group>
    </div>
    <Divider>Component method to determine permissions(If necessary, you can register globally by yourself)</Divider>
    <Authority :value="RoleEnum.SUPER">
      <a-button type="primary" class="mx-4"> havesuperRole permissions are visible </a-button>
    </Authority>

    <Authority :value="RoleEnum.TEST">
      <a-button color="success" class="mx-4"> havetestRole permissions are visible </a-button>
    </Authority>

    <Authority :value="[RoleEnum.TEST, RoleEnum.SUPER]">
      <a-button color="error" class="mx-4"> have[test,super]Role permissions are visible </a-button>
    </Authority>

    <Divider>Function method to determine permissions(Applies to filtering inside functions)</Divider>
    <a-button v-if="hasPermission(RoleEnum.SUPER)" type="primary" class="mx-4"> havesuperRole permissions are visible </a-button>

    <a-button v-if="hasPermission(RoleEnum.TEST)" color="success" class="mx-4"> havetestRole permissions are visible </a-button>

    <a-button v-if="hasPermission([RoleEnum.TEST, RoleEnum.SUPER])" color="error" class="mx-4"> have[test,super]Role permissions are visible </a-button>

    <Divider>Command mode method to determine permissions(This method cannot dynamically modify permissions.)</Divider>
    <a-button v-auth="RoleEnum.SUPER" type="primary" class="mx-4"> havesuperRole permissions are visible </a-button>

    <a-button v-auth="RoleEnum.TEST" color="success" class="mx-4"> havetestRole permissions are visible </a-button>

    <a-button v-auth="[RoleEnum.TEST, RoleEnum.SUPER]" color="error" class="mx-4"> have[test,super]Role permissions are visible </a-button>
  </PageWrapper>
</template>
<script lang="ts">
  import { computed, defineComponent } from 'vue';
  import { Alert, Divider } from 'ant-design-vue';
  import CurrentPermissionMode from '../CurrentPermissionMode.vue';
  import { useUserStore } from '/@/store/modules/user';
  import { RoleEnum } from '/@/enums/roleEnum';
  import { usePermission } from '/@/hooks/web/usePermission';
  import { Authority } from '/@/components/Authority';
  import { PageWrapper } from '/@/components/Page';

  export default defineComponent({
    components: { Alert, PageWrapper, CurrentPermissionMode, Divider, Authority },
    setup() {
      const { changeRole, hasPermission } = usePermission();
      const userStore = useUserStore();

      return {
        userStore,
        RoleEnum,
        isSuper: computed(() => userStore.getRoleList.includes(RoleEnum.SUPER)),
        isTest: computed(() => userStore.getRoleList.includes(RoleEnum.TEST)),
        changeRole,
        hasPermission,
      };
    },
  });
</script>
<style lang="less" scoped>
  .demo {
    background-color: @component-background;
  }
</style>
