<template>
  <div class="p-4">
    <BasicTable @register="registerTable">
      <template #action="{ record }">
        <TableAction
          :actions="[
            {
              label: 'delete',
              icon: 'ic:outline-delete-outline',
              onClick: handleDelete.bind(null, record),
            },
          ]"
          :dropDownActions="[
            {
              label: 'enable',
              popConfirm: {
                title: '是否enable？',
                confirm: handleOpen.bind(null, record),
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
      title: 'ID',
      dataIndex: 'id',
      fixed: 'left',
      width: 280,
    },
    {
      title: 'Name',
      dataIndex: 'name',
      width: 260,
    },
    {
      title: 'address',
      dataIndex: 'address',
    },
    {
      title: 'serial number',
      dataIndex: 'no',
      width: 300,
    },
    {
      title: 'start time',
      width: 200,
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
          title: 'Fixed header and column example',
          api: demoListApi,
          columns: columns,
          canResize: false,
          scroll: { y: 200 },
          actionColumn: {
            width: 160,
            title: 'Action',
            dataIndex: 'action',
          },
          useSearchForm: false,
        },
      });

      const [registerTable] = tableContext;
      function handleDelete(record: Recordable) {
        console.log('点击了delete', record);
      }
      function handleOpen(record: Recordable) {
        console.log('点击了enable', record);
      }
      return {
        registerTable,
        handleDelete,
        handleOpen,
      };
    },
  });
</script>
