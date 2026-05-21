<template>
  <div>
    <!--Reference table-->
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--Slot: table title-->
      <template #tableTitle>
        <a-button type="primary" @click="handleAdd" preIcon="ant-design:plus-outlined">
          Add New
        </a-button>
<!--        <a-button type="primary" preIcon="ant-design:export-outlined" @click="onExportXls">-->
<!--          Export-->
<!--        </a-button>-->
<!--        <j-upload-button type="primary" preIcon="ant-design:import-outlined" @click="onImportXls">-->
<!--          Import-->
<!--        </j-upload-button>-->
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined"></Icon>
                Delete
              </a-menu-item>
            </a-menu>
          </template>
          <a-button>
            Batch Operations
            <Icon icon="mdi:chevron-down"></Icon>
          </a-button>
        </a-dropdown>
      </template>
      <!--Action column-->
      <template #action="{ record }">
        <TableAction
          :actions="getTableAction(record)"
          :dropDownActions="getDropDownAction(record)"
        />
      </template>
    </BasicTable>

    <!-- Form area -->
    <SysTableWhiteListModal @register="registerModal" @success="handleSuccess"/>
  </div>
</template>

<script lang="ts" name="ahh-sysTableWhiteList" setup>
import {BasicTable, TableAction} from '/@/components/Table';
import {useModal} from '/@/components/Modal';
import {useListPage} from '/@/hooks/system/useListPage'
import SysTableWhiteListModal from './modules/SysTableWhiteListModal.vue'
import {columns, searchFormSchema} from './SysTableWhiteList.data';
import {batchDelete, deleteOne, getExportUrl, getImportUrl, list} from './SysTableWhiteList.api';

//Register modal
const [registerModal, {openModal}] = useModal();
//Register table data
const {prefixCls, tableContext, onExportXls, onImportXls} = useListPage({
  tableProps: {
    title: 'System Table Whitelist',
    api: list,
    columns,
    canResize: false,
    formConfig: {
      labelWidth: 120,
      schemas: searchFormSchema,
      autoSubmitOnEnter: true,
      showAdvancedButton: true,
    },
    actionColumn: {
      width: 120,
    },
  },
  exportConfig: {
    name: "System Table Whitelist",
    url: getExportUrl,
  },
  importConfig: {
    url: getImportUrl
  },
})

const [registerTable, {reload}, {rowSelection, selectedRowKeys}] = tableContext

/**
 * Add event
 */
function handleAdd() {
  openModal(true, {
    isUpdate: false,
    showFooter: true,
  });
}

/**
 * Edit event
 */
function handleEdit(record: Recordable) {
  openModal(true, {
    record,
    isUpdate: true,
    showFooter: true,
  });
}

/**
 * Details
 */
function handleDetail(record: Recordable) {
  openModal(true, {
    record,
    isUpdate: true,
    showFooter: false,
  });
}

/**
 * Delete event
 */
async function handleDelete(record) {
  await deleteOne({id: record.id}, reload);
}

/**
 * Batch delete event
 */
async function batchHandleDelete() {
  await batchDelete({ids: selectedRowKeys.value}, reload);
}

/**
 * Success callback
 */
function handleSuccess({isUpdate, values}) {
  reload();
}

/**
 * Action column
 */
function getTableAction(record) {
  return [
    {
      label: 'Edit',
      onClick: handleEdit.bind(null, record),
    }
  ]
}

/**
 * Dropdown action column
 */
function getDropDownAction(record) {
  return [
    {
      label: 'Details',
      onClick: handleDetail.bind(null, record),
    }, {
      label: 'Delete',
      popConfirm: {
        title: 'Are you sure to delete?',
        confirm: handleDelete.bind(null, record),
      }
    }
  ]
}
</script>
<style scoped>

</style>
