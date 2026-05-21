<template>
  <BasicTable @register="registerTable">
    <template #tableTitle>
      <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleCreate"> New</a-button>
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
      <div style="margin-left: 10px;margin-top: 5px">Currently logged in tenant: <span class="tenant-name">{{loginTenantName}}</span> </div>
    </template>
    <template #action="{ record }">
      <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
    </template>
  </BasicTable>
  <!--role user form-->
  <RoleUserTable @register="roleUserDrawer" :disableUserEdit="true"/>
  <!--Character edit drawer-->
  <RoleDrawer @register="registerDrawer" @success="reload" :showFooter="showFooter" />
  <!--Role details-->
  <RoleDesc @register="registerDesc"></RoleDesc>
</template>
<script lang="ts" name="tenant-role-list" setup>
  import { onMounted, ref } from 'vue';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useDrawer } from '/@/components/Drawer';
  import { useModal } from '/@/components/Modal';
  import RoleDesc from './components/RoleDesc.vue';
  import RoleDrawer from './components/RoleDrawer.vue';
  import RoleUserTable from './components/RoleUserTable.vue';
  import { columns, searchFormSchema } from './role.data';
  import { listByTenant, deleteRole, batchDeleteRole, getExportUrl, getImportUrl } from './role.api';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { getLoginTenantName } from "/@/views/system/tenant/tenant.api";
  import { tenantSaasMessage } from "@/utils/common/compUtils";
  
  const showFooter = ref(true);
  const [roleUserDrawer, { openDrawer: openRoleUserDrawer }] = useDrawer();
  const [registerDrawer, { openDrawer }] = useDrawer();
  const [registerModal, { openModal }] = useModal();
  const [registerDesc, { openDrawer: openRoleDesc }] = useDrawer();
  
  // List page public parameters、method
  const { prefixCls, tableContext, onImportXls, onExportXls } = useListPage({
    designScope: 'role-template',
    tableProps: {
      title: 'Tenant role list',
      api: listByTenant,
      columns: columns,
      formConfig: {
        schemas: searchFormSchema,
      },
      actionColumn: {
        width: 120,
      },
      rowSelection: null,
      //Customize default sorting
      defSort: {
        column: 'id',
        order: 'desc',
      },
    },
    exportConfig: {
      name: 'role list',
      url: getExportUrl,
    },
    importConfig: {
      url: getImportUrl,
    },
  });
  const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext;

  /**
   * New
   */
  function handleCreate() {
    showFooter.value = true;
    openDrawer(true, {
      isUpdate: false,
    });
  }
  /**
   * edit
   */
  function handleEdit(record: Recordable) {
    showFooter.value = true;
    openDrawer(true, {
      record,
      isUpdate: true,
    });
  }
  /**
   * Details
   */
  function handleDetail(record) {
    showFooter.value = false;
    openRoleDesc(true, {
      record,
      isUpdate: true,
    });
  }
  /**
   * delete事件
   */
  async function handleDelete(record) {
    await deleteRole({ id: record.id }, reload);
  }
  /**
   * 批量delete事件
   */
  async function batchHandleDelete() {
    await batchDeleteRole({ ids: selectedRowKeys.value }, reload);
  }
  /**
   * role user
   */
  function handleUser(record) {
    //onSelectChange(selectedRowKeys)
    openRoleUserDrawer(true, record);
  }
  /**
   * Action bar
   */
  function getTableAction(record) {
    return [
      {
        label: 'user',
        onClick: handleUser.bind(null, record),
      },
    ];
  }

  /**
   * 下拉Action bar
   */
  function getDropDownAction(record) {
    return [
      {
        label: 'edit',
        onClick: handleEdit.bind(null, record),
      },
      {
        label: 'Details',
        onClick: handleDetail.bind(null, record),
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

  const loginTenantName = ref<string>('');
  
  getTenantName();
  
  async function getTenantName(){
    loginTenantName.value = await getLoginTenantName();
  }

  onMounted(()=>{
    tenantSaasMessage('Tenant role')
  })
</script>

<style scoped lang="less">
  .tenant-name{
    text-decoration:underline;
    margin: 5px;
    font-size: 15px;
  }
</style>
