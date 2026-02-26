<template>
  <div :class="prefixCls">
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--slot:tabletitle-->
      <template #tableTitle>
        <a-button type="primary" preIcon="ant-design:plus-outlined" @click="onAdd">New</a-button>
        <a-button type="primary" preIcon="ant-design:export-outlined" @click="onExportXls"> Export</a-button>
        <j-upload-button type="primary" preIcon="ant-design:import-outlined" @click="onImportXls">import</j-upload-button>
        <a-dropdown v-if="showBatchBtn">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="onDeleteBatch">
                <Icon icon="ant-design:delete-outlined"></Icon>
                <span>delete</span>
              </a-menu-item>
            </a-menu>
          </template>
          <a-button>
            <span>Batch operations</span>
            <Icon icon="mdi:chevron-down"></Icon>
          </a-button>
        </a-dropdown>
      </template>

      <!--Action bar-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
      </template>
    </BasicTable>
    <TemplateModal @register="registerModal" @success="reload" />
    <TemplateTestModal @register="registerTestModal" />
  </div>
</template>

<script lang="ts" setup name="message-template">
  import { unref, computed, toRaw } from 'vue';
  import { ActionItem, BasicTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { useListPage } from '/@/hooks/system/useListPage';
  import TemplateModal from './TemplateModal.vue';
  import TemplateTestModal from './TemplateTestModal.vue';
  import { Api, saveOrUpdate, list, deleteBatch } from './template.api';
  import { columns, searchFormSchema } from './template.data';
  import { useMessage } from '/@/hooks/web/useMessage';
  const { createMessage } = useMessage();

  // List page public parameters、method
  const { prefixCls, onExportXls, onImportXls, tableContext } = useListPage({
    designScope: 'message-template',
    tableProps: {
      title: 'Message center template list data',
      api: list,
      columns: columns,
      formConfig: {
        schemas: searchFormSchema,
      },
    },
    exportConfig: {
      url: Api.exportXls,
      name: 'Message center template list',
    },
    importConfig: {
      url: Api.importXls,
      success: () => reload(),
    },
  });

  // register ListTable
  const [registerTable, { reload, setLoading }, { rowSelection, selectedRowKeys, selectedRows }] = tableContext;
  const [registerModal, { openModal }] = useModal();
  const [registerTestModal, testModal] = useModal();
  const showBatchBtn = computed(() => selectedRowKeys.value.length > 0);

  function onAdd() {
    openModal(true, {
      title: 'New消息模板',
      isUpdate: false,
      showFooter: true,
      record: {},
    });
  }

  function onEdit(record) {
    if (record.useStatus === '1') {
      createMessage.warning('This template has already been applied，Editing prohibited!');
      return;
    }
    openModal(true, {
      title: 'Modify message template',
      isUpdate: true,
      record: record,
      showFooter: true,
    });
  }

  function onDelete(record) {
    if (record) {
      //update-begin-author:taoyan date:2022-7-14 for: VUEN-1652【bug】application状态下不允许delete
      if(record.useStatus == '1'){
        createMessage.warning('该模板已被application禁止delete!');
        return;
      }
      //update-end-author:taoyan date:2022-7-14 for: VUEN-1652【bug】application状态下不允许delete
      doDeleteDepart([record.id], false);
    }
  }

  /**
   * according to ids 批量delete
   * @param idListRef array
   * @param confirm Whether to display a confirmation prompt box
   */
  async function doDeleteDepart(idListRef, confirm = true) {
    const idList = unref(idListRef);
    if (idList.length > 0) {
      try {
        setLoading(true);
        await deleteBatch({ ids: idList.join(',') }, confirm);
        await reload();
      } finally {
        setLoading(false);
      }
    }
  }

  async function onDeleteBatch() {
    try {
      //update-begin-author:taoyan date:2022-7-14 for: VUEN-1652【bug】application状态下不允许delete
      let arr = toRaw(selectedRows.value);
      let temp = arr.filter(item=>item.useStatus=='1')
      if(temp.length>0){
        createMessage.warning('选中的模板已被application禁止delete!');
        return;
      }
      //update-end-author:taoyan date:2022-7-14 for: VUEN-1652【bug】application状态下不允许delete
      await doDeleteDepart(selectedRowKeys);
      selectedRowKeys.value = [];
    } finally {
    }
  }

  // Send message test
  function onSendTest(record) {
    testModal.openModal(true, { record });
  }

  /**
   * Action bar
   */
  function getTableAction(record): ActionItem[] {
    //update-begin---author:wangshuai ---date:20221123  for：[VUEN-2807]Add a viewing function to the message template------------
    return [{ label: 'Check', onClick: handleDetail.bind(null, record)}, { label: 'edit', onClick: onEdit.bind(null, record) }];
    //update-end---author:wangshuai ---date:20221123  for：[VUEN-2807]Add a viewing function to the message template------------
  }

  /**
   * 下拉Action bar
   */
  function getDropDownAction(record): ActionItem[] {
    return [
      { label: 'application', onClick: handleUse.bind(null, record) },
      { label: 'deactivate', onClick: handleNotUse.bind(null, record) },
      { label: 'Send test', onClick: onSendTest.bind(null, record) },
      {
        label: 'delete',
        color: 'error',
        popConfirm: {
          title: '确认要delete吗？',
          confirm: onDelete.bind(null, record),
        },
      },
    ];
  }

  /**
   * application
   */
  async function handleUse(record) {
    let param = { id: record.id, useStatus: '1' };
    await saveOrUpdate(param, true);
    await reload();
  }

  /**
   * deactivate
   */
  async function handleNotUse(record) {
    let param = { id: record.id, useStatus: '0' };
    await saveOrUpdate(param, true);
    await reload();
  }

  /**
   * Check
   * @param record
   */
  function handleDetail(record) {
    openModal(true,{
      title: "Message template details",
      isUpdate: true,
      showFooter: false,
      record:record
    })
  }
</script>

<style lang="less">
  @import 'index';
</style>
