<template>
  <div class="p-4">
    <!--Reference table-->
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--Action bar-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
    </BasicTable>
  </div>
</template>

<script lang="ts" name="basic-table-demo" setup>
  import { BasicColumn, BasicTable, TableAction } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  //Define table columns
  const columns: BasicColumn[] = [
    {
      title: 'Name',
      dataIndex: 'name',
      key: 'name',
      resizable: true,
    },
    {
      title: 'age',
      dataIndex: 'age',
      key: 'age',
    },
    {
      title: 'address',
      dataIndex: 'address',
      key: 'address',
    },
  ];
  // List page public parameters、method
  const { tableContext } = useListPage({
    designScope: 'basic-table-demo',
    tableProps: {
      title: 'Optional form',
      dataSource: [
        {
          id: '1',
          name: 'Hu Ge',
          age: 32,
          address: 'Lincui Road, Chaoyang District1Number',
        },
        {
          id: '2',
          name: 'Liu Shishi',
          age: 32,
          address: 'Baisha Road, Changping District1Number',
        },
      ],
      columns: columns,
      rowSelection: { type: 'checkbox' }, //The default is checkbox Multiple choice，can be set to radio Single choice
      useSearchForm: false,
    },
  });
  //registertabledata
  const [registerTable, { reload }, { rowSelection, selectedRows, selectedRowKeys }] = tableContext;
  /**
   * Action bar
   */
  function getTableAction(record): ActionItem[] {
    return [
      {
        label: 'edit',
        onClick: handleEdit.bind(null, record),
      },
    ];
  }

  function handleEdit(record) {
    console.log(record);
    console.log(selectedRows.value);
    console.log(selectedRowKeys.value);
  }
</script>

<style scoped></style>
