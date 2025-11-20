<template>
  <div class="p-4">
    <BasicTable @register="registerTable">
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
    </BasicTable>
    <WarehouseAreaModal @register="registerModal" @success="handleSuccess" />
  </div>
</template>

<script lang="ts" name="warehouse-area-list" setup>
  import { ref } from 'vue';
  import { BasicTable, TableAction, useTable, ActionItem } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { columns, searchFormSchema } from './area.data';
  import { warehouseAreaApi } from './area.api';
  import WarehouseAreaModal from './WarehouseAreaModal.vue';

  const [registerTable, { reload }] = useTable({
    title: 'Warehouse Area Management',
    api: warehouseAreaApi.list,
    columns,
    formConfig: {
      labelWidth: 120,
      schemas: searchFormSchema,
      autoSubmitOnEnter: true,
    },
    useSearchForm: true,
    showTableSetting: true,
    bordered: true,
    showIndexColumn: true,
    actionColumn: {
      width: 150,
      title: 'Actions',
      dataIndex: 'action',
      slots: { customRender: 'action' },
      fixed: 'right',
    },
  });

  const [registerModal, { openModal }] = useModal();

  function getTableAction(record): ActionItem[] {
    return [
      {
        label: 'Edit',
        onClick: handleEdit.bind(null, record),
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

  function handleAdd() {
    openModal(true, {
      isUpdate: false,
    });
  }

  function handleEdit(record) {
    openModal(true, {
      record,
      isUpdate: true,
    });
  }

  async function handleDelete(record) {
    await warehouseAreaApi.delete({ id: record.id });
    reload();
  }

  function handleSuccess() {
    reload();
  }
</script>