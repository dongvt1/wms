<template>
  <a-spin :spinning="loading">
    <template v-if="treeData && treeData.length > 0">
      <BasicTree ref="basicTree" :treeData="treeData" :checkStrictly="true" style="height: 500px; overflow: auto"></BasicTree>
    </template>
    <a-empty v-else description="No job news" />
  </a-spin>
</template>

<script lang="ts" setup>
  import { computed, ref, watch } from 'vue';
  import { BasicTree } from '/@/components/Tree/index';
  import { getRankRelation } from '../depart.api';

  const props = defineProps({
    data: { type: Object, default: () => ({}) },
  });
  // Currently selected departmentID，may be empty，Represents unselected departments
  const departId = computed(() => props.data?.id);

  const basicTree = ref();
  const loading = ref<boolean>(false);
  //All node information of the tree
  const treeData = ref<any[]>([]);

  watch(departId, (val) => loadData(val), { immediate: true });

  async function loadData(val) {
    try {
      loading.value = true;
      await getRankRelation({ departId: val }).then((res) => {
        if (res.success) {
          treeData.value = res.result;
        }
      });
    } finally {
      loading.value = false;
    }
  }
</script>

<style lang="less" scoped>
  .depart-rule-tree :deep(.scrollbar__bar) {
    pointer-events: none;
  }
</style>
