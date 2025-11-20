<template>
  <div>
    <!--Reference table-->
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--slot:tabletitle-->
      <template #tableTitle>
        <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleCreate"> New</a-button>
        <a-button type="primary" preIcon="ant-design:export-outlined" @click="onExportXls" > Export</a-button>
<!--        <j-upload-button type="primary" preIcon="ant-design:import-outlined" @click="onImportXls" v-auth="'system:user:import'">import</j-upload-button>-->
        <import-excel-progress :upload-url="getImportUrl" @success="reload"></import-excel-progress>
        <a-button type="primary" @click="openModal(true, {})" preIcon="ant-design:hdd-outlined"> recycle bin</a-button>
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined"></Icon>
                delete
              </a-menu-item>
              <a-menu-item key="2" @click="batchFrozen(2)">
                <Icon icon="ant-design:lock-outlined"></Icon>
                freeze
              </a-menu-item>
              <a-menu-item key="3" @click="batchFrozen(1)">
                <Icon icon="ant-design:unlock-outlined"></Icon>
                thaw
              </a-menu-item>
            </a-menu>
          </template>
          <a-button
            >Batch operations
            <Icon icon="mdi:chevron-down"></Icon>
          </a-button>
        </a-dropdown>
      </template>
      <!--Action bar-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
      </template>
    </BasicTable>
    <!--user drawer-->
    <UserDrawer @register="registerDrawer" @success="handleSuccess" />
    <!--Change password-->
    <PasswordModal @register="registerPasswordModal" @success="reload" />
    <!--recycle bin-->
    <UserRecycleBinModal @register="registerModal" @success="reload" />
    <!-- Resigned personnel list pop-up window -->
    <UserQuitModal @register="registerQuitModal" @success="reload" />
  </div>
</template>

