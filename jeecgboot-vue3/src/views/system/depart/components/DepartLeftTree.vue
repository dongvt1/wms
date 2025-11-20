<template>
  <a-card :bordered="false" style="height: 100%">
    <div class="j-table-operator" style="width: 100%;display: flex;align-items: center">
      <a-button type="primary" preIcon="ant-design:plus-outlined" @click="onAddDepart">New</a-button>
      <a-button type="primary" preIcon="ant-design:plus-outlined" @click="onAddChildDepart()">Add subordinate</a-button>
      <a-upload name="file" :showUploadList="false" :customRequest="onImportXls" v-if="!isTenantDepart">
        <a-button type="primary" preIcon="ant-design:import-outlined">import</a-button>
      </a-upload>
      <a-button type="primary" preIcon="ant-design:export-outlined" @click="onExportXls" v-if="!isTenantDepart">Export</a-button>
      <template v-if="checkedKeys.length > 0">
        <a-dropdown>
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="onDeleteBatch">
                <icon icon="ant-design:delete-outlined" />
                <span>delete</span>
              </a-menu-item>
            </a-menu>
          </template>
          <a-button>
            <span>Batch operations </span>
            <icon icon="akar-icons:chevron-down" />
          </a-button>
        </a-dropdown>
      </template>
      <Icon icon="ant-design:question-circle-outlined" style="margin-left: 10px;cursor: pointer" @click="tipShow = true"></Icon>
      <div v-if="loginTenantName" style="margin-left: 10px;"
      >Currently logged in tenant: <span class="tenant-name">{{ loginTenantName }}</span>
      </div>
    </div>
    <a-alert type="info" show-icon class="alert" style="margin-bottom: 8px">
      <template #message>
        <template v-if="checkedKeys.length > 0">
          <span>selected {{ checkedKeys.length }} records</span>
          <a-divider type="vertical" />
          <a @click="checkedKeys = []">Clear</a>
        </template>
        <template v-else>
          <span>No data selected</span>
        </template>
      </template>
    </a-alert>
    <a-spin :spinning="loading">
      <a-input-search placeholder="Search by department name…" style="margin-bottom: 10px" @search="onSearch" />
      <!--Organization tree-->
      <template v-if="treeData.length > 0">
        <a-tree
          v-if="!treeReloading"
          checkable
          :clickRowToExpand="false"
          :treeData="treeData"
          :selectedKeys="selectedKeys"
          :checkStrictly="checkStrictly"
          :load-data="loadChildrenTreeData"
          :checkedKeys="checkedKeys"
          v-model:expandedKeys="expandedKeys"
          @check="onCheck"
          @select="onSelect"
          style="overflow-y: auto;height: calc(100vh - 330px);"
        >
          <template #title="{ key: treeKey, title, dataRef, data }">
            <a-dropdown :trigger="['contextmenu']">
              <Popconfirm
                :open="visibleTreeKey === treeKey"
                title="Sure要delete吗？"
                ok-text="Sure"
                cancel-text="Cancel"
                placement="rightTop"
                @confirm="onDelete(dataRef)"
                @openChange="onVisibleChange"
              >
                <TreeIcon :orgCategory="dataRef.orgCategory" :title="title"></TreeIcon>
              </Popconfirm>

              <template #overlay>
                <a-menu @click="">
                  <a-menu-item key="1" @click="onAddChildDepart(dataRef)" v-if="data.orgCategory !== '3'">Add subordinate</a-menu-item>
                  <a-menu-item key="2" @click="visibleTreeKey = treeKey">
                    <span style="color: red">delete</span>
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </template>
        </a-tree>
      </template>
      <a-empty v-else description="No data yet" />
    </a-spin>
    <DepartFormModal :rootTreeData="treeData" @register="registerModal" @success="loadRootTreeData" />
  </a-card>
  <a-modal v-model:open="tipShow" :footer="null" title="Department Rules Explanation" :width="800">
      <ul class="departmentalRulesTip">
        <li>The current department structure supports the group organizational structure，The first level defaults to company，Subordinates can create subsidiaries、Departments and positions。</li>
        <li><br/></li>
        <li>1、岗位下不能Add subordinate。</li>
        <li>2、Subsidiaries cannot be added directly under a department。</li>
        <li>3、You can continue to add subsidiaries under subsidiaries。</li>
        <li>4、Positions need to be configured with job levels，The level of the position and the relationship between superiors and subordinates are based on the job level and superior position settings.。</li>
        <li>5、The position of chairman can only be chosen by the parent company（Subsidiary or parent company）All positions in each department are superior positions。</li>
        <li>6、For non-chairman positions, only the current parent department and higher-level positions within the department can be selected as superior positions.。</li>
        <li><br/></li>
        <li><b>Special instructions：</b>The logic related to the chairman is fixed and hard-coded，Job level“Chairman”Please do not modify the expression of。</li>
      </ul>
    <div style="height: 10px"></div>
  </a-modal>
