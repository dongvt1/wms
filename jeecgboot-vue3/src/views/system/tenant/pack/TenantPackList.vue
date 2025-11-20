<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="title" @ok="handleSubmit" width="800px" :showCancelBtn="false" :showOkBtn="false">
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <template #tableTitle>
        <a-button preIcon="ant-design:plus-outlined" type="primary" @click="handleAdd" style="margin-right: 5px" v-if="showPackAddAndEdit">New </a-button>
        <a-button
          v-if="selectedRowKeys.length > 0"
          preIcon="ant-design:delete-outlined"
          type="primary"
          @click="handlePackBatch"
          style="margin-right: 5px"
          >Batch delete
        </a-button>
        <a-button
          preIcon="ant-design:sync-outlined"
          type="primary"
          @click="handleSyncDefaultPack"
          style="margin-right: 5px"
          >Initialize default package
        </a-button>
      </template>
      <template #action="{ record }">
        <TableAction :actions="getActions(record)" :dropDownActions="getDropDownAction(record)" />
      </template>
    </BasicTable>
  </BasicModal>
  <TenantPackMenuModal @register="registerPackMenu" @success="success" />
  <TenantPackUserModal @register="registerPackUser" @success="success" />
</template>
<script lang="ts" setup name="tenant-pack-modal">
  import { reactive, ref, unref } from 'vue';
  import { BasicModal, useModal, useModalInner } from '/@/components/Modal';
  import { packColumns, userColumns, packFormSchema } from '../tenant.data';
  import { getTenantUserList, leaveTenant, packList, deleteTenantPack, syncDefaultTenantPack } from '../tenant.api';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { BasicTable, TableAction } from '/@/components/Table';
  import TenantPackMenuModal from './TenantPackMenuModal.vue';
  import {Modal} from "ant-design-vue";
  import TenantPackUserModal from './TenantPackUserModal.vue';
  import {useMessage} from "/@/hooks/web/useMessage";

  const [registerPackMenu, { openModal }] = useModal();
  const [registerPackUser, { openModal: packUserOpenModal }] = useModal();
  const tenantId = ref<number>(0);
  // List page public parameters、method
  const { prefixCls, tableContext } = useListPage({
    designScope: 'tenant-template',
    tableProps: {
      api: packList,
      columns: packColumns,
      immediate: false,
      formConfig: {
        schemas: packFormSchema,
        labelCol: {
          xxl: 8,
        },
        actionColOptions: {
          xs: 24,
          sm: 8,
          md: 8,
          lg: 8,
          xl: 8,
          xxl: 8,
        },
      },
      beforeFetch: (params) => {
        return Object.assign(params, { tenantId: unref(tenantId), packType:'custom' });
      },
    },
  });
  const [registerTable, { reload }, { rowSelection, selectedRowKeys, selectedRows }] = tableContext;
  // Emitsstatement
  const emit = defineEmits(['register', 'success']);
  //是否显示New和edit套餐包
  const showPackAddAndEdit = ref<boolean>(false);
  //form assignment
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    tenantId.value = data.tenantId;
    showPackAddAndEdit.value = data.showPackAddAndEdit;
    success();
  });
  //Set title
  const title = 'Tenant personalized package';

  //form submission event
  async function handleSubmit(v) {
    closeModal();
  }

  function getActions(record) {
    return [
      {
        label: 'user',
        onClick: seeTenantPackUser.bind(null, record),
      },
      {
        label: 'edit',
        onClick: handleEdit.bind(null, record),
        ifShow: ()=>{ return showPackAddAndEdit.value }
      },
    ];
  }

  /**
   * success
   */
  function success() {
    (selectedRowKeys.value = []) && reload();
  }

  /**
   * edit
   * @param record
   */
  async function handleEdit(record) {
    openModal(true, {
      isUpdate: true,
      record: record,
      tenantId: unref(tenantId),
      packType:'custom',
      showFooter: true
    });
  }

  //Default system packages are not allowed to be deleted,Include(super administrator、Organization Account Administrator、Organization application administrator)
  const packCode = reactive<any>(['superAdmin','accountAdmin','appAdmin']);
  const { createMessage } = useMessage();
  
  /**
   * Delete package
   * @param delete
   */
  async function handleDelete(record) {
    //update-begin---author:wangshuai ---date:20230222  for：系统默认套餐包不允许delete------------
    if(packCode.indexOf(record.packCode) != -1){
        createMessage.warning("Default system packages are not allowed to be deleted");
       return;
    }
    //update-end---author:wangshuai ---date:20230222  for：系统默认套餐包不允许delete------------
    //update-begin---author:wangshuai---date:2025-09-03---for:默认套餐不允许delete---
    if(record.packCode && record.packCode.indexOf("default") != -1){
      createMessage.warning("默认套餐包不允许delete");
      return;
    }
    //update-end---author:wangshuai---date:2025-09-03---for:默认套餐不允许delete---
    await deleteTenantPack({ ids: record.id }, success);
  }

  /**
   * Batch delete套餐包
   */
  async function handlePackBatch() {
    let value = selectedRows.value;
    if(value && value.length>0){
      for (let i = 0; i < value.length; i++) {
        if(packCode.indexOf(value[i].packCode) != -1){
          createMessage.warning("Default system packages are not allowed to be deleted");
          return;
        }
        //update-begin---author:wangshuai---date:2025-09-03---for:默认套餐不允许delete---
        if(value[i].packCode && value[i].packCode.indexOf("default") != -1){
          createMessage.warning("默认套餐包不允许delete");
          return;
        }
        //update-end---author:wangshuai---date:2025-09-03---for:默认套餐不允许delete---
      }
    }
    Modal.confirm({
      title: 'delete租户套餐包',
      content: '是否delete租户套餐包',
      okText: 'confirm',
      cancelText: 'Cancel',
      onOk: async () => {
        await deleteTenantPack({ ids: selectedRowKeys.value.join(',')}, success);
      }
    })
  }

  async function handleSyncDefaultPack() {
    Modal.confirm({
      title: 'Initialize default package包',
      content: '是否Initialize default package包',
      okText: 'confirm',
      cancelText: 'Cancel',
      onOk: async () => {
        await syncDefaultTenantPack({tenantId: unref(tenantId)}, success);
      },
    });
  }

  /**
   *
   * New表单
   */
  function handleAdd() {
    openModal(true, {
      isUpdate: false,
      tenantId: unref(tenantId),
      packType:'custom',
      showFooter: true
    });
  }

  /**
   * 套餐包下面的user
   * @param record
   */
  function seeTenantPackUser(record) {
    packUserOpenModal(true,{
      record:record
    })
  }

  /**
   * More
   * @param record
   */
  function getDropDownAction(record) {
    return [
      {
        label: 'Details',
        onClick: handleDetail.bind(null, record),
      },
      {
        label: 'delete',
        popConfirm: {
          title: '是否confirmdelete租户套餐包',
          confirm: handleDelete.bind(null, record),
        },
      },
    ]
  }

  /**
   * Details
   * @param record
   */
  function handleDetail(record) {
    openModal(true, {
      isUpdate: true,
      record: record,
      tenantId: unref(tenantId),
      packType:'custom',
      showFooter: false
    });
  }
</script>
