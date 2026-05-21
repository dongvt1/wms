<template>
  <div class="p-2">
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--Slot: table title-->
      <template #tableTitle>
        <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleCreate">Add New</a-button>
      </template>
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
    </BasicTable>
    <AiOcrModal @register="registerModal" @success="reload()"></AiOcrModal>
    <AiOcrAnalysisModal @register="registerAnalysisModal" @success="reload()"></AiOcrAnalysisModal>
  </div>
</template>
<script lang="ts" name="site-list" setup>
  import { ref } from 'vue';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { list, deleteOcrById } from './AiOcr.api';
  import { columns } from './AiOcr.data';
  import AiOcrModal from './components/AiOcrModal.vue';
  import AiOcrAnalysisModal from './components/AiOcrAnalysisModal.vue';

  const [registerModal, { openModal }] = useModal();
  const [registerAnalysisModal, { openModal: openAnalysisModal }] = useModal();
  
  // Common parameters and methods for list page
  const { prefixCls, tableContext } = useListPage({
    tableProps: {
      api: list,
      columns,
      useSearchForm: false,
      pagination: false,
      actionColumn: {
        width: 120,
      },
      canResize: false,
    },
  });
  const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext;

  /**
   * Add New
   */
  function handleCreate() {
    openModal(true, {});
  }

  /**
   * Edit
   */
  function handleEdit(record: Recordable) {
    openModal(true, {
      record,
      isUpdate: true,
      showFooter: true,
    });
  }

  /**
   * Delete
   */
  async function handleDelete(id) {
    await deleteOcrById({ id: id });
    reload();
  }

  /**
   * Analyze
   * @param record
   */
  function handleAnalysis(record){
    openAnalysisModal(true,{
      isUpdate: true,
      record
    })
  }
  
  /**
   * Action Column
   */
  function getTableAction(record) {
    return [
      {
        label: 'Analyze',
        onClick: handleAnalysis.bind(null, record),
      },
      {
        label: 'Edit',
        onClick: handleEdit.bind(null, record),
      },
      {
        label: 'Delete',
        popConfirm: {
          title: 'Are you sure to delete?',
          placement: 'left',
          confirm: handleDelete.bind(null, record.id),
        },
      },
    ];
  }
</script>
