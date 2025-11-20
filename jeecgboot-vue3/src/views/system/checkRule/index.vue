<template>
  <div :class="prefixCls">
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--Slot: table title-->
      <template #tableTitle>
        <a-button preIcon="ant-design:plus-outlined" type="primary" @click="handleAdd">Add New</a-button>
        <a-button type="primary" preIcon="ant-design:export-outlined" @click="onExportXls"> Export</a-button>
        <j-upload-button type="primary" preIcon="ant-design:import-outlined" @click="onImportXls">Import</j-upload-button>
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined"></Icon>
                <span>Delete</span>
              </a-menu-item>
            </a-menu>
          </template>
          <a-button>
            <span>Batch Operations</span>
            <Icon icon="mdi:chevron-down"></Icon>
          </a-button>
        </a-dropdown>
      </template>

      <!--Action column-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
      </template>
    </BasicTable>

    <CheckRuleTestModal @register="testModal"></CheckRuleTestModal>
    <CheckRuleModal @register="registerModal" @success="reload"></CheckRuleModal>
  </div>
</template>

<script name="system-fillrule" lang="ts" setup>
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';

  import { useModal } from '/@/components/Modal';
  import {
    getCheckRuleList,
    exportUrl,
    importUrl,
    deleteCheckRule,
    batchDeleteCheckRule,
    validateCheckRule,
  } from '/@/views/system/checkRule/check.rule.api';
  import { columns, searchFormSchema } from '/@/views/system/checkRule/check.rule.data';
  import { ActionItem } from '/@/components/Table';

  import CheckRuleTestModal from '/@/views/system/checkRule/CheckRuleTestModal.vue';
  const [testModal, { openModal: openTestModal }] = useModal();

  import CheckRuleModal from '/@/views/system/checkRule/CheckRuleModal.vue';
  const [registerModal, { openModal }] = useModal();

  // List page common parameters and methods
  const { prefixCls, tableContext, createMessage, onExportXls, onImportXls, createSuccessModal } = useListPage({
    designScope: 'check-rule',
    tableProps: {
      title: 'Code Validation Rule Management',
      api: getCheckRuleList,
      columns: columns,
      showIndexColumn: true,
      formConfig: {
        schemas: searchFormSchema,
      },
    },
    exportConfig: {
      url: exportUrl,
      name: 'Code Validation Rule List',
    },
    importConfig: {
      url: importUrl,
      success: () => reload(),
    },
  });
  // Register ListTable
  const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext;

  /**
   * Add event
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
   * Delete event
   */
  async function handleDelete(record) {
    console.log(12345, record);
    await deleteCheckRule({ id: record.id }, reload);
  }

  /**
   * Batch delete event
   */
  async function batchHandleDelete() {
    await batchDeleteCheckRule({ ids: selectedRowKeys.value }, () => {
      selectedRowKeys.value = [];
      reload();
    });
  }

  /**
   * Function test
   */
  function testRule(record) {
    openTestModal(true, { ruleCode: record.ruleCode });
  }

  /**
   * Edit
   */
  function getTableAction(record): ActionItem[] {
    return [{ label: 'Edit', onClick: handleEdit.bind(null, record) }];
  }

  /**
   * Dropdown action column
   */
  function getDropDownAction(record): ActionItem[] {
    return [
      { label: 'Function Test', onClick: testRule.bind(null, record) },
      {
        label: 'Delete',
        color: 'error',
        popConfirm: {
          title: 'Are you sure to delete?',
          confirm: handleDelete.bind(null, record),
        },
      },
    ];
  }
</script>
