<template>
  <BasicDrawer v-bind="$attrs" @register="registerDrawer" width="650px" destroyOnClose showFooter>
    <template #title>
      Role permission configuration
      <a-dropdown>
        <a-button class="more-icon">
          More actions
          <Icon icon="ant-design:down-outlined" size="14px" style="position: relative;top: 1px;right: 5px"></Icon>
        </a-button>
        <template #overlay>
          <a-menu @click="treeMenuClick">
            <a-menu-item key="checkAll">Select all</a-menu-item>
            <a-menu-item key="cancelCheck">Deselect</a-menu-item>
            <div class="line"></div>
            <a-menu-item key="openAll">Expand all</a-menu-item>
            <a-menu-item key="closeAll">Collapse all</a-menu-item>
            <div class="line"></div>
            <a-menu-item key="relation">hierarchical association</a-menu-item>
            <a-menu-item key="standAlone">Hierarchy independent</a-menu-item>
          </a-menu>
        </template>
      </a-dropdown>
    </template>
    <BasicTree
      ref="treeRef"
      checkable
      :treeData="treeData"
      :checkedKeys="checkedKeys"
      :expandedKeys="expandedKeys"
      :selectedKeys="selectedKeys"
      :clickRowToExpand="false"
      :checkStrictly="true"
      title="Permissions owned by"
      @check="onCheck"
      @select="onTreeNodeSelect"
    >
      <template #title="{ slotTitle, ruleFlag }">
        {{ slotTitle }}
        <Icon v-if="ruleFlag" icon="ant-design:align-left-outlined" style="margin-left: 5px; color: red"></Icon>
      </template>
    </BasicTree>
    <!--lower right button-->
    <template #footer>
      <!-- <PopConfirmButton title="Confirm to give up editing？" @confirm="closeDrawer" okText="Sure" cancelText="Cancel"></PopConfirmButton> -->
      <a-button @click="closeDrawer">Cancel</a-button>
      <a-button @click="handleSubmit(false)" type="primary" :loading="loading" ghost style="margin-right: 0.8rem">save only</a-button>
      <a-button @click="handleSubmit(true)" type="primary" :loading="loading">Save and close</a-button>
    </template>
    <RoleDataRuleDrawer @register="registerDrawer1" />
  </BasicDrawer>
