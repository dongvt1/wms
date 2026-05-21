<template>
  <a-spin :spinning="loading">
    <template v-if="treeData.length > 0">
      <BasicTree
        ref="basicTree"
        class="depart-rule-tree"
        checkable
        :treeData="treeData"
        :checkedKeys="checkedKeys"
        :selectedKeys="selectedKeys"
        :expandedKeys="expandedKeys"
        :checkStrictly="true"
        style="height: 500px; overflow: auto"
        @check="onCheck"
        @expand="onExpand"
        @select="onSelect"
      >
        <template #title="{ slotTitle, ruleFlag }">
          <span>{{ slotTitle }}</span>
          <Icon v-if="ruleFlag" icon="ant-design:align-left-outlined" style="margin-left: 5px; color: red" />
        </template>
      </BasicTree>
    </template>
    <a-empty v-else description="No department permissions can be configured" />

    <div class="j-box-bottom-button offset-20" style="margin-top: 30px">
      <div class="j-box-bottom-button-float" :class="[`${prefixCls}`]">
        <a-dropdown :trigger="['click']" placement="top">
          <template #overlay>
            <a-menu>
              <a-menu-item key="3" @click="toggleCheckALL(true)">{{ t('component.tree.selectAll') }}</a-menu-item>
              <a-menu-item key="4" @click="toggleCheckALL(false)">{{ t('component.tree.unSelectAll') }}</a-menu-item>
              <a-menu-item key="5" @click="toggleExpandAll(true)">{{ t('component.tree.expandAll') }}</a-menu-item>
              <a-menu-item key="6" @click="toggleExpandAll(false)">{{ t('component.tree.unExpandAll') }}</a-menu-item>
              <a-menu-item key="7" @click="toggleRelationAll(false)">{{ t('component.tree.checkStrictly') }}</a-menu-item>
              <a-menu-item key="8" @click="toggleRelationAll(true)">{{ t('component.tree.checkUnStrictly') }}</a-menu-item>
            </a-menu>
          </template>
          <a-button style="float: left">
            tree operations
            <Icon icon="ant-design:up-outlined" />
          </a-button>
        </a-dropdown>
        <a-button type="primary" preIcon="ant-design:save-filled" @click="onSubmit">save</a-button>
      </div>
    </div>
  </a-spin>
  <DepartDataRuleDrawer @register="registerDataRuleDrawer" />
</template>

