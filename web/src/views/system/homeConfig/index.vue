<template>
  <div>
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <template #tableTitle>
        <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleCreate">New</a-button>
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined" /> delete
              </a-menu-item>
            </a-menu>
          </template>
          <a-button>Batch operations<Icon icon="mdi:chevron-down" /></a-button>
        </a-dropdown>
      </template>
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
      <template #status="{ text }">
        <a-tag color="pink" v-if="text == 0">Disable</a-tag>
        <a-tag color="#87d068" v-if="text == 1">enable</a-tag>
      </template>
      <template #relationType="{ text, record }">
        <span>{{ record.roleCode == 'DEF_INDEX_ALL' ? 'global default' : text }}</span>
      </template>
      <template #roleCode="{ text, record }">
        <span>{{ record.roleCode == 'DEF_INDEX_ALL' ? 'Menu default homepage' : text }}</span>
      </template>
    </BasicTable>
    <!--Role homepage configuration-->
    <HomeConfigModal @register="register" @success="reload" />
  </div>
</template>
<script lang="ts" name="home-config" setup>
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import HomeConfigModal from './components/HomeConfigModal.vue';
  import { columns, searchFormSchema } from './home.data';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { list, deleteIndex, batchDelete } from './home.api';

  //Pop-up window configuration
  const [register, { openModal }] = useModal();

  // List page public parameters、method
  const { tableContext } = useListPage({
    designScope: 'home-config',
    tableProps: {
      title: 'Home page configuration',
      api: list,
      columns: columns,
      formConfig: {
        labelAlign: 'left',
        labelWidth: 80,
        schemas: searchFormSchema,
        baseRowStyle: {
          marginLeft: '2px',
        },
      },
      actionColumn: {
        width: 80,
      },
      //Customize default sorting
      defSort: {
        column: 'id',
        order: 'desc',
      },
    },
  });
  const [registerTable, { reload, clearSelectedRowKeys }, { rowSelection, selectedRowKeys }] = tableContext;

  /**
   * New事件
   */
  async function handleCreate() {
    openModal(true, {
      isUpdate: false,
    });
  }
  /**
   * Edit event
   */
  async function handleEdit(record) {
    openModal(true, {
      isUpdate: true,
      values: record,
    });
  }
  /**
   * delete事件
   */
  async function handleDelete(record) {
    await deleteIndex({ id: record.id }, () => {
      reload();
    });
  }
  /**
   * 批量delete事件
   */
  async function batchHandleDelete() {
    await batchDelete({ ids: selectedRowKeys.value }, () => {
      clearSelectedRowKeys();
      reload();
    });
  }

  /**
   * Action bar
   */
  function getTableAction(record) {
    return [
      {
        label: 'edit',
        onClick: handleEdit.bind(null, record),
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
</script>