</template>
<script lang="ts" setup>
  import { ref, computed, unref, onMounted } from 'vue';
  import { BasicDrawer, useDrawer, useDrawerInner } from '/@/components/Drawer';
  import { BasicTree, TreeItem } from '/@/components/Tree';
  import { PopConfirmButton } from '/@/components/Button';
  import RoleDataRuleDrawer from './RoleDataRuleDrawer.vue';
  import { queryTreeListForRole, queryRolePermission, saveRolePermission } from '../role.api';
  import { useI18n } from "/@/hooks/web/useI18n";
  import { ROLE_AUTH_CONFIG_KEY } from '/@/enums/cacheEnum';
  const emit = defineEmits(['register']);
  //tree information
  const treeData = ref<TreeItem[]>([]);
  //All node information of the tree
  const allTreeKeys = ref([]);
  //Tree selection node information
  const checkedKeys = ref<any>([]);
  const defaultCheckedKeys = ref([]);
  //Selected node information of the tree
  const selectedKeys = ref([]);
  const roleId = ref('');
  //tree instance
  const treeRef = ref(null);
  const loading = ref(false);

  //unfold foldedkey
  const expandedKeys = ref<any>([]);
  //Whether the selected status of the parent and child nodes is related trueNot relevant，falseassociation
  const checkStrictly = ref<boolean>(false);
  const [registerDrawer1, { openDrawer: openDataRuleDrawer }] = useDrawer();
  const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
    await reset();
    setDrawerProps({ confirmLoading: false, loading: true });
    roleId.value = data.roleId;
    //initialization data
    const roleResult = await queryTreeListForRole();
    // update-begin--author:liaozhiyang---date:20240228---for：【QQYUN-8355】Role permission configuration的菜单翻译
    treeData.value = translateTitle(roleResult.treeList);
    // update-end--author:liaozhiyang---date:20240228---for：【QQYUN-8355】Role permission configuration的菜单翻译
    allTreeKeys.value = roleResult.ids;
    // update-begin--author:liaozhiyang---date:20240531---for：【TV360X-590】Role authorization pop-up window operation cache
    const localData = localStorage.getItem(ROLE_AUTH_CONFIG_KEY);
    if (localData) {
      const obj = JSON.parse(localData);
      obj.level && treeMenuClick({ key: obj.level });
      obj.expand && treeMenuClick({ key: obj.expand });
    } else {
      expandedKeys.value = roleResult.ids;
    }
    // update-end--author:liaozhiyang---date:20240531---for：【TV360X-590】Role authorization pop-up window operation cache
    //Initialize character menu data
    const permResult = await queryRolePermission({ roleId: unref(roleId) });
    checkedKeys.value = permResult;
    defaultCheckedKeys.value = permResult;
    setDrawerProps({ loading: false });
  });
  /**
  * 2024-02-28
  * liaozhiyang
  * Translate menu names
   */
  function translateTitle(data) {
    if (data?.length) {
      data.forEach((item) => {
        if (item.slotTitle) {
          const { t } = useI18n();
          if (item.slotTitle.includes("t('") && t) {
            item.slotTitle = new Function('t', `return ${item.slotTitle}`)(t);
          }
        }
        if (item.children?.length) {
          translateTitle(item.children);
        }
      });
    }
    return data;
  }
  /**
   * Click to select
   * 2024-04-26
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
   * 2024-04-26
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
   * 2024-04-26
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

  /**
   * Select node，Open the data permission drawer
   */
  function onTreeNodeSelect(key) {
    if (key && key.length > 0) {
      selectedKeys.value = key;
    }
    openDataRuleDrawer(true, { functionId: unref(selectedKeys)[0], roleId: unref(roleId) });
  }
  /**
   * Data reset
   */
  function reset() {
    treeData.value = [];
    allTreeKeys.value = [];
    checkedKeys.value = [];
    defaultCheckedKeys.value = [];
    selectedKeys.value = [];
    roleId.value = '';
  }
  /**
   * GettreeExample
   */
  function getTree() {
    const tree = unref(treeRef);
    if (!tree) {
      throw new Error('tree is null!');
    }
    return tree;
  }
  /**
   * submit
   */
  async function handleSubmit(exit) {
    let params = {
      roleId: unref(roleId),
      permissionIds: unref(getTree().getCheckedKeys()).join(','),
      lastpermissionIds: unref(defaultCheckedKeys).join(','),
    };
    //update-begin-author:taoyan date:2023-2-11 for: issues/352 VUERole authorization is saved repeatedly
    if(loading.value===false){
      await doSave(params)
    }else{
      console.log('Please wait until the last execution is completed!');
    }
    if(exit){
      // if closed
      closeDrawer();
    }else{
      // 没有关闭需要重新Get选中数据
      const permResult = await queryRolePermission({ roleId: unref(roleId) });
      defaultCheckedKeys.value = permResult;
    }
  }

  // VUERole authorization is saved repeatedly #352
  async function doSave(params) {
    loading.value = true;
    try {
      await saveRolePermission(params);
    } catch (e) {
      loading.value = false;
    }
    setTimeout(()=>{
      loading.value = false;
    }, 500)
  }
  //update-end-author:taoyan date:2023-2-11 for: issues/352 VUERole authorization is saved repeatedly

  /**
   * Tree menu selection
   * @param key
   */
  function treeMenuClick({ key }) {
    if (key === 'checkAll') {
      checkedKeys.value = allTreeKeys.value;
    } else if (key === 'cancelCheck') {
      checkedKeys.value = [];
    } else if (key === 'openAll') {
      expandedKeys.value = allTreeKeys.value;
      saveLocalOperation('expand', 'openAll');
    } else if (key === 'closeAll') {
      expandedKeys.value = [];
      saveLocalOperation('expand', 'closeAll');
    } else if (key === 'relation') {
      checkStrictly.value = false;
      saveLocalOperation('level', 'relation');
    } else {
      checkStrictly.value = true;
      saveLocalOperation('level', 'standAlone');
    }
  }
  /**
   * 2024-05-31
   * liaozhiyang
   * 【TV360X-590】Role authorization pop-up window operation cache
   * */
  const saveLocalOperation = (key, value) => {
    const localData = localStorage.getItem(ROLE_AUTH_CONFIG_KEY);
    const obj = localData ? JSON.parse(localData) : {};
    obj[key] = value;
    localStorage.setItem(ROLE_AUTH_CONFIG_KEY, JSON.stringify(obj))
  };
</script>

<style lang="less" scoped>
  /** Fixed action button */
  .jeecg-basic-tree {
    position: absolute;
    width: 618px;
  }
  //update-begin---author:wangshuai ---date:20230202  for：Drawer pop-up window title icon drop-down style------------
  .line {
    height: 1px;
    width: 100%;
    border-bottom: 1px solid #f0f0f0;
  }
  .more-icon {
/*    font-size: 20px !important;
    color: black;
    display: inline-flex;*/
    float: right;
    margin-right: 2px;
    cursor: pointer;
  }
  :deep(.jeecg-tree-header) {
    border-bottom: none;
  }
  //update-end---author:wangshuai ---date:20230202  for：Drawer pop-up window title icon drop-down style------------
</style>
