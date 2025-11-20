<template>
  <div class="bg-white m-4 mr-0 overflow-hidden">
    <div v-if="userIdentity === '2'" class="j-table-operator" style="width: 100%">
      <a-button type="primary" preIcon="ant-design:plus-outlined" @click="onAddChildDepart">Add subordinate</a-button>
      <!--      <a-button type="primary" preIcon="ant-design:edit-outlined" @click="editDepart">edit</a-button>-->
      <a-button :disabled="!(checkedKeys && checkedKeys.length > 0)" preIcon="ant-design:delete-outlined" @click="onDeleteBatch">delete</a-button>
    </div>
    <a-spin :spinning="loading">
      <template v-if="userIdentity === '2'">
        <a-input-search placeholder="Search by department name…" style="margin-bottom: 10px" @search="onSearch" />
        <!--Organization tree-->
        <BasicTree
          v-if="!treeReloading"
          :toolbar="false"
          :search="false"
          :showLine="false"
          :clickRowToExpand="false"
          :multiple="false"
          :checkStrictly="true"
          :treeData="treeData"
          :checkedKeys="checkedKeys"
          :selectedKeys="selectedKeys"
          :expandedKeys="expandedKeys"
          :autoExpandParent="autoExpandParent"
          :beforeRightClick="getRightMenuList"
          @select="onSelect"
          @expand="onExpand"
          @check="onCheck"
        />
      </template>
      <a-empty v-else description="Ordinary employees do not have this authority" />
    </a-spin>
    <DepartFormModal :rootTreeData="treeData" @register="registerModal" @success="handleSuccess" />
  </div>
</template>

