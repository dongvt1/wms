<template>
  <div>
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <template #tableTitle>
        <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleAdd">New</a-button>
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined"></Icon>
                delete
              </a-menu-item>
            </a-menu>
          </template>
          <a-button
            >Batch operations
            <Icon icon="ant-design:down-outlined"></Icon>
          </a-button>
        </a-dropdown>
      </template>
      <template #action="{ record }">
        <TableAction :actions="getActions(record)" />
      </template>
    </BasicTable>
    <PositionModal @register="registerModal" @success="reload" />
  </div>
</template>
<script lang="ts" name="TenantPositionList" setup>
  import { onMounted } from 'vue';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { getPositionList, deletePosition, batchDeletePosition, getExportUrl, getImportUrl } from './position.api';
  import { columns, searchFormSchema } from './position.data';
  import PositionModal from './PositionModal.vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { tenantSaasMessage } from '@/utils/common/compUtils';
  import { getTenantId } from '@/utils/auth';
  const [registerModal, { openModal }] = useModal();

  // List page public parameters、method
  const { onExportXls, onImportXls, tableContext } = useListPage({
    tableProps: {
      title: 'Tenant job list',
      api: getPositionList,
      columns: columns,
      formConfig: {
        schemas: searchFormSchema,
      },
      actionColumn: {
        width: 180,
      },
      showIndexColumn: true,
      defSort: {
        column: '',
        order: '',
      },
      beforeFetch(params) {
        return Object.assign({ tenantId: getTenantId() }, params);
      },
    },
    exportConfig: {
      name: 'Tenant job list',
      url: getExportUrl,
    },
    importConfig: {
      url: getImportUrl,
    },
  });

  const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext;

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
        label: 'delete',
        popConfirm: {
          title: '是否确认delete',
          confirm: handleDelete.bind(null, record),
        },
      },
    ];
  }

  /**
   * New事件
   */
  function handleAdd() {
    openModal(true, {
      isUpdate: false,
    });
  }

  /**
   * edit事件
   */
  function handleEdit(record) {
    openModal(true, {
      record,
      isUpdate: true,
    });
  }

  /**
   * delete事件
   */
  async function handleDelete(record) {
    await deletePosition({ id: record.id }, reload);
  }

  /**
   * 批量delete事件
   */
  async function batchHandleDelete() {
    await batchDeletePosition({ ids: selectedRowKeys.value }, () => {
      selectedRowKeys.value = [];
      reload();
    });
  }

  onMounted(() => {
    //Prompt message
    tenantSaasMessage('Tenant position');
  });
</script>
