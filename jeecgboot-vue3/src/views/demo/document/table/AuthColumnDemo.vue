<template>
  <div class="p-4">
    <BasicTable @register="registerTable">
      <template #action="{ record }">
        <TableAction
          :actions="[
            {
              label: 'edit',
              onClick: handleEdit.bind(null, record),
              auth: 'demo:btn:show', // Control whether to display based on permissions: No permission，Don't show
            },
            {
              label: 'delete',
              icon: 'ic:outline-delete-outline',
              onClick: handleDelete.bind(null, record),
              auth: 'super', // Control whether to display based on permissions: Have authority，will be displayed
            },
          ]"
          :dropDownActions="[
            {
              label: 'enable',
              popConfirm: {
                title: '是否enable？',
                confirm: handleOpen.bind(null, record),
              },
              ifShow: (_action) => {
                return record.status !== 'enable'; // Whether to display based on business control: Noenablestate的Don't showenable按钮
              },
            },
            {
              label: 'Disable',
              popConfirm: {
                title: '是否Disable？',
                confirm: handleOpen.bind(null, record),
              },
              ifShow: () => {
                return record.status === 'enable'; // Whether to display based on business control: enablestate的显示Disable按钮
              },
            },
            {
              label: 'Simultaneous control',
              popConfirm: {
                title: 'Whether to display dynamically？',
                confirm: handleOpen.bind(null, record),
              },
              auth: 'super', // At the same time, whether to display based on permissions and business control
              ifShow: () => {
                return true;
              },
            },
          ]"
        />
      </template>
    </BasicTable>
  </div>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { BasicTable, useTable, BasicColumn, TableAction } from '/@/components/Table';

  import { demoListApi } from '/@/api/demo/table';
  import { useListPage } from '/@/hooks/system/useListPage';
  const columns: BasicColumn[] = [
    {
      title: 'serial number',
      dataIndex: 'no',
      width: 100,
    },
    {
      title: 'Name',
      dataIndex: 'name',
      auth: 'demo:field:show', // Control whether to display based on permissions: No permission，Don't show
    },
    {
      title: 'state',
      dataIndex: 'status',
    },
    {
      title: 'address',
      dataIndex: 'address',
      auth: 'super', // At the same time, whether to display based on permissions and business control
      ifShow: (_column) => {
        return true;
      },
    },
    {
      title: 'start time',
      dataIndex: 'beginTime',
    },
    {
      title: 'end time',
      dataIndex: 'endTime',
      width: 200,
    },
  ];
  export default defineComponent({
    components: { BasicTable, TableAction },
    setup() {
      const { tableContext } = useListPage({
        tableProps: {
          title: 'Permission column',
          api: demoListApi,
          columns: columns,
          bordered: true,
          useSearchForm: false,
          actionColumn: {
            width: 250,
            title: 'Action',
            dataIndex: 'action',
            slots: { customRender: 'action' },
          },
        },
      });

      //registertabledata
      const [registerTable] = tableContext;

      function handleEdit(record: Recordable) {
        console.log('点击了edit', record);
      }
      function handleDelete(record: Recordable) {
        console.log('点击了delete', record);
      }
      function handleOpen(record: Recordable) {
        console.log('点击了enable', record);
      }
      return {
        registerTable,
        handleEdit,
        handleDelete,
        handleOpen,
      };
    },
  });
</script>