</template>

<script lang="ts" setup>
  import { inject, nextTick, ref, unref, defineEmits } from 'vue';
  import { useModal } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useMethods } from '/@/hooks/system/useMethods';
  import { Api, deleteBatchDepart, queryDepartAndPostTreeSync } from '../depart.api';
  import { searchByKeywords } from '/@/views/system/departUser/depart.user.api';
  import DepartFormModal from '/@/views/system/depart/components/DepartFormModal.vue';
  import { Popconfirm } from 'ant-design-vue';
  import TreeIcon from "@/components/Form/src/jeecg/components/TreeIcon/TreeIcon.vue";

  const prefixCls = inject('prefixCls');
  const emit = defineEmits(['select', 'rootTreeData']);
  const { createMessage } = useMessage();
  const { handleImportXls, handleExportXls } = useMethods();
  const props = defineProps({
    //Is it a tenant department?
    isTenantDepart: { default: false, type: Boolean },
    //Currently logged in tenant
    loginTenantName: { default: "", type: String },
  })

  const loading = ref<boolean>(false);
  // Department tree list data
  const treeData = ref<any[]>([]);
  // Currently selected item
  const checkedKeys = ref<any[]>([]);
  // Currently expanded item
  const expandedKeys = ref<any[]>([]);
  // Currently selected item
  const selectedKeys = ref<any[]>([]);
  // tree component reload
  const treeReloading = ref<boolean>(false);
  // Are the father and son trees related?
  const checkStrictly = ref<boolean>(true);
  // Currently selected department
  const currentDepart = ref<any>(null);
  // 控制确认delete提示框是否显示
  const visibleTreeKey = ref<any>(null);
  // Search keywords
  const searchKeyword = ref('');
  // Whether the prompt pop-up window is displayed
  const tipShow = ref<boolean>(false);

  // register modal
  const [registerModal, { openModal }] = useModal();

  // Load top-level department information
  async function loadRootTreeData() {
    try {
      loading.value = true;
      treeData.value = [];
      const result = await queryDepartAndPostTreeSync();
      if (Array.isArray(result)) {
        treeData.value = result;
      }
      if (expandedKeys.value.length === 0) {
        autoExpandParentNode();
      } else {
        if (selectedKeys.value.length === 0) {
          let item = treeData.value[0];
          if (item) {
            // The first one is selected by default
            setSelectedKey(item.id, item);
          }
        } else {
          emit('select', currentDepart.value);
        }
      }
      emit('rootTreeData', treeData.value);
    } finally {
      loading.value = false;
    }
  }

  loadRootTreeData();

  // Load sub-department information
  async function loadChildrenTreeData(treeNode) {
    try {
      const result = await queryDepartAndPostTreeSync({
        pid: treeNode.dataRef.id,
      });
      if (result.length == 0) {
        treeNode.dataRef.isLeaf = true;
      } else {
        treeNode.dataRef.children = result;
        if (expandedKeys.value.length > 0) {
          // 判断获取的子级是否有Currently expanded item
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
      emit('rootTreeData', treeData.value);
    } catch (e) {
      console.error(e);
    }
    return Promise.resolve();
  }

  // Automatically expand parent node，Expand only one level
  function autoExpandParentNode() {
    let item = treeData.value[0];
    if (item) {
      if (!item.isLeaf) {
        expandedKeys.value = [item.key];
      }
      // The first one is selected by default
      setSelectedKey(item.id, item);
      reloadTree();
    } else {
      emit('select', null);
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
    if (data) {
      currentDepart.value = data;
      emit('select', data);
    }
  }

  // Add a first-level department
  function onAddDepart() {
    openModal(true, { isUpdate: false, isChild: false });
  }

  // Add sub-department
  function onAddChildDepart(data = currentDepart.value) {
    if (data == null) {
      createMessage.warning('Please select a department first');
      return;
    }
    if(data.orgCategory === '3'){
      createMessage.warning('Cannot add sub-levels under the position！');
      return;
    }
    const record = { parentId: data.id, orgCategory: data.orgCategory };
    openModal(true, { isUpdate: false, isChild: true, record });
  }

  // Search events
  async function onSearch(value: string) {
    if (value) {
      try {
        loading.value = true;
        treeData.value = [];
        let result = await searchByKeywords({ keyWord: value, orgCategory: "1,2,3,4" });
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

  // Tree checkbox selection event
  function onCheck(e) {
    if (Array.isArray(e)) {
      checkedKeys.value = e;
    } else {
      checkedKeys.value = e.checked;
    }
  }

  // tree selection event
  function onSelect(selKeys, event) {
    console.log('select: ', selKeys, event);
    if (selKeys.length > 0 && selectedKeys.value[0] !== selKeys[0]) {
      setSelectedKey(selKeys[0], event.selectedNodes[0]);
    } else {
      // 这样可以防止用户Cancel选择
      setSelectedKey(selectedKeys.value[0]);
    }
  }

  /**
   * according to ids delete部门
   * @param idListRef array
   * @param confirm Whether to display a confirmation prompt box
   */
  async function doDeleteDepart(idListRef, confirm = true) {
    const idList = unref(idListRef);
    if (idList.length > 0) {
      try {
        loading.value = true;
        await deleteBatchDepart({ ids: idList.join(',') }, confirm);
        await loadRootTreeData();
      } finally {
        loading.value = false;
      }
    }
  }

  // delete单个部门
  async function onDelete(data) {
    if (data) {
      onVisibleChange(false);
      doDeleteDepart([data.id], false);
    }
  }

  // 批量delete部门
  async function onDeleteBatch() {
    try {
      await doDeleteDepart(checkedKeys);
      checkedKeys.value = [];
    } finally {
    }
  }

  function onVisibleChange(visible) {
    if (!visible) {
      visibleTreeKey.value = null;
    }
  }

  function onImportXls(d) {
    handleImportXls(d, Api.importExcelUrl, () => {
      loadRootTreeData();
    });
  }

  function onExportXls() {
    //update-begin---author:wangshuai---date:2024-07-05---for:【TV360X-1671】部门管理不支持选中的记录Export---
    let params = {}
    if(checkedKeys.value && checkedKeys.value.length > 0) {
      params['selections'] = checkedKeys.value.join(',')
    }
    handleExportXls('Department information', Api.exportXlsUrl,params);
    //update-end---author:wangshuai---date:2024-07-05---for:【TV360X-1671】部门管理不支持选中的记录Export---
  }

  defineExpose({
    loadRootTreeData,
  });
</script>

<style lang="less" scoped>
  .departmentalRulesTip{
    margin: 20px;
    background-color: #f8f9fb;
    color: #99a1a9;
    border-radius: 4px;
    padding: 12px;
  }
  .tenant-name {
    text-decoration: underline;
    margin: 5px;
    font-size: 15px;
  }
</style>
