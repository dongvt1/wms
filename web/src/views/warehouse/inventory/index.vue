<template>
  <div>
    <InventoryList />
  </div>
</template>

<script lang="ts" setup>
  import InventoryList from './InventoryList.vue';
</script>

<script lang="ts">
export default {
  name: 'WarehouseInventory',
  components: {
    InventoryList,
  },
};
</script>