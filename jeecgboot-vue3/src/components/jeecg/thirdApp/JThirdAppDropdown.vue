<template>
  <a-dropdown v-if="syncToApp && syncToLocal">
    <a-button type="primary" preIcon="ant-design:sync-outlined">synchronous{{ name }}</a-button>
    <template #overlay>
      <a-menu @click="handleMenuClick">
        <a-menu-item v-if="syncToApp" key="to-app">synchronous到{{ name }}</a-menu-item>
        <a-menu-item v-if="getSyncToLocal" key="to-local">synchronousto local</a-menu-item>
      </a-menu>
    </template>
  </a-dropdown>
  <a-button v-else-if="syncToApp" type="primary" preIcon="ant-design:sync-outlined" @click="handleMenuClick({ key: 'to-app' })"
    >synchronous{{ name }}</a-button
  >
  <a-button v-else type="primary" preIcon="ant-design:sync-outlined" @click="handleMenuClick({ key: 'to-local' })">synchronous{{ name }}to local</a-button>
</template>

<script lang="ts" setup>
  /* JThirdAppButton subcomponent of，Not to be used alone */
  import { computed } from 'vue';

  const props = defineProps({
    type: String,
    name: String,
    syncToApp: Boolean,
    syncToLocal: Boolean,
  });
  // statementEmits
  const emit = defineEmits(['to-app', 'to-local']);

  const getSyncToLocal = computed(() => {
    // Due to changes in the corporate WeChat interface，将不再支持synchronousto local
    if (props.type === 'wechatEnterprise') {
      return false;
    }
    return props.syncToLocal;
  });

  function handleMenuClick(event) {
    emit(event.key, { type: props.type });
  }
</script>

<style scoped></style>
