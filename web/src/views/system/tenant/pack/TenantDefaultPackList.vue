<template>
  <div>
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <template #tableTitle>
        <a-button preIcon="ant-design:user-add-outlined" type="primary" @click="handleAdd">+ Default package
        </a-button>
        <a-button
          v-if="selectedRowKeys.length > 0"
          preIcon="ant-design:delete-outlined"
          type="primary"
          @click="handlePackBatch"
          style="margin-right: 5px"
        >Batch delete
        </a-button>
      </template>
      <template #action="{ record }">
        <TableAction :actions="getActions(record)" />
      </template>
    </BasicTable>
    <!--  Package  -->
    <TenantPackMenuModal @register="registerPackMenuModal" @success="handleSuccess"/>
  </div>
</template>
<script lang="ts" name="tenant-default-pack" setup>
  import { ref, unref } from 'vue';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { deleteTenantPack, packList } from '../tenant.api';
  import { defalutPackColumns, defaultPackFormSchema } from "../tenant.data";
  import TenantPackMenuModal from './TenantPackMenuModal.vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { useUserStore } from '/@/store/modules/user';
  import {Modal} from "ant-design-vue";

  const { createMessage } = useMessage();
  const [registerModal, { openModal }] = useModal();
  const [registerPackMenuModal, { openModal: packModal }] = useModal();
  const userStore = useUserStore();

  // List page public parameters、method
  const { prefixCls, tableContext } = useListPage({
    designScope: 'tenant-template',
    tableProps: {
      api: packList,
      columns: defalutPackColumns,
      formConfig: {
        schemas: defaultPackFormSchema,
      },
      beforeFetch: (params) => {
        return Object.assign(params, { packType: 'default' });
      },
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
          title: '是否confirmdelete租户Package',
          confirm: handleDelete.bind(null, record.id),
        },
      },
    ];
  }

  /**
   * editPackage
   */ 
  function handleAdd() {
    packModal(true, {
      isUpdate: false,
      packType:'default',
      showFooter: true
    });
  }
  
  
  /**
   * deleteDefault package包
   */ 
  async function handleDelete(id) {
    await deleteTenantPack({ ids: id }, handleSuccess);
  }
  /**
   * edit
   */
  function handleEdit(record) {
    packModal(true, {     
      isUpdate: true,
      record: record,
      packType:'default',
      showFooter: true
    });
  }

  /**
   * 新增Package
   */
  async function handlePack() {
    if (unref(selectedRowKeys).length > 1) {
      createMessage.warn('Please select one');
      return;
    }
    packModal(true, {
      tenantId: unref(selectedRowKeys.value.join(',')),
    });
  }

  /**
   * delete成功之后回调事件
   */
  function handleSuccess() {
    (selectedRowKeys.value = []) && reload();
  }

  /**
   * Batch deletePackage
   */
  async function handlePackBatch() {
    Modal.confirm({
      title: 'delete租户Package',
      content: '是否delete租户Package',
      okText: 'confirm',
      cancelText: 'Cancel',
      onOk: async () => {
        await deleteTenantPack({ ids: selectedRowKeys.value.join(',')}, handleSuccess);
      }
    })
  }
</script>
