<template>
  <div>
    <BasicTable @register="registerTable" :rowSelection="rowSelection" @fetch-success="onFetchSuccess">
      <template #tableTitle>
        <a-button
          preIcon="ant-design:user-add-outlined"
          type="primary"
          @click="handleInvitation"
          style="margin-right: 5px"
          :disabled="selectedRowKeys.length === 0"
          >Invite users to join</a-button
        >
<!--        <a-button
          preIcon="ant-design:plus-outlined"
          type="primary"
          @click="handlePack"
          style="margin-right: 5px"
          :disabled="selectedRowKeys.length === 0"
          >combo</a-button
        >-->
      </template>
      <template #action="{ record }">
        <TableAction :actions="getActions(record)" />
      </template>
    </BasicTable>
    <TenantInviteUserModal @register="registerSelUserModal" @inviteOk="handleInviteUserOk" />
    <TenantUserModal @register="registerTenUserModal" />
    <!--  product package  -->
    <TenantPackList @register="registerPackModal" />
  </div>
</template>
<script lang="ts" name="tenant-my-tenant-list" setup>
  import { onMounted, ref, unref } from 'vue';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { invitationUserJoin, getTenantPageListByUserId } from '../tenant.api';
  import { columns, searchFormSchema } from '../tenant.data';
  import TenantModal from '../components/TenantModal.vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useListPage } from '/@/hooks/system/useListPage';
  import TenantInviteUserModal from '../components/TenantInviteUserModal.vue';
  import TenantUserModal from '../components/TenantUserList.vue';
  import TenantPackList from '../pack/TenantPackList.vue';
  import { getTenantId } from '/@/utils/auth';
  import { useUserStore } from '/@/store/modules/user';
  import { tenantSaasMessage } from "@/utils/common/compUtils";

  const { createMessage } = useMessage();
  const [registerModal, { openModal }] = useModal();
  const [registerSelUserModal, { openModal: userOpenModal }] = useModal();
  const [registerTenUserModal, { openModal: tenUserOpenModal }] = useModal();
  const [registerPackModal, { openModal: packModal }] = useModal();
  const userStore = useUserStore();

  // List page public parameters、method
  const { prefixCls, tableContext } = useListPage({
    designScope: 'tenant-template',
    tableProps: {
      title: 'Tenant list',
      api: getTenantPageListByUserId,
      columns: columns,
      formConfig: {
        schemas: searchFormSchema,
      },
      actionColumn: {
        width: 150,
        fixed: 'right',
      },
      rowSelection:{
        type: "radio",
      },
      beforeFetch: (params) => {
        return Object.assign(params, { userTenantStatus: '1,3,4' });
      },
    },
  });
  const [registerTable, { reload }, { rowSelection, selectedRowKeys, selectedRows }] = tableContext;

  // The current tenant is selected by default
  function onFetchSuccess(data) {
    let items = data.items;
    console.log('items:', items);
    // Currently logged in tenantID
    let loginTenantId = getTenantId();
    console.log('loginTenantId:', loginTenantId);
    // 如果Currently logged in tenantIDin list，is selected by default
    if (items && items.length > 0 && loginTenantId) {
      for (let i = 0; i < items.length; i++) {
        if (items[i].id == loginTenantId) {
          console.log('items[i].id:', items[i].id);
          selectedRowKeys.value = [items[i].id];
          selectedRows.value = [items[i]];
          return;
        }
      }
    }
  }

  /**
   * Operation column definition
   * @param record
   */
  function getActions(record) {
    return [
      {
        label: 'user',
        onClick: handleSeeUser.bind(null, record.id),
      },
    ];
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
    if (phone) {
      await invitationUserJoin({ ids: selectedRowKeys.value.join(','), phone: phone });
    }
    if (username) {
      await invitationUserJoin({ ids: selectedRowKeys.value.join(','), username: username });
    }
  }

  /**
   * 查看user
   * @param id
   */
  async function handleSeeUser(id) {
    tenUserOpenModal(true, {
      id: id,
    });
  }

  /**
   * 新增product package
   */
  async function handlePack() {
    if (unref(selectedRowKeys).length > 1) {
      createMessage.warn('Please select one');
      return;
    }
    packModal(true, {
      tenantId: unref(selectedRowKeys.value.join(',')),
      //my tenants不显示新增和编辑product package
      showPackAddAndEdit: false
    });
  }

  /**
   * Callback event after successful deletion
   */
  function handleSuccess() {
    (selectedRowKeys.value = []) && reload();
  }

  onMounted(()=>{
    //Prompt message
    tenantSaasMessage('my tenants')
  })
</script>