<script lang="ts" setup>
  import { inject, nextTick, ref } from 'vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { BasicTree, ContextMenuItem } from '/@/components/Tree';
  import { queryMyDepartTreeList, searchByKeywords } from '../depart.user.api';
  import DepartFormModal from '@/views/system/depart/components/DepartFormModal.vue';
  import { useModal } from '@/components/Modal';
  import { deleteBatchDepart } from '@/views/system/depart/depart.api';

  const prefixCls = inject('prefixCls');
  const emit = defineEmits(['select']);
  const { createMessage } = useMessage();

  let loading = ref<boolean>(false);
  // Responsible departmentID
  let myDepIds = ref<any[]>([]);
  // Department tree list data
  let treeData = ref<any[]>([]);
  // Currently expanded item
  let expandedKeys = ref<any[]>([]);
  // Currently selected item
  let selectedKeys = ref<any[]>([]);
  // Currently selected item
  let selectedNode = ref<any>({});
  // Currently selected item
  let checkedKeys = ref<any[]>([]);
  // Whether to automatically expand the parent
  let autoExpandParent = ref<boolean>(true);
  // User ID(1:Ordinary employees  2:Superior)
  let userIdentity = ref<string>('2');
  // tree component reload
  let treeReloading = ref<boolean>(false);
  // register modal
  const [registerModal, { openModal }] = useModal();
  // Load department information
  function loadDepartTreeData() {
    loading.value = true;
    treeReloading.value = true;
    treeData.value = [];
    queryMyDepartTreeList()
      .then((res) => {
        if (res.success) {
          if (Array.isArray(res.result)) {
            treeData.value = res.result;
            myDepIds.value = res.result.map((item) => item.id);
            userIdentity.value = res.message;
            autoExpandParentNode();
          }
        } else {
          createMessage.warning(res.message);
        }
      })
      .finally(async () => {
        await nextTick();
        loading.value = false;
        treeReloading.value = false;
      });
  }

  loadDepartTreeData();

  // Automatically expand parent node，Expand only one level
  function autoExpandParentNode() {
    let keys: Array<any> = [];
    treeData.value.forEach((item, index) => {
      if (item.children && item.children.length > 0) {
        keys.push(item.key);
      }
      if (index === 0) {
        // The first one is selected by default
        setSelectedKey(item.id, item);
      }
    });
    if (keys.length > 0) {
      reloadTree();
      expandedKeys.value = keys;
    }
  }

  // Add sub-department
  function onAddChildDepart() {
    if (selectedKeys.value && selectedKeys.value.length === 0) {
      createMessage.warning('Please select a department first');
      return;
    }
    const record = { parentId: selectedKeys.value[0] };
    openModal(true, { isUpdate: false, isChild: true, record });
  }

  // edit部门
  function editDepart() {
    if (selectedKeys.value && selectedKeys.value.length === 0) {
      createMessage.warning('Please select a department first');
      return;
    }
    if (myDepIds.value.includes(selectedKeys.value[0])) {
      createMessage.warning('不能editResponsible department');
      return;
    }
    console.log('selectedNode', selectedNode.value);
    openModal(true, { isUpdate: false, isChild: true, record: { ...selectedNode.value } });
  }

  // delete部门
  async function onDeleteBatch() {
    const idList = checkedKeys.value;
    if (myDepIds.value.includes(idList[0])) {
      createMessage.warning('不能deleteResponsible department');
      return;
    }
    if (idList.length > 0) {
      try {
        loading.value = true;
        await deleteBatchDepart({ ids: idList.join(',') }, true);
        await loadDepartTreeData();
      } finally {
        loading.value = false;
      }
    }
  }
  // Reload tree component，Prevent data from being unable to be expanded by default
  async function reloadTree() {
    await nextTick();
    treeReloading.value = true;
    await nextTick();
    treeReloading.value = false;
  }

  /**
   * Set the currently selected row
   */
  function setSelectedKey(key: string, data?: object) {
    selectedKeys.value = [key];
    checkedKeys.value = [key];
    if (data) {
      selectedNode.value = { ...data };
      emit('select', data);
    }
  }

  // Search events
  function onSearch(value: string) {
    if (value) {
      loading.value = true;
      searchByKeywords({ keyWord: value, myDeptSearch: '1' })
        .then((result) => {
          if (Array.isArray(result)) {
            treeData.value = result;
          } else {
            createMessage.warning('No department information found');
            treeData.value = [];
          }
        })
        .finally(() => (loading.value = false));
    } else {
      loadDepartTreeData();
    }
  }

  // tree selection event
  function onSelect(selKeys, event) {
    if (selKeys.length > 0 && selectedKeys.value[0] !== selKeys[0]) {
      setSelectedKey(selKeys[0], event.selectedNodes[0]);
    } else {
      // This prevents the user from deselecting
      setSelectedKey(selectedKeys.value[0]);
    }
    checkedKeys.value = [selectedKeys.value[0]];
  }

  // tree selection event
  function onCheck(keys) {
    if (keys.checked && keys.checked.length > 0) {
      checkedKeys.value = [...keys.checked];
    } else {
      checkedKeys.value = [];
    }
  }

  // tree expand event
  function onExpand(keys) {
    expandedKeys.value = keys;
    autoExpandParent.value = false;
  }

  //successful callback
  async function handleSuccess() {
    await loadDepartTreeData();
  }
  /**
   *
   * @param node
   */
  function getRightMenuList(node: any): ContextMenuItem[] {
    return [
      {
        label: 'Add subordinate',
        disabled: myDepIds.value.includes(node.key),
        handler: () => {
          setSelectedKey(node.key);
          onAddChildDepart();
        },
        icon: 'ant-design:plus-outlined',
      },
      {
        label: 'edit',
        disabled: myDepIds.value.includes(node.key),
        handler: () => {
          setSelectedKey(node.key);
          const record = { ...node.dataRef };
          openModal(true, { isUpdate: true, record, isChild: true });
        },
        icon: 'ant-design:edit-outlined',
      },
    ];
  }
</script>
<style lang="less" scoped>
  /*upgradeantd3back，The query box is too close to the tree，Style optimization*/
  :deep(.jeecg-tree-header) {
    margin-bottom: 6px;
  }
</style>
