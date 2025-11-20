<template>
  <BasicTable @register="registerTable" :rowSelection="rowSelection">
    <template #action="{ record }">
      <TableAction :actions="getTableAction(record)" />
    </template>
  </BasicTable>
</template>

<script lang="ts" name="online-user" setup>
import { BasicTable, TableAction } from '/@/components/Table';
import { columns, searchFormSchema } from './OnlineUser.data';
import { list, forceLogout } from './OnlineUser.api';
import { useListPage } from '/@/hooks/system/useListPage';
import { useMessage } from "/@/hooks/web/useMessage";
// List page common parameters and methods
const { prefixCls, tableContext, onImportXls, onExportXls } = useListPage({
  designScope: 'online-user',
  tableProps: {
    //Online user rowKey default id will cause key duplication, leading to duplicate data on the page
    rowKey:'token',
    title: 'Online Users',
    api: list,
    columns: columns,
    formConfig: {
      schemas: searchFormSchema,
    },
    actionColumn: {
      width: 120,
    },
    rowSelection: null,
  },
});
const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext;
const $message = useMessage();

//Action column
function getTableAction(record) {
  return [
    {
      label: 'Force Logout',
      popConfirm: {
        title: 'Force logout user?',
        confirm: handleForce.bind(null, record),
      },
    },
  ];
}

/**
 * Force logout
 * @param record
 */
function handleForce(record) {
   forceLogout({ token: record.token }).then((res)=>{
     if(res.success){
       reload();
       $message.createMessage.success('Force logout user "'+record.realname+'" successful!');
     }else{
       $message.createMessage.warn(res.message);
     }
   })
}
</script>

<style scoped>

</style>