<script lang="ts" setup>
  import { watch, computed, inject, ref, nextTick } from 'vue';
  import { useDrawer } from '/@/components/Drawer';
  import { BasicTree } from '/@/components/Tree/index';
  import DepartDataRuleDrawer from './DepartDataRuleDrawer.vue';
  import { queryRoleTreeList, queryDepartPermission, saveDepartPermission } from '../depart.api';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { translateTitle } from '/@/utils/common/compUtils';
  import { DEPART_MANGE_AUTH_CONFIG_KEY } from '/@/enums/cacheEnum';
  import { useI18n } from '/@/hooks/web/useI18n';

  const { prefixCls } = useDesign('j-depart-form-content');
  const props = defineProps({
    data: { type: Object, default: () => ({}) },
  });
  // Currently selected departmentID，may be empty，Represents unselected departments
  const departId = computed(() => props.data?.id);

  const basicTree = ref();
  const loading = ref<boolean>(false);
  //All node information of the tree
  const allTreeKeys = ref([]);
  const treeData = ref<any[]>([]);
  const expandedKeys = ref<Array<any>>([]);
  const selectedKeys = ref<Array<any>>([]);
  const checkedKeys = ref<Array<any>>([]);
  const lastCheckedKeys = ref<Array<any>>([]);
  const checkStrictly = ref(false);
  const { t } = useI18n();

  // Registration data rule authorization pop-up drawer
  const [registerDataRuleDrawer, dataRuleDrawer] = useDrawer();

  // onCreated
  loadData({
    success: (ids) => {
      // update-begin--author:liaozhiyang---date:20240704---for：【TV360X-1689】Synchronous system role modification method plus cache level association and other functions
      const localData = localStorage.getItem(DEPART_MANGE_AUTH_CONFIG_KEY);
      if (localData) {
        const obj = JSON.parse(localData);
        obj.level && toggleRelationAll(obj.level == 'relation' ? false : true);
        obj.expand && toggleExpandAll(obj.expand == 'openAll' ? true :false);
      } else {
        // expandedKeys.value = ids;
      }
      // update-end--author:liaozhiyang---date:20240704---for：【TV360X-1689】Synchronous system role modification method plus cache level association and other functions
    }
  });
  watch(departId, () => loadDepartPermission(), { immediate: true });

  async function loadData(options: any = {}) {
    try {
      loading.value = true;
      let { treeList, ids } = await queryRoleTreeList();
      //update-begin---author:wangshuai---date:2024-04-08---for:【issues/1169】in department management function【Department authority】Untranslated in Chinese t('') multilingual---
      treeData.value = translateTitle(treeList);
      //update-end---author:wangshuai---date:2024-04-08---for:【issues/1169】in department management function【Department authority】Untranslated in Chinese t('') multilingual---
      // update-begin--author:liaozhiyang---date:20240704---for：【TV360X-1689】Synchronous system role modification method plus cache level association and other functions
      allTreeKeys.value = ids;
      options.success?.(ids);
      // update-end--author:liaozhiyang---date:20240704---for：【TV360X-1689】Synchronous system role modification method plus cache level association and other functions
    } finally {
      loading.value = false;
    }
  }

  async function loadDepartPermission() {
    if (departId.value) {
      try {
        loading.value = true;
        let keys = await queryDepartPermission({ departId: departId.value });
        checkedKeys.value = keys;
        lastCheckedKeys.value = [...keys];
      } finally {
        loading.value = false;
      }
    }
  }

  async function onSubmit() {
    try {
      loading.value = true;
      await saveDepartPermission({
        departId: departId.value,
        permissionIds: checkedKeys.value.join(','),
        lastpermissionIds: lastCheckedKeys.value.join(','),
      });
      await loadData();
      await loadDepartPermission();
    } finally {
      loading.value = false;
    }
  }

  /**
   * Click to select
   * 2024-07-04
   * liaozhiyang
   */
  function onCheck(o, e) {
    // checkStrictly: true=>Hierarchy independent，false=>hierarchical association.
    if (checkStrictly.value) {
      checkedKeys.value = o.checked ? o.checked : o;
    } else {
      const keys = getNodeAllKey(e.node, 'children', 'key');
      if (e.checked) {
        // There may be duplicates under repeated operations.keys，Need to usenew SetRemove the duplicates
        checkedKeys.value = [...new Set([...checkedKeys.value, ...keys])];
      } else {
        const result = removeMatchingItems(checkedKeys.value, keys);
        checkedKeys.value = result;
      }
    }
  }
  /**
   * 2024-07-04
   * liaozhiyang
   * Remove matching array items
   */
  function removeMatchingItems(arr1, arr2) {
    // Use hash table records arr2 elements in
    const hashTable = {};
    for (const item of arr2) {
      hashTable[item] = true;
    }
    // use filter Method iterates through the first array，Filter out items that do not exist in the hash table
    return arr1.filter((item) => !hashTable[item]);
  }
  /**
   * 2024-07-04
   * liaozhiyang
   * Get the current node and all descendants belowkey
   */
  function getNodeAllKey(node: any, children: any, key: string) {
    const result: any = [];
    result.push(node[key]);
    const recursion = (data) => {
      data.forEach((item: any) => {
        result.push(item[key]);
        if (item[children]?.length) {
          recursion(item[children]);
        }
      });
    };
    node[children]?.length && recursion(node[children]);
    return result;
  }

  // treeExpand event
  function onExpand($expandedKeys) {
    expandedKeys.value = $expandedKeys;
  }

  // treeselected event
  function onSelect($selectedKeys, { selectedNodes }) {
    if (selectedNodes[0]?.ruleFlag) {
      let functionId = $selectedKeys[0];
      dataRuleDrawer.openDrawer(true, { departId, functionId });
    }
    selectedKeys.value = [];
  }

  // Switch parent-child association
  async function toggleCheckStrictly(flag) {
    checkStrictly.value = flag;
    await nextTick();
    checkedKeys.value = basicTree.value.getCheckedKeys();
  }

  // Toggle expand collapse
  async function toggleExpandAll(flag) {
    // update-begin--author:liaozhiyang---date:20240704---for：【TV360X-1689】Synchronous system role modification method plus cache level association and other functions
    if (flag) {
      expandedKeys.value = allTreeKeys.value;
      saveLocalOperation('expand', 'openAll');
    } else {
      expandedKeys.value = [];
      saveLocalOperation('expand', 'closeAll');
    }
    // update-end--author:liaozhiyang---date:20240704---for：【TV360X-1689】Synchronous system role modification method plus cache level association and other functions
  }

  // Toggle select all
  async function toggleCheckALL(flag) {
    // update-begin--author:liaozhiyang---date:20240704---for：【TV360X-1689】Synchronous system role modification method plus cache level association and other functions
    if (flag) {
      checkedKeys.value = allTreeKeys.value;
    } else {
      checkedKeys.value = [];
    }
    // update-end--author:liaozhiyang---date:20240704---for：【TV360X-1689】Synchronous system role modification method plus cache level association and other functions
  }

  // 切换hierarchical association(independent)
  const toggleRelationAll = (flag) => {
    // update-begin--author:liaozhiyang---date:20240704---for：【TV360X-1689】Synchronous system role modification method plus cache level association and other functions
    checkStrictly.value = flag;
    if (flag) {
      saveLocalOperation('level', 'standAlone');
    } else {
      saveLocalOperation('level', 'relation');
    }
    // update-end--author:liaozhiyang---date:20240704---for：【TV360X-1689】Synchronous system role modification method plus cache level association and other functions
  };
  /**
   * 2024-07-04
   * liaozhiyang
   * cache
   * */
  const saveLocalOperation = (key, value) => {
    const localData = localStorage.getItem(DEPART_MANGE_AUTH_CONFIG_KEY);
    const obj = localData ? JSON.parse(localData) : {};
    obj[key] = value;
    localStorage.setItem(DEPART_MANGE_AUTH_CONFIG_KEY, JSON.stringify(obj))
  };
</script>

<style lang="less" scoped>
  // 【VUEN-188】Solve the problem of insensitive scroll bar
  .depart-rule-tree :deep(.scrollbar__bar) {
    pointer-events: none;
  }
</style>
