<template>
  <div class="mt-2">
    Current permission mode：
    <a-button type="link">
      {{ permissionMode === PermissionModeEnum.BACK ? 'Background permission mode' : 'Front-end role permission model' }}
    </a-button>
    <a-button class="ml-4" @click="togglePermissionMode" type="primary"> Switch permission mode </a-button>
    <Divider />
  </div>
</template>
<script lang="ts">
  import { defineComponent, computed } from 'vue';
  import { useAppStore } from '/@/store/modules/app';
  import { PermissionModeEnum } from '/@/enums/appEnum';
  import { Divider } from 'ant-design-vue';
  import { usePermission } from '/@/hooks/web/usePermission';
  export default defineComponent({
    name: 'CurrentPermissionMode',
    components: { Divider },
    setup() {
      const appStore = useAppStore();
      const permissionMode = computed(() => appStore.getProjectConfig.permissionMode);
      const { togglePermissionMode } = usePermission();

      return {
        permissionMode,
        PermissionModeEnum,
        togglePermissionMode,
      };
    },
  });
</script>
