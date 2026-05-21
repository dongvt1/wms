<template>
  <div class="p-4">
    <BasicTable @register="registerTable">
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
    </BasicTable>
    <WarehouseShelfModal @register="registerModal" @success="handleSuccess" />
  </div>
</template>

<script lang="ts" name="warehouse-shelf-list" setup>
  import { ref } from 'vue';
  import { BasicTable, TableAction, useTable, ActionItem } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { columns, searchFormSchema, getWarehouseAreaOptions } from './shelf.data';
  import { warehouseShelfApi } from './shelf.api';
  import WarehouseShelfModal from './WarehouseShelfModal.vue';
  import { onMounted } from 'vue';

  const [registerTable, { reload, setProps }] = useTable({
    title: 'Warehouse Shelf Management',
    api: warehouseShelfApi.list,
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
    await warehouseShelfApi.delete({ id: record.id });
    reload();
  }

  function handleSuccess() {
    reload();
  }

  // Get warehouse area options during initialization
  onMounted(async () => {
    try {
      const areaOptions = await getWarehouseAreaOptions();
      // Update regional options in search form
      setProps({
        formConfig: {
          labelWidth: 120,
          schemas: searchFormSchema.map(schema => {
            if (schema.field === 'areaId') {
              return {
                ...schema,
                componentProps: {
                  ...schema.componentProps,
                  options: areaOptions,
                },
              };
            }
            return schema;
          }),
          autoSubmitOnEnter: true,
        },
      });
    } catch (error) {
      console.error('Failed to fetch warehouse area list:', error);
    }
  });
</script>
