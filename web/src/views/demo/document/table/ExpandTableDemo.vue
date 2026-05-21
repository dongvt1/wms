<template>
  <PageWrapper
    title="Expandable table"
    content="Incompatiblescrollshare。TableActionComponents are configurablestopButtonPropagationTo prevent the click event of the action button from bubbling up，in order to cooperateTablecomponentexpandRowByClick"
  >
    <BasicTable @register="registerTable">
      <template #expandedRowRender="{ record }">
        <span>No: {{ record.no }} </span>
      </template>
      <template #action="{ record }">
        <TableAction
          stopButtonPropagation
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
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { BasicTable, useTable, TableAction, BasicColumn } from '/@/components/Table';
  import { PageWrapper } from '/@/components/Page';
  import { demoListApi } from '/@/api/demo/table';
  import { useListPage } from '/@/hooks/system/useListPage';
  const columns: BasicColumn[] = [
    {
      title: 'ID',
      dataIndex: 'id',
      fixed: 'left',
      width: 200,
    },
    {
      title: 'Name',
      dataIndex: 'name',
      width: 150,
      filters: [
        { text: 'Male', value: 'male' },
        { text: 'Female', value: 'female' },
      ],
    },
    {
      title: 'address',
      dataIndex: 'address',
      width: 300,
    },
    {
      title: 'serial number',
      dataIndex: 'no',
      width: 150,
      sorter: true,
      defaultHidden: true,
    },
    {
      title: 'start time',
      width: 150,
      sorter: true,
      dataIndex: 'beginTime',
    },
    {
      title: 'end time',
      width: 150,
      sorter: true,
      dataIndex: 'endTime',
    },
  ];
  export default defineComponent({
    components: { BasicTable, TableAction, PageWrapper },
    setup() {
      const { tableContext } = useListPage({
        designScope: 'basic-table-demo',
        tableProps: {
          api: demoListApi,
          title: 'Expandable table演示',
          titleHelpMessage: ['已enableexpandRowByClick', '已enablestopButtonPropagation'],
          columns: columns,
          rowKey: 'id',
          canResize: false,
          expandRowByClick: true,
          actionColumn: {
            width: 160,
            title: 'Action',
            dataIndex: 'action',
          },
          useSearchForm: false,
        },
      });
      //registertabledata
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
