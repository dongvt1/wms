<template>
  <div class="p-4">
    <!--Reference table-->
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <template #tableTitle>
        <a-button type="primary" preIcon="ant-design:export-outlined" @click="onExportXls"> Export</a-button>
        <j-upload-button type="primary" preIcon="ant-design:import-outlined" @click="onImportXls">import</j-upload-button>
      </template>
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
  import { defHttp } from '/@/utils/http/axios';
  //Define table columns
  const columns: BasicColumn[] = [
    {
      title: 'Name',
      dataIndex: 'name',
      width: 170,
      align: 'left',
      resizable: true,
      sorter: {
        multiple: 1,
      },
    },
    {
      title: 'keywords',
      dataIndex: 'keyWord',
      width: 130,
      resizable: true,
    },
    {
      title: 'Check in time',
      dataIndex: 'punchTime',
      width: 140,
      resizable: true,
    },
    {
      title: 'salary',
      dataIndex: 'salaryMoney',
      width: 140,
      resizable: true,
      sorter: {
        multiple: 2,
      },
    },
    {
      title: 'bonus',
      dataIndex: 'bonusMoney',
      width: 140,
      resizable: true,
    },
    {
      title: 'gender',
      dataIndex: 'sex',
      sorter: {
        multiple: 3,
      },
      filters: [
        { text: 'male', value: '1' },
        { text: 'female', value: '2' },
      ],
      customRender: ({ record }) => {
        return record.sex ? (record.sex == '1' ? 'male' : 'female') : '';
      },
      width: 120,
      resizable: true,
    },
    {
      title: 'Birthday',
      dataIndex: 'birthday',
      width: 120,
      resizable: true,
    },
    {
      title: 'Mail',
      dataIndex: 'email',
      width: 120,
      resizable: true,
    },
  ];

  //ajaxaskapiinterface
  const demoListApi = (params) => {
    return defHttp.get({ url: '/test/jeecgDemo/list', params });
  };
  // List page public parameters、method
  const { tableContext, onExportXls, onImportXls } = useListPage({
    designScope: 'basic-table-demo-filter',
    tableProps: {
      title: 'form search',
      api: demoListApi,
      columns: columns,
      showActionColumn: false,
      useSearchForm: false,
    },
    exportConfig: {
      name: 'Example list',
      url: '/test/jeecgDemo/exportXls',
    },
    importConfig: {
      url: '/test/jeecgDemo/importExcel',
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
  }
</script>

<style scoped></style>
