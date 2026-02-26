<template>
  <div>
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <template #tableTitle>
        <a-button preIcon="ant-design:plus-outlined" type="primary" @click="handleAdd" style="margin-right: 5px">New</a-button>
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
        <a-button
          preIcon="ant-design:user-add-outlined"
          type="primary"
          @click="handleInvitation"
          style="margin-right: 5px"
          :disabled="selectedRowKeys.length === 0"
          >Invite users to join</a-button
        >
        <a-button
          preIcon="ant-design:sliders-outlined"
          type="primary"
          @click="handlePack"
          style="margin-right: 5px"
          :disabled="selectedRowKeys.length === 0"
          >Package management</a-button
        >
        <a-button type="primary" @click="recycleBinClick" preIcon="ant-design:hdd-outlined">recycle bin</a-button>
      </template>
      <template #action="{ record }">
        <TableAction :actions="getActions(record)" />
      </template>
    </BasicTable>
    <TenantModal @register="registerModal" @success="reload" />
    <TenantInviteUserModal @register="registerSelUserModal" @inviteOk="handleInviteUserOk"/>
    <TenantUserModal @register="registerTenUserModal" />
    <!--  product package  -->
    <TenantPackList @register="registerPackModal" />
    <!--  租户recycle bin  -->
    <TenantRecycleBinModal @register="registerRecycleBinModal" @success="reload" />
  </div>
</template>
<script lang="ts" name="system-tenant" setup>
  import { ref, unref } from 'vue';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { getTenantList, deleteTenant, batchDeleteTenant, invitationUserJoin } from './tenant.api';
  import { columns, searchFormSchema } from './tenant.data';
  import TenantModal from './components/TenantModal.vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useListPage } from '/@/hooks/system/useListPage';
  import TenantInviteUserModal from './components/TenantInviteUserModal.vue';
  import TenantUserModal from './components/TenantUserList.vue';
  import TenantPackList from './pack/TenantPackList.vue';
  import TenantRecycleBinModal from './components/TenantRecycleBinModal.vue';

  const { createMessage } = useMessage();
  const [registerModal, { openModal }] = useModal();
  const [registerSelUserModal, { openModal: userOpenModal }] = useModal();
  const [registerTenUserModal, { openModal: tenUserOpenModal }] = useModal();
  const [registerPackModal, { openModal: packModal }] = useModal();
  const [registerRecycleBinModal, { openModal: recycleBinModal }] = useModal();

  // List page public parameters、method
  const { prefixCls, tableContext } = useListPage({
    designScope: 'tenant-template',
    tableProps: {
      title: 'Tenant list',
      api: getTenantList,
      columns: columns,
      formConfig: {
        schemas: searchFormSchema,
        fieldMapToTime: [['fieldTime', ['beginDate', 'endDate'], 'YYYY-MM-DD HH:mm:ss']],
      },
      actionColumn:{
        width: 150,
        fixed:'right'
      }
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
        label: 'edit',
        onClick: handleEdit.bind(null, record),
      },
      {
        label: 'delete',
        popConfirm: {
          title: '是否确认delete',
          placement: 'left',
          confirm: handleDelete.bind(null, record),
        },
      },
      {
        label: 'user',
        onClick: handleSeeUser.bind(null, record.id),
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
    await deleteTenant({ id: record.id }, handleSuccess);
  }

  /**
   * 批量delete事件
   */
  async function batchHandleDelete() {
    await batchDeleteTenant({ ids: selectedRowKeys.value }, handleSuccess);
  }

  /**
   * Invite users to join租户
   */
  function handleInvitation() {
    userOpenModal(true, {});
  }

  /**
   * user选择回调事件
   * @param options
   * @param phone
   * @param userSelectId
   */
  async function handleInviteUserOk(phone, username) {
    //update-begin---author:wangshuai ---date:20230314  for：【QQYUN-4605】Who to invite to join the tenant in the background，没办法选不是租户下的user------------
    if (phone) {
      await invitationUserJoin({ ids: selectedRowKeys.value.join(','), phone: phone });
    }
    if (username) {
      await invitationUserJoin({ ids: selectedRowKeys.value.join(','), username: username });
    }
    //update-end---author:wangshuai ---date:20230314  for：【QQYUN-4605】Who to invite to join the tenant in the background，没办法选不是租户下的user------------
  }

  /**
   * 查看user
   * @param id
   */
  function handleSeeUser(id) {
    tenUserOpenModal(true, {
      id: id,
    });
  }

  /**
   * Newproduct package
   */
  function handlePack() {
    if (unref(selectedRowKeys).length > 1) {
      createMessage.warn('Please select one');
      return;
    }
    packModal(true, {
      tenantId: unref(selectedRowKeys.value.join(',')),
      //我的租户显示New和editproduct package
      showPackAddAndEdit: true
    });
  }

  /**
   * recycle bin
   */
  function recycleBinClick() {
    recycleBinModal(true, {});
  }

  /**
   * delete成功之后回调事件
   */
  function handleSuccess() {
    (selectedRowKeys.value = []) && reload();
  }
</script>