<script lang="ts" name="system-user" setup>
  //tsgrammar
  import { ref, computed, unref } from 'vue';
  import { BasicTable, TableAction, ActionItem } from '/@/components/Table';
  import UserDrawer from './UserDrawer.vue';
  import UserRecycleBinModal from './UserRecycleBinModal.vue';
  import PasswordModal from './PasswordModal.vue';
  import JThirdAppButton from '/@/components/jeecg/thirdApp/JThirdAppButton.vue';
  import UserQuitModal from './UserQuitModal.vue';
  import { useDrawer } from '/@/components/Drawer';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { useModal } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { columns, searchFormSchema } from './user.data';
  import { listNoCareTenant, deleteUser, batchDeleteUser, getImportUrl, getExportUrl, frozenBatch } from './user.api';
  import { usePermission } from '/@/hooks/web/usePermission';
  import ImportExcelProgress from './components/ImportExcelProgress.vue';

  const { createMessage, createConfirm } = useMessage();
  const { isDisabledAuth } = usePermission();
  //registerdrawer
  const [registerDrawer, { openDrawer }] = useDrawer();
  //recycle binmodel
  const [registerModal, { openModal }] = useModal();
  //passwordmodel
  const [registerPasswordModal, { openModal: openPasswordModal }] = useModal();
  //agentmodel
  const [registerAgentModal, { openModal: openAgentModal }] = useModal();
  //离职agentmodel
  const [registerQuitAgentModal, { openModal: openQuitAgentModal }] = useModal();
  //List of resigned usersmodel
  const [registerQuitModal, { openModal: openQuitModal }] = useModal();

  // List page public parameters、method
  const { prefixCls, tableContext, onExportXls, onImportXls } = useListPage({
    designScope: 'user-list',
    tableProps: {
      title: 'User list',
      api: listNoCareTenant,
      columns: columns,
      size: 'small',
      formConfig: {
        // labelWidth: 200,
        schemas: searchFormSchema,
      },
      actionColumn: {
        width: 120,
      },
      beforeFetch: (params) => {
        return Object.assign({ column: 'createTime', order: 'desc' }, params);
      },
    },
    exportConfig: {
      name: 'User list',
      url: getExportUrl,
    },
    importConfig: {
      url: getImportUrl,
    },
  });

  //registertabledata
  const [registerTable, { reload, updateTableDataRecord }, { rowSelection, selectedRows, selectedRowKeys }] = tableContext;

  /**
   * New事件
   */
  function handleCreate() {
    openDrawer(true, {
      isUpdate: false,
      showFooter: true,
      tenantSaas: false,
    });
  }
  /**
   * Edit event
   */
  async function handleEdit(record: Recordable) {
    openDrawer(true, {
      record,
      isUpdate: true,
      showFooter: true,
      tenantSaas: false,
    });
  }
  /**
   * Details
   */
  async function handleDetail(record: Recordable) {
    openDrawer(true, {
      record,
      isUpdate: true,
      showFooter: false,
      tenantSaas: false,
    });
  }
  /**
   * delete事件
   */
  async function handleDelete(record) {
    if ('admin' == record.username) {
      createMessage.warning('Administrator account does not allow this operation！');
      return;
    }
    await deleteUser({ id: record.id }, reload);
  }
  /**
   * 批量delete事件
   */
  async function batchHandleDelete() {
    let hasAdmin = unref(selectedRows).filter((item) => item.username == 'admin');
    if (unref(hasAdmin).length > 0) {
      createMessage.warning('Administrator account does not allow this operation！');
      return;
    }
    await batchDeleteUser({ ids: selectedRowKeys.value }, () => {
      selectedRowKeys.value = [];
      reload();
    });
  }
  /**
   * successful callback
   */
  function handleSuccess() {
    reload();
  }

  /**
   * 打开Change password弹窗
   */
  function handleChangePassword(username) {
    openPasswordModal(true, { username });
  }
  /**
   * freezethaw
   */
  async function handleFrozen(record, status) {
    if ('admin' == record.username) {
      createMessage.warning('Administrator account does not allow this operation！');
      return;
    }
    await frozenBatch({ ids: record.id, status: status }, reload);
  }
  /**
   * 批量freezethaw
   */
  function batchFrozen(status) {
    let hasAdmin = selectedRows.value.filter((item) => item.username == 'admin');
    if (unref(hasAdmin).length > 0) {
      createMessage.warning('Administrator account does not allow this operation！');
      return;
    }
    createConfirm({
      iconType: 'warning',
      title: 'Confirm action',
      content: 'whether' + (status == 1 ? 'thaw' : 'freeze') + 'Select account?',
      onOk: async () => {
        await frozenBatch({ ids: unref(selectedRowKeys).join(','), status: status }, reload);
      },
    });
  }

  /**
   *Synchronous DingTalk and WeChat callbacks
   */
  function onSyncFinally({ isToLocal }) {
    // 同步到本地时刷新下data
    if (isToLocal) {
      reload();
    }
  }

  /**
   * Action bar
   */
  function getTableAction(record): ActionItem[] {
    return [
      {
        label: 'edit',
        onClick: handleEdit.bind(null, record),
        // ifShow: () => hasPermission('system:user:edit'),
      },
    ];
  }
  /**
   * 下拉Action bar
   */
  function getDropDownAction(record): ActionItem[] {
    return [
      {
        label: 'Details',
        onClick: handleDetail.bind(null, record),
      },
      {
        label: 'password',
        //auth: 'user:changepwd',
        onClick: handleChangePassword.bind(null, record.username),
      },
      {
        label: 'delete',
        popConfirm: {
          title: 'whether确认delete',
          confirm: handleDelete.bind(null, record),
        },
      },
      {
        label: 'freeze',
        ifShow: record.status == 1,
        popConfirm: {
          title: '确定freeze吗?',
          confirm: handleFrozen.bind(null, record, 2),
        },
      },
      {
        label: 'thaw',
        ifShow: record.status == 2,
        popConfirm: {
          title: '确定thaw吗?',
          confirm: handleFrozen.bind(null, record, 1),
        },
      },
    ];
  }

</script>

<style scoped></style>
