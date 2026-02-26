<template>
  <!--Reference table-->
  <BasicTable @register="registerTable" :rowSelection="rowSelection">
    <!--Slot: table title-->
    <template #tableTitle>
      <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleCreate"> Add New</a-button>
      <a-button type="primary" preIcon="ant-design:export-outlined" @click="onExportXls"> Export</a-button>
      <j-upload-button type="primary" preIcon="ant-design:import-outlined" @click="onImportXls">Import</j-upload-button>
      <a-button type="primary" @click="handlerRefreshCache" preIcon="ant-design:sync-outlined"> Refresh Cache</a-button>
      <a-button type="primary" @click="openRecycleModal(true)" preIcon="ant-design:hdd-outlined"> Recycle Bin</a-button>

      <a-dropdown v-if="selectedRowKeys.length > 0">
        <template #overlay>
          <a-menu>
            <a-menu-item key="1" @click="batchHandleDelete">
              <Icon icon="ant-design:delete-outlined"></Icon>
              Delete
            </a-menu-item>
          </a-menu>
        </template>
        <a-button
          >Batch Operations
          <Icon icon="ant-design:down-outlined"></Icon>
        </a-button>
      </a-dropdown>
    </template>
    <!--Action column-->
    <template #action="{ record }">
      <TableAction :actions="getTableAction(record)" />
    </template>
  </BasicTable>
  <!--Dictionary modal-->
  <DictModal @register="registerModal" @success="handleSuccess" />
  <!--Dictionary configuration drawer-->
  <DictItemList @register="registerDrawer" />
  <!--Recycle bin modal-->
  <DictRecycleBinModal @register="registerModal1" @success="reload" />
</template>

<script lang="ts" name="system-dict" setup>
  //TypeScript syntax
  import { ref, computed, unref } from 'vue';
  import { BasicTable, TableAction } from '/src/components/Table';
  import { useDrawer } from '/src/components/Drawer';
  import { useModal } from '/src/components/Modal';
  import DictItemList from './components/DictItemList.vue';
  import DictModal from './components/DictModal.vue';
  import DictRecycleBinModal from './components/DictRecycleBinModal.vue';
  import { useMessage } from '/src/hooks/web/useMessage';
  import { removeAuthCache, setAuthCache } from '/src/utils/auth';
  import { columns, searchFormSchema } from './dict.data';
  import { list, deleteDict, batchDeleteDict, getExportUrl, getImportUrl, refreshCache, queryAllDictItems } from './dict.api';
  import { DB_DICT_DATA_KEY } from '/src/enums/cacheEnum';
  import { useUserStore } from '/@/store/modules/user';

  const { createMessage } = useMessage();
  //Dictionary modal
  const [registerModal, { openModal }] = useModal();
  //Dictionary configuration drawer
  const [registerDrawer, { openDrawer }] = useDrawer();
  import { useListPage } from '/@/hooks/system/useListPage';

  //Recycle bin modal
  const [registerModal1, { openModal: openRecycleModal }] = useModal();

  // List page common parameters and methods
  const { prefixCls, tableContext, onExportXls, onImportXls } = useListPage({
    designScope: 'dict-template',
    tableProps: {
      title: 'Data Dictionary',
      api: list,
      columns: columns,
      formConfig: {
        schemas: searchFormSchema,
      },
      actionColumn: {
        width: 240,
      },
    },
    //update-begin---author:wangshuai ---date:20220616  for：[issues/I5AMDD]import/Export function，The prompt was not delivered after the operation export.url/import.url parameter------------
    exportConfig: {
      name: 'Data Dictionary List',
      url: getExportUrl,
    },
    importConfig: {
      url: getImportUrl,
    },
    //update-end---author:wangshuai ---date:20220616  for：[issues/I5AMDD]import/Export function，The prompt was not delivered after the operation export.url/import.url parameter--------------
  });

  //Register table data
  const [registerTable, { reload, updateTableDataRecord }, { rowSelection, selectedRowKeys, selectedRows }] = tableContext;

  /**
   * Add event
   */
  function handleCreate() {
    openModal(true, {
      isUpdate: false,
    });
  }
  /**
   * Edit event
   */
  async function handleEdit(record: Recordable) {
    openModal(true, {
      record,
      isUpdate: true,
    });
  }
  /**
   * Details
   */
  async function handleDetail(record) {
    openModal(true, {
      record,
      isUpdate: true,
    });
  }
  /**
   * Delete event
   */
  async function handleDelete(record) {
    await deleteDict({ id: record.id }, reload);
  }
  /**
   * Batch delete event
   */
  async function batchHandleDelete() {
    await batchDeleteDict({ ids: selectedRowKeys.value }, () => {
      // update-begin--author:liaozhiyang---date:20240701---for：【TV360X-1665】Even if the data dictionary is deleted in batches, the selection will be cleared.
      reload();
      selectedRowKeys.value = [];
      selectedRows.value = [];
      // update-end--author:liaozhiyang---date:20240701---for：【TV360X-1665】Even if the data dictionary is deleted in batches, the selection will be cleared.
    });
  }
  /**
   * Success callback
   */
  function handleSuccess({ isUpdate, values }) {
    if (isUpdate) {
      updateTableDataRecord(values.id, values);
    } else {
      reload();
    }
  }
  /**
   * Refresh cache
   */
  async function handlerRefreshCache() {
    const result = await refreshCache();
    if (result.success) {
      const res = await queryAllDictItems();
      removeAuthCache(DB_DICT_DATA_KEY);
      // update-begin--author:liaozhiyang---date:20230908---for：【QQYUN-6417】The problem of slow dictionary in production environment
      const userStore = useUserStore();
      userStore.setAllDictItems(res.result);
      // update-end--author:liaozhiyang---date:20230908---for：【QQYUN-6417】The problem of slow dictionary in production environment
      createMessage.success('Cache refresh completed!');
    } else {
      createMessage.error('Cache refresh failed!');
    }
  }
  /**
   * Dictionary configuration
   */
  function handleItem(record) {
    openDrawer(true, {
      id: record.id,
    });
  }
  /**
   * Action column
   */
  function getTableAction(record) {
    return [
      {
        label: 'Edit',
        onClick: handleEdit.bind(null, record),
      },
      {
        label: 'Dictionary Config',
        onClick: handleItem.bind(null, record),
      },
      {
        label: 'Delete',
        popConfirm: {
          title: 'Are you sure to delete?',
          confirm: handleDelete.bind(null, record),
        },
      },
    ];
  }
</script>

<style scoped></style>
