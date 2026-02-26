<template>
  <a-card :bordered="false" style="height: 100%;" :body-style="{ background: backgroundColor }" >
    <a-spin :spinning="loading">
      <a-input-search v-if="showSearch" placeholder="Search by department name…" style="margin-bottom: 10px" @search="onSearch" allowClear />
      <!--Organization tree-->
      <template v-if="treeData.length > 0">
        <a-tree
          v-if="!treeReloading"
          :style="{ background: backgroundColor }"
          showLine
          :clickRowToExpand="false"
          :treeData="treeData"
          :selectedKeys="selectedKeys"
          :load-data="loadChildrenTreeData"
          v-model:expandedKeys="expandedKeys"
          @select="onSelect"
        >
          <template #title="{ orgCategory, title }">
            <TreeIcon :orgCategory="orgCategory" :title="title"></TreeIcon>
          </template>
        </a-tree>
      </template>
      <a-empty v-else description="No data available" />
    </a-spin>
  </a-card>
</template>

<script lang="ts" setup>
  import { inject, nextTick, ref, unref } from 'vue';
  import { queryDepartTreeSync } from '../address.api';
  import { searchByKeywords } from '/@/views/system/departUser/depart.user.api';
  import { Popconfirm } from 'ant-design-vue';
  import TreeIcon from "@/components/Form/src/jeecg/components/TreeIcon/TreeIcon.vue";

  const prefixCls = inject('prefixCls');
  // Define props
  const props = defineProps({
    // Whether to show search box
    showSearch: {
      type: Boolean,
      default: true,
    },
    // Background color
    backgroundColor: {
      type: String,
      default: 'inherit',
    },
  });
  const emit = defineEmits(['select', 'rootTreeData']);

  const loading = ref<boolean>(false);
  // Department tree list data
  const treeData = ref<any[]>([]);
  // Currently expanded items
  const expandedKeys = ref<any[]>([]);
  // Currently selected items
  const selectedKeys = ref<any[]>([]);
  // Tree component reload
  const treeReloading = ref<boolean>(false);
  // Currently selected department
  const currentDepart = ref<any>(null);
  // Search keyword
  const searchKeyword = ref('');

  // Load top-level department information
  async function loadRootTreeData() {
    try {
      loading.value = true;
      treeData.value = [];
      const result = await queryDepartTreeSync();
      if (Array.isArray(result)) {
        treeData.value = result;
      }
      if (expandedKeys.value.length === 0) {
        autoExpandParentNode();
      }
    } finally {
      loading.value = false;
    }
  }

  loadRootTreeData();

  // Load child-level department information
  async function loadChildrenTreeData(treeNode) {
    try {
      const result = await queryDepartTreeSync({
        pid: treeNode.dataRef.id,
      });
      if (result.length == 0) {
        treeNode.dataRef.isLeaf = true;
      } else {
        treeNode.dataRef.children = result;
        if (expandedKeys.value.length > 0) {
          // Check if the obtained child level has currently expanded items
          let subKeys: any[] = [];
          for (let key of expandedKeys.value) {
            if (result.findIndex((item) => item.id === key) !== -1) {
              subKeys.push(key);
            }
          }
          if (subKeys.length > 0) {
            expandedKeys.value = [...expandedKeys.value];
          }
        }
      }
      treeData.value = [...treeData.value];
    } catch (e) {
      console.error(e);
    }
    return Promise.resolve();
  }

  // Auto-expand parent node, only expand first level
  function autoExpandParentNode() {
    let item = treeData.value[0];
    if (item) {
      if (!item.isLeaf) {
        expandedKeys.value = [item.key];
      }
      reloadTree();
    }
  }

  // Reload tree component to prevent default expansion data
  async function reloadTree() {
    await nextTick();
    treeReloading.value = true;
    await nextTick();
    treeReloading.value = false;
  }

  /**
   * Set currently selected row
   */
  function setSelectedKey(key: string, data?: object) {
    selectedKeys.value = [key];
    if (data) {
      currentDepart.value = data;
      emit('select', data);
    }
  }

  // Search event
  async function onSearch(value: string) {
    if (value) {
      try {
        loading.value = true;
        treeData.value = [];
        let result = await searchByKeywords({ keyWord: value, orgCategory: '1,2,3,4' });
        if (Array.isArray(result)) {
          treeData.value = result;
        }
        autoExpandParentNode();
      } finally {
        loading.value = false;
      }
    } else {
      loadRootTreeData();
    }
    searchKeyword.value = value;
  }

  // Tree selection event
  function onSelect(selKeys, event) {
    if (selKeys.length > 0 && selectedKeys.value[0] !== selKeys[0]) {
      setSelectedKey(selKeys[0], event.selectedNodes[0]);
    } else {
      // This can prevent users from canceling selection
      setSelectedKey(selectedKeys.value[0]);
    }
  }

  defineExpose({
    loadRootTreeData,
  });
</script>
