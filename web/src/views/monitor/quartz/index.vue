<template>
  <div>
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <template #tableTitle>
        <a-button preIcon="ant-design:plus-outlined" type="primary" @click="handleAdd" style="margin-right: 5px">New</a-button>
        <a-button type="primary" preIcon="ant-design:export-outlined" @click="onExportXls"> Export</a-button>
        <j-upload-button type="primary" preIcon="ant-design:import-outlined" @click="onImportXls">import</j-upload-button>
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
            <Icon icon="mdi:chevron-down"></Icon>
          </a-button>
        </a-dropdown>
      </template>
      <template #action="{ record }">
        <TableAction :actions="getActions(record)" :dropDownActions="getDropDownAction(record)" />
      </template>
    </BasicTable>
    <QuartzModal @register="registerModal" @success="reload" />
  </div>
</template>
<script lang="ts" name="monitor-quartz" setup>
  import { ref } from 'vue';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { getQuartzList, deleteQuartz, batchDeleteQuartz, executeImmediately, resumeJob, pauseJob, getExportUrl, getImportUrl } from './quartz.api';
  import { columns, searchFormSchema } from './quartz.data';
  import QuartzModal from './QuartzModal.vue';
  import { useMessage } from '/@/hooks/web/useMessage';

  const { createMessage } = useMessage();
  const [registerModal, { openModal }] = useModal();
  // List page public parameters、method
  const { prefixCls, tableContext, onExportXls, onImportXls } = useListPage({
    designScope: 'quartz-template',
    tableProps: {
      title: 'task list',
      api: getQuartzList,
      columns: columns,
      actionColumn: {
        width: 180,
      },
      formConfig: {
        labelWidth: 120,
        schemas: searchFormSchema,
        fieldMapToTime: [['fieldTime', ['beginDate', 'endDate'], 'YYYY-MM-DD HH:mm:ss']],
      },
    },
    exportConfig: {
      name: '定时task list',
      url: getExportUrl,
    },
    importConfig: {
      url: getImportUrl,
    },
  });

  const [registerTable, { reload }, { rowSelection, selectedRowKeys, selectedRows }] = tableContext;

  /**
   * Operation column definition
   * @param record
   */
  function getActions(record) {
    return [
      {
        label: 'start up',
        popConfirm: {
          title: '是否start up选中任务?',
          confirm: handlerResume.bind(null, record),
        },
        ifShow: (_action) => {
          return record.status == -1;
        },
      },
      {
        label: 'stop',
        popConfirm: {
          title: 'Whether to pause the selected task?',
          confirm: handlerPause.bind(null, record),
        },
        ifShow: (_action) => {
          return record.status == 0;
        },
      },
    ];
  }

  /**
   * Drop down action bar
   */
  function getDropDownAction(record) {
    return [
      {
        label: 'Execute immediately',
        popConfirm: {
          title: '是否Execute immediately任务?',
          confirm: handlerExecute.bind(null, record),
        },
      },
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
    await deleteQuartz({ id: record.id }, reload);
  }

  /**
   * Execute immediately
   */
  async function handlerExecute(record) {
    await executeImmediately({ id: record.id }, reload);
  }

  /**
   * pause
   */
  async function handlerPause(record) {
    await pauseJob({ id: record.id }, reload);
  }

  /**
   * start up
   */
  async function handlerResume(record) {
    await resumeJob({ id: record.id }, reload);
  }

  /**
   * 批量delete事件
   */
  async function batchHandleDelete() {
    await batchDeleteQuartz({ ids: selectedRowKeys.value }, () => {
      // -update-begin--author:liaozhiyang---date:20240702---for：【TV360X-1662】Menu management、定时任务批量delete清空选中
      reload();
      selectedRows.value = [];
      selectedRowKeys.value = [];
      // -update-end--author:liaozhiyang---date:20240702---for：【TV360X-1662】Menu management、定时任务批量delete清空选中
    });
  }
</script>
