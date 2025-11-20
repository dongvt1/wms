<template>
  <!--Reference table-->
  <div class="p-4">
    <BasicTable @register="registerTable">
      <template #bodyCell="{ column, text }">
        <template v-if="column.dataIndex === 'name'">
          <a>{{ text }}</a>
        </template>
      </template>
      <template #footer>footer</template>
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
      width: 300,
    },
    {
      title: 'age',
      dataIndex: 'age',
      key: 'age',
      width: 300,
    },
    {
      title: 'address',
      dataIndex: 'address',
      key: 'address',
      ellipsis: true,
    },
    {
      title: 'long content column',
      dataIndex: 'address',
      key: 'address 2',
      ellipsis: true,
    },
    {
      title: 'long content column',
      dataIndex: 'address',
      key: 'address 3',
      ellipsis: true,
    },
  ];
  // List page public parameters、method
  const { tableContext } = useListPage({
    designScope: 'basic-table-demo',
    tableProps: {
      title: 'border table',
      dataSource: [
        {
          key: '1',
          name: 'Zhang San',
          age: 32,
          address: 'Nanli, Academy of Sciences, Datun Road, Chaoyang District, Beijing, China1Building No.3unit401',
        },
        {
          key: '2',
          name: 'Liu Si',
          age: 32,
          address: 'Shanghu Shijia, Shunsha Road, Changping District, Beijing, China2Building No.7unit503',
        },
      ],
      columns: columns,
      showActionColumn: false,
      useSearchForm: false,
    },
  });
  //registertabledata
  const [registerTable] = tableContext;
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
