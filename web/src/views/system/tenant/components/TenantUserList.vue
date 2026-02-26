<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="title" @ok="handleSubmit" width="800px">
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <template #tableTitle>
        <a-button v-if="selectedRowKeys.length>0" preIcon="ant-design:delete-outlined" type="primary" @click="handleLeaveBatch" style="margin-right: 5px">Please leave in bulk</a-button>
      </template>
      <template #action="{ record }">
        <TableAction :actions="getActions(record)" />
      </template>
    </BasicTable>
  </BasicModal>
</template>
<script lang="ts" setup>
import { ref, computed, unref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { BasicForm, useForm } from '/@/components/Form/index';
import { userColumns, userSearchFormSchema } from "../tenant.data";
import { getTenantUserList, leaveTenant } from "../tenant.api";
import { useListPage } from "/@/hooks/system/useListPage";
import { BasicTable, TableAction } from '/@/components/Table';

const tenantId = ref<number>(0);
// List page public parameters、method
const { prefixCls, tableContext } = useListPage({
  designScope: 'tenant-template',
  tableProps: {
    api: getTenantUserList,
    columns: userColumns,
    immediate:false,
    formConfig: {
      schemas: userSearchFormSchema,
      //update-begin---author:wangshuai ---date:20230704  for：【QQYUN-5698】style issue------------
      labelWidth: 40,
      //update-end---author:wangshuai ---date:20230704  for：【QQYUN-5698】style issue------------
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
      return Object.assign(params, { userTenantId: unref(tenantId) });
    },
  },
});
const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext;

// Emitsstatement
const emit = defineEmits(['register', 'success']);
//form assignment
const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
  tenantId.value = data.id;
  success();
});
//Set title
const title = 'member';
//form submission event
async function handleSubmit(v) {
  closeModal();
}

function getActions(record) {
  return [
    {
      label: 'Remove',
      onClick: handleLeave.bind(null, record.id),
    },
  ]
}

/**
 * success
 */
function success() {
  (selectedRowKeys.value = []) && reload();
}

/**
 * please leave
 * @param id
 */
async function handleLeave(id) {
  await leaveTenant({userIds:id,tenantId:unref(tenantId)},success)
}

/**
 * Please leave in bulk
 */
async function  handleLeaveBatch(){
  await leaveTenant({userIds:selectedRowKeys.value.join(","),tenantId:unref(tenantId)},success)
}
</script>

<style scoped>
  /*update-begin---author:wangshuai ---date:20220705  for：Query componentinputwith suffix，Hide------------*/
  :deep(.ant-input-suffix){
    display: none;
  }
  /*update-begin---author:wangshuai ---date:20220705  for：Query componentinputwith suffix，Hide------------*/
</style>
