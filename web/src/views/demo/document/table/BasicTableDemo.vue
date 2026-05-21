<template>
  <div class="p-4">
    <!--Reference table-->
    <BasicTable @register="registerTable">
      <!--Action bar-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
    </BasicTable>
  </div>
</template>

<script lang="ts" name="basic-table-demo" setup>
  import { ActionItem, BasicColumn, BasicTable, TableAction } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  //Define table columns
  const columns: BasicColumn[] = [
    {
      title: 'Name',
      dataIndex: 'name',
      key: 'name',
      resizable: false,
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
      title: 'User list',
      dataSource: [
        {
          key: '1',
          name: 'Hu Ge',
          age: 32,
          address: 'Lincui Road, Chaoyang District1Number',
        },
        {
          key: '2',
          name: 'Liu Shishi',
          age: 32,
          address: 'Baisha Road, Changping District1Number',
        },
      ],
      columns: columns,
      size: 'large', //compact table large
      striped: false, //Zebra print set false
      showActionColumn: true,
      useSearchForm: false,
    },
  });
  //registertabledata
  const [registerTable, methods] = tableContext;
  console.log('methods', methods);
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
  }
</script>

<style scoped></style>
