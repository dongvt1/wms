<template>
  <div class="p-4">
    <BasicTable @register="registerTable" :indexColumnProps="indexColumnProps">
      <template #tableTitle>
        <a-button preIcon="ant-design:plus-outlined" type="primary" @click="handleAdd" style="margin-right: 5px">New</a-button>
        <a-button type="primary" @click="openRecycleModal(true)" preIcon="ant-design:hdd-outlined"> recycle bin</a-button>
      </template>
      <template #status="{ record, text }">
        <a-tag color="pink" v-if="text == 0">Disable</a-tag>
        <a-tag color="#87d068" v-if="text == 1">normal</a-tag>
      </template>
      <template #action="{ record }">
        <TableAction :actions="getActions(record)" />
      </template>
    </BasicTable>
    <RouteModal @register="registerDrawer" @success="reload" />
    <!--recycle bin弹窗-->
    <RouteRecycleBinModal @register="registerRecycleModal" @success="reload" />
  </div>
</template>
<script lang="ts" name="monitor-route" setup>
  import { ref } from 'vue';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { getRouteList, deleteRoute, copyRoute } from './route.api';
  import { columns } from './route.data';
  import RouteModal from './RouteModal.vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useDrawer } from '/@/components/Drawer';
  import { useListPage } from '/@/hooks/system/useListPage';
  import RouteRecycleBinModal from './components/RouteRecycleBinModal.vue';
  const { createMessage } = useMessage();
  const [registerDrawer, { openDrawer }] = useDrawer();
  const checkedKeys = ref<Array<string | number>>([]);

  //recycle binmodel
  const [registerRecycleModal, { openModal: openRecycleModal }] = useModal();
  // List page public parameters、method
  const { prefixCls, tableContext } = useListPage({
    designScope: 'router-template',
    tableProps: {
      title: 'Route list',
      api: getRouteList,
      useSearchForm: false,
      columns: columns,
    },
  });

  const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext;
  /**
   * Serial number column configuration
   */
  const indexColumnProps = {
    dataIndex: 'index',
    width: '15px',
  };

  /**
   * Operation column definition
   * @param record
   */
  function getActions(record) {
    return [
      {
        label: 'edit',
        onClick: handleEdit.bind(null, record),
      },
      {
        label: 'copy',
        popConfirm: {
          title: '是否确认copy',
          confirm: handleCopy.bind(null, record),
        },
      },
      {
        label: 'delete',
        popConfirm: {
          title: '是否确认delete',
          confirm: handleDelete.bind(null, record),
        },
      },
    ];
  }

  /**
   * Select event
   */
  function onSelectChange(selectedRowKeys: (string | number)[]) {
    checkedKeys.value = selectedRowKeys;
  }

  /**
   * New事件
   */
  function handleAdd() {
    openDrawer(true, {
      isUpdate: false,
    });
  }

  /**
   * edit事件
   */
  function handleEdit(record) {
    openDrawer(true, {
      record,
      isUpdate: true,
    });
  }
  /**
   * copy
   */
  async function handleCopy(record) {
    await copyRoute({ id: record.id }, reload);
    createMessage.success('copy成功');
  }

  /**
   * delete事件
   */
  async function handleDelete(record) {
    await deleteRoute({ id: record.id }, reload);
  }
</script>
