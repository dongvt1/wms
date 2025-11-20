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
      title: 'name',
      dataIndex: 'name',
      customCell: (record, index, column) => ({
        colSpan: index < 4 ? 1 : 5,
      }),
      customRender: ({ text, record, index, column }) => {
        return index < 4 ? text : `${record.name}/${record.age}/${record.address}/${record.phone}`;
      },
    },
    {
      title: 'age',
      dataIndex: 'age',
      customCell: (record, index, column) => {
        if (index == 4) {
          return { colSpan: 0 };
        }
      },
    },
    {
      title: 'home address',
      dataIndex: 'address',
      customCell: (record, index, column) => {
        if (index == 4) {
          return { colSpan: 0 };
        }
      },
    },
    {
      title: 'Contact number',
      colSpan: 2,
      dataIndex: 'tel',
      customCell: (record, index, column) => {
        if (index === 2) {
          return { rowSpan: 2 };
        }
        if (index === 3) {
          return { rowSpan: 0 };
        }
        if (index === 4) {
          return { colSpan: 0 };
        }
      },
    },
    {
      title: 'Phone',
      colSpan: 0,
      dataIndex: 'phone',
      customCell: (record, index, column) => {
        if (index === 4) {
          return { colSpan: 0 };
        }
      },
    },
  ];
  // List page public parameters、method
  const { tableContext } = useListPage({
    designScope: 'basic-table-demo',
    tableProps: {
      title: 'Merge rows and columns',
      dataSource: [
        {
          key: '1',
          name: 'Yin Jiale',
          age: 32,
          tel: '0319-5972018',
          phone: 17600080009,
          address: 'Changping District, Beijing',
        },
        {
          key: '2',
          name: 'Long Jiayu',
          tel: '0319-5972018',
          phone: 17600060007,
          age: 42,
          address: 'Haidian District, Beijing',
        },
        {
          key: '3',
          name: 'He Zehui',
          age: 32,
          tel: '0319-5972018',
          phone: 17600040005,
          address: 'Mentougou District, Beijing',
        },
        {
          key: '4',
          name: 'Shen Yong',
          age: 18,
          tel: '0319-5972018',
          phone: 17600010003,
          address: 'Chaoyang District, Beijing',
        },
        {
          key: '5',
          name: 'Bai Jiayi',
          age: 18,
          tel: '0319-5972018',
          phone: 17600010002,
          address: 'Fengtai District, Beijing',
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
