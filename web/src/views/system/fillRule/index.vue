<template>
  <div :class="prefixCls">
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--slot:tabletitle-->
      <template #tableTitle>
        <a-button preIcon="ant-design:plus-outlined" type="primary" @click="handleAdd">New</a-button>
        <a-button type="primary" preIcon="ant-design:export-outlined" @click="onExportXls"> Export</a-button>
        <j-upload-button type="primary" preIcon="ant-design:import-outlined" @click="onImportXls">import</j-upload-button>
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
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
    <FillRuleModal @register="registerModal" @success="reload" />
  </div>
</template>

<script name="system-fillrule" lang="ts" setup>
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { getFillRuleList, exportUrl, importUrl, deleteFillRule, batchDeleteFillRule, handleTest } from '/@/views/system/fillRule/fill.rule.api';
  import { columns, searchFormSchema } from '/@/views/system/fillRule/fill.rule.data';
  import { useModal } from '/@/components/Modal';
  import { ActionItem } from '/@/components/Table';
  const [registerModal, { openModal }] = useModal();
  import FillRuleModal from '/@/views/system/fillRule/FillRuleModal.vue';

  // List page public parameters、method
  const { prefixCls, tableContext, createMessage, createSuccessModal, onExportXls, onImportXls } = useListPage({
    designScope: 'fill-rule',
    tableProps: {
      title: 'Value filling rule management page',
      api: getFillRuleList,
      columns: columns,
      showIndexColumn: true,
      formConfig: {
        schemas: searchFormSchema,
      },
    },
    exportConfig: {
      url: exportUrl,
      name: 'List of filling rules',
    },
    importConfig: {
      url: importUrl,
      success: () => reload(),
    },
  });
  // register ListTable
  const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext;

  /**
   * New事件
   */
  function handleAdd() {
    openModal(true, {
      isUpdate: false,
    });
  }

  /**
   * Edit event
   */
  function handleEdit(record) {
    console.log('record....', record);
    openModal(true, {
      record,
      isUpdate: true,
    });
  }

  /**
   * delete事件
   */
  async function handleDelete(record) {
    console.log(12345, record);
    await deleteFillRule({ id: record.id }, reload);
  }

  /**
   * 批量delete事件
   */
  async function batchHandleDelete() {
    await batchDeleteFillRule({ ids: selectedRowKeys.value }, () => {
      selectedRowKeys.value = [];
      reload();
    });
  }

  /**
   * Functional testing
   */
  function testRule(record) {
    let params = { ruleCode: record.ruleCode };
    handleTest(params).then((res) => {
      if (res.success) {
        createSuccessModal({
          title: '填值规则Functional testing',
          content: 'Generate results：' + res.result,
        });
      } else {
        createMessage.warn(res.message);
      }
    });
  }

  /**
   * edit
   */
  function getTableAction(record): ActionItem[] {
    return [{ label: 'edit', onClick: handleEdit.bind(null, record) }];
  }

  /**
   * 下拉Action bar
   */
  function getDropDownAction(record): ActionItem[] {
    return [
      { label: 'Functional testing', onClick: testRule.bind(null, record) },
      {
        label: 'delete',
        color: 'error',
        popConfirm: {
          title: '确认要delete吗？',
          confirm: handleDelete.bind(null, record),
        },
      },
    ];
  }
</script>
